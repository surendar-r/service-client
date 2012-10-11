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
package com.photon.phresco.service.client.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ApplicationType;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.Customer;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.commons.model.LogInfo;
import com.photon.phresco.commons.model.Permission;
import com.photon.phresco.commons.model.PlatformType;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.Property;
import com.photon.phresco.commons.model.Role;
import com.photon.phresco.commons.model.SettingsTemplate;
import com.photon.phresco.commons.model.Technology;
import com.photon.phresco.commons.model.User;
import com.photon.phresco.commons.model.VideoInfo;
import com.photon.phresco.commons.model.WebService;
import com.photon.phresco.configuration.Environment;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.impl.RestClient;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Interface for making service calls to Phresco Framework and Admin Console
 */
public interface ServiceManager {
	
	/**
	 * To get the rest client for all given object 
	 * @param <E>
	 * @param contextPath
	 * @return
	 * @throws PhrescoException
	 */
	<E> RestClient<E> getRestClient(String contextPath) throws PhrescoException;
	
	/**
	 * To get the userinfo
	 * @return
	 * @throws PhrescoException
	 */
	User getUserInfo() throws PhrescoException;
	
	/**
	 * To get the video info
	 * @return List<VideoInfo>
	 * @throws PhrescoException
	 */
	List<VideoInfo> getVideoInfos() throws PhrescoException;
	
	/**
	 * To create the application type for the given customer
	 * @param appTypes
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse createApplicationTypes(List<ApplicationType> appTypes, String customerId) throws PhrescoException;
	
	/**
	 * To update the details of the given application type
	 * @param appType
	 * @param appTypeId
	 * @param customerId
	 * @throws PhrescoException
	 */
	void updateApplicationType(ApplicationType appType, String appTypeId, String customerId) throws PhrescoException;
	
	/**
	 * To delete the given application type
	 * @param appTypeId
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse deleteApplicationType(String appTypeId, String customerId) throws PhrescoException;
   
	/**
	 * To get all the technologies of the given customer
	 * @param customerId
	 * @return List<Technology>
	 * @throws PhrescoException
	 */
	List<Technology> getArcheTypes(String customerId) throws PhrescoException;
	
	/**
	 * To get all the technologies of the given customer and the apptype
	 * @param customerId
	 * @param appTypeId
	 * @return List<Technology>
	 * @throws PhrescoException
	 */
	List<Technology> getArcheTypes(String customerId, String appTypeId) throws PhrescoException;
	
	/**
	 * To get the details of the given technology
	 * @param archeTypeId
	 * @param customerId
	 * @return Technology
	 * @throws PhrescoException
	 */
	Technology getArcheType(String archeTypeId, String customerId) throws PhrescoException;
	
	/**
	 * To create the technology for the given customer
	 * @param multiPart
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse createArcheTypes(Technology technology,  List<InputStream> inputStreams, String customerId) throws PhrescoException;
	
	/**
	 * To update the details of the given technology
	 * @param technology
	 * @param archeTypeId
	 * @param customerId
	 * @throws PhrescoException
	 */
	ClientResponse updateArcheType(Technology technology, List<InputStream> inputStreams, String customerId) throws PhrescoException;
	
	/**
	 * To delete the given technology
	 * @param archeTypeId
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse deleteArcheType(String archeTypeId, String customerId) throws PhrescoException;
	
	/**
	 * To get all the application types of the given customer
	 * @param customerId
	 * @return List<ApplicationType>
	 * @throws PhrescoException
	 */
	List<ApplicationType> getApplicationTypes(String customerId) throws PhrescoException;
	
	/**
	 * To get the details of the given application type
	 * @param appTypeId
	 * @param customerId
	 * @return ApplicationType
	 * @throws PhrescoException
	 */
	ApplicationType getApplicationType(String appTypeId, String customerId) throws PhrescoException;
	
	/**
	 * To get all the servers of the given customer
	 * @param customerId
	 * @return List<DownloadInfo>
	 * @throws PhrescoException
	 */
	List<DownloadInfo> getServers(String customerId) throws PhrescoException;
	
	/**
	 * To get the servers of the given customer and the technology
	 * @param customerId
	 * @param techId
	 * @return List<DownloadInfo>
	 * @throws PhrescoException
	 */
	List<DownloadInfo> getServers(String customerId, String techId) throws PhrescoException;
	
	/**
	 * To get all the databases of the given customer
	 * @param customerId
	 * @return List<DownloadInfo>
	 * @throws PhrescoException
	 */
	List<DownloadInfo> getDatabases(String customerId) throws PhrescoException;
	
	/**
	 * To get the databases of the given customer and the technology
	 * @param customerId
	 * @param techId
	 * @return List<DownloadInfo>
	 * @throws PhrescoException
	 */
	List<DownloadInfo> getDatabases(String customerId, String techId) throws PhrescoException;
	
	/**
	 * To get the Webservices
	 * @return
	 * @throws PhrescoException
	 */
	List<WebService> getWebServices() throws PhrescoException;
	
