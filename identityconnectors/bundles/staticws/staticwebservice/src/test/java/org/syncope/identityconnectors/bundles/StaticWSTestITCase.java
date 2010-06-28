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
package org.syncope.identityconnectors.bundles;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.operations.APIOperation;
import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syncope.identityconnectors.bundles.staticwebservice.WebServiceConnector;
import org.syncope.identityconnectors.bundles.staticwebservice.provisioning.interfaces.Provisioning;

public class StaticWSTestITCase {

    private static final Logger log =
            LoggerFactory.getLogger(WebServiceConnector.class);

    final private String ENDPOINT_PREFIX =
            "http://localhost:8888/wstarget/services";

    final private String SERVICE =
            "/provisioning";

    final private String bundlename =
            "org.syncope.identityconnectors.bundles.staticws";

    final private String bundleversion =
            "0.1-SNAPSHOT";

    final private String bundleclass =
            WebServiceConnector.class.getName();

    private ConnectorFacade connector;

    /**
     * Uses the ConnectorInfoManager to retrieve a ConnectorInfo object
     * for the connector.
     */
    @Before
    public void init() {

        ConnectorInfoManagerFactory connectorInfoManagerFactory =
                ConnectorInfoManagerFactory.getInstance();

        File bundleDirectory = new File("target");

        APIConfiguration apiConfig = null;

        Throwable t = null;

        try {
            // take bundles repository (directory or jar).
            URL url = IOUtil.makeURL(
                    bundleDirectory, "staticwebservice-0.1-SNAPSHOT.jar");


            assertNotNull(url);

            if (log.isDebugEnabled()) {
                log.debug("URL: " + url.toString());
            }

            ConnectorInfoManager manager =
                    connectorInfoManagerFactory.getLocalManager(url);

            assertNotNull(manager);

            // list connectors info
            List<ConnectorInfo> infos = manager.getConnectorInfos();
            if (infos != null) {
                log.debug("infos size: " + infos.size());
                for (ConnectorInfo i : infos) {
                    log.debug("Name: " + i.getConnectorDisplayName());
                }
            }

            if (log.isDebugEnabled()) {
                log.debug(
                        "\nBundle name: " + bundlename +
                        "\nBundle version: " + bundleversion +
                        "\nBundle class: " + bundleclass);
            }

            // specify a connector.
            ConnectorKey key = new ConnectorKey(
                    bundlename,
                    bundleversion,
                    bundleclass);

            assertNotNull(key);

            // get the specified connector.
            ConnectorInfo info = manager.findConnectorInfo(key);

            assertNotNull(info);

            // create default configuration
            apiConfig = info.createDefaultAPIConfiguration();

            assertNotNull(apiConfig);

        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Bundle file not found", e);
            }

            t = e;
        }

        assertNull(t);

        // retrieve the ConfigurationProperties.
        ConfigurationProperties properties =
                apiConfig.getConfigurationProperties();

        assertNotNull(properties);

        // Print out what the properties are (not necessary)
        List<String> propertyNames = properties.getPropertyNames();

        for (String propName : propertyNames) {
            ConfigurationProperty prop = properties.getProperty(propName);

            if (log.isDebugEnabled()) {
                log.debug(
                        "\nProperty Name: " + prop.getName() +
                        "\nProperty Type: " + prop.getType());
            }
        }

        // Set all of the ConfigurationProperties needed by the connector.
        properties.setPropertyValue(
                "endpoint", ENDPOINT_PREFIX + SERVICE);
        properties.setPropertyValue(
                "servicename", Provisioning.class.getName());

        // Use the ConnectorFacadeFactory's newInstance() method to get
        // a new connector.
        connector =
                ConnectorFacadeFactory.getInstance().newInstance(apiConfig);

        assertNotNull(connector);

        // Make sure we have set up the Configuration properly
        t = null;
        try {

            connector.validate();
            connector.test();

        } catch (RuntimeException re) {
            t = re;
        }

        assertNull(t);
    }

    /**
     * Checks if a particular operation is supported.
     */
    @Test
    public void checkForOperation() {
        Set<Class<? extends APIOperation>> ops =
                connector.getSupportedOperations();

        // check to see if the set contains the operation you care about
        assertTrue(ops.contains(CreateApiOp.class));
    }

    /**
     * Gets schema from the target resource.
     */
    @Test
    public void schema() {
        Schema schema = connector.schema();

        assertNotNull(schema);

        Set<ObjectClassInfo> ocis = schema.getObjectClassInfo();

        assertNotNull(ocis);

        Set<AttributeInfo> attrs = null;

        for (ObjectClassInfo oci : ocis) {
            attrs = oci.getAttributeInfo();

            assertNotNull(attrs);

            for (AttributeInfo attr : attrs) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "\nAttribute name: " + attr.getName() +
                            "\nAttribute type: " + attr.getType().getName());
                }
            }
        }
    }

    /**
     * Seraches for user accounts.
     */
    @Test
    public void search() {
        final List<ConnectorObject> results = new ArrayList<ConnectorObject>();

        ResultsHandler resultsHandler = new ResultsHandler() {

            @Override
            public boolean handle(ConnectorObject obj) {
                results.add(obj);
                return true;
            }
        };

        Filter usernameFilter = FilterBuilder.startsWith(
                AttributeBuilder.build(Name.NAME, "test"));

        Filter nameFilter = FilterBuilder.equalTo(
                AttributeBuilder.build("nome", "jhon"));

        Filter surnameFilter = FilterBuilder.equalTo(
                AttributeBuilder.build("cognome", "doe"));

        Filter filter = FilterBuilder.or(
                usernameFilter,
                FilterBuilder.and(nameFilter, surnameFilter));

        connector.search(ObjectClass.ACCOUNT, filter, resultsHandler, null);

        // pay attention: results will be returned according to the filter
        assertFalse(results.isEmpty());

        if (log.isDebugEnabled()) {
            for (ConnectorObject obj : results) {
                log.debug(
                        "\nName: " + obj.getName() +
                        "\nUID: " + obj.getUid());
            }
        }
    }

    /**
     * Creates user account.
     */
    @Test
    public void create() {
        Set<Attribute> attrs = new HashSet<Attribute>();
        attrs.add(new Name("TESTUSER"));

        attrs.add(AttributeBuilder.buildPassword("TESTPASSWORD".toCharArray()));

        attrs.add(AttributeBuilder.build("cognome", "Doe"));
        attrs.add(AttributeBuilder.build("privacy", true));
        attrs.add(AttributeBuilder.build("data di nascita", "12/03/1990"));

        Uid userUid = connector.create(ObjectClass.ACCOUNT, attrs, null);

        assertNotNull(userUid);
        assertEquals("TESTUSER", userUid.getUidValue());
    }

    /**
     * Updates user account.
     */
    @Test
    public void update() {
        Set attrs = new HashSet();
        attrs.add(new Name("TESTUSER"));

        attrs.add(AttributeBuilder.buildPassword("NEWPASSWORD".toCharArray()));

        Uid userUid = connector.update(
                ObjectClass.ACCOUNT, new Uid("TESTUSER"), attrs, null);

        assertNotNull(userUid);
        assertEquals("TESTUSER", userUid.getUidValue());
    }

    /**
     * Deletes user account.
     */
    @Test
    public void delete() {
        Uid userUid = connector.authenticate(
                ObjectClass.ACCOUNT, "TESTUSER",
                new GuardedString("TESTPASSWORD".toCharArray()),
                null);

        assertNotNull(userUid);

        connector.delete(ObjectClass.ACCOUNT, userUid, null);
    }
}
