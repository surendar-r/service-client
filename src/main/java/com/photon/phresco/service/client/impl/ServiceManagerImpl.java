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
package com.photon.phresco.service.client.impl;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ApplicationType;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.CoreOption;
import com.photon.phresco.commons.model.Customer;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.commons.model.License;
import com.photon.phresco.commons.model.LogInfo;
import com.photon.phresco.commons.model.Permission;
import com.photon.phresco.commons.model.PlatformType;
import com.photon.phresco.commons.model.ProjectInfo;
import com.photon.phresco.commons.model.Property;
import com.photon.phresco.commons.model.Role;
import com.photon.phresco.commons.model.SettingsTemplate;
import com.photon.phresco.commons.model.Technology;
import com.photon.phresco.commons.model.TechnologyGroup;
import com.photon.phresco.commons.model.TechnologyOptions;
import com.photon.phresco.commons.model.User;
import com.photon.phresco.commons.model.VersionInfo;
import com.photon.phresco.commons.model.VideoInfo;
import com.photon.phresco.commons.model.WebService;
import com.photon.phresco.configuration.Environment;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.client.api.Content;
import com.photon.phresco.service.client.api.ServiceClientConstant;
import com.photon.phresco.service.client.api.ServiceContext;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.util.Constants;
import com.photon.phresco.util.Credentials;
import com.photon.phresco.util.ServiceConstants;
import com.phresco.pom.site.Reports;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

@SuppressWarnings("unchecked")
public class ServiceManagerImpl implements ServiceManager, ServiceClientConstant, ServiceConstants, Constants {

	private static final String HEADER_NAME_AUTHORIZATION = "Authorization";
	private static final Logger S_LOGGER = Logger.getLogger(ServiceManagerImpl.class);
    private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
    
    private EhCacheManager cacheManager;
    
    private String serverPath = null;
    private static User userInfo = null;
    private String apiKey = null;
    
    private static String debugMsg = "Entered into ServiceManagerImpl.getDownloadInfo(List<DownloadInfo> downloadInfo)";
    private static final String CACHE_MODULES_KEY = "modules";

	public ServiceManagerImpl(String serverPath) throws PhrescoException {
    	super();
    	this.serverPath = serverPath;
    }

    public ServiceManagerImpl(ServiceContext context) throws PhrescoException {
    	super();
    	init(context);
    	cacheManager = new EhCacheManager();
    	cacheManager.resetCache();
    }
    
