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

import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.model.ModuleGroup;
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
		List<ModuleGroup> modules=new ArrayList<ModuleGroup>();
		ModuleGroup moduleGroup = new ModuleGroup();
		moduleGroup.setId("test-jsLibs");
		moduleGroup.setName("TestjsLibsone");
		moduleGroup.setCustomerId("photon");
		moduleGroup.setTechId("php");
		moduleGroup.setGroupId("phresco");
		moduleGroup.setType("js"); 
		modules.add(moduleGroup);
        RestClient<ModuleGroup> newApp = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        ClientResponse clientResponse = newApp.create(modules);
        assertNotNull(clientResponse);    
	}
	
	@Test
    public void testGetjsLibs() throws PhrescoException {
		RestClient<ModuleGroup> moduleGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
		Map<String, String> query = new HashMap<String, String>();
		query.put(REST_QUERY_TYPE, "js");
		query.put(REST_QUERY_CUSTOMERID, "photon");
		moduleGroupClient.queryStrings(query);
		GenericType<List<ModuleGroup>> genericType = new GenericType<List<ModuleGroup>>(){};
		List<ModuleGroup> modules = moduleGroupClient.get(genericType);
		assertNotNull(modules);
    }
	
	@Test
	public void testUpdatejsLibs() throws PhrescoException{
		RestClient<ModuleGroup> moduleGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
	    List<ModuleGroup> ModuleGroups = new ArrayList<ModuleGroup>();
	    ModuleGroup moduleGroup = new ModuleGroup();
	    moduleGroup.setId("test-jsLibs");
		moduleGroup.setName("TestjsLibsUpdate");
		moduleGroup.setCustomerId("phresco");
		moduleGroup.setTechId("php");
		moduleGroup.setType("Js");
		ModuleGroups.add(moduleGroup);
	    GenericType<List<ModuleGroup>> genericType = new GenericType<List<ModuleGroup>>() {};
	    List<ModuleGroup> modules = moduleGroupClient.update(ModuleGroups, genericType);
	    assertNotNull(modules);
	}
	
	@Test
    public void testGetjsLibsById() throws PhrescoException {
		String id= "test-jsLibs" ;
    	RestClient<ModuleGroup> moduleGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
		GenericType<ModuleGroup> genericType = new GenericType<ModuleGroup>(){};
		moduleGroupClient.setPath(id);
		ModuleGroup module = moduleGroupClient.getById(genericType);
        assertNotNull(module);
	}
	
	@Test
	public void testUpdatejsLibsById() throws PhrescoException {
		String moduleId="test-jsLibs";
        ModuleGroup module = new ModuleGroup();
        module.setId("test-module");
        module.setName("Test-moduleUpdateById");
        module.setCustomerId("photon");
        RestClient<ModuleGroup> moduleGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        moduleGroupClient.setPath(moduleId);
        GenericType<ModuleGroup> genericType = new GenericType<ModuleGroup>() {};
        ModuleGroup modules = moduleGroupClient.updateById(module, genericType);
        assertNotNull(modules);
	}
	
	@Test
	public void testDeletejsLibsById() throws PhrescoException {
		String id="test-jsLibs";
        RestClient<ModuleGroup> moduleGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        moduleGroupClient.setPath(id);
        ClientResponse clientResponse = moduleGroupClient.deleteById();
        assertNotNull(clientResponse);
        
    }
}