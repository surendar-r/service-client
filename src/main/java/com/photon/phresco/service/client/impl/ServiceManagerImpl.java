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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ApplicationType;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.Customer;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.commons.model.LogInfo;
import com.photon.phresco.commons.model.Permission;
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
import com.photon.phresco.service.client.api.Content;
import com.photon.phresco.service.client.api.ServiceClientConstant;
import com.photon.phresco.service.client.api.ServiceContext;
import com.photon.phresco.service.client.api.ServiceManager;
import com.photon.phresco.util.Constants;
import com.photon.phresco.util.Credentials;
import com.photon.phresco.util.ServiceConstants;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

@SuppressWarnings("unchecked")
public class ServiceManagerImpl implements ServiceManager, ServiceClientConstant, ServiceConstants, Constants {

    private static final Logger S_LOGGER = Logger.getLogger(ServiceManagerImpl.class);
    private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
    private EhCacheManager manager;
    
    private String serverPath = null;
    private static User userInfo = null;
    
    private static final String CACHE_FEATURES_KEY = "features";
    private static final String CACHE_MODULES_KEY = "modules";
    private static final String CACHE_JSLIBS_KEY = "jsLibs";

	public ServiceManagerImpl(String serverPath) throws PhrescoException {
    	super();
    	this.serverPath = serverPath;
    }

    public ServiceManagerImpl(ServiceContext context) throws PhrescoException {
    	super();
    	init(context);
    	manager = new EhCacheManager();
    }
    
