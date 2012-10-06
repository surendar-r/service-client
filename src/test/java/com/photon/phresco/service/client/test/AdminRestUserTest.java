package com.photon.phresco.service.client.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.User;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.impl.RestClient;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class AdminRestUserTest extends BaseRestTest {
	
	@Before
    public void Initilaization() throws PhrescoException {
    	initialize();
    }
     
    @Test
    public void testCreateUser() throws PhrescoException {
        List<User> users = new ArrayList<User>();
        User user = new User();
        user.setId("test-user");
        user.setName("Test user");
        user.setDescription("users");
        users.add(user);
        RestClient<User> userClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_USERS);
        ClientResponse clientResponse = userClient.create(users);
    }
    
    @Test
    public void findUser() throws PhrescoException {
        RestClient<User> userClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_USERS);
        GenericType<List<User>> genericType = new GenericType<List<User>>(){};
        List<User> user = userClient.get(genericType);
        System.out.println(user.size());
    }
    
    
   @Test
    public void getUserById() throws PhrescoException {
        String userId = "test-user";
        RestClient<User> userClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_USERS);
        userClient.setPath(userId);
        GenericType<User> genericType = new GenericType<User>(){};
        User user = userClient.getById(genericType);
        Assert.assertEquals("Test User ", user.getName());
    }
    
    @Test
    public void updateUser() throws PhrescoException {
        String userId = "test-user";
        User user = new User();
        user.setId("test-user");
        user.setName("Test user");
        user.setDescription("updated users");
        RestClient<User> userClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_USERS);
        userClient.setPath(userId);
        GenericType<User> genericType = new GenericType<User>() {};
        userClient.updateById(user, genericType);
    }
    
    @Test
    public void deleteUser() throws PhrescoException {
        String userId = "test-user";
        RestClient<User> userClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_USERS);
        userClient.setPath(userId);
        ClientResponse clientResponse = userClient.deleteById();
    }
}
