package com.photon.phresco.service.client.test;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.impl.RestClient;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

public class AdminRestDownloadInfoTest extends BaseRestTest {
    private static final String TEST_DOWNLOADINFO_ID = "test-downloadinfo";

	@Before
    public void initilaization() throws PhrescoException {
    	initialize();
    }
    
	@Test
	public void testCreateDownloadInfo() throws PhrescoException {
		 List<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
		 DownloadInfo downloadInfo = new DownloadInfo();
		 downloadInfo.setId(TEST_DOWNLOADINFO_ID);
		 downloadInfo.setName("Test customer");
		 downloadInfos.add(downloadInfo);
		 RestClient<DownloadInfo> downloadInfoClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_DOWNLOADS);
		 ClientResponse clientResponse = downloadInfoClient.create(downloadInfos);
		 assertNotNull(clientResponse);
	 }

    @Test
    public void getDownloadInfos() throws PhrescoException {
        RestClient<DownloadInfo> downloadInfoClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_DOWNLOADS);
        GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
        List<DownloadInfo> DownloadInfos = downloadInfoClient.get(genericType);
        assertNotNull(DownloadInfos);
    }
    
    @Test
    public void getDownloadInfo() throws PhrescoException {
        String downloadInfoId = TEST_DOWNLOADINFO_ID;
        RestClient<DownloadInfo> downloadInfoClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_DOWNLOADS);
        downloadInfoClient.setPath(downloadInfoId);
        GenericType<DownloadInfo> genericType = new GenericType<DownloadInfo>(){};
        DownloadInfo info = downloadInfoClient.getById(genericType);
        assertNotNull(info);
    } 
    
    @Test
    public void updateDownloadInfo() throws PhrescoException {
        String downloadInfoId = TEST_DOWNLOADINFO_ID;
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setId(TEST_DOWNLOADINFO_ID);
        downloadInfo.setName("Test customer update");
        RestClient<DownloadInfo> downloadInfoClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_DOWNLOADS);
        downloadInfoClient.setPath(downloadInfoId);
        GenericType<DownloadInfo> genericType = new GenericType<DownloadInfo>() {};
        DownloadInfo info=downloadInfoClient.updateById(downloadInfo, genericType);
        assertNotNull(info); 
    }
    
    @Test
    public void deleteDownloadInfo() throws PhrescoException {
        String downloadInfoId = TEST_DOWNLOADINFO_ID;
        RestClient<DownloadInfo> downloadInfoClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_DOWNLOADS);
        downloadInfoClient.setPath(downloadInfoId);
        ClientResponse clientResponse = downloadInfoClient.deleteById();
        assertNotNull(clientResponse);
    }

	 
}
