package com.photon.phresco.service.client.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.License;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.impl.RestClient;
import com.sun.jersey.api.client.GenericType;

public class ComponentRestLicenseTest extends BaseRestTest {
	
	@Before
	public void initilaization() throws PhrescoException{
		initialize();
	}
	
	@Test
    public void testFindLicenses() throws PhrescoException{
    	RestClient<License> licenseClient = serviceManager.getRestClient(REST_API_COMPONENT + "/licenses");
        GenericType<List<License>> genericType = new GenericType<List<License>>(){};
        List<License> licenses = licenseClient.get(genericType);
        Assert.assertEquals(69, licenses.size());
    }
}
