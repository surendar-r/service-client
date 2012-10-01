/*
 * ###
 * Phresco Service Client
 * %%
 * Copyright (C) 1999 - 2012 Photon Infotech Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ###
 */

package com.photon.phresco.service.client.test;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.VideoInfo;
import com.photon.phresco.commons.model.VideoType;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.Content;
import com.photon.phresco.service.client.api.ServiceClientConstant;
import com.photon.phresco.service.client.api.ServiceContext;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.service.client.factory.ServiceClientFactory;
import com.photon.phresco.service.client.impl.RestClient;
import com.photon.phresco.service.client.util.RestUtil;
import com.photon.phresco.util.ServiceConstants;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

public class AdminRestVideosTest extends BaseRestTest {

	
	@Before
	public void Initilaization() throws PhrescoException {
		initialize();
	}
	
	@Test 
	public void testCreateVideoInfos() throws PhrescoException {
		
		MultiPart multiPart = new MultiPart();
		
		VideoInfo video = createVideo();
        BodyPart jsonPart = new BodyPart();
        jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        jsonPart.setEntity(video);
        Date date = new Date();
		Content content = new Content(Content.Type.JSON, video.getId(), date, date, date, 0);
        jsonPart.setContentDisposition(content);
        multiPart.bodyPart(jsonPart);
		RestClient<VideoInfo> videoInfoclient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_VIDEOS);
		 ClientResponse clientResponse = videoInfoclient.create(multiPart);
		
	}
    
private VideoInfo createVideo() {
	VideoInfo info = new VideoInfo();
	info.setId("vedioId");
	info.setName("About phrescoUpdate");
	info.setDescription("intro about phresoco");
	
	List<VideoType> videoList =new ArrayList<VideoType>();
	VideoType vType=new VideoType();
	
	ArtifactGroup artifactGroup = new ArtifactGroup();
	List<ArtifactInfo> artifactInfos =new ArrayList<ArtifactInfo>();
	ArtifactInfo artiInfo=new ArtifactInfo();
	artiInfo.setVersion("1.8");
	artiInfo.setName("artiNameUpdate");
	long fileSize=8;
	artiInfo.setFileSize(fileSize);
	artifactInfos.add(artiInfo);
	artifactGroup.setVersions(artifactInfos);
	vType.setArtifactGroup(artifactGroup);
	videoList.add(vType);
	
	info.setVideoList(videoList);
		
	return info;
	}

//	@Test
    public void getVideoInfo() throws PhrescoException {
        String VideoInfoId = "TestvideoInfo";
        RestClient<VideoInfo> videoInfoclient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_VIDEOS);
        videoInfoclient.setPath(VideoInfoId);
        GenericType<VideoInfo> genericType = new GenericType<VideoInfo>(){};
        VideoInfo Info = videoInfoclient.getById(genericType);
        assertNotNull(Info);
    }
	
//	@Test
	public void FindVideoInfos() throws PhrescoException {
		RestClient<VideoInfo> videoInfoclient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_VIDEOS);
		GenericType<List<VideoInfo>> genericType = new GenericType<List<VideoInfo>>(){};
        List<VideoInfo> VideoInfos = videoInfoclient.get(genericType);
		assertNotNull(VideoInfos);
	}
	
	
//	@Test
	public void UpdateVideoInfos() throws PhrescoException {
		
		MultiPart multiPart = new MultiPart();
		
		VideoInfo video = createVideo();
        BodyPart jsonPart = new BodyPart();
        jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        jsonPart.setEntity(video);
        Date date = new Date();
		Content content = new Content(Content.Type.JSON, video.getId(), date, date, date, 0);
        jsonPart.setContentDisposition(content);
        multiPart.bodyPart(jsonPart);
		RestClient<VideoInfo> videoInfoclient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_VIDEOS);
        videoInfoclient.update(multiPart);
	}

	@Ignore
	public void testDeleteVideosInfos() throws PhrescoException {
		throw new PhrescoException(EX_PHEX00001);
	}


	
	
//	@Test
	public void getVideoInfos() throws PhrescoException  {
    	RestClient<VideoInfo> videoInfosClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_VIDEOS);
    	GenericType<List<VideoInfo>> genericType = new GenericType<List<VideoInfo>>(){};
    	List<VideoInfo> videoInfos = videoInfosClient.get(genericType);
    	assertNotNull(videoInfos);
	
	}	
//	@Test
	public void DeleteVideoInfo() throws PhrescoException  {
	String VideoInfoId = "TestvideoInfo";
	RestClient<VideoInfo> videoInfosClient = serviceManager.getRestClient(REST_API_ADMIN + REST_API_VIDEOS);
	videoInfosClient.setPath(VideoInfoId);
	ClientResponse clientResponse = videoInfosClient.deleteById();
	assertNotNull(clientResponse);
	   }    	
	    	
    	
}