    public <E> RestClient<E> getRestClient(String contextPath) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getRestClient(String contextPath)" + contextPath);
        }
    	
    	StringBuilder builder = new StringBuilder();
    	builder.append(serverPath);
    	builder.append(contextPath);
    	RestClient<E> restClient = new RestClient<E>(builder.toString());
    	
    	//Adding API Key
    	if (apiKey != null) {
        	restClient.addHeader(HEADER_NAME_AUTHORIZATION, apiKey);	
    	}
    	
    	restClient.addHeader(PHR_AUTH_TOKEN, userInfo.getToken());
    	
    	return restClient;
	}
    
    public User getUserInfo() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getUserInfo())");
        }
    	
		return userInfo;
	}

	public void setUserInfo(User userInfo) throws PhrescoException {
		this.userInfo = userInfo;
	}
	
	private void init(ServiceContext context) throws PhrescoException {
		this.serverPath = (String) context.get(SERVICE_URL);
    	String password = (String) context.get(SERVICE_PASSWORD);
		String username = (String) context.get(SERVICE_USERNAME);
		this.apiKey = (String) context.get(SERVICE_API_KEY);
		
		doLogin(username, password, apiKey);
	}
	
    private void doLogin(String username, String password, String apiServiceKey) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.doLogin(String username, String password)");
        }

        //encode the password
        byte[] encodeBase64 = Base64.encodeBase64(password.getBytes());
        String encodedString = new String(encodeBase64);
        
    	Credentials credentials = new Credentials(username, encodedString); 
    	Client client = ClientHelper.createClient();
        WebResource resource = client.resource(serverPath + "/" + LOGIN);

        Builder builder = resource.accept(MediaType.APPLICATION_JSON);
        if (apiServiceKey != null) {
        	builder = builder.header(HEADER_NAME_AUTHORIZATION, apiServiceKey);	
        }
        ClientResponse response = builder.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, credentials);
        GenericType<User> genericType = new GenericType<User>() {};
        userInfo = response.getEntity(genericType);
    }
    
    @Override
    public List<VideoInfo> getVideoInfos() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getVideoInfos()");
        }
    	
    	RestClient<VideoInfo> videoInfosClient = getRestClient(REST_API_ADMIN + REST_API_VIDEOS);
    	GenericType<List<VideoInfo>> genericType = new GenericType<List<VideoInfo>>(){};
    	
    	return videoInfosClient.get(genericType);
    }
    
    private List<Technology> getArcheTypesFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getArcheTypesFromServer(String customerId)");
        }
    	
    	RestClient<Technology> archeTypeClient = getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
    	archeTypeClient.queryString(REST_QUERY_CUSTOMERID, customerId);
		GenericType<List<Technology>> genericType = new GenericType<List<Technology>>(){};
		
		return archeTypeClient.get(genericType);
    }
    
    @Override
    public List<Technology> getArcheTypes(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getArcheTypes(String customerId)");
        }

        CacheKey key = new CacheKey(customerId, Technology.class.getName());
    	List<Technology> archeTypes = (List<Technology>) cacheManager.get(key);
		if (CollectionUtils.isEmpty(archeTypes)) {
			archeTypes = getArcheTypesFromServer(customerId);
			cacheManager.add(key, archeTypes);
		}
    	
    	return archeTypes;
	}
    
    @Override
    public List<Technology> getArcheTypes(String customerId, String appTypeId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getArcheTypes(String customerId, String appTypeId)");
        }

        CacheKey key = new CacheKey(customerId, Technology.class.getName(), appTypeId);
        List<Technology> archeTypes = (List<Technology>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(archeTypes)) {
            RestClient<Technology> archeTypeClient = getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
            Map<String, String> queryStringsMap = new HashMap<String, String>();
            queryStringsMap.put(REST_QUERY_CUSTOMERID, customerId);
            queryStringsMap.put(REST_QUERY_APPTYPEID, appTypeId);
            archeTypeClient.queryStrings(queryStringsMap);
            GenericType<List<Technology>> genericType = new GenericType<List<Technology>>(){};
            archeTypes = archeTypeClient.get(genericType);
            cacheManager.add(key, archeTypes);
        }
        
        return archeTypes;
    }
    
    @Override
    public Technology getArcheType(String archeTypeId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getArcheType(String archeTypeId, String customerId)");
        }
        
        CacheKey key = new CacheKey(customerId, Technology.class.getName());
        List<Technology> archeTypes = (List<Technology>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(archeTypes)) {
    		archeTypes = getArcheTypesFromServer(customerId);
			cacheManager.add(key, archeTypes);
    	}
        if (CollectionUtils.isNotEmpty(archeTypes)) {
        	for (Technology archeType : archeTypes) {
				if (archeType.getId().equals(archeTypeId)) {
					return archeType;
				}
			}
        }
        
        return null;
    }
    
    @Override
    public ClientResponse createArcheTypes(Technology technology, Map<String, InputStream> inputStreamMap, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createArcheTypes(List<Technology> archeTypes, String customerId)");
        }
        
        MultiPart multiPart = createMultiPart(technology, inputStreamMap, technology.getName());
    	RestClient<Technology> newApp = getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
		ClientResponse clientResponse = newApp.create(multiPart);
		CacheKey key = new CacheKey(customerId, Technology.class.getName());
		cacheManager.add(key, getArcheTypesFromServer(customerId));
		
		return clientResponse;
    }
    
    @Override
    public ClientResponse updateArcheType(Technology technology, Map<String, InputStream> inputStreamMap, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateArcheTypes(Technology technology, InputStream inputStream, String customerId)");
        }
    	
        MultiPart multiPart = createMultiPart(technology, inputStreamMap, technology.getName());
    	RestClient<Technology> editArchetype = getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
    	ClientResponse clientResponse = editArchetype.update(multiPart);
		CacheKey key = new CacheKey(customerId, Technology.class.getName());
		cacheManager.add(key, getArcheTypesFromServer(customerId));
		
		return clientResponse;
    }
    
    @Override
    public ClientResponse deleteArcheType(String archeTypeId, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.deleteArcheType(String archeTypeId, String customerId)");
    	}

    	RestClient<Technology> deleteArchetype = getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
    	deleteArchetype.setPath(archeTypeId);
    	ClientResponse clientResponse = deleteArchetype.deleteById();
    	CacheKey key = new CacheKey(customerId, Technology.class.getName());
    	cacheManager.add(key, getArcheTypesFromServer(customerId));

    	return clientResponse;
    }
    
    private List<ApplicationType> getApplicationTypesFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getApplicationTypesFromServer(String customerId)");
        }
    	
    	RestClient<ApplicationType> appTypeClient = getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
    	appTypeClient.queryString(REST_QUERY_CUSTOMERID, customerId);
		GenericType<List<ApplicationType>> genericType = new GenericType<List<ApplicationType>>(){};
		
		return appTypeClient.get(genericType);
    }
    
    @Override
    public List<ApplicationType> getApplicationTypes(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getApplicationTypes(String customerId)");
        }
        
        CacheKey key = new CacheKey(customerId, ApplicationType.class.getName());
    	List<ApplicationType> appTypes = (List<ApplicationType>) cacheManager.get(key);
    	try {
    		if (CollectionUtils.isEmpty(appTypes)) {
    			appTypes = getApplicationTypesFromServer(customerId);
    			cacheManager.add(key, appTypes);
    		}
    	} catch(Exception e) {
    		throw new PhrescoException(e);
    	}
    	
    	return appTypes;
	}
    
    @Override
    public ApplicationType getApplicationType(String appTypeId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getApplicationType(String appTypeId, String customerId)");
        }

        CacheKey key = new CacheKey(customerId, ApplicationType.class.getName());
    	List<ApplicationType> appTypes = (List<ApplicationType>) cacheManager.get(key);
    	if (CollectionUtils.isEmpty(appTypes)) {
			appTypes = getApplicationTypesFromServer(customerId);
			cacheManager.add(key, appTypes);
		}
        if (CollectionUtils.isNotEmpty(appTypes)) {
        	for (ApplicationType appType : appTypes) {
				if (appType.getId().equals(appTypeId)) {
					return appType;
				}
			}
        }

        return null;
    }

    @Override
    public ClientResponse createApplicationTypes(List<ApplicationType> appTypes, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createApplicationTypes(List<ApplicationType> appTypes, String customerId)");
        }
    	
    	RestClient<ApplicationType> newApp = getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
		ClientResponse clientResponse = newApp.create(appTypes);
		CacheKey key = new CacheKey(customerId, ApplicationType.class.getName());
		cacheManager.add(key, getApplicationTypesFromServer(customerId));
		
		return clientResponse;
    }
    
    @Override
    public void updateApplicationType(ApplicationType appType, String appTypeId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateApplicationTypes(ApplicationType appType, String appTypeId, String customerId)");
        }
    	
    	RestClient<ApplicationType> editApptype = getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
    	editApptype.setPath(appTypeId);
		GenericType<ApplicationType> genericType = new GenericType<ApplicationType>() {};
		editApptype.updateById(appType, genericType);
		CacheKey key = new CacheKey(customerId, ApplicationType.class.getName());
		cacheManager.add(key, getApplicationTypesFromServer(customerId));
    }
    
    @Override
    public ClientResponse deleteApplicationType(String appTypeId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deleteApplicationType(String appTypeId, String customerId)");
        }
    	
	    RestClient<ApplicationType> deleteApptype = getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
	    deleteApptype.setPath(appTypeId);
	    ClientResponse clientResponse = deleteApptype.deleteById();
	    CacheKey key = new CacheKey(customerId, ApplicationType.class.getName());
	    cacheManager.add(key, getApplicationTypesFromServer(customerId));
	    
	    return clientResponse;
    }
    
    private List<DownloadInfo> getServersFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getServers(String customerId)");
        }
    	
		RestClient<DownloadInfo> serverClient = getRestClient(REST_API_COMPONENT + REST_API_SERVERS);
        serverClient.queryString(REST_QUERY_CUSTOMERID, customerId);
		GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
		
		return serverClient.get(genericType);
	}
    
    @Override
    public List<DownloadInfo> getServers(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getServers(String customerId)");
        }
    	
        CacheKey key = new CacheKey(DownloadInfo.class.getName());
		List<DownloadInfo> servers = (List<DownloadInfo>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(servers)) {
        	servers = getServersFromServer(customerId);
        	cacheManager.add(key, servers);
        }
		
		return servers;
	}
    
    @Override
    public List<DownloadInfo> getServers(String customerId, String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getServers(String customerId, String techId)");
        }
        
        CacheKey key = new CacheKey(customerId, DownloadInfo.class.getName(), techId);
        List<DownloadInfo> servers = (List<DownloadInfo>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(servers)) {
            RestClient<DownloadInfo> serverClient = getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
            Map<String, String> queryStringsMap = new HashMap<String, String>();
            queryStringsMap.put(REST_QUERY_CUSTOMERID, customerId);
            queryStringsMap.put(REST_QUERY_TECHID, techId);
            queryStringsMap.put(REST_QUERY_TYPE, DownloadInfo.Category.SERVER.name());
            serverClient.queryStrings(queryStringsMap);
            GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
            servers = serverClient.get(genericType);
            cacheManager.add(key, servers);
        }
        
        return servers;
    }
    
    private List<DownloadInfo> getDatabasesFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDatabases(String customerId)");
        }
    	
		RestClient<DownloadInfo> dbClient = getRestClient(REST_API_COMPONENT + REST_API_DATABASES);
        dbClient.queryString(REST_QUERY_CUSTOMERID, customerId);
		GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
		
		return dbClient.get(genericType);
	}
    
    @Override
    public List<DownloadInfo> getDatabases(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDatabases(String customerId)");
        }
    	
        CacheKey key = new CacheKey(DownloadInfo.class.getName());
		List<DownloadInfo> databases = (List<DownloadInfo>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(databases)) {
        	databases = getDatabasesFromServer(customerId);
        	cacheManager.add(key, databases);
        }
		
		return databases;
	}
    
    @Override
    public List<DownloadInfo> getDatabases(String customerId, String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDatabases(String techId, String customerId)");
        }
        
        CacheKey key = new CacheKey(DownloadInfo.class.getName(), techId);
        List<DownloadInfo> databases = (List<DownloadInfo>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(databases)) {
            RestClient<DownloadInfo> dbClient = getRestClient(REST_API_COMPONENT + REST_API_DATABASES);
            Map<String, String> queryStringsMap = new HashMap<String, String>();
            queryStringsMap.put(REST_QUERY_CUSTOMERID, customerId);
            queryStringsMap.put(REST_QUERY_TECHID, techId);
            dbClient.queryStrings(queryStringsMap);
            GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
            databases = dbClient.get(genericType);
            cacheManager.add(key, databases);
        }
        
        return databases;
    }
    
    private List<WebService> getWebServicesFromServer() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getWebServices(String customerId)");
        }
    	
		RestClient<WebService> webServiceClient = getRestClient(REST_API_COMPONENT + REST_API_WEBSERVICES);
		GenericType<List<WebService>> genericType = new GenericType<List<WebService>>(){};
		
		return webServiceClient.get(genericType);
	}
    
    @Override
    public List<WebService> getWebServices() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getWebServices(String techId, String customerId)");
        }
        
        CacheKey key = new CacheKey(WebService.class.getName());
        List<WebService> webServices = (List<WebService>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(webServices)) {
            webServices = getWebServicesFromServer();
            cacheManager.add(key, webServices);
        }
        
        return webServices;
    }
    
    private WebService getWebServiceFromServer(String id) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getWebServices(String id)");
        }
    	
		RestClient<WebService> webServiceClient = getRestClient(REST_API_COMPONENT + REST_API_WEBSERVICES);
		webServiceClient.setPath(id);
		GenericType<WebService> genericType = new GenericType<WebService>(){};
		
		return webServiceClient.getById(genericType);
	}
    
    @Override
    public WebService getWebService(String id) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getWebService(String id)");
        }
        
        CacheKey key = new CacheKey("", WebService.class.getName(), id);
        WebService webService = (WebService) cacheManager.get(key);
        if (webService == null) {
            webService = getWebServiceFromServer(id);
            cacheManager.add(key, webService);
        }
        
        return webService;
    }
    
    @Override
    public List<ArtifactGroup> getFeatures(String customerId, String techId, String type) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getModules(String customerId, String techId)");
        }
        
        CacheKey key = new CacheKey(customerId, type, techId);
        List<ArtifactGroup> modules = (List<ArtifactGroup>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(modules)) {
            modules = getFeaturesFromServer(customerId, techId, type);
            cacheManager.add(key, modules);
        }
        
        return modules;
    }

    private List<ArtifactGroup> getFeaturesFromServer(String customerId, String techId, String type) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getModules(String customerId, String techId)");
        }
        
        RestClient<ArtifactGroup> moduleGroupClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        Map<String, String> queryStringsMap = new HashMap<String, String>();
        queryStringsMap.put(REST_QUERY_CUSTOMERID, customerId);
        queryStringsMap.put(REST_QUERY_TYPE, type);
        queryStringsMap.put(REST_QUERY_TECHID, techId);
        moduleGroupClient.queryStrings(queryStringsMap);
        GenericType<List<ArtifactGroup>> genericType = new GenericType<List<ArtifactGroup>>(){};
        List<ArtifactGroup> modules = moduleGroupClient.get(genericType);
        
        return modules;
    }
    
    @Override
    public List<ArtifactGroup> getComponents(String customerId, String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getJSLibs(String customerId, String techId)");
        }
        
        CacheKey key = new CacheKey(customerId, REST_QUERY_TYPE_COMPONENT, techId);
        List<ArtifactGroup> components = (List<ArtifactGroup>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(components)) {
            RestClient<ArtifactGroup> moduleGroupClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
            Map<String, String> queryStringsMap = new HashMap<String, String>();
            queryStringsMap.put(REST_QUERY_CUSTOMERID, customerId);
            queryStringsMap.put(REST_QUERY_TYPE, REST_QUERY_TYPE_COMPONENT);
            queryStringsMap.put(REST_QUERY_TECHID, techId);
            moduleGroupClient.queryStrings(queryStringsMap);
            GenericType<List<ArtifactGroup>> genericType = new GenericType<List<ArtifactGroup>>(){};
            components = moduleGroupClient.get(genericType);
            cacheManager.add(key, components);
        }
        
        return components;
    }
    
    @Override
    public ArtifactGroup getFeature(String moduleGroupId, String customerId, String techId, String type) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getFeature(String moduleId, String customerId)");
        }
        
        CacheKey key = new CacheKey(moduleGroupId);
        ArtifactGroup moduleGroup = (ArtifactGroup) cacheManager.get(key);
        if (moduleGroup == null) {
            List<ArtifactGroup> features = getFeatures(customerId, techId, type);
            if (CollectionUtils.isNotEmpty(features)) {
                for (ArtifactGroup artifactGroup : features) {
                    if (artifactGroup.getId().equals(moduleGroupId)) {
                        moduleGroup = artifactGroup;
                        cacheManager.add(key, artifactGroup);
                        break;
                    }
                }
            }
        }
        
        return moduleGroup;
    }
    
    /**
     * To create features
     * @param moduleGroup
     * @param inputStream
     * @param customerId
     * @return MultiPart
     * @throws PhrescoException
     */
    @Override
    public ClientResponse createFeatures(ArtifactGroup moduleGroup,
    		Map<String, InputStream> inputStreamMap, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createFeatures(ArtifactGroup moduleGroup, InputStream inputStream, String customerId)");
        }
        
        MultiPart multiPart = createMultiPart(moduleGroup, inputStreamMap, moduleGroup.getName());
        RestClient<ArtifactGroup> moduleClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        ClientResponse response = moduleClient.create(multiPart);

        //To set all the features
        String type = moduleGroup.getType().name();
        String techId = moduleGroup.getAppliesTo().get(0).getTechId();
        CacheKey featuresKey = new CacheKey(customerId, type, techId);
        cacheManager.add(featuresKey, getFeaturesFromServer(customerId, techId, type));
        
        //To set the artifactGroup against its Id
        ArtifactGroup artifactGroup = response.getEntity(ArtifactGroup.class);
        CacheKey key = new CacheKey(artifactGroup.getId());
        cacheManager.add(key, artifactGroup);
        
        return response;
    }
    
    /**
     * To create multipart for feature
     * @param moduleGroup
     * @param inputStream
     * @return MultiPart
     * @throws PhrescoException
     */
    @Override
    public ClientResponse updateFeature(ArtifactGroup moduleGroup, Map<String, InputStream> inputStreamMap, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.updateFeature(ArtifactGroup moduleGroup, InputStream inputStream, String customerId)");
    	}
    	
    	MultiPart multiPart = createMultiPart(moduleGroup, inputStreamMap, moduleGroup.getName());
    	RestClient<ArtifactGroup> moduleClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
