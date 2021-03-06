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
package org.syncope.core.persistence.dao.impl;

import java.util.List;
import javax.persistence.Query;
import org.springframework.stereotype.Repository;
import org.syncope.core.persistence.beans.AbstractDerAttr;
import org.syncope.core.persistence.dao.DerAttrDAO;

@Repository
public class DerAttrDAOImpl extends AbstractDAOImpl implements DerAttrDAO {

    @Override
    public <T extends AbstractDerAttr> T find(
            final Long id, final Class<T> reference) {

        return entityManager.find(reference, id);
    }

    @Override
    public <T extends AbstractDerAttr> List<T> findAll(
            final Class<T> reference) {

        Query query = entityManager.createQuery(
                "SELECT e FROM " + reference.getSimpleName() + " e");
        return query.getResultList();
    }

    @Override
    public <T extends AbstractDerAttr> T save(
            final T derivedAttribute) {

        return entityManager.merge(derivedAttribute);
    }

    @Override
    public <T extends AbstractDerAttr> void delete(
            final Long id, final Class<T> reference) {

        T derivedAttribute = find(id, reference);
        if (derivedAttribute == null) {
            return;
        }

        delete(derivedAttribute);
    }

    @Override
    public <T extends AbstractDerAttr> void delete(
            final T derivedAttribute) {

        if (derivedAttribute.getOwner() != null) {
            derivedAttribute.getOwner().removeDerivedAttribute(
                    derivedAttribute);
        }
        derivedAttribute.getDerivedSchema().removeDerivedAttribute(
                derivedAttribute);

        entityManager.remove(derivedAttribute);
    }
}
