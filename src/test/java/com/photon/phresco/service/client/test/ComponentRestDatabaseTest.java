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
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.commons.model.Element;
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

public class ComponentRestDatabaseTest implements ServiceConstants {

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
	public void testCreateDownloadInfo() throws PhrescoException {
		List<Element> technologies = new ArrayList<Element>();
		Element tech = new Element();
		tech.setId("tech-php");
		technologies.add(tech);
	    List<DownloadInfo> DownloadInfo = new ArrayList<DownloadInfo>();
	    DownloadInfo db = new DownloadInfo();
	    db.setId("testDownloadInfo");
	    db.setName("TestDownloadInfo");
	    db.setDescription("This is a test DownloadInfo");
	    db.setAppliesToTechIds(technologies);
	    DownloadInfo.add(db);
        RestClient<DownloadInfo> newApp = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_DATABASES);
        ClientResponse clientResponse = newApp.create(DownloadInfo);
    }
	
	
	@Test
    public void testGetDownloadInfos() throws PhrescoException {
		String techId="Html5";
    	RestClient<DownloadInfo> dbClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_DATABASES);
    	dbClient.queryString(REST_QUERY_TECHID, techId);
    	dbClient.queryString(REST_QUERY_CUSTOMERID, "photon");
		GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
		List<DownloadInfo> DownloadInfos = dbClient.get(genericType);
		assertNotNull(DownloadInfos);
    }

	@Test
	public void testUpdateDownloadInfo() throws PhrescoException{
		RestClient<DownloadInfo> DownloadInfoClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_DATABASES);
		List<Element> technologies = new ArrayList<Element>();
		Element tech = new Element();
		tech.setId("tech-php");
		technologies.add(tech);
	    List<DownloadInfo> DownloadInfo = new ArrayList<DownloadInfo>();
	    DownloadInfo db = new DownloadInfo();
	    db.setId("testDownloadInfo");
	    db.setName("TestDownloadInfo");
	    db.setDescription("This is a test DownloadInfo update");
	    db.setAppliesToTechIds(technologies);
	    DownloadInfo.add(db);
	    GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>() {};
	   
	    List<DownloadInfo> clientResponse = DownloadInfoClient.update(DownloadInfo, genericType);
	    
	}

	@Test
    public void testGetDownloadInfosById() throws PhrescoException {
		String Id = "testDownloadInfo";
    	RestClient<DownloadInfo> dbClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_DATABASES);
    	dbClient.setPath(Id);
		GenericType<DownloadInfo> genericType = new GenericType<DownloadInfo>(){};
		DownloadInfo DownloadInfos=dbClient.getById(genericType);
		assertNotNull(DownloadInfos);
    }
	
	@Test
	public void testUpdateDownloadInfoById() throws PhrescoException {
        RestClient<DownloadInfo> editDB = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_DATABASES);
        DownloadInfo db = new DownloadInfo();
        db.setId("testDownloadInfo");
	    db.setName("TestDownloadInfoUpdateBYId");
	    db.setDescription("This is a test DownloadInfo updateId");
	    editDB.setPath("testDownloadInfo");
        GenericType<DownloadInfo> genericType = new GenericType<DownloadInfo>() {};
        editDB.updateById(db, genericType);
    }

	@Test
	public void testDeleteDownloadInfo() throws PhrescoException {
        RestClient<DownloadInfo> deleteDownloadInfo = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_DATABASES);
        deleteDownloadInfo.setPath("testDownloadInfo");
        ClientResponse clientResponse = deleteDownloadInfo.deleteById();
    }
}