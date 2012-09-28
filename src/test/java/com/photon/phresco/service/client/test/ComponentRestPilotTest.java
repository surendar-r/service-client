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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.Element;
import com.photon.phresco.commons.model.TechnologyInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.impl.RestClient;
import com.photon.phresco.util.ServiceConstants;
import com.photon.phresco.util.Utility;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

public class ComponentRestPilotTest extends BaseRestTest {
	
	@Before
	public void initilaization() throws PhrescoException {
		initialize();
	}
	
	public ApplicationInfo createAppInfo() {
		ApplicationInfo appInfo = new ApplicationInfo();
		appInfo.setId("PHP-blog");
		appInfo.setCode("");
		appInfo.setCreationDate(new Date());
		appInfo.setDescription("Test Pilot Project");
		appInfo.setEmailSupported(true);
		appInfo.setName("PHP Blog");
		
		ArtifactGroup pilotContent = new ArtifactGroup();
		pilotContent.setGroupId("pilots");
		pilotContent.setArtifactId("tech-php");
		ArtifactInfo info = new ArtifactInfo();
		info.setVersion("2.0");
		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
		artifactInfos.add(info);
		pilotContent.setVersions(artifactInfos);
		appInfo.setPilotContent(pilotContent);
		
		Element pilotInfo = new Element();
		pilotInfo.setName("phpblog");
		appInfo.setPilotInfo(pilotInfo);
		
		List<String> list = new ArrayList<String>();
		list.add("mysql");
		appInfo.setSelectedDatabases(list);
		
		list = new ArrayList<String>();
		list.add("jsscroll");
		appInfo.setSelectedJSLibs(list);
		
		list = new ArrayList<String>();
		list.add("mod_Blog");
		appInfo.setSelectedModules(list);
		
		list = new ArrayList<String>();
		list.add("tomcat");
		appInfo.setSelectedServers(list);
		
		TechnologyInfo techInfo = new TechnologyInfo();
		techInfo.setAppTypeId("web-app");
		techInfo.setVersion("6.0");
		appInfo.setTechInfo(techInfo);
		
		List<String> customerIds = new ArrayList<String>();
		customerIds.add("Tester");
		appInfo.setCustomerIds(customerIds);
		
		return appInfo;
	}
	
	@Test
	public void testCreatePilots() throws PhrescoException, IOException {
		InputStream fis = null;
		try {
			MultiPart multiPart = new MultiPart();
			BodyPart bodyPart = new BodyPart();
			bodyPart.setEntity(createAppInfo());
			bodyPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
			
			BodyPart binaryPart = new BodyPart();
			fis = this.getClass().getClassLoader().getResourceAsStream("tech-php.zip");
			binaryPart.setEntity(fis);
			binaryPart.setMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
			
			multiPart.bodyPart(bodyPart);
			multiPart.bodyPart(binaryPart);
			
			RestClient<ApplicationInfo> techClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
	        ClientResponse create = techClient.create(multiPart);

	        multiPart.close();

	        Assert.assertEquals(201, create.getStatus());
			
		} finally {
			Utility.closeStream(fis);
		}
	}

	@Test
	public void testFindPilots() throws PhrescoException {
		String customerId = "photon";
		RestClient<ApplicationInfo> pilotClient = serviceManager
				.getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
		pilotClient.queryString(REST_QUERY_CUSTOMERID, customerId);
		GenericType<List<ApplicationInfo>> genericType = new GenericType<List<ApplicationInfo>>() {};
		List<ApplicationInfo> projectInfos = pilotClient.get(genericType);
		
		Assert.assertEquals(1, projectInfos.size());
	}

	@Test
	public void testUpdatePilot() throws PhrescoException {
		List<ApplicationInfo> appInfos = new ArrayList<ApplicationInfo>();
		ApplicationInfo appinfo = createAppInfo();
		appinfo.setName("PHP_Blog");
		appInfos.add(appinfo);
		RestClient<ApplicationInfo> pilotClient = serviceManager
				.getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
		GenericType<List<ApplicationInfo>> genericType = new GenericType<List<ApplicationInfo>>() { };
		List<ApplicationInfo> clientResponse = pilotClient.update(appInfos, genericType);
		
		Assert.assertEquals("Tester", clientResponse.get(0).getCustomerIds().get(0));
	}

	@Test
	public void testGetPilotById() throws PhrescoException {
		String id = "PHP-blog";
		RestClient<ApplicationInfo> pilotClient = serviceManager
				.getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
		pilotClient.setPath(id);
		GenericType<ApplicationInfo> genericType = new GenericType<ApplicationInfo>() { };
		ApplicationInfo projectInfo = pilotClient.getById(genericType);
		Assert.assertEquals("Tester", projectInfo.getCustomerIds().get(0));
	}

	@Test
	public void testUpdatePilotById() throws PhrescoException {
		String id = "PHP-blog";
		RestClient<ApplicationInfo> client = serviceManager
				.getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
		client.setPath(id);
		GenericType<ApplicationInfo> genericType = new GenericType<ApplicationInfo>() {	};
		ApplicationInfo appInfo = createAppInfo();
		appInfo.setName("PHP Blog");
		ApplicationInfo applicationInfo = client.updateById(appInfo, genericType);
		Assert.assertEquals("PHP Blog", applicationInfo.getName());
	}

	@Test
	public void testDeletePilot() throws PhrescoException {
		RestClient<ApplicationInfo> deletePilot = serviceManager
				.getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
		deletePilot.setPath("PHP-blog");
		ClientResponse clientResponse = deletePilot.deleteById();
		System.out.println(clientResponse.getStatus());
	}
	
}