/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.rest;

import static org.junit.Assert.*;

import java.util.List;
import org.apache.syncope.client.mod.UserMod;
import org.apache.syncope.client.to.MembershipTO;
import org.apache.syncope.client.to.SchemaTO;
import org.apache.syncope.client.to.UserTO;
import org.apache.syncope.client.util.AttributableOperations;
import org.apache.syncope.client.validation.SyncopeClientCompositeErrorException;
import org.apache.syncope.client.validation.SyncopeClientException;
import org.apache.syncope.services.SchemaService;
import org.apache.syncope.types.AttributableType;
import org.apache.syncope.types.EntityViolationType;
import org.apache.syncope.types.SchemaType;
import org.apache.syncope.types.SyncopeClientExceptionType;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@FixMethodOrder(MethodSorters.JVM)
public class SchemaTestITCase extends AbstractTest {

    @Test
    public void create() {
        SchemaTO schemaTO = buildSchemaTO("testAttribute", SchemaType.String);
        schemaTO.setMandatoryCondition("false");

        SchemaTO newSchemaTO = schemaService.create(AttributableType.USER, SchemaService.SchemaType.NORMAL, schemaTO);
        assertEquals(schemaTO, newSchemaTO);

        newSchemaTO = schemaService.create(AttributableType.MEMBERSHIP, SchemaService.SchemaType.NORMAL, schemaTO);
        assertEquals(schemaTO, newSchemaTO);
    }

    @Test
    public void createWithNotPermittedName() {
        SchemaTO schemaTO = new SchemaTO();
        schemaTO.setName("failedLogins");
        schemaTO.setType(SchemaType.String);

        try {
            schemaService.create(AttributableType.USER, SchemaService.SchemaType.NORMAL, schemaTO);
            fail("This should not be reacheable");
        } catch (SyncopeClientCompositeErrorException scce) {
            SyncopeClientException sce = scce.getException(SyncopeClientExceptionType.InvalidUSchema);

            assertNotNull(sce.getElements());
            assertEquals(1, sce.getElements().size());
            assertTrue(sce.getElements().iterator().next()
                    .contains(EntityViolationType.InvalidUSchema.name()));
        }
    }

    @Test
    public void createREnumWithoutEnumeration() {
        SchemaTO schemaTO = new SchemaTO();
        schemaTO.setName("enumcheck");
        schemaTO.setType(SchemaType.Enum);

        try {
            schemaService.create(AttributableType.ROLE, SchemaService.SchemaType.NORMAL, schemaTO);
            fail("This should not be reacheable");
        } catch (SyncopeClientCompositeErrorException scce) {
            SyncopeClientException sce = scce.getException(SyncopeClientExceptionType.InvalidRSchema);

            assertNotNull(sce.getElements());
            assertEquals(1, sce.getElements().size());
            assertTrue(sce.getElements().iterator().next()
                    .contains(EntityViolationType.InvalidSchemaTypeSpecification.name()));
        }
    }

    @Test
    public void createUEnumWithoutEnumeration() {
        SchemaTO schemaTO = new SchemaTO();
        schemaTO.setName("enumcheck");
        schemaTO.setType(SchemaType.Enum);

        try {
            schemaService.create(AttributableType.USER, SchemaService.SchemaType.NORMAL, schemaTO);
            fail("This should not be reacheable");
        } catch (SyncopeClientCompositeErrorException scce) {
            SyncopeClientException sce = scce.getException(SyncopeClientExceptionType.InvalidUSchema);

            assertNotNull(sce.getElements());
            assertEquals(1, sce.getElements().size());
            assertTrue(sce.getElements().iterator().next()
                    .contains(EntityViolationType.InvalidSchemaTypeSpecification.name()));
        }
    }

    @Test
    public void delete() {
        SchemaTO schemaTO = buildSchemaTO("todelete", SchemaType.String);
        schemaTO.setMandatoryCondition("false");
        schemaService.create(AttributableType.USER, SchemaService.SchemaType.NORMAL, schemaTO);

        SchemaTO deletedSchema = schemaService.delete(AttributableType.USER, SchemaService.SchemaType.NORMAL, schemaTO.getName());
        assertNotNull(deletedSchema);
        SchemaTO firstname = null;
        try {
            firstname = schemaService.read(AttributableType.USER, SchemaService.SchemaType.NORMAL, schemaTO.getName());
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        }
        assertNull(firstname);
    }

