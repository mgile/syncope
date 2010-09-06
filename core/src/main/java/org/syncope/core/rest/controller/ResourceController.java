
/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.syncope.core.rest.controller;

import java.util.ArrayList;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javassist.NotFoundException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.syncope.client.to.ResourceTO;
import org.syncope.client.to.ResourceTOs;
import org.syncope.client.to.SchemaMappingTOs;
import org.syncope.client.validation.SyncopeClientCompositeErrorException;
import org.syncope.client.validation.SyncopeClientException;
import org.syncope.core.persistence.beans.AbstractSchema;
import org.syncope.core.persistence.beans.TargetResource;
import org.syncope.core.persistence.beans.SchemaMapping;
import org.syncope.core.persistence.beans.role.SyncopeRole;
import org.syncope.core.persistence.dao.ResourceDAO;
import org.syncope.core.persistence.dao.SchemaDAO;
import org.syncope.core.persistence.dao.SyncopeRoleDAO;
import org.syncope.core.rest.data.ResourceDataBinder;
import org.syncope.types.SchemaType;
import org.syncope.types.SyncopeClientExceptionType;

@Controller
@RequestMapping("/resource")
public class ResourceController extends AbstractController {

    @Autowired
    private ResourceDAO resourceDAO;
    @Autowired
    private SchemaDAO schemaDAO;
    @Autowired
    private SyncopeRoleDAO syncopeRoleDAO;
    @Autowired
    private ResourceDataBinder binder;

    @RequestMapping(method = RequestMethod.POST,
    value = "/create")
    public ResourceTO create(HttpServletResponse response,
            @RequestBody ResourceTO resourceTO)
            throws SyncopeClientCompositeErrorException, NotFoundException {

        if (log.isDebugEnabled()) {
            log.debug("Creation request received");
        }

        SyncopeClientCompositeErrorException compositeErrorException =
                new SyncopeClientCompositeErrorException(
                HttpStatus.BAD_REQUEST);

        if (resourceTO == null) {
            log.error("Missing resource");

            throw new NotFoundException("Missing resource");
        }

        TargetResource resource = null;

        try {
            if (log.isDebugEnabled()) {
                log.debug("Verify that resource dosn't exist");
            }

            if (resourceDAO.find(resourceTO.getName()) != null) {
                SyncopeClientException ex = new SyncopeClientException(
                        SyncopeClientExceptionType.AlreadyExists);

                ex.addElement(resourceTO.getName());
                compositeErrorException.addException(ex);

                throw compositeErrorException;
            }

            if (log.isDebugEnabled()) {
                log.debug("Resource data binder ..");
            }

            resource = binder.getResource(resourceTO);
            if (resource == null) {
                log.error("Resource creation failed");

                SyncopeClientException ex = new SyncopeClientException(
                        SyncopeClientExceptionType.Unknown);

                compositeErrorException.addException(ex);

                throw compositeErrorException;
            }
        } catch (Throwable t) {
            if (log.isErrorEnabled()) {
                log.error("Unknown exception", t);
            }

            SyncopeClientException ex = new SyncopeClientException(
                    SyncopeClientExceptionType.Unknown);

            compositeErrorException.addException(ex);

            throw compositeErrorException;
        }

        response.setStatus(HttpServletResponse.SC_CREATED);
        return binder.getResourceTO(resource);
    }

    @RequestMapping(method = RequestMethod.POST,
    value = "/update")
    public ResourceTO update(HttpServletResponse response,
            @RequestBody ResourceTO resourceTO)
            throws SyncopeClientCompositeErrorException, NotFoundException {

        if (log.isDebugEnabled()) {
            log.debug("Update request received");
        }

        TargetResource resource = null;

        if (resourceTO != null && resourceTO.getName() != null) {
            resource = resourceDAO.find(resourceTO.getName());
        }

        if (resource == null) {
            log.error("Missing resource");

            throw new NotFoundException(resourceTO.getName());
        }

        SyncopeClientCompositeErrorException compositeErrorException =
                new SyncopeClientCompositeErrorException(
                HttpStatus.BAD_REQUEST);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Remove old mappings ..");
            }

            // remove older mappings
            List<SchemaMapping> mappings = resource.getMappings();
            for (SchemaMapping mapping : mappings) {
                mapping.setResource(null);
                schemaDAO.removeMapping(mapping.getId());
            }
            resource.getMappings().clear();

            if (log.isDebugEnabled()) {
                log.debug("Resource data binder ..");
            }

