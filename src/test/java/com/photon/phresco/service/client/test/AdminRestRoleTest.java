package com.photon.phresco.service.client.test;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.Role;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.impl.RestClient;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class AdminRestRoleTest extends BaseRestTest {

	    @Before
	    public void Initilaization() throws PhrescoException {
	    	initialize();
	    }
	     
	    @Test
	    public void testCreateRole() throws PhrescoException {
	        List<Role> roles = new ArrayList<Role>();
	        Role role = new Role();
	        role.setId("test-role");
	        role.setName("Test role");
	        role.setDescription("roles");
	        roles.add(role);
	        RestClient<Role> roleClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_ROLES);
	        ClientResponse clientResponse = roleClient.create(roles);
	    }
	    
	    @Test
	    public void findRole() throws PhrescoException {
	        RestClient<Role> roleClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_ROLES);
	        GenericType<List<Role>> genericType = new GenericType<List<Role>>(){};
	        List<Role> role = roleClient.get(genericType);
	        Assert.assertEquals(1, role.size());
	    }
	    
	    
	    @Test
	    public void getRoleById() throws PhrescoException {
	        String roleId = "test-role";
	        RestClient<Role> roleClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_ROLES);
	        roleClient.setPath(roleId);
	        GenericType<Role> genericType = new GenericType<Role>(){};
	        Role role = roleClient.getById(genericType);
	        Assert.assertEquals("Test role", role.getName());
	    }
	    
	    @Test
	    public void updateRole() throws PhrescoException {
	        String roleId = "test-role";
	        Role role = new Role();
	        role.setId("test-role");
	        role.setName("Test role");
	        role.setDescription("updated roles");
	        RestClient<Role> roleClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_ROLES);
	        roleClient.setPath(roleId);
	        GenericType<Role> genericType = new GenericType<Role>() {};
	        roleClient.updateById(role, genericType);
	    }
	    
	    @Test
	    public void deleteRole() throws PhrescoException {
	        String roleId = "test-role";
	        RestClient<Role> roleClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_ROLES);
	        roleClient.setPath(roleId);
	        ClientResponse clientResponse = roleClient.deleteById();
	    }
	}