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

import org.syncope.core.persistence.validation.entity.InvalidEntityException;
import static org.junit.Assert.*;

import java.util.List;
import javax.validation.ValidationException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.syncope.core.persistence.beans.user.UAttr;
import org.syncope.core.persistence.beans.user.USchema;
import org.syncope.core.persistence.AbstractTest;
import org.syncope.core.persistence.beans.user.SyncopeUser;
import org.syncope.core.persistence.beans.user.UAttrUniqueValue;
import org.syncope.core.util.AttributableUtil;
import org.syncope.types.EntityViolationType;

@Transactional
public class AttrTest extends AbstractTest {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private AttrDAO attrDAO;

    @Autowired
    private SchemaDAO userSchemaDAO;

    @Test
    public final void findAll() {
        List<UAttr> list = attrDAO.findAll(UAttr.class);
        assertEquals("did not get expected number of attributes ",
                8, list.size());
    }

    @Test
    public final void findById() {
        UAttr attribute = attrDAO.find(100L, UAttr.class);
        assertNotNull("did not find expected attribute schema",
                attribute);
        attribute = attrDAO.find(200L, UAttr.class);
        assertNotNull("did not find expected attribute schema",
                attribute);
    }

    @Test
    public final void read() {
        UAttr attribute = attrDAO.find(100L, UAttr.class);
        assertNotNull(attribute);

        assertTrue(attribute.getValues().isEmpty());
        assertNotNull(attribute.getUniqueValue());
    }

    @Test
    public final void save()
            throws ClassNotFoundException {

        SyncopeUser user = userDAO.find(1L);

        USchema emailSchema = userSchemaDAO.find("email", USchema.class);
        assertNotNull(emailSchema);

        UAttr attribute = new UAttr();
        attribute.setSchema(emailSchema);
        attribute.setOwner(user);

        Exception thrown = null;
        try {
            attribute.addValue("john.doe@gmail.com", AttributableUtil.USER);
            attribute.addValue("mario.rossi@gmail.com", AttributableUtil.USER);
        } catch (ValidationException e) {
            LOG.error("Unexpected exception", e);
            thrown = e;
        }
        assertNull("no validation exception expected here ", thrown);

        try {
            attribute.addValue("http://www.apache.org", AttributableUtil.USER);
        } catch (ValidationException e) {
            thrown = e;
        }
        assertNotNull("validation exception expected here ", thrown);

        InvalidEntityException iee = null;
        try {
            attribute = attrDAO.save(attribute);
        } catch (InvalidEntityException e) {
            iee = e;
        }
        assertNull(iee);

        UAttr actual = attrDAO.find(attribute.getId(),
                UAttr.class);
        assertNotNull("expected save to work", actual);
        assertEquals(attribute, actual);
    }

    @Test
    public final void validateAndSave() {
        final USchema emailSchema =
                userSchemaDAO.find("email", USchema.class);
        assertNotNull(emailSchema);

        final USchema usernameSchema =
                userSchemaDAO.find("username", USchema.class);
        assertNotNull(usernameSchema);

        UAttr attribute = new UAttr();
        attribute.setSchema(emailSchema);

        UAttrUniqueValue uauv = new UAttrUniqueValue();
        uauv.setAttribute(attribute);
        uauv.setSchema(usernameSchema);
        uauv.setStringValue("a value");

        attribute.setUniqueValue(uauv);

        InvalidEntityException iee = null;
        try {
            attribute = attrDAO.save(attribute);
        } catch (InvalidEntityException e) {
            iee = e;
        }
        assertNotNull(iee);
        // for attribute
        assertTrue(iee.hasViolation(EntityViolationType.InvalidValueList));
        // for uauv
        assertTrue(iee.hasViolation(EntityViolationType.InvalidSchema));
    }

    @Test
    public final void delete() {
        UAttr attribute = attrDAO.find(200L, UAttr.class);
        String attrSchemaName = attribute.getSchema().getName();

        attrDAO.delete(attribute.getId(), UAttr.class);

        USchema schema = userSchemaDAO.find(attrSchemaName, USchema.class);
        assertNotNull("user attribute schema deleted when deleting values",
                schema);
    }
}
