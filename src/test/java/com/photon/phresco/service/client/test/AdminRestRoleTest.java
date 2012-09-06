package com.photon.phresco.service.client.test;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.Role;
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

public class AdminRestRoleTest implements ServiceConstants {

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
	    public void testCreateRole() throws PhrescoException {
	        List<Role> roles = new ArrayList<Role>();
	        Role role = new Role();
	        role.setId("test-role");
	        role.setName("Test role");
	        role.setDescription("roles");
	        roles.add(role);
	        RestClient<Role> roleClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_ROLES);
	        ClientResponse clientResponse = roleClient.create(roles);
	        assertNotNull(clientResponse);
	    }
	    
	    @Test
	    public void findRole() throws PhrescoException {
	        RestClient<Role> roleClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_ROLES);
	        GenericType<List<Role>> genericType = new GenericType<List<Role>>(){};
	        List<Role> role = roleClient.get(genericType);
	        assertNotNull(role);
	    }
	    
	    
	    @Test
	    public void getRoleById() throws PhrescoException {
	        String roleId = "test-role";
	        RestClient<Role> roleClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_ROLES);
	        roleClient.setPath(roleId);
	        GenericType<Role> genericType = new GenericType<Role>(){};
	        Role role = roleClient.getById(genericType);
	        assertNotNull(role);
	    }
	    
	    @Test
	    public void updateRole() throws PhrescoException {
	        String roleId = "test-role";
	        Role role = new Role();
	        role.setId("test-role");
	        role.setName("Test role update");
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
	        assertNotNull(clientResponse);
	    }
	}