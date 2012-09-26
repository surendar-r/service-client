package com.photon.phresco.service.client.test;

import java.util.ArrayList;
import java.util.List;

import com.photon.phresco.commons.model.ApplicationType;
import com.photon.phresco.commons.model.Technology;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.ServiceClientConstant;
import com.photon.phresco.service.client.api.ServiceContext;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.service.client.factory.ServiceClientFactory;
import com.photon.phresco.service.client.impl.RestClient;
import com.photon.phresco.service.client.util.RestUtil;
import com.photon.phresco.util.ServiceConstants;
import com.sun.jersey.api.client.ClientResponse;

public class BaseRestTest implements ServiceConstants {

	public static final String PHOTON_APP_TYPE_ID = "test-appType-photon";
	public static final String TEST_TECH_ID = "test-tech-id";
    public static final String TEST_CUSTOMER_ID = "test-cutomer-id";

	public static final String LOGIN_USER_ID = "jebastin_p";
	public static final String LOGIN_USER_PASSWORD = "Phresco@123";
	
	protected ServiceContext context = null;
	protected ServiceManager serviceManager = null;

	protected void initialize() throws PhrescoException {
		context = new ServiceContext();
		System.out.println("Server Path " + RestUtil.getServerPath());
		
        context.put(ServiceClientConstant.SERVICE_URL, RestUtil.getServerPath());
        context.put(ServiceClientConstant.SERVICE_USERNAME, LOGIN_USER_ID);
        context.put(ServiceClientConstant.SERVICE_PASSWORD, LOGIN_USER_PASSWORD);
        serviceManager = ServiceClientFactory.getServiceManager(context);
	}

	protected ApplicationType createAppType(String id, String name, String description, List<String> customerIds) {
		ApplicationType appType = new ApplicationType();
		if (id != null) {
		    appType.setId(id);
		}
		
	    appType.setName(name);
	    appType.setDescription(description);
		appType.setCustomerIds(customerIds);
		return appType;
	}
	
	protected ApplicationType saveApptype(ApplicationType appType) throws PhrescoException {
		String id = appType.getId();
		System.out.println("id " + id);
        RestClient<ApplicationType> apptypeRestClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
        List<ApplicationType> appTypes = new ArrayList<ApplicationType>();
        appTypes.add(appType);
		ClientResponse response = apptypeRestClient.create(appTypes);
		System.out.println("response.getStatus() " + response.getStatus());
		return appType;
	}

	protected Technology saveTechnology(Technology tech) throws PhrescoException {
		String id = tech.getId();
		System.out.println("id " + id);
        RestClient<Technology> apptypeRestClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
        List<Technology> techs = new ArrayList<Technology>();
        techs.add(tech);
        System.out.println("tech " + tech);
		ClientResponse response = apptypeRestClient.create(techs);
		
		System.out.println("response.getStatus() " + response.getStatus());
		return tech;
	}
}
