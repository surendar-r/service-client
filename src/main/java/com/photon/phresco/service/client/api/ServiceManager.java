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

import java.io.InputStream;
import java.util.List;

import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ApplicationType;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.Customer;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.commons.model.LogInfo;
import com.photon.phresco.commons.model.Permission;
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
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

/**
 * Interface for making service calls to Phresco Framework
 */
public interface ServiceManager {
	
	<E> RestClient<E> getRestClient(String contextPath) throws PhrescoException;
	
	User getUserInfo() throws PhrescoException;
	
	List<VideoInfo> getVideoInfos() throws PhrescoException;
	
	ClientResponse createApplicationTypes(List<ApplicationType> appTypes, String customerId) throws PhrescoException;
	
	void updateApplicationType(ApplicationType appType, String appTypeId, String customerId) throws PhrescoException;
	
	ClientResponse deleteApplicationType(String appTypeId, String customerId) throws PhrescoException;
   
	List<Technology> getArcheTypes(String customerId) throws PhrescoException;
	
	List<Technology> getArcheTypes(String customerId, String appTypeId) throws PhrescoException;
	
	Technology getArcheType(String archeTypeId, String customerId) throws PhrescoException;
	
	BodyPart createBodyPart(String name, Content.Type jarType, InputStream jarIs) throws PhrescoException;
	
	ClientResponse createArcheTypes(MultiPart multiPart, String customerId) throws PhrescoException;
	
	void updateArcheType(Technology technology, String archeTypeId, String customerId) throws PhrescoException;
	
	ClientResponse deleteArcheType(String archeTypeId, String customerId) throws PhrescoException;
	
	List<ApplicationType> getApplicationTypes(String customerId) throws PhrescoException;
	
	ApplicationType getApplicationType(String appTypeId, String customerId) throws PhrescoException;
	
	List<DownloadInfo> getServers(String customerId) throws PhrescoException;
	
	List<DownloadInfo> getServers(String customerId, String techId) throws PhrescoException;
	
	List<DownloadInfo> getDatabases(String customerId) throws PhrescoException;
	
	List<DownloadInfo> getDatabases(String customerId, String techId) throws PhrescoException;
	
	List<WebService> getWebServices(String customerId) throws PhrescoException;
	
	List<WebService> getWebServices(String techId, String customerId) throws PhrescoException;
	
	List<ArtifactGroup> getModules(String customerId, String techId, String type) throws PhrescoException;
	
	List<ArtifactGroup> getComponents(String customerId, String techId) throws PhrescoException;
	
	ArtifactGroup getFeature(String moduleId, String customerId) throws PhrescoException;
	
	ClientResponse createFeatures(MultiPart multiPart, String customerId) throws PhrescoException;
	
	ClientResponse updateFeature(MultiPart multiPart, String moduleId, String customerId) throws PhrescoException;
	
	ClientResponse deleteFeature(String moduleId, String customerId) throws PhrescoException;
	
	List<Customer> getCustomers() throws PhrescoException;
	
	Customer getCustomer(String customerId) throws PhrescoException;
	
	ClientResponse createCustomers(List<Customer> customers) throws PhrescoException;
	
	void updateCustomer(Customer customer, String customerId) throws PhrescoException;
	
	ClientResponse deleteCustomer(String customerId) throws PhrescoException;
	
	List<SettingsTemplate> getconfigTemplates(String customerId) throws PhrescoException;
	
	SettingsTemplate getConfigTemplate(String configId, String customerId) throws PhrescoException;
	
	ClientResponse createConfigTemplates(List<SettingsTemplate> settings, String customerId) throws PhrescoException;
	
	void updateConfigTemp(SettingsTemplate settingTemp, String configId, String customerId) throws PhrescoException;
	
	ClientResponse deleteConfigTemp(String id, String customerId) throws PhrescoException;
	
	List<ApplicationInfo> getPilotProjects(String techId) throws PhrescoException;
	
	ApplicationInfo getPilotProject(String projectId, String customerId) throws PhrescoException;
	
	ClientResponse createPilotProjects(MultiPart multiPart, String customerId) throws PhrescoException;
	
	void updatePilotProject(MultiPart multiPart, String projectId, String customerId) throws PhrescoException;
	
	ClientResponse deletePilotProject(String projectId, String customerId) throws PhrescoException;
	
	List<Role> getRoles() throws PhrescoException;
	
	Role getRole(String roleId) throws PhrescoException;

	ClientResponse createRoles(List<Role> role) throws PhrescoException;
	
	void updateRole(Role role, String id) throws PhrescoException;
	
	ClientResponse deleteRole(String id) throws PhrescoException;
	
	List<DownloadInfo> getDownloads(String customerId) throws PhrescoException;
	
	DownloadInfo getDownload(String id, String customerId) throws PhrescoException;
	
	ClientResponse createDownloads(MultiPart multiPart, String customerId) throws PhrescoException;
	
	void updateDownload(MultiPart multiPart, String id, String customerId) throws PhrescoException;
	
	ClientResponse deleteDownloadInfo(String id, String customerId) throws PhrescoException;
	
	ClientResponse createProject(ApplicationInfo appInfo) throws PhrescoException;
	
	ClientResponse updateProject(ApplicationInfo appInfo) throws PhrescoException;
	
	ClientResponse updateDocumentProject(ApplicationInfo appInfo) throws PhrescoException;
	
	List<Environment> getDefaultEnvFromServer() throws PhrescoException;
	
	List<Property> getGlobalUrls(String customerId) throws PhrescoException;
	
	Property getGlobalUrl(String globalUrlId, String customerId) throws PhrescoException;
	
	ClientResponse createGlobalUrl(List<Property> globalUrl, String customerId) throws PhrescoException;
	
	ClientResponse deleteglobalUrl(String globalurlId, String customerId) throws PhrescoException;
	
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
}