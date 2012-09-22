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

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.ArtifactGroup;
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

public class ComponentRestjsLibsTest implements ServiceConstants {

	public ServiceContext context = null;
	public ServiceManager serviceManager = null;
	
	@Before
	public void Initilaization() throws PhrescoException {
		context = new ServiceContext();
        context.put(ServiceClientConstant.SERVICE_URL, RestUtil.getServerPath());
        context.put(ServiceClientConstant.SERVICE_USERNAME, "demouser");
        context.put(ServiceClientConstant.SERVICE_PASSWORD, "phresco");
        serviceManager = ServiceClientFactory.getServiceManager(context);
	}
	
	@Test
	public void testCreatejsLibs() throws PhrescoException {
		List<ArtifactGroup> modules=new ArrayList<ArtifactGroup>();
		ArtifactGroup ArtifactGroup = new ArtifactGroup();
		ArtifactGroup.setId("test-jsLibs");
		ArtifactGroup.setName("TestjsLibsone");
		ArtifactGroup.setType("js"); 
		modules.add(ArtifactGroup);
        RestClient<ArtifactGroup> newApp = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        ClientResponse clientResponse = newApp.create(modules);
        assertNotNull(clientResponse);    
	}
	
	@Test
    public void testGetjsLibs() throws PhrescoException {
		RestClient<ArtifactGroup> ArtifactGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
		Map<String, String> query = new HashMap<String, String>();
		query.put(REST_QUERY_TYPE, "js");
		query.put(REST_QUERY_CUSTOMERID, "photon");
		ArtifactGroupClient.queryStrings(query);
		GenericType<List<ArtifactGroup>> genericType = new GenericType<List<ArtifactGroup>>(){};
		List<ArtifactGroup> modules = ArtifactGroupClient.get(genericType);
		assertNotNull(modules);
    }
	
	@Test
	public void testUpdatejsLibs() throws PhrescoException{
		RestClient<ArtifactGroup> ArtifactGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
	    List<ArtifactGroup> ArtifactGroups = new ArrayList<ArtifactGroup>();
	    ArtifactGroup ArtifactGroup = new ArtifactGroup();
	    ArtifactGroup.setId("test-jsLibs");
		ArtifactGroup.setName("TestjsLibsUpdate");
		ArtifactGroup.setType("Js");
		ArtifactGroups.add(ArtifactGroup);
	    GenericType<List<ArtifactGroup>> genericType = new GenericType<List<ArtifactGroup>>() {};
	    List<ArtifactGroup> modules = ArtifactGroupClient.update(ArtifactGroups, genericType);
	    assertNotNull(modules);
	}
	
	@Test
    public void testGetjsLibsById() throws PhrescoException {
		String id= "test-jsLibs" ;
    	RestClient<ArtifactGroup> ArtifactGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
		GenericType<ArtifactGroup> genericType = new GenericType<ArtifactGroup>(){};
		ArtifactGroupClient.setPath(id);
		ArtifactGroup module = ArtifactGroupClient.getById(genericType);
        assertNotNull(module);
	}
	
	@Test
	public void testUpdatejsLibsById() throws PhrescoException {
		String moduleId="test-jsLibs";
        ArtifactGroup module = new ArtifactGroup();
        module.setId("test-module");
        module.setName("Test-moduleUpdateById");
        RestClient<ArtifactGroup> ArtifactGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        ArtifactGroupClient.setPath(moduleId);
        GenericType<ArtifactGroup> genericType = new GenericType<ArtifactGroup>() {};
        ArtifactGroup modules = ArtifactGroupClient.updateById(module, genericType);
        assertNotNull(modules);
	}
	
	@Test
	public void testDeletejsLibsById() throws PhrescoException {
		String id="test-jsLibs";
        RestClient<ArtifactGroup> ArtifactGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        ArtifactGroupClient.setPath(id);
        ClientResponse clientResponse = ArtifactGroupClient.deleteById();
        assertNotNull(clientResponse);
        
    }
}