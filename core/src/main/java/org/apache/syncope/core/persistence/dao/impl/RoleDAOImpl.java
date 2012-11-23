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
package org.apache.syncope.core.persistence.dao.impl;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.apache.syncope.core.persistence.beans.Entitlement;
import org.apache.syncope.core.persistence.beans.ExternalResource;
import org.apache.syncope.core.persistence.beans.membership.Membership;
import org.apache.syncope.core.persistence.beans.role.SyncopeRole;
import org.apache.syncope.core.persistence.beans.user.SyncopeUser;
import org.apache.syncope.core.persistence.dao.EntitlementDAO;
import org.apache.syncope.core.persistence.dao.RoleDAO;
import org.apache.syncope.core.persistence.dao.UserDAO;
import org.apache.syncope.core.util.EntitlementUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RoleDAOImpl extends AbstractDAOImpl implements RoleDAO {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private EntitlementDAO entitlementDAO;

    @Override
    public SyncopeRole find(final Long id) {
        TypedQuery<SyncopeRole> query = entityManager.createQuery("SELECT e FROM SyncopeRole e WHERE e.id = :id",
                SyncopeRole.class);
        query.setParameter("id", id);

        SyncopeRole result = null;
        try {
            result = query.getSingleResult();
        } catch (NoResultException e) {
        }

        return result;
    }

    @Override
    public List<SyncopeRole> find(final String name) {
        Query query = entityManager.createQuery("SELECT e FROM SyncopeRole e WHERE e.name = :name");
        query.setParameter("name", name);

        return query.getResultList();
    }

    @Override
    public SyncopeRole find(final String name, final Long parentId) {
        Query query;
        if (parentId != null) {
            query = entityManager.createQuery("SELECT r FROM SyncopeRole r WHERE "
                    + "r.name=:name AND r.parent.id=:parentId");
            query.setParameter("parentId", parentId);
        } else {
            query = entityManager.createQuery("SELECT r FROM SyncopeRole r WHERE "
                    + "r.name=:name AND r.parent IS NULL");
        }
        query.setParameter("name", name);

        List<SyncopeRole> result = query.getResultList();
        return result.isEmpty()
                ? null
                : result.get(0);
    }

    private void findSameOwnerDescendants(final List<SyncopeRole> result, final SyncopeRole role) {
        List<SyncopeRole> children = findChildren(role);
        if (children != null) {
            for (SyncopeRole child : children) {
                if ((child.getUserOwner() == null && child.getRoleOwner() == null && child.isInheritOwner())
                        || (child.getUserOwner() != null && child.getUserOwner().equals(role.getUserOwner()))
                        || (child.getRoleOwner() != null && child.getRoleOwner().equals(role.getRoleOwner()))) {

                    findDescendants(result, child);
                }
            }
        }
        result.add(role);
    }

    @Override
    public List<SyncopeRole> findOwned(final SyncopeUser owner) {
        StringBuilder queryString = new StringBuilder("SELECT e FROM ").append(SyncopeRole.class.getSimpleName()).
                append(" e WHERE e.userOwner=:owner ");
        for (Long roleId : owner.getRoleIds()) {
            queryString.append("OR e.roleOwner.id=").append(roleId).append(' ');
        }

        Query query = entityManager.createQuery(queryString.toString());
        query.setParameter("owner", owner);

        List<SyncopeRole> result = new ArrayList<SyncopeRole>();
        for (SyncopeRole role : (List<SyncopeRole>) query.getResultList()) {
            findSameOwnerDescendants(result, role);
        }

        return result;
    }

    @Override
    public List<SyncopeRole> findByEntitlement(final Entitlement entitlement) {
        Query query = entityManager.createQuery("SELECT e FROM " + SyncopeRole.class.getSimpleName() + " e "
                + "WHERE :entitlement MEMBER OF e.entitlements");
        query.setParameter("entitlement", entitlement);

        return query.getResultList();
    }

    @Override
    public List<SyncopeRole> findByResource(final ExternalResource resource) {
        Query query = entityManager.createQuery("SELECT e FROM " + SyncopeRole.class.getSimpleName() + " e "
                + "WHERE :resource MEMBER OF e.resources");
        query.setParameter("resource", resource);

        return query.getResultList();
    }

    private void findAncestors(final List<SyncopeRole> result, final SyncopeRole role) {
        if (role.getParent() != null && !result.contains(role.getParent())) {
            result.add(role.getParent());
            findAncestors(result, role.getParent());
        }
    }

    @Override
    public List<SyncopeRole> findAncestors(final SyncopeRole role) {
        List<SyncopeRole> result = new ArrayList<SyncopeRole>();
        findAncestors(result, role);
        return result;
    }

    @Override
    public List<SyncopeRole> findChildren(final SyncopeRole role) {
        Query query = entityManager.createQuery("SELECT r FROM SyncopeRole r WHERE " + "r.parent=:role");
        query.setParameter("role", role);
        return query.getResultList();
    }

    private void findDescendants(final List<SyncopeRole> result, final SyncopeRole role) {
        List<SyncopeRole> children = findChildren(role);
        if (children != null) {
            for (SyncopeRole child : children) {
                findDescendants(result, child);
            }
        }
        result.add(role);
    }

    @Override
    public List<SyncopeRole> findDescendants(final SyncopeRole role) {
        List<SyncopeRole> result = new ArrayList<SyncopeRole>();
        findDescendants(result, role);
        return result;
    }

    @Override
    public List<SyncopeRole> findAll() {
        Query query = entityManager.createQuery("SELECT e FROM SyncopeRole e");
        return query.getResultList();
    }

    @Override
    public List<Membership> findMemberships(final SyncopeRole role) {
        Query query = entityManager.createQuery("SELECT e FROM " + Membership.class.getSimpleName() + " e"
                + " WHERE e.syncopeRole=:role");
        query.setParameter("role", role);

        return query.getResultList();
    }

    @Override
    public SyncopeRole save(final SyncopeRole role) {
        // reset account policy in case of inheritance
        if (role.isInheritAccountPolicy()) {
            role.setAccountPolicy(null);
        }

        // reset password policy in case of inheritance
        if (role.isInheritPasswordPolicy()) {
            role.setPasswordPolicy(null);
        }

        final SyncopeRole savedRole = entityManager.merge(role);
        entitlementDAO.saveEntitlementRole(savedRole);

        return savedRole;
    }

    @Override
    public void delete(final Long id) {
        SyncopeRole role = find(id);
        if (role == null) {
            return;
        }

        for (SyncopeRole roleToBeDeleted : findDescendants(role)) {
            for (Membership membership : findMemberships(roleToBeDeleted)) {
                membership.getSyncopeUser().removeMembership(membership);
                userDAO.save(membership.getSyncopeUser());

                entityManager.remove(membership);
            }

            roleToBeDeleted.getEntitlements().clear();

            roleToBeDeleted.setParent(null);
            roleToBeDeleted.setUserOwner(null);
            roleToBeDeleted.setRoleOwner(null);
            entityManager.remove(roleToBeDeleted);

            entitlementDAO.delete(EntitlementUtil.getEntitlementNameFromRoleId(roleToBeDeleted.getId()));
        }
    }
}