//     	moduleClient.setPath(moduleGroup.getId());
 		ClientResponse response = moduleClient.update(multiPart);
 		
 		CacheKey moduleGroupKey = new CacheKey(moduleGroup.getId());
 		cacheManager.add(moduleGroupKey, moduleGroup);
 		List<CoreOption> appliesTo = moduleGroup.getAppliesTo();
 		for (CoreOption coreOption : appliesTo) {
 			CacheKey key = new CacheKey(customerId, moduleGroup.getType().name(), coreOption.getTechId());
 			List<ArtifactGroup> features = getFeaturesFromServer(customerId, coreOption.getTechId(), moduleGroup.getType().name());
 	 		cacheManager.add(key, features);
		}
 		
 		return response;
    }
    
    /**
     * To create multipart
     * @param object
     * @param inputStream
     * @param name
     * @return
     * @throws PhrescoException
     */
    private MultiPart createMultiPart(Object object, Map<String, InputStream> inputStreamMap, String name) throws PhrescoException {
    	MultiPart multiPart = new MultiPart();
    	BodyPart jsonBodyPart = createJSONBodyPart(Content.Type.JSON, name, object);
    	multiPart.bodyPart(jsonBodyPart);
    	Iterator iter = inputStreamMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			BodyPart binaryBodyPart = createBinaryBodyPart(Content.Type.ARCHETYPE, key, (InputStream) inputStreamMap.get(key));
			multiPart.bodyPart(binaryBodyPart);
		}

    	return multiPart;
    }
    
    /**
     * To create binary BodyPart
     * @param type
     * @param name
     * @param inputStream
     * @return BodyPart
     * @throws PhrescoException
     */
    private BodyPart createBinaryBodyPart(Content.Type type, String name, InputStream inputStream) throws PhrescoException {
        BodyPart binaryBodyPart = new BodyPart();
        binaryBodyPart.setMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        binaryBodyPart.setEntity(inputStream);
        Date date = new Date();
        Content content = new Content(type, name, date, date, date, 0);
        binaryBodyPart.setContentDisposition(content);

        return binaryBodyPart;
    }
    
    /**
     * To create JSON BodyPart
     * @param type
     * @param name
     * @param obj
     * @return BodyPart
     * @throws PhrescoException
     */
    private BodyPart createJSONBodyPart(Content.Type type, String name, Object obj) throws PhrescoException {
        BodyPart jsonBodyPart = new BodyPart();
        jsonBodyPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        jsonBodyPart.setEntity(obj);
        Date date = new Date();
        Content content = new Content(type, name, date, date, date, 0);
        jsonBodyPart.setContentDisposition(content);

        return jsonBodyPart;
    }

    public ClientResponse deleteFeature(String moduleId, CacheKey key) throws PhrescoException {
    	if (isDebugEnabled) {
     		S_LOGGER.debug("Entered into ServiceManagerImpl.deleteFeatures(String moduleId, String customerId)");
     	}
        String customerId = key.getCustId();
    	String tech = key.getId();
     	RestClient<ArtifactGroup> deleteModule = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
     	deleteModule.setPath(moduleId);
     	ClientResponse response = deleteModule.deleteById();
    	cacheManager.add(key, getModulesFromServer(customerId, tech));

     	return response;
    }
    
    private List<ArtifactGroup> getModulesFromServer(String customerId, String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getModulesFromServer()");
        }
        
        RestClient<ArtifactGroup> moduleGroupClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
    	Map<String, String> headers = new HashMap<String, String>();
    	headers.put(REST_QUERY_TECHID, techId);
    	headers.put(REST_QUERY_CUSTOMERID, customerId);
    	headers.put(REST_QUERY_TYPE, REST_QUERY_TYPE_MODULE);
    	moduleGroupClient.queryStrings(headers);
    	GenericType<List<ArtifactGroup>> genericType = new GenericType<List<ArtifactGroup>>(){};

    	return moduleGroupClient.get(genericType);
        
    }
    
    private List<Customer> getCustomersFromServer() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getCustomersFromServer()");
        }
        
        RestClient<Customer> customersClient = getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        GenericType<List<Customer>> genericType = new GenericType<List<Customer>>(){};
        
        return customersClient.get(genericType);
    }
    
    @Override
    public List<Customer> getCustomers() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getCustomers()");
        }
        
        CacheKey key = new CacheKey(Customer.class.getName());
		List<Customer> customers = (List<Customer>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(customers)) {
        	customers = getCustomersFromServer();
        	cacheManager.add(key, customers);
        }
        
        return customers;
    }
    
    private Customer getCustomerFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getCustomerFromServer(String customerId)");
        }
        
        RestClient<Customer> customersClient = getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        customersClient.setPath(customerId);
        GenericType<Customer> genericType = new GenericType<Customer>(){};
        
        return customersClient.getById(genericType);
    }
    
    @Override
    public Customer getCustomer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getCustomer(String customerId)" + customerId);
        }
        
        CacheKey key = new CacheKey(customerId, Customer.class.getName());
        Customer customer = (Customer) cacheManager.get(key);
        if (customer == null) {
        	customer = getCustomerFromServer(customerId);
        	cacheManager.add(key, customer);
        }

        return customer;
    }
    
	@Override
	public ClientResponse createCustomers(Customer customer,
			Map<String, InputStream> inputStreamMap) throws PhrescoException {
		if (isDebugEnabled) {
			S_LOGGER.debug("Entered into ServiceManagerImpl.createCustomers(List<Customer> customers)");
		}
		MultiPart multiPart = createMultiPart(customer, inputStreamMap, customer.getName());
		RestClient<Customer> customersClient = getRestClient(REST_API_ADMIN
				+ REST_API_CUSTOMERS);
		ClientResponse response = customersClient.create(multiPart);
		CacheKey key = new CacheKey(Customer.class.getName());
		cacheManager.add(key, getCustomersFromServer());
		return response;
	}
    
    @Override
    public void updateCustomer(Customer customer, Map<String, InputStream> inputStreamMap) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateCustomer(Customer customer, String customerId)" + customer.getId());
        }
       
        MultiPart multiPart = createMultiPart(customer, inputStreamMap, customer.getName());
        RestClient<Customer> customersClient = getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        ClientResponse response = customersClient.update(multiPart);
        CacheKey key = new CacheKey(Customer.class.getName());
        cacheManager.add(key, getCustomersFromServer());
    }
    
    @Override
    public ClientResponse deleteCustomer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deleteCustomer(String customerId)" + customerId);
        }
        
        RestClient<Customer> customersClient = getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        customersClient.setPath(customerId);
        ClientResponse response = customersClient.deleteById();
        CacheKey key = new CacheKey(Customer.class.getName());
        cacheManager.add(key, getCustomersFromServer());
        
        return response;
    }
    
    private List<SettingsTemplate> getConfigTemplatesFromServer(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getConfigTemplatesFromServer(String customerId)" + customerId);
        }
    	
    	RestClient<SettingsTemplate> settingClient = getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
    	settingClient.queryString(REST_QUERY_CUSTOMERID, customerId);
        GenericType<List<SettingsTemplate>> genericType = new GenericType<List<SettingsTemplate>>(){};
        
        return settingClient.get(genericType);
    	
    }
    
    @Override
    public List<SettingsTemplate> getConfigTemplates(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Enetered into ServiceManagerImpl.getconfigTemplates(String customerId)");
    	}

    	CacheKey key = new CacheKey(customerId, SettingsTemplate.class.getName());
    	List<SettingsTemplate> configTemplates = (List<SettingsTemplate>) cacheManager.get(key);
		if (CollectionUtils.isEmpty(configTemplates)) {
			configTemplates = getConfigTemplatesFromServer(customerId);
			cacheManager.add(key, configTemplates);
		}
    	
    	return configTemplates;
    }
    
    private List<SettingsTemplate> getConfigTemplatesFromServer(String customerId, String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getConfigTemplatesFromServer(String customerId, String techId)" + customerId);
        }
        
        RestClient<SettingsTemplate> settingClient = getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
        settingClient.queryString(REST_QUERY_CUSTOMERID, customerId);
        settingClient.queryString(REST_QUERY_TECHID, techId);
        GenericType<List<SettingsTemplate>> genericType = new GenericType<List<SettingsTemplate>>(){};
        
        return settingClient.get(genericType);
        
    }
    
    @Override
    public List<SettingsTemplate> getConfigTemplates(String customerId, String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Enetered into ServiceManagerImpl.getconfigTemplates(String customerId, String techId)");
        }

        CacheKey key = new CacheKey(customerId, SettingsTemplate.class.getName(), techId);
        List<SettingsTemplate> configTemplates = (List<SettingsTemplate>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(configTemplates)) {
            configTemplates = getConfigTemplatesFromServer(customerId, techId);
            cacheManager.add(key, configTemplates);
        }
        
        return configTemplates;
    }
    
    @Override
    public ClientResponse createConfigTemplates(List<SettingsTemplate> settings, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createConfigTemplates(List<SettingTemplate> settings, String customerId)");
        }
        
        RestClient<SettingsTemplate> settingsClient = getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
        ClientResponse clientResponse = settingsClient.create(settings);
        CacheKey key = new CacheKey(customerId, SettingsTemplate.class.getName());
    	cacheManager.add(key, getConfigTemplatesFromServer(customerId));
        
        return clientResponse ;
    }
    
    @Override
    public SettingsTemplate getConfigTemplate(String configId, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getConfigTemplate(String configId, String customerId)");
    	}
    	
    	CacheKey key = new CacheKey(customerId, SettingsTemplate.class.getName());
    	List<SettingsTemplate> configTemps = (List<SettingsTemplate>) cacheManager.get(key);
    	if (CollectionUtils.isEmpty(configTemps)) {
    		configTemps = getConfigTemplatesFromServer(customerId);
			cacheManager.add(key, configTemps);
    	}
    	if (CollectionUtils.isNotEmpty(configTemps)) {
    		for (SettingsTemplate configTemp : configTemps) {
				if (configTemp.getId().equals(configId)) {
					return configTemp;
				}
			}
    	}
    	
    	return null;
    }
    
    @Override
    public void updateConfigTemp(SettingsTemplate settingTemp, String configId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateConfigTemp(String configId, String customerId)");
        }
    	RestClient<SettingsTemplate> editConfigTemp = getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
    	editConfigTemp.setPath(configId);
		GenericType<SettingsTemplate> genericType = new GenericType<SettingsTemplate>() {};
		editConfigTemp.updateById(settingTemp, genericType);
		CacheKey key = new CacheKey(customerId, SettingsTemplate.class.getName());
		cacheManager.add(key, getConfigTemplatesFromServer(customerId));
    }
    
    @Override
    public ClientResponse deleteConfigTemp(String id, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.deleteConfigTemp(String id, String customerId)");
    	}

    	RestClient<SettingsTemplate> configTempClient = getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
    	configTempClient.setPath(id);
    	ClientResponse response = configTempClient.deleteById();
    	CacheKey key = new CacheKey(customerId, SettingsTemplate.class.getName());
    	cacheManager.add(key, getConfigTemplatesFromServer(customerId));
    	
    	return response;
    }
    
    private List<ApplicationInfo> getPilotProjectsFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getPilotProjectFromServer(String customerId)");
        }
    	
    	RestClient<ApplicationInfo> pilotClient = getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
    	Map<String, String> headers = new HashMap<String, String>();
    	headers.put(REST_QUERY_CUSTOMERID, customerId);
    	pilotClient.queryStrings(headers);
		GenericType<List<ApplicationInfo>> genericType = new GenericType<List<ApplicationInfo>>(){};
		
		return pilotClient.get(genericType);
    }
    
    private List<ApplicationInfo> getPilotProjectsFromServer(String customerId, String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getPilotProjectFromServer(String customerId)");
        }
        RestClient<ApplicationInfo> pilotClient = getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(REST_QUERY_CUSTOMERID, customerId);
        headers.put(REST_QUERY_TECHID, techId);
        pilotClient.queryStrings(headers);
        GenericType<List<ApplicationInfo>> genericType = new GenericType<List<ApplicationInfo>>(){};
        
        return pilotClient.get(genericType);
    }
    
    @Override
    public List<ApplicationInfo> getPilotProjects(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getPilotProjects(String customerId)" + customerId);
        }
        
        CacheKey key = new CacheKey(customerId, ApplicationInfo.class.getName());
        List<ApplicationInfo> pilotProjects = (List<ApplicationInfo>) cacheManager.get(key);
        try {	
    		if (CollectionUtils.isEmpty(pilotProjects)) {
    			pilotProjects = getPilotProjectsFromServer(customerId);
    			cacheManager.add(key, pilotProjects);
    		}
    	} catch(Exception e){
    		throw new PhrescoException(e);
    	}
    	
        return pilotProjects;
    }
    
    @Override
    public List<ApplicationInfo> getPilotProjects(String customerId, String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getPilotProjects(String customerId)" + customerId);
        }
        CacheKey key = new CacheKey(customerId, ApplicationInfo.class.getName(), techId);
        List<ApplicationInfo> pilotProjects = (List<ApplicationInfo>) cacheManager.get(key);
        try {
            if (CollectionUtils.isEmpty(pilotProjects)) {
                pilotProjects = getPilotProjectsFromServer(customerId, techId);
                cacheManager.add(key, pilotProjects);
            }
        } catch(Exception e) {
            throw new PhrescoException(e);
        }
        
        return pilotProjects;
    }
    
    @Override
    public ApplicationInfo getPilotProject(String projectId, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getPilotProject(String projectId, String customerId)");
    	}
    	
    	CacheKey key = new CacheKey(customerId, ApplicationInfo.class.getName());
    	List<ApplicationInfo> pilotProjects = (List<ApplicationInfo>) cacheManager.get(key);
    	if (CollectionUtils.isEmpty(pilotProjects)) {
			pilotProjects = getPilotProjectsFromServer(customerId);
			cacheManager.add(key, pilotProjects);
		}
    	if (CollectionUtils.isNotEmpty(pilotProjects)) {
    		for (ApplicationInfo pilotProject : pilotProjects) {
				if (pilotProject.getId().equals(projectId)) {
					return pilotProject;
				}
			}
    	}

    	return null;
    }
    
    @Override
    public ClientResponse createPilotProjects(ApplicationInfo pilotProjInfo, Map<String, InputStream> inputStreamMap, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createPilotProjects(List<ProjectInfo> proInfo, String customerId)");
        }
        
        MultiPart multiPart = createMultiPart(pilotProjInfo, inputStreamMap, pilotProjInfo.getName());
        RestClient<ProjectInfo> pilotClient = getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
        ClientResponse response = pilotClient.create(multiPart);
        CacheKey key = new CacheKey(customerId, ApplicationInfo.class.getName());
        cacheManager.add(key, getPilotProjectsFromServer(customerId));
        
        return response;
    }
    
    @Override
    public void updatePilotProject(ApplicationInfo pilotProjInfo, Map<String, InputStream> inputStreamMap, String projectId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updatePilotProject(ProjectInfo projectInfo, String projectId)" + projectId);
        }
       
        MultiPart multiPart = createMultiPart(pilotProjInfo, inputStreamMap, pilotProjInfo.getName());
        RestClient<ApplicationInfo> pilotproClient = getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
        pilotproClient.update(multiPart);
        CacheKey key = new CacheKey(customerId, ApplicationInfo.class.getName());
        cacheManager.add(key, getPilotProjectsFromServer(customerId));
    }
    
    @Override
    public ClientResponse deletePilotProject(String projectId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deletePilotProject(String projectId)" + projectId);
        }
        
        RestClient<ProjectInfo> pilotproClient = getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
        pilotproClient.setPath(projectId);
        ClientResponse response = pilotproClient.deleteById();
        CacheKey key = new CacheKey(customerId, ApplicationInfo.class.getName());
        cacheManager.add(key, getPilotProjectsFromServer(customerId));
        
        return response;
    }
    
    private List<Role> getRolesFromServer() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getRolesServer()");
        }
    	
        RestClient<Role> roleClient = getRestClient(REST_API_ADMIN + REST_API_ROLES);
        GenericType<List<Role>> genericType = new GenericType<List<Role>>(){};
        
        return roleClient.get(genericType);	
    }
    
    @Override
    public List<Role> getRoles() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getRoles())");
        }
        
        CacheKey key = new CacheKey(Role.class.getName());
        List<Role> roles = (List<Role>) cacheManager.get(key);
        try {
    		if (CollectionUtils.isEmpty(roles)) {
    			roles = getRolesFromServer();
    			cacheManager.add(key, roles);
    		}
    	} catch(Exception e) {
    		throw new PhrescoException(e);
    	}
    	
        return roles;	
    }
    
    @Override
    public Role getRole(String roleId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getPilotsProjects(List<ProjectInfo> proInfo)");
    	}
    	
    	CacheKey key = new CacheKey(Role.class.getName());
    	List<Role> roles = (List<Role>) cacheManager.get(key);
    	if (CollectionUtils.isEmpty(roles)) {
    		roles = getRolesFromServer();
			cacheManager.add(key, roles);
    	}
    	if (CollectionUtils.isNotEmpty(roles)) {
    		for (Role role : roles) {
				if (role.getId().equals(roleId)) {
					return role;
				}
			}
    	}
    	
    	return null;
    }
    
    @Override
    public ClientResponse createRoles(List<Role> role) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.createroles(List<Role> role)");
    	}	
    	
    	RestClient<Role> roleClient = getRestClient(REST_API_ADMIN + REST_API_ROLES);
    	ClientResponse response = roleClient.create(role);
    	CacheKey key = new CacheKey(Role.class.getName());
    	cacheManager.add(key, getRolesFromServer());
    	
    	return response;
    }
    
    @Override
    public ClientResponse deleteRole(String id) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into RestClient.deleteRole(String id)" + id);
    	}

    	RestClient<Role> roleClient = getRestClient(REST_API_ADMIN + REST_API_ROLES);
    	roleClient.setPath(id);
    	ClientResponse response = roleClient.deleteById();
    	CacheKey key = new CacheKey(Role.class.getName());
    	cacheManager.add(key, getRolesFromServer());
    	
    	return response;
    }

    @Override
    public void updateRole(Role role, String id) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into RestClient.updateRole(Role role, String id)" + id);
    	}

    	RestClient<Role> roleClient = getRestClient(REST_API_ADMIN + REST_API_ROLES);
    	roleClient.setPath(id);
    	GenericType<Role> genericType = new GenericType<Role>() {};
    	roleClient.updateById(role, genericType);
    	CacheKey key = new CacheKey(Role.class.getName());
    	cacheManager.add(key, getRolesFromServer());
    }
    
    
    private List<VideoInfo> getVideosFromServer() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getVideosFromServer()");
        }
        
        RestClient<VideoInfo> videoClient = getRestClient(REST_API_ADMIN + REST_API_VIDEOS);
        GenericType<List<VideoInfo>> genericType = new GenericType<List<VideoInfo>>(){};
        
        return videoClient.get(genericType);
    }
    
    @Override
    public List<VideoInfo> getVideos() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getVideos())");
        }
        
        CacheKey key = new CacheKey(VideoInfo.class.getName());
        List<VideoInfo> videos = (List<VideoInfo>) cacheManager.get(key);
        try {
    		if (CollectionUtils.isEmpty(videos)) {
    			videos = getVideosFromServer();
    			cacheManager.add(key, videos);
    		}
    	} catch(Exception e) {
    		throw new PhrescoException(e);
    	}
    	
        return videos;	
    }
    
    @Override
    public VideoInfo getVideo(String videoId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getVideo(String videoId)");
    	}
    	
    	CacheKey key = new CacheKey(VideoInfo.class.getName());
        List<VideoInfo> videos = (List<VideoInfo>) cacheManager.get(key);
    	if (CollectionUtils.isEmpty(videos)) {
    		videos = getVideosFromServer();
			cacheManager.add(key, videos);
    	}
    	if (CollectionUtils.isNotEmpty(videos)) {
    		for (VideoInfo video : videos) {
				if (video.getId().equals(videoId)) {
					return video;
				}
			}
    	}
    	
    	return null;
    }
    
    @Override
    public ClientResponse createVideos(VideoInfo videoInfo, Map<String, InputStream> inputStreamMap) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createVideos(VideoInfo videoInfo, InputStream videoIs, InputStream imgIs)");
        }
        MultiPart multiPart = createMultiPart(videoInfo, inputStreamMap, videoInfo.getName());
        RestClient<VideoInfo> videoInfoClient = getRestClient(REST_API_ADMIN + REST_API_VIDEOS);
        ClientResponse response = videoInfoClient.create(multiPart);
        CacheKey key = new CacheKey(VideoInfo.class.getName());
        cacheManager.add(key, getVideosFromServer());
        return response;
    }
    
    @Override
    public void updateVideo(VideoInfo videoInfo,Map<String, InputStream> inputStreamMap, String id) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.updateVideo(VideoInfo createVideoInstance,List<InputStream> inputStreams, String id)" + id);
    	}
    	
    	MultiPart multiPart = createMultiPart(videoInfo, inputStreamMap, videoInfo.getName());
    	RestClient<VideoInfo> videoInfoClient = getRestClient(REST_API_ADMIN + REST_API_VIDEOS);
    	videoInfoClient.update(multiPart);
    	CacheKey key = new CacheKey(VideoInfo.class.getName());
    	cacheManager.add(key, getVideosFromServer());
    }
    
    @Override
    public ClientResponse deleteVideo(String id) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.deleteVideo(String id)" + id);
    	}
    	RestClient<VideoInfo> videoClient = getRestClient(REST_API_ADMIN + REST_API_VIDEOS);
    	videoClient.setPath(id);
    	ClientResponse response = videoClient.deleteById();
    	CacheKey key = new CacheKey(VideoInfo.class.getName());
    	cacheManager.add(key, getVideosFromServer());
    	return response;
    }
    
    @Override
    public List<DownloadInfo> getDownloads(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDownloadInfo(List<DownloadInfo> downloadInfo)");
        }
        
        CacheKey key = new CacheKey(customerId, DownloadInfo.class.getName());
        List<DownloadInfo> downloadInfos = (List<DownloadInfo>) cacheManager.get(key);
        try {
            if (CollectionUtils.isEmpty(downloadInfos)) {
                downloadInfos = getDownloadsFromServer(customerId);
                cacheManager.add(key, downloadInfos);
            }
        } catch(Exception e){
            throw new PhrescoException(e);
        }
        
        return downloadInfos;
    }
    
    @Override
    public List<DownloadInfo> getDownloads(String customerId, String techId, String category) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug(debugMsg);
        }
        
        try {
                return getDownloadsFromServer(customerId, techId, category);
        } catch(Exception e){
            throw new PhrescoException(e);
        }
    }
    
    @Override
    public List<Reports> getReports(String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug(debugMsg);
        }
        
    	CacheKey key = new CacheKey(techId, Reports.class.getName());
        List<Reports> reports = (List<Reports>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(reports)) {
        	reports = getTechReportsFromServer(techId);
        }
        
        return reports;
    }
    
    @Override
    public List<Reports> getReports() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug(debugMsg);
        }
        
        CacheKey key = new CacheKey(Reports.class.getName());
        List<Reports> reports = (List<Reports>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(reports)) {
        	reports = getReportsFromServer();
        }
        
        return reports;
    }
    
    private List<DownloadInfo> getDownloadsFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDownloadsFromServer(String customerId)");
        }
    	
    	RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
    	downloadClient.queryString(REST_QUERY_CUSTOMERID, customerId);
		GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
		
		return downloadClient.get(genericType);
    }
    
    private List<DownloadInfo> getDownloadsFromServer(String customerId, String techId, String category) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDownloadsFromServer(String customerId, String techId)");
        }
        
        RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
        Map<String, String> queryStringsMap = new HashMap<String, String>();
        queryStringsMap.put(REST_QUERY_CUSTOMERID, customerId);
        queryStringsMap.put(REST_QUERY_TECHID, techId);
        queryStringsMap.put(REST_QUERY_TYPE, category);
        downloadClient.queryStrings(queryStringsMap);
        GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
        
        return downloadClient.get(genericType);
    }
    
    private List<Reports> getTechReportsFromServer(String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getReportsFromServer(String techId)");
        }
        RestClient<Reports> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_REPORTS);
        downloadClient.queryString(REST_QUERY_TECHID, techId);
        GenericType<List<Reports>> genericType = new GenericType<List<Reports>>(){};
        return downloadClient.get(genericType);
    }
    
    private List<Reports> getReportsFromServer() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getReportsFromServer(String techId)");
        }
        RestClient<Reports> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_REPORTS);
        GenericType<List<Reports>> genericType = new GenericType<List<Reports>>(){};
        return downloadClient.get(genericType);
    }

    @Override
    public DownloadInfo getDownload(String downloadId, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into Restclient.getDownload(String downloadId, String customerId)");
    	}
    	
    	CacheKey key = new CacheKey(customerId, DownloadInfo.class.getName());
    	List<DownloadInfo> downloadInfos = (List<DownloadInfo>) cacheManager.get(key);
    	if (CollectionUtils.isEmpty(downloadInfos)) {
    		downloadInfos = getDownloadsFromServer(customerId);
			cacheManager.add(key, downloadInfos);
    	}
    	if (CollectionUtils.isNotEmpty(downloadInfos)) {
    		for (DownloadInfo downloadInfo : downloadInfos) {
				if (downloadInfo.getId().equals(downloadId)) {
					return downloadInfo;
				}
			}
    	}
    	
    	return null;
    }
    
    @Override
    public ClientResponse createDownloads(DownloadInfo downloadInfo, Map<String, InputStream> inputStreamMap, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createDownloadInfo(List<DownloadInfo> downloadInfo)");
        }
    	
    	MultiPart multiPart = createMultiPart(downloadInfo, inputStreamMap, downloadInfo.getName());
    	RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
    	ClientResponse response = downloadClient.create(multiPart);
    	CacheKey key = new CacheKey(customerId, DownloadInfo.class.getName());
    	cacheManager.add(key, getDownloadsFromServer(customerId));
    	
    	return response;
    }

    @Override
    public void updateDownload(DownloadInfo downloadInfo, Map<String, InputStream> inputStreamMap, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateDownload(DownloadInfo downloadInfo, String downloadId)");
        }
        
        MultiPart multiPart = createMultiPart(downloadInfo, inputStreamMap, downloadInfo.getName());
        RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
        downloadClient.update(multiPart);
        CacheKey key = new CacheKey(customerId, DownloadInfo.class.getName());
        cacheManager.add(key, getDownloadsFromServer(customerId));
    }

    @Override
    public ClientResponse deleteDownloadInfo(String downloadId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deleteDownloadInfo(String downloadId)");
        }

        RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
        downloadClient.setPath(downloadId);
        ClientResponse response = downloadClient.deleteById();
        CacheKey key = new CacheKey(customerId, DownloadInfo.class.getName());
        cacheManager.add(key, getDownloadsFromServer(customerId));

        return response;
    }
    
    private DownloadInfo getDownloadFromServer(String id) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDownloadFromServer(String customerId)");
        }
        
        RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
        downloadClient.setPath(id);
        GenericType<DownloadInfo> genericType = new GenericType<DownloadInfo>(){};
        
        return downloadClient.getById(genericType);
    }
    
    @Override
    public DownloadInfo getDownloadInfo(String id) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDownload(String id)" + id);
        }
        
        CacheKey key = new CacheKey(id, DownloadInfo.class.getName());
        DownloadInfo download = (DownloadInfo) cacheManager.get(key);
        if (download == null) {
        	download = getDownloadFromServer(id);
        	cacheManager.add(key, download);
        }

        return download;
    }
    
    private ArtifactGroup getArtifactGroupFromServer(String id) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getArtifactGroupFromServer(String id)");
        }
        
        RestClient<ArtifactGroup> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        downloadClient.setPath(id);
        GenericType<ArtifactGroup> genericType = new GenericType<ArtifactGroup>(){};
        
        return downloadClient.getById(genericType);
    }
    
    @Override
    public ArtifactGroup getArtifactGroupInfo(String id) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getArtifactGroupInfo(String id)" + id);
        }
        
        CacheKey key = new CacheKey(id, ArtifactGroup.class.getName());
        ArtifactGroup artifactgroup = (ArtifactGroup) cacheManager.get(key);
        if (artifactgroup == null) {
        	artifactgroup = getArtifactGroupFromServer(id);
        	cacheManager.add(key, artifactgroup);
        }

        return artifactgroup;
    }
    
    @Override
    public Technology getTechnology(String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getTechnology(String techId)");
        }
        
        CacheKey key = new CacheKey(techId, Technology.class.getName());
        Technology technology = (Technology) cacheManager.get(key);
        if (technology == null) {
        	technology = getTechnologyFromServer(techId);
        	cacheManager.add(key, technology);
        }

        return technology;
    }
    
    private Technology getTechnologyFromServer(String techId) throws PhrescoException {
    	
    	 RestClient<Technology> techClient = getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
         techClient.setPath(techId);
         GenericType<Technology> genericType = new GenericType<Technology>(){};
         Technology tech = techClient.getById(genericType);
         
         return tech;
	}
    
    @Override
    public ArtifactInfo getArtifactInfo(String id) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getArtifactInfo(String id)" + id);
        }
        CacheKey key = new CacheKey(id, ArtifactInfo.class.getName());
        ArtifactInfo artifactInfo = (ArtifactInfo) cacheManager.get(key);
        if (artifactInfo == null) {
        	artifactInfo = getArtifactInfoFromServer(id);
        	cacheManager.add(key, artifactInfo);
        }

        return artifactInfo;
    }
    
    private ArtifactInfo getArtifactInfoFromServer(String id) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getArtifactInfoFromServer(String id)");
        }
        
        RestClient<ArtifactInfo> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_ARTIFACTINFO);
        Map<String, String> headers = new HashMap<String, String>();
    	headers.put(REST_QUERY_ID, id);
    	downloadClient.queryStrings(headers);
        GenericType<ArtifactInfo> genericType = new GenericType<ArtifactInfo>(){};
        
        return downloadClient.getById(genericType);
    }
    
    @Override
    public ClientResponse createProject(ProjectInfo projectInfo) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createProject(ProjectInfo projectInfo)");
        }

        RestClient<ProjectInfo> projectClient = getRestClient(REST_API_PROJECT + REST_API_PROJECT_CREATE);

        return projectClient.create(projectInfo, MEDIATYPE_ZIP, MediaType.APPLICATION_JSON);
    }
    
    @Override
    public ClientResponse updateProject(ProjectInfo projectInfo) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateProject(ProjectInfo projectInfo)");
        }

        RestClient<ProjectInfo> projectClient = getRestClient(REST_API_PROJECT + REST_API_PROJECT_UPDATE);

        return projectClient.create(projectInfo, MEDIATYPE_ZIP, MediaType.APPLICATION_JSON);
    }
    
    @Override
    public ClientResponse updateDocumentProject(ApplicationInfo projectInfo) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateDocumentProject(ProjectInfo projectInfo)");
        }
        
        RestClient<ApplicationInfo> projectClient = getRestClient(REST_API_PROJECT + REST_APP_UPDATEDOCS);
        ClientResponse response = projectClient.create(projectInfo, MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON);
        
        return response;
    }
    
    @Override
    public Environment getDefaultEnvFromServer() throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDefaultEnvFromServer()");
        }
    	
    	RestClient<Environment> envClient = getRestClient(REST_API_ENV_PATH);
		GenericType<Environment> genericType = new GenericType<Environment>(){};
		
		return envClient.getById(genericType);
    }
    
    private List<Property> getGlobalUrlFromServer() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getGlobalUrlFromServer(String customerId)");
        }
    	RestClient<Property> globalUrlClient = getRestClient(REST_API_COMPONENT + REST_API_PROPERTY);
		GenericType<List<Property>> genericType = new GenericType<List<Property>>(){};
		
		return globalUrlClient.get(genericType);
    }

    @Override
    public List<Property> getGlobalUrls() throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getGlobalUrls(List<GlobalURL> globalUrl)");
        }
    	
    	CacheKey key = new CacheKey(Property.class.getName());
     	List<Property> globalUrls = (List<Property>) cacheManager.get(key);
    	try {	
    		if (CollectionUtils.isEmpty(globalUrls)) {
    			globalUrls = getGlobalUrlFromServer();
    			cacheManager.add(key, globalUrls);
    		}
    	} catch(Exception e){
    		throw new PhrescoException(e);
    	}
    	
    	return globalUrls;
    }
    
    @Override
    public Property getGlobalUrl(String globalUrlId) throws PhrescoException {
    	if(isDebugEnabled){
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getGlobalUrl(String globalUrlId, String customerId)");
    	}
    	
    	CacheKey key = new CacheKey(Property.class.getName());
    	List<Property> globalUrls = (List<Property>) cacheManager.get(key);
    	if (CollectionUtils.isEmpty(globalUrls)) {
    		globalUrls = getGlobalUrlFromServer();
			cacheManager.add(key, globalUrls);
    	}
    	if (CollectionUtils.isNotEmpty(globalUrls)) {
    		for (Property globalUrl : globalUrls) {
				if (globalUrl.getId().equals(globalUrlId)) {
					return globalUrl;
				}
			}
    	}
    	
    	return null;
    }
    
    @Override
    public ClientResponse createGlobalUrl(List<Property> globalUrl) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createGlobalUrl(List<GlobalURL> globalUrl)");
        }
    	
    	RestClient<Property> globalClient = getRestClient(REST_API_COMPONENT + REST_API_PROPERTY);
    	ClientResponse response = globalClient.create(globalUrl);
    	CacheKey key = new CacheKey(Property.class.getName());
    	cacheManager.add(key, getGlobalUrlFromServer());
    	
    	return response;
    }
    
    @Override
    public void updateGlobalUrl(Property globalUrl, String globalurlId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateGlobalUrl(GlobalURL globalUrl, String globalurlId, String customerId)");
        }
    	RestClient<Property> editGlobalUrl = getRestClient(REST_API_COMPONENT + REST_API_PROPERTY);
    	editGlobalUrl.setPath(globalurlId);
		GenericType<Property> genericType = new GenericType<Property>() {};
		editGlobalUrl.updateById(globalUrl, genericType);
		CacheKey key = new CacheKey(Property.class.getName());
		cacheManager.add(key, getGlobalUrlFromServer());
    }
    
    @Override
    public ClientResponse deleteGlobalUrl(String globalurlId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deleteglobalUrl(String globalurlId, String customerId)");
        }

        RestClient<Property> globalUrlClient = getRestClient(REST_API_COMPONENT + REST_API_PROPERTY);
        globalUrlClient.setPath(globalurlId);
        ClientResponse response = globalUrlClient.deleteById();
        CacheKey key = new CacheKey(Property.class.getName());
        cacheManager.add(key, getGlobalUrlFromServer());

        return response;
    }
   
    private List<Permission> getPermissionsFromServer() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getPermissionsFromServer()");
        }
        
        RestClient<Permission> permissionClient = getRestClient(REST_API_ADMIN + REST_API_PERMISSIONS);
        GenericType<List<Permission>> genericType = new GenericType<List<Permission>>(){};
        
        return permissionClient.get(genericType);
    }
    
    @Override
    public List<Permission> getPermissions() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getPermissions()");
        }
        
        CacheKey key = new CacheKey(Permission.class.getName());
		List<Permission> permissions = (List<Permission>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(permissions)) {
        	permissions = getPermissionsFromServer();
        	cacheManager.add(key, permissions);
        }
        
        return permissions;
    }
    
    @Override
    public ClientResponse deletePermission(String permissionId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deletePermission(String permissionId)" + permissionId);
        }
        
        RestClient<Permission> permissionClient = getRestClient(REST_API_ADMIN + REST_API_PERMISSIONS);
        permissionClient.setPath(permissionId);
        ClientResponse response = permissionClient.deleteById();
        CacheKey key = new CacheKey(Permission.class.getName());
        cacheManager.add(key, getPermissionsFromServer());
        
        return response;
    }
    
    @Override
    public String getCiConfigPath(String repoType, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getCiConfigPath(String repoType, String customerId)");
        }
    	
    	RestClient<String> ciClient = getRestClient(REST_API_REPO + REST_API_CI_CONFIG);
    	Map<String, String> queryStringsMap = new HashMap<String, String>();
    	queryStringsMap.put(REST_QUERY_TYPE, repoType);
    	queryStringsMap.put(REST_QUERY_CUSTOMERID, customerId);
    	ciClient.queryStrings(queryStringsMap);
    	GenericType<String> genericType = new GenericType<String>() {};
    	
    	return ciClient.getById(genericType, MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN);
    }
    
    @Override
    public InputStream getCredentialXml(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getJdkHomeXml(String customerId)");
        }
    	
    	RestClient<String> client = getRestClient(REST_API_REPO + REST_API_CI_CREDENTIAL);
    	client.queryString(REST_QUERY_CUSTOMERID, customerId);
    	ClientResponse response = client.get(MediaType.APPLICATION_XML);

    	return response.getEntityInputStream();
    }
    
    @Override
    public InputStream getJdkHomeXml(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getJdkHomeXml(String customerId)");
        }
    	
    	RestClient<String> client = getRestClient(REST_API_REPO + REST_API_CI_JDK_HOME);
    	client.queryString(REST_QUERY_CUSTOMERID, customerId);
    	ClientResponse response = client.get(MediaType.APPLICATION_XML);

    	return response.getEntityInputStream();
    }
    
    @Override
    public InputStream getMavenHomeXml(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getMavenHomeXml(String customerId)");
        }
    	
    	RestClient<String> client = getRestClient(REST_API_REPO + REST_API_CI_MAVEN_HOME);
    	client.queryString(REST_QUERY_CUSTOMERID, customerId);
    	ClientResponse response = client.get(MediaType.APPLICATION_XML);

    	return response.getEntityInputStream();
    }
    
    @Override
    public InputStream getMailerXml(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getMailerXml(String customerId)");
        }
    	
    	RestClient<String> client = getRestClient(REST_API_REPO + REST_API_CI_MAILER_HOME);
    	client.queryString(REST_QUERY_CUSTOMERID, customerId);
    	ClientResponse response = client.get(MediaType.APPLICATION_XML);

    	return response.getEntityInputStream();
    }
    
    @Override
    public ClientResponse getEmailExtPlugin(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getEmailExtPlugin(String customerId)");
        }
    	
    	RestClient<String> client = getRestClient(REST_API_REPO + REST_API_CI_MAIL_PLUGIN);
    	client.queryString(REST_QUERY_CUSTOMERID, customerId);
    	
        return client.get(MediaType.APPLICATION_OCTET_STREAM);
    }
    
    @Override
    public ClientResponse sendErrorReport(List<LogInfo> loginfo) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.sendErrorReport(List<LogInfo> loginfo)");
        }
        
        RestClient<LogInfo> client = getRestClient(REST_API_ADMIN + REST_API_LOG);
        ClientResponse response = client.create(loginfo);
        
        return response;
    }
    
    @Override
    public List<Property> getProperties() throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getProperties()");
    	}

    	CacheKey key = new CacheKey(Property.class.getName());
    	List<Property> properties = (List<Property>) cacheManager.get(key);
    	if (properties == null) {
    		properties = getPropertiesFromServer();
    		cacheManager.add(key, properties);
    	}
    	
    	return properties;
    }

    private List<Property> getPropertiesFromServer() throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getForumPathFromServer()");
    	}

    	RestClient<Property> adminClient = getRestClient(REST_API_COMPONENT + REST_API_PROPERTY);
    	GenericType<List<Property>> genericType = new GenericType<List<Property>>() {};
    	List<Property> properties = adminClient.get(genericType);

    	return properties;
    }

	@Override
	public List<User> getSyncUsers() throws PhrescoException {
		if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getUsers()");
        }
		
		RestClient<User> syncUsers = getRestClient(REST_API_ADMIN + REST_API_USERS_IMPORT);
		GenericType<List<User>> genericType = new GenericType<List<User>>() {};
		ClientResponse response = syncUsers.create(userInfo, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
		List<User> users = response.getEntity(genericType);
		
		return users;
	}
	
	private List<User> getUsersFromServer() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getUsersFromServer()");
        }
        
    	RestClient<User> userClient = getRestClient(REST_API_ADMIN + REST_API_USERS);
		GenericType<List<User>> genericType = new GenericType<List<User>>(){};
		
		return userClient.get(genericType);
    }
    
    public List<User> getUsersFromDB() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getUsers()");
        }
        
        CacheKey key = new CacheKey(User.class.getName());
		List<User> users = (List<User>) cacheManager.get(key);
        if (CollectionUtils.isEmpty(users)) {
        	users = getUsersFromServer();
        	cacheManager.add(key, users);
        }
        
        return users;
    }

	@Override
    public List<PlatformType> getPlatforms() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getPlatforms()");
        }
        
        RestClient<PlatformType> client = getRestClient(REST_API_COMPONENT + REST_API_PLATFORMS);
        GenericType<List<PlatformType>> genericType = new GenericType<List<PlatformType>>(){};
        List<PlatformType> platforms = client.get(genericType);
        
        return platforms;
    }
	
	@Override 
	public List<TechnologyOptions> getOptions() throws PhrescoException {
		if (isDebugEnabled) {
			S_LOGGER.debug("Entered into ServiceManagerImpl.getOptions()");
		}
		RestClient<TechnologyOptions> client = getRestClient(REST_API_COMPONENT + REST_API_OPTIONS);
		GenericType<List<TechnologyOptions>> genericType = new GenericType<List<TechnologyOptions>>(){};
		List<TechnologyOptions> options = client.get(genericType);
		
		return options;
	}

	@Override
	public List<License> getLicenses() throws PhrescoException {
		if (isDebugEnabled) {
			S_LOGGER.debug("Entered into ServiceManagerImpl.getLicenses()");
		}
		RestClient<License> restClient = getRestClient(REST_API_COMPONENT + REST_API_LICENSE);
		GenericType<List<License>> genericType = new GenericType<List<License>>(){};
		return restClient.get(genericType);
	}

	@Override
	public VersionInfo getVersionInfo(String currentVersion) throws PhrescoException {
		RestClient<VersionInfo> restClient = getRestClient(ServiceConstants.REST_API_VERSION);
		restClient.queryString(VERSION, currentVersion);
		GenericType<VersionInfo> genericType = new GenericType<VersionInfo>(){};
		return restClient.getById(genericType);
	}

	@Override
	public ClientResponse getUpdateVersionContent(String customerId) throws PhrescoException {
		RestClient<VersionInfo> restClient = getRestClient(ServiceConstants.REST_API_VERSION + "/update");
		restClient.queryString(REST_QUERY_CUSTOMERID, customerId);
		return restClient.get(MediaType.MULTIPART_FORM_DATA);
	}

	@Override
	public InputStream getIcon(String id) throws PhrescoException {
		RestClient<VersionInfo> restClient = getRestClient(REST_API_ADMIN + "/icon");
		restClient.queryString(REST_QUERY_ID, id);
		ClientResponse clientResponse = restClient.get(MediaType.MULTIPART_FORM_DATA);
		return clientResponse.getEntityInputStream();
	}

	@Override
    public ClientResponse createTechnologyGroups(List<TechnologyGroup> technologyGroup, String customerId ) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createTechnologyGroups(List<TechnologyGroup> technologyGroup, String customerId)");
        }
    	RestClient<TechnologyGroup> newTechGrp = getRestClient(REST_API_COMPONENT + REST_API_TECHGROUPS);
		ClientResponse clientResponse = newTechGrp.create(technologyGroup);
		CacheKey key = new CacheKey(customerId, ApplicationType.class.getName());
		cacheManager.add(key, getApplicationTypesFromServer(customerId));
		
		return clientResponse;
    }
    
    @Override
    public ClientResponse deleteTechnologyGroups(String TechGrpId, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.deleteTechnologyGroups(String TechGrpId, String customerId)");
    	}

    	RestClient<TechnologyGroup> deleteTechGrp = getRestClient(REST_API_COMPONENT + REST_API_TECHGROUPS);
    	deleteTechGrp.setPath(TechGrpId);
    	ClientResponse clientResponse = deleteTechGrp.deleteById();
    	CacheKey key = new CacheKey(customerId, ApplicationType.class.getName());
		cacheManager.add(key, getApplicationTypesFromServer(customerId));

    	return clientResponse;
    }

}