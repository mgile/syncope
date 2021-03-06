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
package org.syncope.core.persistence.dao;

import java.util.List;
import javassist.NotFoundException;
import org.syncope.core.persistence.beans.Entitlement;
import org.syncope.core.persistence.beans.role.SyncopeRole;
import org.syncope.core.persistence.validation.entity.InvalidEntityException;

public interface EntitlementDAO extends DAO {

    Entitlement find(String name);

    List<Entitlement> findAll();

    Entitlement save(Entitlement entitlement)
            throws InvalidEntityException;

    Entitlement save(SyncopeRole role);

    void delete(String name);

    void delete(Entitlement entitlement);
}
