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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactGroup.Type;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.Technology;
import com.photon.phresco.commons.model.Technology.Option;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.Content;
import com.photon.phresco.service.client.factory.ServiceClientFactory;
import com.photon.phresco.service.client.impl.RestClient;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

public class ComponentRestTechnologiesTest extends BaseRestTest {
	
	private static final String PHRESCO_TEST_ARCHETYPE_JAR = "phresco-test-archetype.jar";
	private static final String PHRESCO_TEST_ARCHETYPE_PLUGIN_JAR = "phresco-test-archetype-plugin.jar";

	@Before
	public void Initilaization() throws PhrescoException {
		initialize();
	}

    public Technology createTechnology() throws PhrescoException {
    	Technology tech = new Technology();
    	tech.setAppTypeId(PHOTON_APP_TYPE_ID);
    	List<String> customerIds = new ArrayList<String>();
    	customerIds.add(DEFAULT_CUSTOMER_NAME);
		tech.setCustomerIds(customerIds);

    	//Name
    	tech.setName("Test Technology 1");
    	tech.setDescription("Test Technology for Phresco Apptype");
    	
    	//Technology versions
    	List<String> versions = new ArrayList<String>();
    	versions.add("6.18");
    	versions.add("6.19");
    	versions.add("7.0");
    	tech.setTechVersions(versions);
    	
    	//ArchetypeInfo
    	ArtifactGroup archetypeInfo = new ArtifactGroup();
    	archetypeInfo.setGroupId("com.photon.phresco.archetypes");
    	archetypeInfo.setArtifactId("test-technology");
    	archetypeInfo.setHelpText("This is helpText to be shown on tooltip");
    	archetypeInfo.setPackaging("archetype");
    	archetypeInfo.setType(Type.ARCHETYPE);
    	
    	List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
    	ArtifactInfo artifactInfo = new ArtifactInfo();
    	artifactInfo.setFileSize(1024 * 1024 * 2);

//    	Plugin and dependencies needs to be uploaded and ids should be provided 
//		List<String> dependencyIds = new ArrayList<String>();
//		artifactInfo.setDependencies(dependencyIds);
		
		artifactInfos.add(artifactInfo);
		archetypeInfo.setVersions(artifactInfos);
		tech.setArchetypeInfo(archetypeInfo);

		//Technology Options
		tech.setOptions(Arrays.asList(Option.values()));
		return tech;
    }
	
    @Test
	public void testCreateTechnology() throws PhrescoException, IOException {
		InputStream is = null, fis = null;
		
		try {
			//Create a multipart
			MultiPart multiPart = new MultiPart();
			
			//Add technology in the body part
			Technology tech = createTechnology();
	        BodyPart jsonPart = new BodyPart();
	        jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
	        jsonPart.setEntity(tech);
	        Date date = new Date();
			Content content = new Content(Content.Type.JSON, tech.getId(), date, date, date, 0);
	        jsonPart.setContentDisposition(content);
	        multiPart.bodyPart(jsonPart);
	        
	        //Add the archetype jar into the body
	        BodyPart archetypePart = new BodyPart();
	        archetypePart.setMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
	        fis = this.getClass().getClassLoader().getResourceAsStream(PHRESCO_TEST_ARCHETYPE_JAR);
	        archetypePart.setEntity(fis);
	        
	        content = new Content(Content.Type.ARCHETYPE, tech.getId(), date, date, date, fis.available());
	        archetypePart.setContentDisposition(content);
	        multiPart.bodyPart(archetypePart);
	
	        //Add the plugin jar into the body
	        BodyPart pluginPart = new BodyPart();
	        pluginPart.setMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
	        is = this.getClass().getClassLoader().getResourceAsStream(PHRESCO_TEST_ARCHETYPE_PLUGIN_JAR);
	        pluginPart.setEntity(is);
	        content = new Content(Content.Type.ARCHETYPE, tech.getId(), date, date, date, is.available());
	        pluginPart.setContentDisposition(content);
	        multiPart.bodyPart(pluginPart);
	        
	        //Send the data to the service
	        serviceManager = ServiceClientFactory.getServiceManager(context);            
	        RestClient<Technology> techClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
	        ClientResponse create = techClient.create(multiPart);

	        multiPart.close();

	        System.out.println(create.getStatus());
		} finally {
            if (is != null) {
            	is.close();
            }

            if (fis != null) {
            	fis.close();
            }
        }
        
	}

//	public void createTest() throws FileNotFoundException, PhrescoException {
//		
//	    MultiPart multiPart = new MultiPart();
//	    
//	    Technology technology = new Technology();
//	    technology.setId("drup");
//	    technology.setName("Drupal Technology");
        
//        BodyPart jsonPart = new BodyPart();
//        jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
//        jsonPart.setEntity(technology);
//        Content content = new Content("plugin", "drup", null, null, null, 0);
//        jsonPart.setContentDisposition(content);
//        multiPart.bodyPart(jsonPart);
               
