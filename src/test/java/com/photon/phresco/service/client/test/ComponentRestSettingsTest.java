package com.photon.phresco.service.client.test;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.SettingsTemplate;
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

public class ComponentRestSettingsTest extends BaseRestTest {
	
	@Before
	public void initilaization() throws PhrescoException {
		initialize();
	}

	@Test
	public void testCreateSettings() throws PhrescoException {
		List<SettingsTemplate> settingTemplate=new ArrayList<SettingsTemplate>();
		SettingsTemplate st = new SettingsTemplate();
		st.setId("testSetting");
		st.setType("server");
		settingTemplate.add(st);
        RestClient<SettingsTemplate> newSetting = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
        ClientResponse clientResponse = newSetting.create(settingTemplate);
    }

	@Test
    public void testGetSettings() throws PhrescoException {
    	RestClient<SettingsTemplate> stClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
    	stClient.queryString(REST_QUERY_CUSTOMERID, "photon");
    	GenericType<List<SettingsTemplate>> genericType = new GenericType<List<SettingsTemplate>>(){};
		List<SettingsTemplate> settingTemplate = stClient.get(genericType);
		assertNotNull(settingTemplate);
    }

	@Test
	public void testUpdateSettingTemplate() throws PhrescoException{
		RestClient<SettingsTemplate> stClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
	    List<SettingsTemplate> settingTemp = new ArrayList<SettingsTemplate>();
	    SettingsTemplate st = new SettingsTemplate();
	    st.setId("testSetting");
	    st.setType("server update");
	    settingTemp.add(st);
	    GenericType<List<SettingsTemplate>> genericType = new GenericType<List<SettingsTemplate>>() {};
	    List<SettingsTemplate> clientResponse = stClient.update(settingTemp, genericType);
	}
	
	@Test
    public void testGetSettingsById() throws PhrescoException {
		String Id = "testSetting";
    	RestClient<SettingsTemplate> stClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
    	stClient.setPath(Id);
		GenericType<SettingsTemplate> genericType = new GenericType<SettingsTemplate>(){};
		SettingsTemplate settingsTemplate = stClient.getById(genericType);
		assertNotNull(settingsTemplate);
    }
	
	@Test
	public void testUpdateSettingsById() throws PhrescoException {
    	RestClient<SettingsTemplate> stClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
		GenericType<SettingsTemplate> genericType = new GenericType<SettingsTemplate>(){};
		SettingsTemplate st=new SettingsTemplate();
		st.setId("testSetting");
		st.setType("server updateById");
		stClient.setPath("testSetting");
		stClient.updateById(st, genericType);
    }
	
	@Test
	public void testDeleteSetting() throws PhrescoException {
        RestClient<SettingsTemplate> deleteSettingTemplate = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
        deleteSettingTemplate.setPath("testSetting");
        ClientResponse clientResponse = deleteSettingTemplate.deleteById();
    }
	

}
