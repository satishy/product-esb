/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.esb.proxyservice.test.passThroughProxy;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.esb.integration.common.clients.registry.ResourceAdminServiceClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class PickEndPointFromRegistryTestCase extends ESBIntegrationTest {
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        uploadResourcesToConfigRegistry();
        loadESBConfigurationFromClasspath(
                "/artifacts/ESB/proxyconfig/proxy/passThroughProxy/pick_end_point_from_registry.xml");

    }

    @Test(groups = "wso2.esb", description = "- Pass through proxy" +
                                             "- Create a proxy service and pick the endpoint from registry (config)")
    public void testPassThroughProxy() throws Exception {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("StockQuoteProxy"), null, "WSO2");

        String lastPrice = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "last"))
                .getText();
        assertNotNull(lastPrice, "Fault: response message 'last' price null");

        String symbol = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "symbol"))
                .getText();
        assertEquals(symbol, "WSO2", "Fault: value 'symbol' mismatched");

        String name = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "name"))
                .getText();
        assertEquals(name, "WSO2 Company", "Fault: value 'name' mismatched");


    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        clearUploadedResource();
        super.cleanup();
    }


    private void uploadResourcesToConfigRegistry() throws Exception {

        ResourceAdminServiceClient resourceAdminServiceStub =
                new ResourceAdminServiceClient(context.getContextUrls().getBackEndUrl(), getSessionCookie());

        resourceAdminServiceStub.deleteResource("/_system/config/proxy");
        resourceAdminServiceStub.addCollection("/_system/config/", "proxy", "",
                                               "Contains test proxy tests files");

        Assert.assertTrue(resourceAdminServiceStub.addResource("/_system/config/proxy/registry_endpoint.xml", "application/xml", "xml files",
                                                               setEndpoints(new DataHandler(new URL("file:///" + getESBResourceLocation() +
                                                                                                    File.separator + "proxyconfig" + File.separator + "proxy"
                                                                                                    + File.separator + "utils" + File.separator + "registry_endpoint.xml"))))
                , "Adding Resource failed");
        ResourceData[] resources = resourceAdminServiceStub.getResource("/_system/config/proxy/registry_endpoint.xml");
        Assert.assertNotNull(resources, "Resource object is null");
        Assert.assertEquals(resources[0].getName(), "registry_endpoint.xml", "Resource Not Exist");

        Thread.sleep(1000);

    }

    private void clearUploadedResource()
            throws InterruptedException, ResourceAdminServiceExceptionException, RemoteException, XPathExpressionException {

        ResourceAdminServiceClient resourceAdminServiceStub =
                new ResourceAdminServiceClient(context.getContextUrls().getBackEndUrl(), getSessionCookie());

        Assert.assertTrue(resourceAdminServiceStub.deleteResource("/_system/config/proxy"), "Resource deletion failed");
        Thread.sleep(1000);

    }
}