       /* BodyPart binaryPart = new BodyPart();
        binaryPart.setMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        InputStream fis  = new FileInputStream(new File("d://Temp/drupal-maven-plugin-2.0.0.6001-SNAPSHOT.jar"));
        binaryPart.setEntity(fis);
        content = new Content("plugin", "drup", null, null, null, 0);
        binaryPart.setContentDisposition(content);
        multiPart.bodyPart(binaryPart);

        BodyPart binaryPart2 = new BodyPart();
        binaryPart2.setMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        fis  = new FileInputStream(new File("d://Temp/phresco-drupal7-archetype-1.2.0.9000.jar"));
        binaryPart2.setEntity(fis);
        content = new Content("appType", "drup", null, null, null, 0);
        binaryPart2.setContentDisposition(content);
        multiPart.bodyPart(binaryPart2);*/
        
//        serviceManager = ServiceClientFactory.getServiceManager(context);            
//        RestClient<Technology> techClient = serviceManager.getRestClient("/components/technologies");
//        techClient.queryString("appId", "web-app");
//        ClientResponse create = techClient.create(multiPart);
//        System.out.println(create.getStatus());
//	}
	

//	@Ignore
//    public void testGetTechnologies() {
//        try {
//            serviceManager = ServiceClientFactory.getServiceManager(context);            
//            RestClient<Technology> techClient = serviceManager.getRestClient("/component/technologies");
//            GenericType<List<Technology>> genericType = new GenericType<List<Technology>>(){};
//            List<Technology> list = techClient.get(genericType);
////            id = list.get(0).getId();
////            id2 = list.get(1).getId();
////            id3 = list.get(2).getId();
//            for (Technology tech : list) {
//                System.out.println("Tech Name == " + tech.getName() + " id " + tech.getId());
//                System.out.println("tec " + tech);
//            }
//            
//        } catch (PhrescoException e) {
//            e.printStackTrace();
//        }
//    }
//    
//	@Ignore
//    public void testPutTechnologies() throws PhrescoException {
//    	List<Technology> techs = new ArrayList<Technology>();
//    	Technology tech = new Technology();
//    	System.out.println("id = " + id);
//    	tech.setId(id);
//    	tech.setName("Java");
//    	List<String> versions = new ArrayList<String>();
//    	versions.add("1.5");
//    	versions.add("1.6");
//		techs.add(tech);
//    	serviceManager = ServiceClientFactory.getServiceManager(context);
//		RestClient<Technology> techClient = serviceManager.getRestClient("/component/technologies");
//		GenericType<List<Technology>> type = new GenericType<List<Technology>>(){};
//		List<Technology> entity = techClient.update(techs, type);
//		for (Technology technology : entity) {
//			System.out.println("tec " + technology);
//		}
//    }
//    
//	@Ignore
//    public void testGetTechnologyById() throws PhrescoException {
//        try {
//	    	serviceManager=ServiceClientFactory.getServiceManager(context);
//	    	RestClient<Technology> techClient = serviceManager.getRestClient("/component/technologies");
//	    	techClient.setPath(id2);
//	    	GenericType<Technology> genericType = new GenericType<Technology>()  {};
//	    	Technology tech = techClient.getById(genericType);
//	    	System.out.println("name == " + tech);
//    	    
//        } catch(PhrescoException e){
//        	e.printStackTrace();
//        }
//    }
//    
//	@Ignore
//    public void testPutTechnologyById() throws PhrescoException {
//    	Technology tech = new Technology();
//    	tech.setId(id2);
//    	tech.setName("android-native");
//    	List<String> versions = new ArrayList<String>();
//    	versions.add("1.0");
//    	versions.add("3.0");
//    	serviceManager = ServiceClientFactory.getServiceManager(context);
//		RestClient<Technology> techClient = serviceManager.getRestClient("/component/technologies");
//		techClient.setPath(id2);
//		GenericType<Technology> genericType = new GenericType<Technology>()  {};
//		Technology technology = techClient.updateById(tech, genericType);
//		System.out.println(technology);
//    }
//
//	@Ignore
//    public void testDeleteTechnologyById() throws PhrescoException {
//    	serviceManager = ServiceClientFactory.getServiceManager(context);            
//    	RestClient<Technology> techClient = serviceManager.getRestClient("/component/technologies");
//    	techClient.setPath(id3);
//    	ClientResponse response = techClient.deleteById();
//    	System.out.println(response.getStatus());
//    }
//	
//	@Test
//	public void createTest() throws FileNotFoundException, PhrescoException {
//	    MultiPart multiPart = new MultiPart();
//	    
//	    Technology technology = new Technology();
//	    technology.setId("drup");
//	    technology.setName("Drupal Technology");
//        
//        BodyPart jsonPart = new BodyPart();
//        jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
//        jsonPart.setEntity(technology);
//        Content content = new Content("plugin", "drup", null, null, null, 0);
//        jsonPart.setContentDisposition(content);
//        multiPart.bodyPart(jsonPart);
//               
//       /* BodyPart binaryPart = new BodyPart();
//        binaryPart.setMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
//        InputStream fis  = new FileInputStream(new File("d://Temp/drupal-maven-plugin-2.0.0.6001-SNAPSHOT.jar"));
//        binaryPart.setEntity(fis);
//        content = new Content("plugin", "drup", null, null, null, 0);
//        binaryPart.setContentDisposition(content);
//        multiPart.bodyPart(binaryPart);
//
//        BodyPart binaryPart2 = new BodyPart();
//        binaryPart2.setMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
//        fis  = new FileInputStream(new File("d://Temp/phresco-drupal7-archetype-1.2.0.9000.jar"));
//        binaryPart2.setEntity(fis);
//        content = new Content("appType", "drup", null, null, null, 0);
//        binaryPart2.setContentDisposition(content);
//        multiPart.bodyPart(binaryPart2);*/
//        
//        serviceManager = ServiceClientFactory.getServiceManager(context);            
//        RestClient<Technology> techClient = serviceManager.getRestClient("/components/technologies");
//        techClient.queryString("appId", "web-app");
//        ClientResponse create = techClient.create(multiPart);
//        System.out.println(create.getStatus());
//	}
	
}