	/**
	 * To get the features of the the of the specified type(module/js) for the given customer and techid
	 * @param customerId
	 * @param techId
	 * @param type
	 * @return List<ArtifactGroup>
	 * @throws PhrescoException
	 */
	List<ArtifactGroup> getFeatures(String customerId, String techId, String type) throws PhrescoException;
	
	/**
	 * To get all the components of the given customer and the techId
	 * @param customerId
	 * @param techId
	 * @return List<ArtifactGroup>
	 * @throws PhrescoException
	 */
	List<ArtifactGroup> getComponents(String customerId, String techId) throws PhrescoException;
	
	/**
	 * To get the details of the given feature
	 * @param moduleId
	 * @param customerId
	 * @return ArtifactGroup
	 * @throws PhrescoException
	 */
	ArtifactGroup getFeature(String moduleId, String customerId, String techId, String type) throws PhrescoException;
	
	/**
	 * To create feature for the given customer
	 * @param multiPart
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse createFeatures(ArtifactGroup moduleGroup, List<InputStream> inputStreams, String customerId) throws PhrescoException, IOException;
	
	/**
	 * To update the details of the given feature
	 * @param multiPart
	 * @param moduleId
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse updateFeature(ArtifactGroup moduleGroup, List<InputStream> inputStreams, String customerId) throws PhrescoException, IOException;
	
	/**
	 * To delete the given feature
	 * @param moduleId
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse deleteFeature(String moduleId, String customerId) throws PhrescoException;
	
	/**
	 * To get all the customers
	 * @return List<Customer>
	 * @throws PhrescoException
	 */
	List<Customer> getCustomers() throws PhrescoException;
	
	/**
	 * To get the details of the given customer
	 * @param customerId
	 * @return Customer
	 * @throws PhrescoException
	 */
	Customer getCustomer(String customerId) throws PhrescoException;
	
	/**
	 * To create a list of customers
	 * @param customers
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse createCustomers(List<Customer> customers) throws PhrescoException;
	
	
	/**
	 * To update the details of the given customer
	 * @param customer
	 * @param customerId
	 * @throws PhrescoException
	 */
	void updateCustomer(Customer customer, String customerId) throws PhrescoException;
	
	/**
	 * To delete the given customer
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse deleteCustomer(String customerId) throws PhrescoException;
	
	/**
	 * TO get Users List from LDAP
	 * @throws PhrescoException
	 */
	List<User> getSyncUsers() throws PhrescoException;
	
	/**
	 * TO get Users List from DB
	 * @throws PhrescoException
	 */
	List<User> getUsersFromDB() throws PhrescoException;
	
	/**
	 * To get the config templates of the given customer
	 * @param customerId
	 * @return List<SettingsTemplate>
	 * @throws PhrescoException
	 */
	List<SettingsTemplate> getconfigTemplates(String customerId) throws PhrescoException;
	
	/**
	 * To get the details of the given config template
	 * @param configId
	 * @param customerId
	 * @return SettingsTemplate
	 * @throws PhrescoException
	 */
	SettingsTemplate getConfigTemplate(String configId, String customerId) throws PhrescoException;
	
	/**
	 * To create config templates
	 * @param settings
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse createConfigTemplates(List<SettingsTemplate> settings, String customerId) throws PhrescoException;
	
	/**
	 * To update the details of the given config template
	 * @param settingTemp
	 * @param configId
	 * @param customerId
	 * @throws PhrescoException
	 */
	void updateConfigTemp(SettingsTemplate settingTemp, String configId, String customerId) throws PhrescoException;
	
	/**
	 * To delete the given config template
	 * @param id
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse deleteConfigTemp(String id, String customerId) throws PhrescoException;
	
	/**
	 * To get the pilot projects based on the given customer and the techId
	 * @param customerId
	 * @param techId
	 * @return
	 * @throws PhrescoException
	 */
    List<ApplicationInfo> getPilotProjects(String customerId, String techId) throws PhrescoException;
	
	/**
	 * To get all the pilot projects of the given customer
	 * @param techId
	 * @return List<ApplicationInfo>
	 * @throws PhrescoException
	 */
	List<ApplicationInfo> getPilotProjects(String customerId) throws PhrescoException;
	
	/**
	 * To get the details of the given pilot project
	 * @param projectId
	 * @param customerId
	 * @return ApplicationInfo
	 * @throws PhrescoException
	 */
	ApplicationInfo getPilotProject(String projectId, String customerId) throws PhrescoException;
	
	/**
	 * To create pilot project for the given customer
	 * @param multiPart
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse createPilotProjects(ApplicationInfo pilotProj, List<InputStream> inputStreams, String customerId) throws PhrescoException;
	
	/**
	 * To update the details of the given pilot project
	 * @param multiPart
	 * @param projectId
	 * @param customerId
	 * @throws PhrescoException
	 */
	void updatePilotProject(ApplicationInfo pilotProj, List<InputStream> inputStreams, String projectId, String customerId) throws PhrescoException;
	
	/**
	 * To delete the given pilot project
	 * @param projectId
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse deletePilotProject(String projectId, String customerId) throws PhrescoException;
	
	/**
	 * To get all the roles
	 * @return List<Role>
	 * @throws PhrescoException
	 */
	List<Role> getRoles() throws PhrescoException;
	
