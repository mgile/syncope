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
package org.syncope.core.persistence.beans.membership;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import org.syncope.core.persistence.beans.AbstractAttributable;
import org.syncope.core.persistence.beans.AbstractAttr;
import org.syncope.core.persistence.beans.AbstractDerAttr;
import org.syncope.core.persistence.beans.TargetResource;
import org.syncope.core.persistence.beans.role.SyncopeRole;
import org.syncope.core.persistence.beans.user.SyncopeUser;

@Entity
@Table(uniqueConstraints =
@UniqueConstraint(columnNames = {
    "syncopeUser_id",
    "syncopeRole_id"
}))
public class Membership extends AbstractAttributable {

    @Id
    private Long id;

    @ManyToOne
    private SyncopeUser syncopeUser;

    @ManyToOne
    private SyncopeRole syncopeRole;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    @Valid
    private List<MAttr> attributes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    @Valid
    private List<MDerAttr> derivedAttributes;

    public Membership() {
        attributes = new ArrayList<MAttr>();
        derivedAttributes = new ArrayList<MDerAttr>();
        targetResources = Collections.EMPTY_SET;
    }

    @Override
    public Long getId() {
        return id;
    }

    public SyncopeRole getSyncopeRole() {
        return syncopeRole;
    }

    public void setSyncopeRole(SyncopeRole syncopeRole) {
        this.syncopeRole = syncopeRole;
    }

    public SyncopeUser getSyncopeUser() {
        return syncopeUser;
    }

    public void setSyncopeUser(SyncopeUser syncopeUser) {
        this.syncopeUser = syncopeUser;
    }

    @Override
    public <T extends AbstractAttr> boolean addAttribute(T attribute) {
        return attributes.add((MAttr) attribute);
    }

    @Override
    public <T extends AbstractAttr> boolean removeAttribute(T attribute) {
        return attributes.remove((MAttr) attribute);
    }

    @Override
    public List<? extends AbstractAttr> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(List<? extends AbstractAttr> attributes) {
        this.attributes = (List<MAttr>) attributes;
    }

    @Override
    public <T extends AbstractDerAttr> boolean addDerivedAttribute(
            T derivedAttribute) {

        return derivedAttributes.add(
                (MDerAttr) derivedAttribute);
    }

    @Override
    public <T extends AbstractDerAttr> boolean removeDerivedAttribute(
            T derivedAttribute) {

        return derivedAttributes.remove(
                (MDerAttr) derivedAttribute);
    }

    @Override
    public List<? extends AbstractDerAttr> getDerivedAttributes() {
        return derivedAttributes;
    }

    @Override
    public void setDerivedAttributes(
            List<? extends AbstractDerAttr> derivedAttributes) {

        this.derivedAttributes =
                (List<MDerAttr>) derivedAttributes;
    }

    @Override
    public boolean addTargetResource(TargetResource resource) {
        return false;
    }

    @Override
    public boolean removeTargetResource(TargetResource resource) {
        return false;
    }

    @Override
    public Set<TargetResource> getTargetResources() {
        return Collections.EMPTY_SET;
    }

    @Override
    public void setResources(Set<TargetResource> resources) {
    }

    @Override
    public String toString() {
        return "Membership[" + "id=" + id
                + ", " + syncopeUser
                + ", " + syncopeRole + ']';
    }
}
