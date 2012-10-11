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

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.photon.phresco.commons.model.ApplicationType;
import com.photon.phresco.commons.model.User;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.impl.ClientHelper;
import com.photon.phresco.service.client.impl.RestClient;
import com.photon.phresco.service.client.util.RestUtil;
import com.photon.phresco.util.Credentials;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class APIManagerTest extends BaseRestTest {

    private static final String HEADER_NAME_AUTHORIZATION = "Authorization";
//    private static final String API_KEY = "Bearer fzlOdZfQaWfgr7uoZ7rcJI5hhjMa";
//    private static final String API_KEY = "Bearer czxnYrfBVR5qw20z6jU5fTZwLtIa";
    private static final String API_KEY = "Bearer hOUSaaxxU4idOvh3Vm4t_eG0r7Qa";
    
    
	@Before
	public void init() throws PhrescoException {
		initialize();
	}

	@Test
	public void testLoginAPI() throws PhrescoException {
    	String username = RestUtil.getUserName();
        String password = RestUtil.getPassword();
        String serverPath = RestUtil.getServerPath();
        
//        String serverPath = "http://localhost:8080/service/rest/api";
//        String serverPath = "http://172.16.21.186:8280/2.0-service/1.0.0/rest/api";
//        String serverPath = "http://172.16.21.186:2020/2.0-service/1.0.0/rest/api";
        
        System.out.println("serverPath " + serverPath);

        //encode the password
        byte[] encodeBase64 = Base64.encodeBase64(password.getBytes());
        String encodedString = new String(encodeBase64);
        
    	Credentials credentials = new Credentials(username, encodedString); 
    	Client client = ClientHelper.createClient();
        WebResource resource = client.resource(serverPath + "/" + LOGIN);

        Builder builder = resource.accept(MediaType.APPLICATION_JSON);
        if (API_KEY != null) {
        	builder = builder.header(HEADER_NAME_AUTHORIZATION, API_KEY);	
        }
        
        ClientResponse response = builder.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, credentials);
        GenericType<User> genericType = new GenericType<User>() {};
        User userInfo = response.getEntity(genericType);
        System.out.println("userInfo is " + userInfo);
	}

	@Test
	public void testGetAppTypes() throws PhrescoException {
		User userInfo = serviceManager.getUserInfo();
		System.out.println("serviceManager " + userInfo);
		
        RestClient<ApplicationType> applicationTypeClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
        applicationTypeClient.queryString(REST_QUERY_CUSTOMERID, "photon");
        GenericType<List<ApplicationType>> genericType = new GenericType<List<ApplicationType>>(){};
        List<ApplicationType> applicationTypes = applicationTypeClient.get(genericType);
        System.out.println(applicationTypes);
	}
	
	
}