    public <E> RestClient<E> getRestClient(String contextPath) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getRestClient(String contextPath)" + contextPath);
        }
    	
    	StringBuilder builder = new StringBuilder();
    	builder.append(serverPath);
    	builder.append(contextPath);
    	RestClient<E> restClient = new RestClient<E>(builder.toString());
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
		doLogin(username, password);
	}
	
    private void doLogin(String username, String password) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.doLogin(String username, String password)");
        }
    	
    	Credentials credentials = new Credentials(username, password); 
    	Client client = ClientHelper.createClient();
        WebResource resource = client.resource(serverPath + "/login");
        resource.accept(MediaType.APPLICATION_JSON);
        ClientResponse response = resource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, credentials);
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
    	List<Technology> archeTypes = (List<Technology>) manager.get(key);
		if (CollectionUtils.isEmpty(archeTypes)) {
			archeTypes = getArcheTypesFromServer(customerId);
			manager.add(key, archeTypes);
		}
    	
    	return archeTypes;
	}
    
    @Override
    public List<Technology> getArcheTypes(String customerId, String appTypeId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getArcheTypes(String customerId, String appTypeId)");
        }

        CacheKey key = new CacheKey(customerId, Technology.class.getName(), appTypeId);
        List<Technology> archeTypes = (List<Technology>) manager.get(key);
        if (CollectionUtils.isEmpty(archeTypes)) {
            RestClient<Technology> archeTypeClient = getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
            Map<String, String> queryStringsMap = new HashMap<String, String>();
            queryStringsMap.put(REST_QUERY_CUSTOMERID, customerId);
            queryStringsMap.put(REST_API_PATH_PARAM_ID, appTypeId);
            archeTypeClient.queryStrings(queryStringsMap);
            GenericType<List<Technology>> genericType = new GenericType<List<Technology>>(){};
            archeTypes = archeTypeClient.get(genericType);
            manager.add(key, archeTypes);
        }
        
        return archeTypes;
    }
    
    @Override
    public Technology getArcheType(String archeTypeId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getArcheType(String archeTypeId, String customerId)");
        }
        
        CacheKey key = new CacheKey(customerId, Technology.class.getName());
        List<Technology> archeTypes = (List<Technology>) manager.get(key);
        if (CollectionUtils.isEmpty(archeTypes)) {
    		archeTypes = getArcheTypesFromServer(customerId);
			manager.add(key, archeTypes);
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
    public BodyPart createBodyPart(String name, Content.Type jarType, InputStream jarIs ) throws PhrescoException {
    	BodyPart binaryPart = new BodyPart();
	    binaryPart.setMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
	    binaryPart.setEntity(jarIs);
	    Content content = new Content(jarType, name, null, null, null, 0);
		binaryPart.setContentDisposition(content);

		return binaryPart;
    }
    
    @Override
    public ClientResponse createArcheTypes(MultiPart multiPart, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createArcheTypes(List<Technology> archeTypes, String customerId)");
        }
        
    	RestClient<Technology> newApp = getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
		ClientResponse clientResponse = newApp.create(multiPart);
		CacheKey key = new CacheKey(customerId, Technology.class.getName());
		manager.add(key, getArcheTypesFromServer(customerId));
		
		return clientResponse;
    }
    
    @Override
    public void updateArcheType(Technology technology, String archeTypeId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateArcheTypes(Technology technology, String archeTypeId, String customerId)");
        }
    	
    	RestClient<Technology> editArchetype = getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
    	editArchetype.setPath(archeTypeId);
		GenericType<Technology> genericType = new GenericType<Technology>() {};
		editArchetype.updateById(technology, genericType);
		CacheKey key = new CacheKey(customerId, Technology.class.getName());
		manager.add(key, getArcheTypesFromServer(customerId));
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
    	manager.add(key, getArcheTypesFromServer(customerId));

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
    	List<ApplicationType> appTypes = (List<ApplicationType>) manager.get(key);
    	try {
    		if (CollectionUtils.isEmpty(appTypes)) {
    			appTypes = getApplicationTypesFromServer(customerId);
    			manager.add(key, appTypes);
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
    	List<ApplicationType> appTypes = (List<ApplicationType>) manager.get(key);
    	if (CollectionUtils.isEmpty(appTypes)) {
			appTypes = getApplicationTypesFromServer(customerId);
			manager.add(key, appTypes);
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
		manager.add(key, getApplicationTypesFromServer(customerId));
		
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
		manager.add(key, getApplicationTypesFromServer(customerId));
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
	    manager.add(key, getApplicationTypesFromServer(customerId));
	    
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
		List<DownloadInfo> servers = (List<DownloadInfo>) manager.get(key);
        if (CollectionUtils.isEmpty(servers)) {
        	servers = getServersFromServer(customerId);
        	manager.add(key, servers);
        }
		
		return servers;
	}
    
    @Override
    public List<DownloadInfo> getServers(String customerId, String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getServers(String customerId, String techId)");
        }
        
        CacheKey key = new CacheKey(DownloadInfo.class.getName(), techId);
        List<DownloadInfo> servers = (List<DownloadInfo>) manager.get(key);
        if (CollectionUtils.isEmpty(servers)) {
            RestClient<DownloadInfo> serverClient = getRestClient(REST_API_COMPONENT + REST_API_SERVERS);
            Map<String, String> queryStringsMap = new HashMap<String, String>();
            queryStringsMap.put(REST_QUERY_CUSTOMERID, customerId);
            queryStringsMap.put(REST_QUERY_TECHID, techId);
            serverClient.queryStrings(queryStringsMap);
            GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
            servers = serverClient.get(genericType);
            manager.add(key, servers);
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
		List<DownloadInfo> databases = (List<DownloadInfo>) manager.get(key);
        if (CollectionUtils.isEmpty(databases)) {
        	databases = getDatabasesFromServer(customerId);
        	manager.add(key, databases);
        }
		
		return databases;
	}
    
    @Override
    public List<DownloadInfo> getDatabases(String customerId, String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDatabases(String techId, String customerId)");
        }
        
        CacheKey key = new CacheKey(DownloadInfo.class.getName(), techId);
        List<DownloadInfo> databases = (List<DownloadInfo>) manager.get(key);
        if (CollectionUtils.isEmpty(databases)) {
            RestClient<DownloadInfo> dbClient = getRestClient(REST_API_COMPONENT + REST_API_DATABASES);
            Map<String, String> queryStringsMap = new HashMap<String, String>();
            queryStringsMap.put(REST_QUERY_CUSTOMERID, customerId);
            queryStringsMap.put(REST_QUERY_TECHID, techId);
            dbClient.queryStrings(queryStringsMap);
            GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
            databases = dbClient.get(genericType);
            manager.add(key, databases);
        }
        
        return databases;
    }
    
    private List<WebService> getWebServicesFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getWebServices(String customerId)");
        }
    	
		RestClient<WebService> webServiceClient = getRestClient(REST_API_COMPONENT + REST_API_WEBSERVICES);
		webServiceClient.queryString(REST_QUERY_CUSTOMERID, customerId);
		GenericType<List<WebService>> genericType = new GenericType<List<WebService>>(){};
		
		return webServiceClient.get(genericType);
	}
    
    @Override
    public List<WebService> getWebServices(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getWebServices(String customerId)");
        }
    	
        CacheKey key = new CacheKey(customerId, WebService.class.getName());
		List<WebService> webServices = (List<WebService>) manager.get(key);
        if (CollectionUtils.isEmpty(webServices)) {
        	webServices = getWebServicesFromServer(customerId);
        	manager.add(key, webServices);
        }
		
		return webServices;
	}
    
    @Override
    public List<WebService> getWebServices(String customerId, String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getWebServices(String techId, String customerId)");
        }
        
        CacheKey key = new CacheKey(customerId, WebService.class.getName(), techId);
        List<WebService> webServices = (List<WebService>) manager.get(key);
        if (CollectionUtils.isEmpty(webServices)) {
            RestClient<WebService> webServiceClient = getRestClient(REST_API_COMPONENT + REST_API_WEBSERVICES);
            Map<String, String> queryStringsMap = new HashMap<String, String>();
            queryStringsMap.put(REST_QUERY_CUSTOMERID, customerId);
            queryStringsMap.put(REST_QUERY_TECHID, techId);
            webServiceClient.queryStrings(queryStringsMap);
            GenericType<List<WebService>> genericType = new GenericType<List<WebService>>(){};
            webServices = webServiceClient.get(genericType);
            manager.add(key, webServices);
        }
        
        return webServices;
    }
    
    @Override
    public List<ArtifactGroup> getModules(String customerId, String techId, String type) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getModules(String customerId, String techId)");
        }
        
        CacheKey key = new CacheKey(customerId, REST_QUERY_TYPE_MODULE, techId);
        List<ArtifactGroup> modules = (List<ArtifactGroup>) manager.get(key);
        if (CollectionUtils.isEmpty(modules)) {
            modules = getModulesFromServer(customerId, techId, type);
        }
        
        return modules;
    }

    private List<ArtifactGroup> getModulesFromServer(String customerId, String techId, String type) throws PhrescoException {
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
        CacheKey key = new CacheKey(customerId, CACHE_MODULES_KEY, techId);
        manager.add(key, modules);
        
        return modules;
    }
    
    @Override
    public List<ArtifactGroup> getComponents(String customerId, String techId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getJSLibs(String customerId, String techId)");
        }
        
        CacheKey key = new CacheKey(customerId, REST_QUERY_TYPE_COMPONENT, techId);
        List<ArtifactGroup> components = (List<ArtifactGroup>) manager.get(key);
        if (CollectionUtils.isEmpty(components)) {
            RestClient<ArtifactGroup> moduleGroupClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
            Map<String, String> queryStringsMap = new HashMap<String, String>();
            queryStringsMap.put(REST_QUERY_CUSTOMERID, customerId);
            queryStringsMap.put(REST_QUERY_TYPE, REST_QUERY_TYPE_COMPONENT);
            queryStringsMap.put(REST_QUERY_TECHID, techId);
            moduleGroupClient.queryStrings(queryStringsMap);
            GenericType<List<ArtifactGroup>> genericType = new GenericType<List<ArtifactGroup>>(){};
            components = moduleGroupClient.get(genericType);
            manager.add(key, components);
        }
        
        return components;
    }
    
    private List<ArtifactGroup> getFeaturesFromServer(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getFeaturesFromServer(String customerId)");
    	}

    	RestClient<ArtifactGroup> moduleGroupClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
    	moduleGroupClient.queryString(REST_QUERY_CUSTOMERID, customerId);
    	GenericType<List<ArtifactGroup>> genericType = new GenericType<List<ArtifactGroup>>(){};

    	return moduleGroupClient.get(genericType);
    }
    
    @Override
    public ArtifactGroup getFeature(String moduleId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getFeature(String moduleId, String customerId)");
        }

        CacheKey key = new CacheKey(customerId, CACHE_FEATURES_KEY);
        List<ArtifactGroup> modules = (List<ArtifactGroup>) manager.get(key);
        if (CollectionUtils.isEmpty(modules)) {
        	modules = getFeaturesFromServer(customerId);
			manager.add(key, modules);
        }
        if (CollectionUtils.isNotEmpty(modules)) {
        	for (ArtifactGroup ArtifactGroup : modules) {
				if (ArtifactGroup.getId().equals(moduleId)) {
					return ArtifactGroup;
				}
			}
        }
        
        return null;
    }
    
    @Override
    public ClientResponse createFeatures(MultiPart multiPart, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createFeatures(MultiPart multiPart, String customerId)");
        }
        
        RestClient<ArtifactGroup> moduleClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        ClientResponse response = moduleClient.create(multiPart);
        // TODO:Lohes
