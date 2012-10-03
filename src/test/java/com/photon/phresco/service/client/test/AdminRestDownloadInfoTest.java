package com.photon.phresco.service.client.test;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;

import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.commons.model.VideoInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.Content;
import com.photon.phresco.service.client.impl.RestClient;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

public class AdminRestDownloadInfoTest extends BaseRestTest {
    private static final String TEST_DOWNLOADINFO_ID = "test-downloadinfo";

	@Before
    public void initilaization() throws PhrescoException {
    	initialize();
    }
    
//	@Test
	public void testCreateDownloadInfo() throws PhrescoException {
		
		MultiPart multiPart = new MultiPart();
		
		DownloadInfo download = createDownload();
        BodyPart jsonPart = new BodyPart();
        jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        jsonPart.setEntity(download);
        Date date = new Date();
		Content content = new Content(Content.Type.JSON, download.getId(), date, date, date, 0);
        jsonPart.setContentDisposition(content);
        multiPart.bodyPart(jsonPart);
		
        RestClient<ArtifactGroup> downloadInfoClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
		 ClientResponse clientResponse = downloadInfoClient.create(multiPart);
	 }

	private DownloadInfo createDownload() {
		DownloadInfo downloadInfo = new DownloadInfo();
		downloadInfo.setId(TEST_DOWNLOADINFO_ID);
		downloadInfo.setName("TestDownloadUpdate");
		List<String> appliesToTechs = new ArrayList<String>();
		appliesToTechs.add("php");
		downloadInfo.setAppliesToTechIds(appliesToTechs);
		ArtifactGroup artifactGroup = new ArtifactGroup();
		artifactGroup.setArtifactId("testArtifact");
		List<String> customerIds = new ArrayList<String>();
		customerIds.add("photon");
		artifactGroup.setCustomerIds(customerIds);
		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
		ArtifactInfo info = new ArtifactInfo();
		info.setVersion("1.2");
		artifactInfos.add(info);
		artifactGroup.setVersions(artifactInfos);
		artifactGroup.setName("testArtifactName");
		downloadInfo.setArtifactGroup(artifactGroup);
		return downloadInfo;
	}

//    @Test
    public void getDownloadInfos() throws PhrescoException {
        RestClient<DownloadInfo> downloadInfoClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
        GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
        List<DownloadInfo> DownloadInfos = downloadInfoClient.get(genericType);
        assertNotNull(DownloadInfos);
    }
    
//    @Test
    public void getDownloadInfo() throws PhrescoException {
        String downloadInfoId = TEST_DOWNLOADINFO_ID;
        RestClient<DownloadInfo> downloadInfoClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
        downloadInfoClient.setPath(downloadInfoId);
        GenericType<DownloadInfo> genericType = new GenericType<DownloadInfo>(){};
        DownloadInfo info = downloadInfoClient.getById(genericType);
        assertNotNull(info);
    } 
    
//    @Test
    public void updateDownloadInfo() throws PhrescoException {
        String downloadInfoId = TEST_DOWNLOADINFO_ID;
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setId(TEST_DOWNLOADINFO_ID);
        downloadInfo.setName("Test customer update");
        RestClient<DownloadInfo> downloadInfoClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
        downloadInfoClient.setPath(downloadInfoId);
        GenericType<DownloadInfo> genericType = new GenericType<DownloadInfo>() {};
        DownloadInfo info=downloadInfoClient.updateById(downloadInfo, genericType);
        assertNotNull(info); 
    }
    
    @Test
    public void deleteDownloadInfo() throws PhrescoException {
        String downloadInfoId = TEST_DOWNLOADINFO_ID;
        RestClient<DownloadInfo> downloadInfoClient = serviceManager.getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
        downloadInfoClient.setPath(downloadInfoId);
        ClientResponse clientResponse = downloadInfoClient.deleteById();
        assertNotNull(clientResponse);
    }

	 
}
