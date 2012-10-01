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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactGroup.Type;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.Content;
import com.photon.phresco.service.client.impl.RestClient;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

public class ComponentRestModulesTest extends BaseRestTest {

	@Before
	public void initilaization() throws PhrescoException {
		initialize();
	}
	
	@Test
	public void testCreateModules() throws PhrescoException, IOException {
		InputStream is = null, fis = null;
		
		try {
			//Create a multipart
			MultiPart multiPart = new MultiPart();
			
			//Add technology in the body part
			ArtifactGroup module = createModule();
	        BodyPart jsonPart = new BodyPart();
	        jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
	        jsonPart.setEntity(module);
	        Date date = new Date();
			Content content = new Content(Content.Type.JSON, module.getId(), date, date, date, 0);
	        jsonPart.setContentDisposition(content);
	        multiPart.bodyPart(jsonPart);
	        
	        //Add the archetype jar into the body
	        BodyPart archetypePart = new BodyPart();
	        archetypePart.setMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
	        fis = this.getClass().getClassLoader().getResourceAsStream(PHRESCO_TEST_ARCHETYPE_JAR);
	        archetypePart.setEntity(fis);
	        
	        content = new Content(Content.Type.ARCHETYPE, module.getId(), date, date, date, fis.available());
	        archetypePart.setContentDisposition(content);
	        multiPart.bodyPart(archetypePart);
	        
	        RestClient<ArtifactGroup> moduleClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
	        ClientResponse response = moduleClient.create(multiPart);
	        
	        Assert.assertEquals(201, response.getStatus());
		} finally {
            if (is != null) {
            	is.close();
            }

            if (fis != null) {
            	fis.close();
            }
        }
	}
	
	private ArtifactGroup createModule() {
		ArtifactGroup group = new ArtifactGroup(TEST_MODULE_ID);
		group.setGroupId("com.photon.phresco");
		group.setName("sampleUpdate");
		group.setArtifactId("test-module");
		group.setHelpText("This is helpText to be shown on tooltip");
		group.setPackaging("jar");
		group.setType(Type.FEATURE);

    	List<String> customerIds = new ArrayList<String>();
    	customerIds.add(DEFAULT_CUSTOMER_NAME);
		group.setCustomerIds(customerIds);

    	List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
    	ArtifactInfo artifactInfo = new ArtifactInfo();
    	artifactInfo.setFileSize(1024 * 1024 * 2);
    	artifactInfo.setVersion("1.3");

//    	Plugin and dependencies needs to be uploaded and ids should be provided 
//		List<String> dependencyIds = new ArrayList<String>();
//		artifactInfo.setDependencies(dependencyIds);
		
		artifactInfos.add(artifactInfo);
		group.setVersions(artifactInfos);

		return group;
	}

//	@Test
    public void testGetModules() throws PhrescoException {
    	RestClient<ArtifactGroup> moduleGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
    	Map<String, String> query = new HashMap<String, String>();
    	query.put(REST_QUERY_TYPE, ArtifactGroup.Type.FEATURE.name());
    	query.put(REST_QUERY_TECHID, "drupal");
    	query.put(REST_QUERY_CUSTOMERID, DEFAULT_CUSTOMER_NAME);
    	moduleGroupClient.queryStrings(query);
		GenericType<List<ArtifactGroup>> genericType = new GenericType<List<ArtifactGroup>>(){};
		List<ArtifactGroup> modules = moduleGroupClient.get(genericType);
		int size = modules.size();
		
		System.out.println(modules);
		
		assertNotNull(modules);
    }
	
	@Test
	public void testUpdateModules() throws PhrescoException, IOException{
		InputStream is = null, fis = null;
		try {
			MultiPart multiPart = new MultiPart();
			
			ArtifactGroup module = createModule();
	        BodyPart jsonPart = new BodyPart();
	        jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
	        jsonPart.setEntity(module);
	        Date date = new Date();
			Content content = new Content(Content.Type.JSON, module.getId(), date, date, date, 0);
	        jsonPart.setContentDisposition(content);
	        multiPart.bodyPart(jsonPart);
	        
	        BodyPart archetypePart = new BodyPart();
	        archetypePart.setMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
	        fis = this.getClass().getClassLoader().getResourceAsStream(PHRESCO_TEST_ARCHETYPE_JAR);
	        archetypePart.setEntity(fis);
	        
	        content = new Content(Content.Type.ARCHETYPE, module.getId(), date, date, date, fis.available());
	        archetypePart.setContentDisposition(content);
	        multiPart.bodyPart(archetypePart);
		
	        RestClient<ArtifactGroup> moduleGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
		    ClientResponse modules = moduleGroupClient.update(multiPart);
		} finally {
            if (is != null) {
            	is.close();
            }

            if (fis != null) {
            	fis.close();
            }
        }
	}
	
//	@Test
    public void testGetModuleById() throws PhrescoException {
    	RestClient<ArtifactGroup> moduleGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
		GenericType<ArtifactGroup> genericType = new GenericType<ArtifactGroup>(){};
		moduleGroupClient.setPath(TEST_MODULE_ID);
		ArtifactGroup module = moduleGroupClient.getById(genericType);
		System.out.println(module);
		
        assertNotNull(module);
        
        Assert.assertEquals(TEST_MODULE_ID, module.getId());
	}
	
//	@Test
	public void testUpdateModuleById() throws PhrescoException {
		ArtifactGroup module = new ArtifactGroup(TEST_MODULE_ID);
        module.setName("TestmoduleUpdateById");
        RestClient<ArtifactGroup> moduleGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        moduleGroupClient.setPath(TEST_MODULE_ID);
        GenericType<ArtifactGroup> genericType = new GenericType<ArtifactGroup>() {};
        ArtifactGroup modules = moduleGroupClient.updateById(module, genericType);
        
        assertNotNull(modules);
	}
	
//	@Test
	public void testDeleteModuleById() throws PhrescoException {
        RestClient<ArtifactGroup> moduleGroupClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        moduleGroupClient.setPath(TEST_MODULE_ID);
        ClientResponse clientResponse = moduleGroupClient.deleteById();
        
        Assert.assertEquals(200, clientResponse.getStatus());
    }
	
}