            resource = binder.getResource(resource, resourceTO);
            if (resource == null) {
                if (log.isErrorEnabled()) {
                    log.error("Resource creation failed");
                }

                SyncopeClientException ex = new SyncopeClientException(
                        SyncopeClientExceptionType.Unknown);

                compositeErrorException.addException(ex);
                throw compositeErrorException;
            }
        } catch (Throwable t) {
            log.error("Unknown exception", t);

            SyncopeClientException ex = new SyncopeClientException(
                    SyncopeClientExceptionType.Unknown);

            compositeErrorException.addException(ex);

            throw compositeErrorException;
        }

        response.setStatus(HttpServletResponse.SC_CREATED);
        return binder.getResourceTO(resource);
    }

    @RequestMapping(method = RequestMethod.DELETE,
    value = "/delete/{resourceName}")
    public void delete(HttpServletResponse response,
            @PathVariable("resourceName") String resourceName)
            throws NotFoundException {

        TargetResource resource = resourceDAO.find(resourceName);

        if (resource == null) {

            if (log.isErrorEnabled()) {
                log.error("Could not find resource '" + resourceName + "'");
            }

            throw new NotFoundException(resourceName);

        } else {

            resourceDAO.delete(resourceName);

        }
    }

    @RequestMapping(method = RequestMethod.GET,
    value = "/read/{resourceName}")
    public ResourceTO read(HttpServletResponse response,
            @PathVariable("resourceName") String resourceName)
            throws NotFoundException {

        TargetResource resource = resourceDAO.find(resourceName);
        if (resource == null) {
            log.error("Could not find resource '" + resourceName + "'");

            throw new NotFoundException(resourceName);
        }

        return binder.getResourceTO(resource);
    }

    @RequestMapping(method = RequestMethod.GET,
    value = "/list")
    public ResourceTOs list(HttpServletResponse response)
            throws NotFoundException {

        List<TargetResource> resources = resourceDAO.findAll();

        if (resources == null) {
            log.error("No resource found");

            throw new NotFoundException("No resource found");
        }

        return binder.getResourceTOs(resources);
    }

    @RequestMapping(method = RequestMethod.POST,
    value = "/{resourceName}/mappings/create")
    public SchemaMappingTOs createMappings(HttpServletResponse response,
            @PathVariable("resourceName") String resourceName,
            @RequestBody SchemaMappingTOs mappings)
            throws SyncopeClientCompositeErrorException {

        Set<SchemaMapping> actuals = new HashSet<SchemaMapping>();

        try {

            TargetResource resource = null;
            if (resourceName != null) {
                resource = resourceDAO.find(resourceName);
            }

            if (resource == null) {
                if (log.isErrorEnabled()) {
                    log.error("Missing resource");
                }

                SyncopeClientException ex = new SyncopeClientException(
                        SyncopeClientExceptionType.RequiredValuesMissing);

                ex.addElement("resource");

                throw ex;
            }

            if (mappings == null || mappings.getMappings().isEmpty()) {
                if (log.isErrorEnabled()) {
                    log.error("Missing mapping");
                }

                SyncopeClientException ex = new SyncopeClientException(
                        SyncopeClientExceptionType.RequiredValuesMissing);

                ex.addElement("mappings");

                throw ex;
            }

            // resource.getMappings() can never return a null value
            List<SchemaMapping> existentMappings = resource.getMappings();

            for (SchemaMapping mapping : existentMappings) {
                if (mapping != null) {// a list can contain null values
                    mapping.setResource(null);
                    schemaDAO.removeMapping(mapping.getId());
                }
            }

            // to be sure ...
            resource.setMappings(new ArrayList<SchemaMapping>());

            List<SchemaMapping> schemaMappings =
                    binder.getSchemaMappings(resource, mappings);

            SchemaMapping actual = null;

            for (SchemaMapping schemaMapping : schemaMappings) {
                // --------------------------------------
                // Synchronize resource
                // --------------------------------------
                resource.addMapping(actual);
                // --------------------------------------

                // --------------------------------------
                // Synchronize schema
                // --------------------------------------
                String schemaName = schemaMapping.getSchemaName();
                SchemaType schemaType = schemaMapping.getSchemaType();

                try {
                    // check for schema type
                    schemaType.getSchemaClass().asSubclass(AbstractSchema.class);

                    /**
                     * Schema type could be:
                     * * UserSchema
                     * * RoleSchema
                     * * MembershipSchema
                     */
                    if (log.isDebugEnabled()) {
                        log.debug("Schema type " + schemaType.getClassName());
                    }

                    AbstractSchema schema = schemaDAO.find(
                            schemaName, schemaType.getSchemaClass());

                    if (schema != null) {
                        schema.removeMapping(schemaMapping);
                    }

                } catch (ClassCastException e) {
                    /**
                     * Schema type could be:
                     * * AccountId
                     * * Password
                     */
                    if (log.isDebugEnabled()) {
                        log.debug("Wrong schema type "
                                + schemaType.getClassName());
                    }
                }
                // --------------------------------------

                actual = schemaDAO.saveMapping(schemaMapping);
                actuals.add(actual);
            }

            resourceDAO.save(resource);

        } catch (SyncopeClientException ex) {

            SyncopeClientCompositeErrorException compositeErrorException =
                    new SyncopeClientCompositeErrorException(
                    HttpStatus.BAD_REQUEST);

            compositeErrorException.addException(ex);

            throw compositeErrorException;

        } catch (Throwable t) {

            if (log.isErrorEnabled()) {
                log.error("Unknown exception", t);
            }

            SyncopeClientException ex = new SyncopeClientException(
                    SyncopeClientExceptionType.Unknown);

            SyncopeClientCompositeErrorException compositeErrorException =
                    new SyncopeClientCompositeErrorException(
                    HttpStatus.BAD_REQUEST);

            compositeErrorException.addException(ex);

            throw compositeErrorException;
        }

        response.setStatus(HttpServletResponse.SC_CREATED);
        return binder.getSchemaMappingTOs(actuals);
    }

    @RequestMapping(method = RequestMethod.DELETE,
    value = "/{resourceName}/mappings/delete")
    public void deleteMappings(HttpServletResponse response,
            @PathVariable("resourceName") String resourceName)
            throws NotFoundException {

        TargetResource resource = resourceDAO.find(resourceName);

        if (resource == null) {

            if (log.isErrorEnabled()) {
                log.error("Could not find resource '" + resourceName + "'");
            }

            throw new NotFoundException(resourceName);

        } else {

            List<SchemaMapping> mappings = resource.getMappings();

            // resource.getMappings() can never return a null value

            for (SchemaMapping mapping : mappings) {
                mapping.setResource(null);
                schemaDAO.removeMapping(mapping.getId());
            }

            // to be sure ...
            resource.setMappings(Collections.EMPTY_LIST);
        }
    }

    @RequestMapping(method = RequestMethod.GET,
    value = "/{resourceName}/mappings/list")
    public SchemaMappingTOs getResourceMapping(HttpServletResponse response,
            @PathVariable("resourceName") String resourceName)
            throws SyncopeClientCompositeErrorException {

        TargetResource resource = null;
        if (resourceName != null) {
            resource = resourceDAO.find(resourceName);
        }

        if (resource == null) {
            if (log.isErrorEnabled()) {
                log.error("Resource " + resourceName + " not found.");
            }

            SyncopeClientCompositeErrorException compositeErrorException =
                    new SyncopeClientCompositeErrorException(
                    HttpStatus.BAD_REQUEST);

            SyncopeClientException ex = new SyncopeClientException(
                    SyncopeClientExceptionType.RequiredValuesMissing);

            ex.addElement("resource");

            compositeErrorException.addException(ex);

            throw compositeErrorException;
        }

        List<SchemaMapping> schemaMappings = resource.getMappings();

        // resource.getMappings() can never return a null value

        return binder.getSchemaMappingTOs(schemaMappings);
    }

    @RequestMapping(method = RequestMethod.GET,
    value = "/{roleName}/resources/mappings/list")
    public SchemaMappingTOs getRoleResourcesMapping(
            HttpServletResponse response,
            @PathVariable("roleName") Long roleId)
            throws SyncopeClientCompositeErrorException {

        SyncopeRole role = null;
        if (roleId != null) {
            role = syncopeRoleDAO.find(roleId);
        }

        if (role == null) {
            if (log.isErrorEnabled()) {
                log.error("Role " + roleId + " not found.");
            }

            SyncopeClientCompositeErrorException compositeErrorException =
                    new SyncopeClientCompositeErrorException(
                    HttpStatus.BAD_REQUEST);

            SyncopeClientException ex = new SyncopeClientException(
                    SyncopeClientExceptionType.RequiredValuesMissing);

            ex.addElement("resource");

            compositeErrorException.addException(ex);

            throw compositeErrorException;
        }

        SchemaMappingTOs roleMappings = new SchemaMappingTOs();

        Set<TargetResource> resources = role.getTargetResources();

        SchemaMappingTOs resourceMappings = null;

        for (TargetResource resource : resources) {
            if (log.isDebugEnabled()) {
                log.debug("Ask for the mappings of '" + resource + "'");
            }

            List<SchemaMapping> schemaMappings = resource.getMappings();

            if (log.isDebugEnabled()) {
                log.debug("The mappings of '" + resource + "' are '"
                        + schemaMappings + "'");
            }

            resourceMappings = binder.getSchemaMappingTOs(schemaMappings);

            if (log.isDebugEnabled()) {
                log.debug("The mappings TO of '" + resource + "' are '"
                        + resourceMappings.getMappings() + "'");
            }

            roleMappings.getMappings().addAll(resourceMappings.getMappings());
        }

        if (log.isDebugEnabled()) {
            log.debug("Mappings found: " + roleMappings.getMappings());
        }

        return roleMappings;
    }
}