    @Test
    public void list() {
        List<SchemaTO> userSchemas = schemaService.list(AttributableType.USER, SchemaService.SchemaType.NORMAL);
        assertFalse(userSchemas.isEmpty());
        for (SchemaTO schemaTO : userSchemas) {
            assertNotNull(schemaTO);
        }

        List<SchemaTO> roleSchemas = schemaService.list(AttributableType.ROLE, SchemaService.SchemaType.NORMAL);
        assertFalse(roleSchemas.isEmpty());
        for (SchemaTO schemaTO : roleSchemas) {
            assertNotNull(schemaTO);
        }

        List<SchemaTO> membershipSchemas = schemaService.list(AttributableType.MEMBERSHIP, SchemaService.SchemaType.NORMAL);
        assertFalse(membershipSchemas.isEmpty());
        for (SchemaTO schemaTO : membershipSchemas) {
            assertNotNull(schemaTO);
        }
    }

    @Test
    public void update() {
        SchemaTO schemaTO = schemaService.read(AttributableType.ROLE, SchemaService.SchemaType.NORMAL, "icon");
        assertNotNull(schemaTO);

        SchemaTO updatedTO = schemaService.update(AttributableType.ROLE, SchemaService.SchemaType.NORMAL, schemaTO.getName(), schemaTO);
        assertEquals(schemaTO, updatedTO);

        updatedTO.setType(SchemaType.Date);
        try {
            schemaService.update(AttributableType.ROLE, SchemaService.SchemaType.NORMAL, schemaTO.getName(), updatedTO);
            fail("This should not be reacheable");
        } catch (SyncopeClientCompositeErrorException scce) {
            SyncopeClientException sce = scce.getException(SyncopeClientExceptionType.InvalidRSchema);
            assertNotNull(sce);
        }
    }

    @Test
    public void issue258() {
        SchemaTO schemaTO = new SchemaTO();
        schemaTO.setName("schema_issue258");
        schemaTO.setType(SchemaType.Double);

        schemaTO = schemaService.create(AttributableType.USER, SchemaService.SchemaType.NORMAL, schemaTO);
        assertNotNull(schemaTO);

        UserTO userTO = UserTestITCase.getUniqueSampleTO("issue258@syncope.apache.org");
        userTO.addAttribute(attributeTO(schemaTO.getName(), "1.2"));

        userTO = userService.create(userTO);
        assertNotNull(userTO);

        schemaTO.setType(SchemaType.Long);
        try {
            schemaService.update(AttributableType.USER, SchemaService.SchemaType.NORMAL, schemaTO.getName(), schemaTO);
            fail("This should not be reacheable");
        } catch (SyncopeClientCompositeErrorException scce) {
            SyncopeClientException sce = scce.getException(SyncopeClientExceptionType.InvalidUSchema);
            assertNotNull(sce);
        }
    }

    @Test
    public void issue259() {
        SchemaTO schemaTO = buildSchemaTO("schema_issue259", SchemaType.Double);
        schemaTO.setUniqueConstraint(true);

        schemaTO = schemaService.create(AttributableType.USER, SchemaService.SchemaType.NORMAL, schemaTO);
        assertNotNull(schemaTO);

        UserTO userTO = UserTestITCase.getUniqueSampleTO("issue259@syncope.apache.org");
        userTO.addAttribute(attributeTO(schemaTO.getName(), "1"));
        userTO = userService.create(userTO);
        assertNotNull(userTO);

        UserTO newUserTO = AttributableOperations.clone(userTO);
        MembershipTO membership = new MembershipTO();
        membership.setRoleId(2L);
        newUserTO.addMembership(membership);

        UserMod userMod = AttributableOperations.diff(newUserTO, userTO);

        userTO = userService.update(userMod.getId(), userMod);
        assertNotNull(userTO);
    }

    @Test
    public void issue260() {
        SchemaTO schemaTO = buildSchemaTO("schema_issue260", SchemaType.Double);
        schemaTO.setUniqueConstraint(true);

        schemaTO = schemaService.create(AttributableType.USER, SchemaService.SchemaType.NORMAL, schemaTO);
        assertNotNull(schemaTO);

        UserTO userTO = UserTestITCase.getUniqueSampleTO("issue260@syncope.apache.org");
        userTO.addAttribute(attributeTO(schemaTO.getName(), "1.2"));
        userTO = userService.create(userTO);
        assertNotNull(userTO);

        schemaTO.setUniqueConstraint(false);
        try {
            schemaService.update(AttributableType.USER, SchemaService.SchemaType.NORMAL, schemaTO.getName(), schemaTO);
            fail("This should not be reacheable");
        } catch (SyncopeClientCompositeErrorException scce) {
            SyncopeClientException sce = scce.getException(SyncopeClientExceptionType.InvalidUSchema);
            assertNotNull(sce);
        }
    }

	private SchemaTO buildSchemaTO(String name, SchemaType type) {
		SchemaTO schemaTO = new SchemaTO();
        schemaTO.setName(name + getUUIDString());
        schemaTO.setType(type);
		return schemaTO;
	}
}
