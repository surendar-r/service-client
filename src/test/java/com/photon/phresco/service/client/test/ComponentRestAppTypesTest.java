/*
 * ###
 * Phresco Service Client
 * %%
 * Copyright (C) 1999 - 2012 Photon Infotech Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ###
 */

package com.photon.phresco.service.client.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.ApplicationType;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.ServiceClientConstant;
import com.photon.phresco.service.client.api.ServiceContext;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.service.client.factory.ServiceClientFactory;
import com.photon.phresco.service.client.impl.RestClient;
import com.photon.phresco.service.client.util.RestUtil;
import com.photon.phresco.util.ServiceConstants;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class ComponentRestAppTypesTest extends BaseRestTest {
	
	@Before
	public void Initilaization() throws PhrescoException {
		initialize();
	}

	@Test
	public void testCreateApplicationTypes() throws PhrescoException {
	    List<ApplicationType> appTypes = new ArrayList<ApplicationType>();

	    List<String> customerIds = new ArrayList<String>();
	    customerIds.add("photon");
	    
	    ApplicationType appType = createAppType(PHOTON_APP_TYPE_ID, "Test Photon Apptype", "This is a test application type", customerIds);
	    appTypes.add(appType);

	    List<String> customerIds2 = new ArrayList<String>();
	    customerIds2.add("vwr");
		
		ApplicationType appType2 = createAppType("test-appType-vwr", "VWR App Type", "This is a test application type", customerIds2);
		appTypes.add(appType2);
		
	    List<String> customerIds3 = new ArrayList<String>();
	    customerIds3.add("vwr");
	    customerIds3.add("macys");
		ApplicationType appType3 = createAppType("test-appType-vwr-macys", "VWR MACYS", "This is a test application type", customerIds3);
	    appTypes.add(appType3);
	    
        RestClient<ApplicationType> newApp = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
        ClientResponse clientResponse = newApp.create(appTypes);
    }
	
	@Test
    public void testFindAppTypes() throws PhrescoException {
        RestClient<ApplicationType> applicationTypeClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
        applicationTypeClient.queryString(REST_QUERY_CUSTOMERID, "photon");
        GenericType<List<ApplicationType>> genericType = new GenericType<List<ApplicationType>>(){};
        List<ApplicationType> applicationTypes = applicationTypeClient.get(genericType);
        Assert.assertEquals(1, applicationTypes.size());

        //vwr customer
        RestClient<ApplicationType> appTypeClientVWR = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
        appTypeClientVWR.queryString(REST_QUERY_CUSTOMERID, "vwr");
        applicationTypes = appTypeClientVWR.get(genericType);
        Assert.assertEquals(3, applicationTypes.size());
        
        //macys customer
        RestClient<ApplicationType> appTypeClientMacys = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
        appTypeClientMacys.queryString(REST_QUERY_CUSTOMERID, "macys");
        applicationTypes = appTypeClientMacys.get(genericType);
        Assert.assertEquals(2, applicationTypes.size());

        //non-exising customer
        RestClient<ApplicationType> appTypeClientNonExisting = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
        appTypeClientNonExisting.queryString(REST_QUERY_CUSTOMERID, "non-existing");
        applicationTypes = appTypeClientNonExisting.get(genericType);
        //Should throw an error, saying invalid customer
        Assert.assertEquals(1, applicationTypes.size());
    }

    @Test
    public void testGetAppTypesById() throws PhrescoException {
    	RestClient<ApplicationType> applicationTypeClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
    	applicationTypeClient.setPath(PHOTON_APP_TYPE_ID);
    	GenericType<ApplicationType> genericType = new GenericType<ApplicationType>(){};
    	ApplicationType applicationTypes = applicationTypeClient.getById(genericType);
    	Assert.assertEquals(PHOTON_APP_TYPE_ID, applicationTypes.getId());
    }
	
	@Test
	public void testUpdateApplicationTypesById() throws PhrescoException {
        RestClient<ApplicationType> editApptype = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
        ApplicationType appType = new ApplicationType();
        appType.setId(PHOTON_APP_TYPE_ID);
        appType.setName("Photon App Type - Updated");
        String description = "This is a test application type update";
		appType.setDescription(description);
        editApptype.setPath(PHOTON_APP_TYPE_ID);
        List<String> customerIds = new ArrayList<String>();
	    customerIds.add("photon");
		appType.setCustomerIds(customerIds);
		
        GenericType<ApplicationType> genericType = new GenericType<ApplicationType>() {};
        ApplicationType appTypeUpdated = editApptype.updateById(appType, genericType);
        
        Assert.assertEquals(PHOTON_APP_TYPE_ID, appTypeUpdated.getId());
        Assert.assertEquals(description, appTypeUpdated.getDescription());
    }

	@Test
	public void testDeleteApplicationType() throws PhrescoException {
	    List<String> customerIds = new ArrayList<String>();
	    customerIds.add("vwr");
	    customerIds.add("macys");
		ApplicationType appType = createAppType("type-to-delete", "App type to delete", "This is a test application type", customerIds);
		appType = saveApptype(appType);
		
		RestClient<ApplicationType> apptypeRestClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
		apptypeRestClient.setPath(appType.getId());
        ClientResponse response = apptypeRestClient.deleteById();
        System.out.println("clientResponse in deleteApplicationType()" +  response.getStatus());
    }
	
}