//        CacheKey key = new CacheKey(customerId, CACHE_FEATURES_KEY);
//        manager.add(key, getModulesFromServer(customerId));
        
        return response;
    }
    
    @Override
    public ClientResponse updateFeature(MultiPart multiPart, String moduleId, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.updateFeature(MultiPart multiPart, String moduleId, String customerId)");
    	}
     	
    	RestClient<ArtifactGroup> editModule = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
     	editModule.setPath(moduleId);
 		ClientResponse response = editModule.create(multiPart);
        // TODO:Lohes
// 		CacheKey key = new CacheKey(customerId, CACHE_FEATURES_KEY);
// 		manager.add(key, getModulesFromServer(customerId));
 		
 		return response;
    }

    @Override
    public ClientResponse deleteFeature(String moduleId, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
     		S_LOGGER.debug("Entered into ServiceManagerImpl.deleteFeatures(String moduleId, String customerId)");
     	}

     	RestClient<ArtifactGroup> deleteModule = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
     	deleteModule.setPath(moduleId);
     	ClientResponse response = deleteModule.deleteById();
        // TODO:Lohes
//     	CacheKey key = new CacheKey(customerId, CACHE_FEATURES_KEY);
//     	manager.add(key, getModulesFromServer(customerId));
     	
     	return response;
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
		List<Customer> customers = (List<Customer>) manager.get(key);
        if (CollectionUtils.isEmpty(customers)) {
        	customers = getCustomersFromServer();
        	manager.add(key, customers);
        }
        
        return customers;
    }
    
    @Override
    public Customer getCustomer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getCustomer(String customerId)" + customerId);
        }
        
        CacheKey key = new CacheKey(Customer.class.getName());
        List<Customer> customers = (List<Customer>) manager.get(key);
        if (CollectionUtils.isEmpty(customers)) {
        	customers = getCustomersFromServer();
        	manager.add(key, customers);
        }
        if (CollectionUtils.isNotEmpty(customers)) {
        	for (Customer customer : customers) {
				if (customer.getId().equals(customerId)) {
					return customer;
				}
			}
        }
        
        return null;
    }
    
    @Override
    public ClientResponse createCustomers(List<Customer> customers) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createCustomers(List<Customer> customers)");
        }
        
        RestClient<Customer> customersClient = getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        ClientResponse response = customersClient.create(customers);
        CacheKey key = new CacheKey(Customer.class.getName());
        manager.add(key, getCustomersFromServer());
        
        return response;
    }
    
    @Override
    public void updateCustomer(Customer customer, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateCustomer(Customer customer, String customerId)" + customerId);
        }
        
        RestClient<Customer> customersClient = getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        customersClient.setPath(customerId);
        GenericType<Customer> genericType = new GenericType<Customer>() {};
        customersClient.updateById(customer, genericType);
        CacheKey key = new CacheKey(Customer.class.getName());
        manager.add(key, getCustomersFromServer());
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
        manager.add(key, getCustomersFromServer());
        
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
    public List<SettingsTemplate> getconfigTemplates(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Enetered into ServiceManagerImpl.getconfigTemplates(String customerId)");
    	}

    	CacheKey key = new CacheKey(customerId, SettingsTemplate.class.getName());
    	List<SettingsTemplate> configTemplates = (List<SettingsTemplate>) manager.get(key);
		if (CollectionUtils.isEmpty(configTemplates)) {
			configTemplates = getConfigTemplatesFromServer(customerId);
			manager.add(key, configTemplates);
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
    	manager.add(key, getConfigTemplatesFromServer(customerId));
        
        return clientResponse ;
    }
    
    @Override
    public SettingsTemplate getConfigTemplate(String configId, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getConfigTemplate(String configId, String customerId)");
    	}
    	
    	CacheKey key = new CacheKey(customerId, SettingsTemplate.class.getName());
    	List<SettingsTemplate> configTemps = (List<SettingsTemplate>) manager.get(key);
    	if (CollectionUtils.isEmpty(configTemps)) {
    		configTemps = getConfigTemplatesFromServer(customerId);
			manager.add(key, configTemps);
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
		manager.add(key, getConfigTemplatesFromServer(customerId));
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
    	manager.add(key, getConfigTemplatesFromServer(customerId));
    	
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
    
    @Override
    public List<ApplicationInfo> getPilotProjects(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getPilotProjects(String customerId)" + customerId);
        }
        
        CacheKey key = new CacheKey(customerId, ProjectInfo.class.getName());
        List<ApplicationInfo> pilotProjects = (List<ApplicationInfo>) manager.get(key);
        try {	
    		if (CollectionUtils.isEmpty(pilotProjects)) {
    			pilotProjects = getPilotProjectsFromServer(customerId);
    			manager.add(key, pilotProjects);
    		}
    	} catch(Exception e){
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
    	List<ApplicationInfo> pilotProjects = (List<ApplicationInfo>) manager.get(key);
    	if (CollectionUtils.isEmpty(pilotProjects)) {
			pilotProjects = getPilotProjectsFromServer(customerId);
			manager.add(key, pilotProjects);
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
    public ClientResponse createPilotProjects(MultiPart multiPart, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createPilotProjects(List<ProjectInfo> proInfo, String customerId)");
        }
        
        RestClient<ProjectInfo> pilotClient = getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
        ClientResponse response = pilotClient.create(multiPart);
        CacheKey key = new CacheKey(customerId, ProjectInfo.class.getName());
        manager.add(key, getPilotProjectsFromServer(customerId));
        
        return response;
    }
    
    @Override
    public void updatePilotProject(MultiPart multiPart, String projectId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updatePilotProject(ProjectInfo projectInfo, String projectId)" + projectId);
        }
        
        RestClient<ApplicationInfo> pilotproClient = getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
        pilotproClient.setPath(projectId);
        pilotproClient.create(multiPart);
        CacheKey key = new CacheKey(customerId, ProjectInfo.class.getName());
        manager.add(key, getPilotProjectsFromServer(customerId));
    }
    
    @Override
    public ClientResponse deletePilotProject(String projectId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deletePilotProject(String projectId)" + projectId);
        }
        
        RestClient<ProjectInfo> pilotproClient = getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
        pilotproClient.setPath(projectId);
        ClientResponse response = pilotproClient.deleteById();
        CacheKey key = new CacheKey(customerId, ProjectInfo.class.getName());
        manager.add(key, getPilotProjectsFromServer(customerId));
        
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
        List<Role> roles = (List<Role>) manager.get(key);
        try {	
    		if (CollectionUtils.isEmpty(roles)) {
    			roles = getRolesFromServer();
    			manager.add(key, roles);
    		}
    	} catch(Exception e){
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
    	List<Role> roles = (List<Role>) manager.get(key);
    	if (CollectionUtils.isEmpty(roles)) {
    		roles = getRolesFromServer();
			manager.add(key, roles);
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
    	manager.add(key, getRolesFromServer());
    	
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
    	manager.add(key, getRolesFromServer());
    	
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
    	manager.add(key, getRolesFromServer());
    }
    
    @Override
    public List<DownloadInfo> getDownloads(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDownloadInfo(List<DownloadInfo> downloadInfo)");
        }
        
        CacheKey key = new CacheKey(DownloadInfo.class.getName());
        List<DownloadInfo> downloadInfos = (List<DownloadInfo>) manager.get(key);
        try {   
            if (CollectionUtils.isEmpty(downloadInfos)) {
                downloadInfos = getDownloadsFromServer();
                manager.add(key, downloadInfos);
            }
        } catch(Exception e){
            throw new PhrescoException(e);
        }
        
        return downloadInfos;
    }
    
    private List<DownloadInfo> getDownloadsFromServer() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDownloadInfosFromServer()");
        }
    	
    	RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
		GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
		
		return downloadClient.get(genericType);
    }

    @Override
    public DownloadInfo getDownload(String downloadId, String customerId) throws PhrescoException {
    	if(isDebugEnabled){
    		S_LOGGER.debug("Entered into Restclient.getDownload(String downloadId, String customerId)");
    	}
    	
    	CacheKey key = new CacheKey(DownloadInfo.class.getName());
    	List<DownloadInfo> downloadInfos = (List<DownloadInfo>) manager.get(key);
    	if (CollectionUtils.isEmpty(downloadInfos)) {
    		downloadInfos = getDownloadsFromServer();
			manager.add(key, downloadInfos);
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
    public ClientResponse createDownloads(MultiPart multiPart, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createDownloadInfo(List<DownloadInfo> downloadInfo)");
        }
    	
    	RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
    	ClientResponse response = downloadClient.create(multiPart);
    	CacheKey key = new CacheKey(DownloadInfo.class.getName());
    	manager.add(key, getDownloadsFromServer());
    	
    	return response;
    }

    @Override
    public void updateDownload(MultiPart multiPart, String downloadId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateDownload(DownloadInfo downloadInfo, String downloadId)");
        }
        
        RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
        downloadClient.setPath(downloadId);
        downloadClient.create(multiPart);
        CacheKey key = new CacheKey(DownloadInfo.class.getName());
        manager.add(key, getDownloadsFromServer());
    }

    @Override
    public ClientResponse deleteDownloadInfo(String downloadId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deleteDownloadInfo(String downloadId)");
        }

        RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_COMPONENT + REST_API_DOWNLOADS);
        downloadClient.setPath(downloadId);
        ClientResponse response = downloadClient.deleteById();
        CacheKey key = new CacheKey(DownloadInfo.class.getName());
        manager.add(key, getDownloadsFromServer());

        return response;
    }

    @Override
    public ClientResponse createProject(ApplicationInfo projectInfo) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createProject(ProjectInfo projectInfo)");
        }

        RestClient<ApplicationInfo> projectClient = getRestClient(REST_API_PROJECT + REST_API_PROJECT_CREATE);

        return projectClient.create(projectInfo, MEDIATYPE_ZIP, MediaType.APPLICATION_JSON);
    }
    
    @Override
    public ClientResponse updateProject(ApplicationInfo projectInfo) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateProject(ProjectInfo projectInfo)");
        }

        RestClient<ApplicationInfo> projectClient = getRestClient(REST_API_PROJECT + REST_API_PROJECT_UPDATE);

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
    public List<Environment> getDefaultEnvFromServer() throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDefaultEnvFromServer()");
        }
    	
    	RestClient<Environment> envClient = getRestClient(REST_API_ENV_PATH);
		GenericType<List<Environment>> genericType = new GenericType<List<Environment>>(){};
		
		return envClient.get(genericType);
    }
    
    private List<Property> getGlobalUrlFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getGlobalUrlFromServer(String customerId)");
        }
    	
    	RestClient<Property> globalUrlClient = getRestClient(REST_API_ADMIN + REST_API_GLOBALURL);
		GenericType<List<Property>> genericType = new GenericType<List<Property>>(){};
		
		return globalUrlClient.get(genericType);
    }

    @Override
    public List<Property> getGlobalUrls(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getGlobalUrls(List<GlobalURL> globalUrl)");
        }
    	
    	CacheKey key = new CacheKey(Property.class.getName());
     	List<Property> globalUrls = (List<Property>) manager.get(key);
    	try {	
    		if (CollectionUtils.isEmpty(globalUrls)) {
    			globalUrls = getGlobalUrlFromServer(customerId);
    			manager.add(key, globalUrls);
    		}
    	} catch(Exception e){
    		throw new PhrescoException(e);
    	}
    	
    	return globalUrls;
    }
    
    @Override
    public Property getGlobalUrl(String globalUrlId, String customerId) throws PhrescoException {
    	if(isDebugEnabled){
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getGlobalUrl(String globalUrlId, String customerId)");
    	}
    	
    	CacheKey key = new CacheKey(Property.class.getName());
    	List<Property> globalUrls = (List<Property>) manager.get(key);
    	if (CollectionUtils.isEmpty(globalUrls)) {
    		globalUrls = getGlobalUrlFromServer(customerId);
			manager.add(key, globalUrls);
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
    public ClientResponse createGlobalUrl(List<Property> globalUrl, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createGlobalUrl(List<GlobalURL> globalUrl)");
        }
    	
    	RestClient<Property> globalClient = getRestClient(REST_API_ADMIN + REST_API_GLOBALURL);
    	ClientResponse response = globalClient.create(globalUrl);
    	CacheKey key = new CacheKey(Property.class.getName());
    	manager.add(key, getGlobalUrlFromServer(customerId));
    	
    	return response;
    }
    
    @Override
    public void updateGlobalUrl(Property globalUrl, String globalurlId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateGlobalUrl(GlobalURL globalUrl, String globalurlId, String customerId)");
        }
    	
    	RestClient<Property> editGlobalUrl = getRestClient(REST_API_COMPONENT + REST_API_GLOBALURL);
    	editGlobalUrl.setPath(globalurlId);
		GenericType<Property> genericType = new GenericType<Property>() {};
		editGlobalUrl.updateById(globalUrl, genericType);
		CacheKey key = new CacheKey(customerId, Property.class.getName());
		manager.add(key, getGlobalUrlFromServer(customerId));
    }
    
    @Override
    public ClientResponse deleteGlobalUrl(String globalurlId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deleteglobalUrl(String globalurlId, String customerId)");
        }

        RestClient<Property> globalUrlClient = getRestClient(REST_API_ADMIN + REST_API_GLOBALURL);
        globalUrlClient.setPath(globalurlId);
        ClientResponse response = globalUrlClient.deleteById();
        CacheKey key = new CacheKey(Property.class.getName());
        manager.add(key, getGlobalUrlFromServer(customerId));

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
		List<Permission> permissions = (List<Permission>) manager.get(key);
        if (CollectionUtils.isEmpty(permissions)) {
        	permissions = getPermissionsFromServer();
        	manager.add(key, permissions);
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
        manager.add(key, getPermissionsFromServer());
        
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
    public Property getForumPath(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getForumPath(String customerId)");
        }
    	
    	RestClient<Property> adminClient = getRestClient(REST_API_ADMIN + REST_API_FORUMS);
    	GenericType<Property> genericType = new GenericType<Property>() {};
    	adminClient.queryString(REST_QUERY_CUSTOMERID, customerId);
    	Property adminConfigInfo = adminClient.getById(genericType);
    	
    	return adminConfigInfo;
    }
}