	/**
	 * To get the details of the given role
	 * @param roleId
	 * @return Role
	 * @throws PhrescoException
	 */
	Role getRole(String roleId) throws PhrescoException;

	/**
	 * To create the given roles
	 * @param role
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse createRoles(List<Role> role) throws PhrescoException;
	
	/**
	 * To update the details of the given role
	 * @param role
	 * @param id
	 * @throws PhrescoException
	 */
	void updateRole(Role role, String id) throws PhrescoException;
	
	/**
	 * To delete the given role
	 * @param id
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse deleteRole(String id) throws PhrescoException;
	
	/**
	 * To create the  video
	 * @param video
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse createVideos(VideoInfo videoInfo, List<InputStream> inputStreams) throws PhrescoException;
	
	/**
	 * To update the  video
	 * @param multiPart
	 * @param projectId
	 * @return 
	 * @throws PhrescoException
	 */
	void updateVideo(VideoInfo videoInfo,List<InputStream> inputStreams, String videoId) throws PhrescoException;
	
	/**
	 * To delete the given video
	 * @param id
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse deleteVideo(String id) throws PhrescoException;
	
	/**
	 * To get all the videos
	 * @return List<VideoInfo>
	 * @throws PhrescoException
	 */
	List<VideoInfo> getVideos() throws PhrescoException;
	
	/**
	 * To get the details of the given video
	 * @param videoId
	 * @return video
	 * @throws PhrescoException
	 */
	VideoInfo getVideo(String videoId) throws PhrescoException;
	
	/**
	 * To get the downloads of the given customer
	 * @param customerId
	 * @return List<DownloadInfo>
	 * @throws PhrescoException
	 */
	List<DownloadInfo> getDownloads(String customerId) throws PhrescoException;
	
	/**
	 * To get the download infos for the given customer and techid
	 * @param customerId
	 * @param techId
	 * @return
	 * @throws PhrescoException
	 */
	List<DownloadInfo> getDownloads(String customerId, String techId) throws PhrescoException;
	
	/**
	 * To get the details of the given download id
	 * @param id
	 * @param customerId
	 * @return DownloadInfo
	 * @throws PhrescoException
	 */
	DownloadInfo getDownload(String id, String customerId) throws PhrescoException;
	
	/**
	 * To create download for the given customer
	 * @param multiPart
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse createDownloads(DownloadInfo downloadInfo, List<InputStream> inputStreams, String customerId) throws PhrescoException;
	
	/**
	 * To update the details of the given download
	 * @param multiPart
	 * @param id
	 * @param customerId
	 * @throws PhrescoException
	 */
	void updateDownload(DownloadInfo downloadInfo, List<InputStream> inputStreams, String customerId) throws PhrescoException;
	
	/**
	 * To delete the given download info
	 * @param id
	 * @param customerId
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse deleteDownloadInfo(String id, String customerId) throws PhrescoException;
	
	/**
	 * To create project
	 * @param projectInfo
	 * @return ClientResponse
	 * @throws PhrescoException
	 */
	ClientResponse createProject(ProjectInfo projectInfo) throws PhrescoException;
	
	/**
     * To update the details of the given project
     * @param appInfo
     * @return ClientResponse
     * @throws PhrescoException
     */
	ClientResponse updateProject(ProjectInfo appInfo) throws PhrescoException;
	
	ClientResponse updateDocumentProject(ApplicationInfo appInfo) throws PhrescoException;
	
	Environment getDefaultEnvFromServer() throws PhrescoException;
	
	List<Property> getGlobalUrls(String customerId) throws PhrescoException;
	
	Property getGlobalUrl(String globalUrlId, String customerId) throws PhrescoException;
	
	ClientResponse createGlobalUrl(List<Property> globalUrl, String customerId) throws PhrescoException;
	
	ClientResponse deleteGlobalUrl(String globalurlId, String customerId) throws PhrescoException;
	
	void updateGlobalUrl(Property globalUrl, String globalurlId, String customerId) throws PhrescoException;
	
	List<Permission> getPermissions() throws PhrescoException;
	
	ClientResponse deletePermission(String permissionId) throws PhrescoException;
	
	String getCiConfigPath(String repoType, String customerId) throws PhrescoException;
	
	InputStream getCredentialXml(String customerId) throws PhrescoException;
	
	InputStream getJdkHomeXml(String customerId) throws PhrescoException;
	
	InputStream getMavenHomeXml(String customerId) throws PhrescoException;
	
	InputStream getMailerXml(String customerId) throws PhrescoException;
	
	ClientResponse getEmailExtPlugin(String customerId) throws PhrescoException;
	
	ClientResponse sendErrorReport(List<LogInfo> loginfo) throws PhrescoException;
	
	Property getForumPath(String customerId) throws PhrescoException;
	
	/**
	 * To get all the platforms
	 * @return
	 * @throws PhrescoException
	 */
	List<PlatformType> getPlatforms() throws PhrescoException;
}