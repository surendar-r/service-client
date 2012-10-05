package com.photon.phresco.service.client.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.PlatformType;
import com.photon.phresco.commons.model.VideoInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.impl.RestClient;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class ComponentRestPlatformTest extends BaseRestTest {

	@Before
	public void Initilaization() throws PhrescoException {
		initialize();
	}
	
	@Test
	public void testFindPlateform() throws PhrescoException{
		RestClient<PlatformType> platformTypeclient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_PLATFORMS);
		GenericType<List<PlatformType>> genericType = new GenericType<List<PlatformType>>(){};
        List<PlatformType> platForm = platformTypeclient.get(genericType);
        Assert.assertEquals(10, platForm.size());
	}

}
