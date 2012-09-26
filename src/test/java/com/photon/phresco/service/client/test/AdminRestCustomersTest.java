package com.photon.phresco.service.client.test;

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.photon.phresco.commons.model.Customer;
import com.photon.phresco.commons.model.RepoInfo;
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

public class AdminRestCustomersTest extends BaseRestTest {

    @Before
    public void Initilaization() throws PhrescoException {
    	initialize();
    }
    
//    @Test
    public void testCreateCustomers() throws PhrescoException, MalformedURLException {
        List<Customer> customers = new ArrayList<Customer>();
        Customer customer = createCustomer("photon", "photon", "photon");
        customer.setRepoInfo(createRepoInfo("photon"));
        customers.add(customer);
        
        customer = createCustomer("macys", "macys", "macys");
        customer.setRepoInfo(createRepoInfo("macys"));
        customers.add(customer);
        
        customer = createCustomer("vwr", "vwr", "vwr");
        customer.setRepoInfo(createRepoInfo("vwr"));
        customers.add(customer);
        
        customer = createCustomer("bestbuy", "bestbuy", "bestbuy");
        customer.setRepoInfo(createRepoInfo("bestbuy"));
        customers.add(customer);

        customer = createCustomer(TEST_CUSTOMER_ID, "Test Customer", "Test Customer");
        customer.setRepoInfo(createRepoInfo(TEST_CUSTOMER_ID));
        customers.add(customer);
        
        RestClient<Customer> customersClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        ClientResponse clientResponse = customersClient.create(customers);
        
        Assert.assertEquals(clientResponse.getStatus(), 200);
    }
    
    private Customer createCustomer(String id, String name, String desc) {
    	Customer customer = new Customer(id);
    	customer.setName(name);
    	customer.setDescription(desc);
    	return customer;
    }
    
    private RepoInfo createRepoInfo(String customer) throws MalformedURLException {
        RepoInfo info = new RepoInfo();
        info.setCustomerId(customer);
        byte[] encodeBase64 = Base64.encodeBase64("dummy123".getBytes());
        String encodedPassword = new String(encodeBase64);
        info.setRepoPassword(encodedPassword);
        info.setReleaseRepoURL("http://172.16.18.178:8080/nexus/content/repositories/2.0TestRepo/");
        info.setSnapshotRepoURL("");
        info.setGroupRepoURL("http://172.16.18.178:8080/nexus/content/groups/public/");
        info.setRepoUserName("admin");
        return info;
    }
    
//    @Test
    public void getCustomers() throws PhrescoException {
        RestClient<Customer> customersClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        GenericType<List<Customer>> genericType = new GenericType<List<Customer>>(){};
        List<Customer> customers = customersClient.get(genericType);
        
        Assert.assertEquals(5, customers.size());
    }
    
    
//	@Test
    public void getCustomerById() throws PhrescoException {
        RestClient<Customer> customersClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        customersClient.setPath(TEST_CUSTOMER_ID);
        GenericType<Customer> genericType = new GenericType<Customer>(){};
        Customer customer = customersClient.getById(genericType);
        
        Assert.assertEquals(TEST_CUSTOMER_ID, customer.getId());
    }
    
    @Test
    public void updateCustomer() throws PhrescoException {
        Customer customer = new Customer();
        customer.setId(TEST_CUSTOMER_ID);
        String name = "Test customer update";
		customer.setName(name);
        customer.setDescription("Updated");
        
        RestClient<Customer> customersClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        customersClient.setPath(TEST_CUSTOMER_ID);
        GenericType<Customer> genericType = new GenericType<Customer>() {};
        Customer updateCustomer = customersClient.updateById(customer, genericType);
        
        Assert.assertEquals(name, updateCustomer.getName());
        Assert.assertEquals(TEST_CUSTOMER_ID, updateCustomer.getId());
    }
    
    @Test
    public void deleteCustomer() throws PhrescoException {
        RestClient<Customer> customersClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        customersClient.setPath(TEST_CUSTOMER_ID);
        ClientResponse clientResponse = customersClient.deleteById();
        
        assertNotNull(clientResponse);

        Assert.assertEquals(clientResponse.getStatus(), 200);
    }
}