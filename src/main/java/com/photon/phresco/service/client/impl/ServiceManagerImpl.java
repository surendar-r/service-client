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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.photon.phresco.commons.model.Customer;
import com.photon.phresco.commons.model.Role;
import com.photon.phresco.commons.model.User;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.model.ApplicationType;
import com.photon.phresco.model.Database;
import com.photon.phresco.model.DownloadInfo;
import com.photon.phresco.model.ModuleGroup;
import com.photon.phresco.model.ProjectInfo;
import com.photon.phresco.model.Server;
import com.photon.phresco.model.SettingsTemplate;
import com.photon.phresco.model.Technology;
import com.photon.phresco.model.VideoInfo;
import com.photon.phresco.model.WebService;
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
    User userInfo = null;
    
    private String CACHE_APPTYPES_KEY = "appTypes";
    private String CACHE_ARCHETYPES_KEY = "archetypes";
    private String CACHE_PILOT_PROJ_KEY = "pilotProjects";
    private String CACHE_FEATURES_KEY = "features";
    private String CACHE_MODULES_KEY = "modules";
    private String CACHE_JSLIBS_KEY = "jsLibs";
    private String CACHE_DOWNLOADS_KEY = "downloads";
    private String CACHE_CUSTOMERS_KEY = "customers";
    private String CACHE_ROLES_KEY = "roles";
    private String CACHE_SERVERS_KEY = "servers";
    private String CACHE_DATABASES_KEY = "databases";
    private String CACHE_WEBSERVICES_KEY = "webServices";

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
    
    public List<Technology> getArcheTypes(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getArcheTypes(String customerId)");
        }

        CacheKey key = new CacheKey(customerId, CACHE_ARCHETYPES_KEY);
    	List<Technology> archeTypes = (List<Technology>) manager.get(key);
		if (CollectionUtils.isEmpty(archeTypes)) {
			archeTypes = getArcheTypesFromServer(customerId);
			manager.add(key, archeTypes);
		}
    	
    	return archeTypes;
	}
    
    public Technology getArcheType(String archeTypeId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getArcheType(String archeTypeId, String customerId)");
        }
        
        CacheKey key = new CacheKey(customerId, CACHE_ARCHETYPES_KEY);
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
    
    public BodyPart createBodyPart (String name, String jarType, InputStream jarIs ) throws PhrescoException {
    	BodyPart binaryPart = new BodyPart();
	    binaryPart.setMediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE);
	    binaryPart.setEntity(jarIs);
	    Content content = new Content(jarType, name, null, null, null, 0);
		binaryPart.setContentDisposition(content);

		return binaryPart;
    }
    
    public ClientResponse createArcheTypes(MultiPart multiPart, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createArcheTypes(List<Technology> archeTypes, String customerId)");
        }
        
    	RestClient<Technology> newApp = getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
		ClientResponse clientResponse = newApp.create(multiPart);
		CacheKey key = new CacheKey(customerId, CACHE_ARCHETYPES_KEY);
		manager.add(key, getArcheTypesFromServer(customerId));
		
		return clientResponse;
    }
    
    public void updateArcheType(Technology technology, String archeTypeId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateArcheTypes(Technology technology, String archeTypeId, String customerId)");
        }
    	
    	RestClient<Technology> editArchetype = getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
    	editArchetype.setPath(archeTypeId);
		GenericType<Technology> genericType = new GenericType<Technology>() {};
		editArchetype.updateById(technology, genericType);
		CacheKey key = new CacheKey(customerId, CACHE_ARCHETYPES_KEY);
		manager.add(key, getArcheTypesFromServer(customerId));
    }
    
    public ClientResponse deleteArcheType(String archeTypeId, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.deleteArcheType(String archeTypeId, String customerId)");
    	}

    	RestClient<Technology> deleteArchetype = getRestClient(REST_API_COMPONENT + REST_API_TECHNOLOGIES);
    	deleteArchetype.setPath(archeTypeId);
    	ClientResponse clientResponse = deleteArchetype.deleteById();
    	CacheKey key = new CacheKey(customerId, CACHE_ARCHETYPES_KEY);
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
    
    public List<ApplicationType> getApplicationTypes(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getApplicationTypes(String customerId)");
        }
        
        CacheKey key = new CacheKey(customerId, CACHE_APPTYPES_KEY);
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
    
    public ApplicationType getApplicationType(String appTypeId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getApplicationType(String appTypeId, String customerId)");
        }

        CacheKey key = new CacheKey(customerId, CACHE_APPTYPES_KEY);
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

    public ClientResponse createApplicationTypes(List<ApplicationType> appTypes, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createApplicationTypes(List<ApplicationType> appTypes, String customerId)");
        }
    	
    	RestClient<ApplicationType> newApp = getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
		ClientResponse clientResponse = newApp.create(appTypes);
		CacheKey key = new CacheKey(customerId, CACHE_APPTYPES_KEY);
		manager.add(key, getApplicationTypesFromServer(customerId));
		
		return clientResponse;
    }
    
    public void updateApplicationType(ApplicationType appType, String appTypeId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateApplicationTypes(ApplicationType appType, String appTypeId, String customerId)");
        }
    	
    	RestClient<ApplicationType> editApptype = getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
    	editApptype.setPath(appTypeId);
		GenericType<ApplicationType> genericType = new GenericType<ApplicationType>() {};
		editApptype.updateById(appType, genericType);
		CacheKey key = new CacheKey(customerId, CACHE_APPTYPES_KEY);
		manager.add(key, getApplicationTypesFromServer(customerId));
    }
    
    public ClientResponse deleteApplicationType(String appTypeId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deleteApplicationType(String appTypeId, String customerId)");
        }
    	
	    RestClient<ApplicationType> deleteApptype = getRestClient(REST_API_COMPONENT + REST_API_APPTYPES);
	    deleteApptype.setPath(appTypeId);
	    ClientResponse clientResponse = deleteApptype.deleteById();
	    CacheKey key = new CacheKey(customerId, CACHE_APPTYPES_KEY);
	    manager.add(key, getApplicationTypesFromServer(customerId));
	    
	    return clientResponse;
    }
    
    public List<Server> getServersFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getServers(String customerId)");
        }
    	
		RestClient<Server> serverClient = getRestClient(REST_API_COMPONENT + REST_API_SERVERS);
        serverClient.queryString(REST_QUERY_CUSTOMERID, customerId);
		GenericType<List<Server>> genericType = new GenericType<List<Server>>(){};
		
		return serverClient.get(genericType);
	}
    
    public List<Server> getServers(String techId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getServers(String techId, String customerId)");
        }
    	
        CacheKey key = new CacheKey(CACHE_SERVERS_KEY);
		List<Server> servers = (List<Server>) manager.get(key);
        if (CollectionUtils.isEmpty(servers)) {
        	servers = getServersFromServer(customerId);
        	manager.add(key, servers);
        }
        List<Server> serversByTechId = new ArrayList<Server>();
        if (CollectionUtils.isNotEmpty(servers)) {
        	for (Server server : servers) {
				if (server.getTechnologies().contains(techId)) {
					serversByTechId.add(server);
				}
			}
        }
		
		return servers;
	}
    
    private List<Database> getDatabasesFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDatabases(String customerId)");
        }
    	
		RestClient<Database> dbClient = getRestClient(REST_API_COMPONENT + REST_API_DATABASES);
        dbClient.queryString(REST_QUERY_CUSTOMERID, customerId);
		GenericType<List<Database>> genericType = new GenericType<List<Database>>(){};
		
		return dbClient.get(genericType);
	}
    
    public List<Database> getDatabases(String techId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDatabases(String techId, String customerId)");
        }
    	
        CacheKey key = new CacheKey(CACHE_DATABASES_KEY);
		List<Database> databases = (List<Database>) manager.get(key);
        if (CollectionUtils.isEmpty(databases)) {
        	databases = getDatabasesFromServer(customerId);
        	manager.add(key, databases);
        }
        List<Database> dbsByTechId = new ArrayList<Database>();
        if (CollectionUtils.isNotEmpty(databases)) {
        	for (Database database : databases) {
				if (database.getTechnologies().contains(techId)) {
					dbsByTechId.add(database);
				}
			}
        }
		
		return dbsByTechId;
	}
    
    public List<WebService> getWebServicesFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getWebServices(String customerId)");
        }
    	
		RestClient<WebService> webServiceClient = getRestClient(REST_API_COMPONENT + REST_API_WEBSERVICES);
		webServiceClient.queryString(REST_QUERY_CUSTOMERID, customerId);
		GenericType<List<WebService>> genericType = new GenericType<List<WebService>>(){};
		
		return webServiceClient.get(genericType);
	}
    
    public List<WebService> getWebServices(String techId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getWebServices(String techId, String customerId)");
        }
    	
        CacheKey key = new CacheKey(CACHE_WEBSERVICES_KEY);
		List<WebService> webServices = (List<WebService>) manager.get(key);
        if (CollectionUtils.isEmpty(webServices)) {
        	webServices = getWebServicesFromServer(customerId);
        	manager.add(key, webServices);
        }
        List<WebService> webServiceByTechId = new ArrayList<WebService>();
        if (CollectionUtils.isNotEmpty(webServices)) {
        	for (WebService webService : webServices) {
				if (webService.getTechnologies().contains(techId)) {
					webServiceByTechId.add(webService);
				}
			}
        }
		
		return webServiceByTechId;
	}
    
    private List<ModuleGroup> getModulesFromServer(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getModulesFromServer(String customerId)");
    	}

    	RestClient<ModuleGroup> moduleGroupClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
    	Map<String, String> headers = new HashMap<String, String>();
    	headers.put(REST_QUERY_CUSTOMERID, customerId);
    	headers.put(REST_QUERY_TYPE, REST_QUERY_TYPE_MODULE);
    	moduleGroupClient.queryStrings(headers);
    	GenericType<List<ModuleGroup>> genericType = new GenericType<List<ModuleGroup>>(){};

    	return moduleGroupClient.get(genericType);
    }
    
    public List<ModuleGroup> getModules(String customerId) throws PhrescoException {
    	if(isDebugEnabled) {
    		S_LOGGER.debug("Enetered into ServiceManagerImpl.getModules(String customerId)");
    	}

    	CacheKey key = new CacheKey(customerId, CACHE_MODULES_KEY);
    	List<ModuleGroup> modules = (List<ModuleGroup>) manager.get(key);
		if (CollectionUtils.isEmpty(modules)) {
			modules = getModulesFromServer(customerId);
			manager.add(key, modules);
		}
    	
    	return modules;
    }
    
    private List<ModuleGroup> getJSLibsFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getJSLibsFromServer(String customerId)");
        }
    	
    	RestClient<ModuleGroup> jsLibClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
    	Map<String, String> headers = new HashMap<String, String>();
    	headers.put(REST_QUERY_CUSTOMERID, customerId);
    	headers.put(REST_QUERY_TYPE, REST_QUERY_TYPE_JS);
    	jsLibClient.queryStrings(headers);
    	GenericType<List<ModuleGroup>> genericType = new GenericType<List<ModuleGroup>>(){};
    	
    	return jsLibClient.get(genericType);
    }
    
    public List<ModuleGroup> getJsLibs(String customerId) throws PhrescoException {
    	if(isDebugEnabled) {
    		S_LOGGER.debug("Enetered into ServiceManagerImpl.getJsLibs(String customerId)");
    	}

    	CacheKey key = new CacheKey(customerId, CACHE_JSLIBS_KEY);
    	List<ModuleGroup> modules = (List<ModuleGroup>) manager.get(key);
		if (CollectionUtils.isEmpty(modules)) {
			modules = getJSLibsFromServer(customerId);
			manager.add(key, modules);
		}
    	
    	return modules;
    }
    
    private List<ModuleGroup> getFeaturesFromServer(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getFeaturesFromServer(String customerId)");
    	}

    	RestClient<ModuleGroup> moduleGroupClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
    	moduleGroupClient.queryString(REST_QUERY_CUSTOMERID, customerId);
    	GenericType<List<ModuleGroup>> genericType = new GenericType<List<ModuleGroup>>(){};

    	return moduleGroupClient.get(genericType);
    }
    
    public List<ModuleGroup> getFeatures(String customerId) throws PhrescoException {
    	if(isDebugEnabled) {
    		S_LOGGER.debug("Enetered into ServiceManagerImpl.getFeatures(String customerId)");
    	}

    	CacheKey key = new CacheKey(customerId, CACHE_FEATURES_KEY);
    	List<ModuleGroup> modules = (List<ModuleGroup>) manager.get(key);
		if (CollectionUtils.isEmpty(modules)) {
			modules = getFeaturesFromServer(customerId);
			manager.add(key, modules);
		}
    	
    	return modules;
    }
     
    public ModuleGroup getFeature(String moduleId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getFeature(String moduleId, String customerId)");
        }

        CacheKey key = new CacheKey(customerId, CACHE_FEATURES_KEY);
        List<ModuleGroup> modules = (List<ModuleGroup>) manager.get(key);
        if (CollectionUtils.isEmpty(modules)) {
        	modules = getFeaturesFromServer(customerId);
			manager.add(key, modules);
        }
        if (CollectionUtils.isNotEmpty(modules)) {
        	for (ModuleGroup moduleGroup : modules) {
				if (moduleGroup.getId().equals(moduleId)) {
					return moduleGroup;
				}
			}
        }
        
        return null;
    }
    
    public ClientResponse createFeatures(List<ModuleGroup> modules, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createFeatures(List<ModuleGroup> modules)");
        }
        
        RestClient<ModuleGroup> moduleClient = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
        ClientResponse response = moduleClient.create(modules);
        CacheKey key = new CacheKey(customerId, CACHE_FEATURES_KEY);
        manager.add(key, getModulesFromServer(customerId));
        
        return response;
    }
     
    public void updateFeature(ModuleGroup module, String moduleId, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.updateFeatures(ModuleGroup module, String moduleId)");
    	}
     	
    	RestClient<ModuleGroup> editModule = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
     	editModule.setPath(moduleId);
 		GenericType<ModuleGroup> genericType = new GenericType<ModuleGroup>() {};
 		editModule.updateById(module, genericType);
 		CacheKey key = new CacheKey(customerId, CACHE_FEATURES_KEY);
 		manager.add(key, getModulesFromServer(customerId));
    }

    public ClientResponse deleteFeature(String moduleId, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
     		S_LOGGER.debug("Entered into ServiceManagerImpl.deleteFeatures(String moduleId, String customerId)");
     	}

     	RestClient<ModuleGroup> deleteModule = getRestClient(REST_API_COMPONENT + REST_API_MODULES);
     	deleteModule.setPath(moduleId);
     	ClientResponse response = deleteModule.deleteById();
     	CacheKey key = new CacheKey(customerId, CACHE_FEATURES_KEY);
     	manager.add(key, getModulesFromServer(customerId));
     	
     	return response;
    }
    
    public List<Customer> getCustomersFromServer() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getCustomersFromServer()");
        }
        
        RestClient<Customer> customersClient = getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        GenericType<List<Customer>> genericType = new GenericType<List<Customer>>(){};
        
        return customersClient.get(genericType);
    }
    
    public List<Customer> getCustomers() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getCustomers()");
        }
        
        CacheKey key = new CacheKey(CACHE_CUSTOMERS_KEY);
		List<Customer> customers = (List<Customer>) manager.get(key);
        if (CollectionUtils.isEmpty(customers)) {
        	customers = getCustomersFromServer();
        	manager.add(key, customers);
        }
        
        return customers;
    }
    
    public Customer getCustomer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getCustomer(String customerId)" + customerId);
        }
        
        CacheKey key = new CacheKey(CACHE_CUSTOMERS_KEY);
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
    
    public ClientResponse createCustomers(List<Customer> customers) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createCustomers(List<Customer> customers)");
        }
        
        RestClient<Customer> customersClient = getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        ClientResponse response = customersClient.create(customers);
        CacheKey key = new CacheKey(CACHE_CUSTOMERS_KEY);
        manager.add(key, getCustomersFromServer());
        
        return response;
    }
    
    public void updateCustomer(Customer customer, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateCustomer(Customer customer, String customerId)" + customerId);
        }
        
        RestClient<Customer> customersClient = getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        customersClient.setPath(customerId);
        GenericType<Customer> genericType = new GenericType<Customer>() {};
        customersClient.updateById(customer, genericType);
        CacheKey key = new CacheKey(CACHE_CUSTOMERS_KEY);
        manager.add(key, getCustomersFromServer());
    }
    
    public ClientResponse deleteCustomer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deleteCustomer(String customerId)" + customerId);
        }
        
        RestClient<Customer> customersClient = getRestClient(REST_API_ADMIN + REST_API_CUSTOMERS);
        customersClient.setPath(customerId);
        ClientResponse response = customersClient.deleteById();
        CacheKey key = new CacheKey(CACHE_CUSTOMERS_KEY);
        manager.add(key, getCustomersFromServer());
        
        return response;
    }
    
    public List<SettingsTemplate> getSettings() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getSettings()");
        }
        
        RestClient<SettingsTemplate> settingClient = getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
        GenericType<List<SettingsTemplate>> genericType = new GenericType<List<SettingsTemplate>>(){};
        
        return settingClient.get(genericType);
    }
    
    public SettingsTemplate getSettings(String settingsId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getSettings(String settingsId)" + settingsId);
        }
        
        RestClient<SettingsTemplate> settingClient = getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
        settingClient.setPath(settingsId);
        GenericType<SettingsTemplate> genericType = new GenericType<SettingsTemplate>(){};
        
        return settingClient.getById(genericType);
    }
    
    public ClientResponse createSettings(List<SettingsTemplate> settings) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createSettings(List<SettingTemplate> settings)");
        }
        
        RestClient<SettingsTemplate> settingsClient = getRestClient(REST_API_COMPONENT + REST_API_SETTINGS);
        
        return settingsClient.create(settings);
    }
    
    private List<ProjectInfo> getPilotProjectsFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getPilotProjectFromServer(String customerId)");
        }
    	
    	RestClient<ProjectInfo> pilotClient = getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
    	Map<String, String> headers = new HashMap<String, String>();
    	headers.put(REST_QUERY_CUSTOMERID, customerId);
    	pilotClient.queryStrings(headers);
		GenericType<List<ProjectInfo>> genericType = new GenericType<List<ProjectInfo>>(){};
		
		return pilotClient.get(genericType);
    }
    
    public List<ProjectInfo> getPilotProjects(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getPilotProjects(String customerId)" + customerId);
        }
        
        CacheKey key = new CacheKey(customerId, CACHE_PILOT_PROJ_KEY);
        List<ProjectInfo> pilotProjects = (List<ProjectInfo>) manager.get(key);
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
    
    public ProjectInfo getPilotProject(String projectId, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getPilotProject(String projectId, String customerId)");
    	}
    	
    	CacheKey key = new CacheKey(customerId, CACHE_PILOT_PROJ_KEY);
    	List<ProjectInfo> pilotProjects = (List<ProjectInfo>) manager.get(key);
    	if (CollectionUtils.isEmpty(pilotProjects)) {
			pilotProjects = getPilotProjectsFromServer(customerId);
			manager.add(key, pilotProjects);
		}
    	if (CollectionUtils.isNotEmpty(pilotProjects)) {
    		for (ProjectInfo pilotProject : pilotProjects) {
				if (pilotProject.getId().equals(projectId)) {
					return pilotProject;
				}
			}
    	}

    	return null;
    }
    
    public ClientResponse createPilotProjects(List<ProjectInfo> proInfo, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createPilotProjects(List<ProjectInfo> proInfo, String customerId)");
        }
        
        RestClient<ProjectInfo> pilotClient = getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
        ClientResponse response = pilotClient.create(proInfo);
        CacheKey key = new CacheKey(customerId, CACHE_PILOT_PROJ_KEY);
        manager.add(key, getPilotProjectsFromServer(customerId));
        
        return response;
    }
    
    public void updatePilotProject(ProjectInfo projectInfo, String projectId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updatePilotProject(ProjectInfo projectInfo, String projectId)" + projectId);
        }
        
        RestClient<ProjectInfo> pilotproClient = getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
        pilotproClient.setPath(projectId);
        GenericType<ProjectInfo> genericType = new GenericType<ProjectInfo>() {};
        pilotproClient.updateById(projectInfo, genericType);
        CacheKey key = new CacheKey(customerId, CACHE_PILOT_PROJ_KEY);
        manager.add(key, getPilotProjectsFromServer(customerId));
    }
    
    public ClientResponse deletePilotProject(String projectId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deletePilotProject(String projectId)" + projectId);
        }
        
        RestClient<ProjectInfo> pilotproClient = getRestClient(REST_API_COMPONENT + REST_API_PILOTS);
        pilotproClient.setPath(projectId);
        ClientResponse response = pilotproClient.deleteById();
        CacheKey key = new CacheKey(customerId, CACHE_PILOT_PROJ_KEY);
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
    
    public List<Role> getRoles() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getRoles())");
        }
        
        CacheKey key = new CacheKey(CACHE_ROLES_KEY);
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
    
    public Role getRole(String roleId) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.getPilotsProjects(List<ProjectInfo> proInfo)");
    	}
    	
    	CacheKey key = new CacheKey(CACHE_ROLES_KEY);
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
    
    public ClientResponse createRoles(List<Role> role) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into ServiceManagerImpl.createroles(List<Role> role)");
    	}	
    	
    	RestClient<Role> roleClient = getRestClient(REST_API_ADMIN + REST_API_ROLES);
    	ClientResponse response = roleClient.create(role);
    	CacheKey key = new CacheKey(CACHE_ROLES_KEY);
    	manager.add(key, getRolesFromServer());
    	
    	return response;
    }
    
    public ClientResponse deleteRole(String id) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into RestClient.deleteRole(String id)" + id);
    	}

    	RestClient<Role> roleClient = getRestClient(REST_API_ADMIN + REST_API_ROLES);
    	roleClient.setPath(id);
    	ClientResponse response = roleClient.deleteById();
    	CacheKey key = new CacheKey(CACHE_ROLES_KEY);
    	manager.add(key, getRolesFromServer());
    	
    	return response;
    }

    public void updateRole(Role role, String id) throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entered into RestClient.updateRole(Role role, String id)" + id);
    	}

    	RestClient<Role> roleClient = getRestClient(REST_API_ADMIN + REST_API_ROLES);
    	roleClient.setPath(id);
    	GenericType<Role> genericType = new GenericType<Role>() {};
    	roleClient.updateById(role, genericType);
    	CacheKey key = new CacheKey(CACHE_ROLES_KEY);
    	manager.add(key, getRolesFromServer());
    }

    private List<DownloadInfo> getDownloadsFromServer(String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDownloadInfosFromServer()");
        }
    	
    	RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_ADMIN + REST_API_DOWNLOADS);
		GenericType<List<DownloadInfo>> genericType = new GenericType<List<DownloadInfo>>(){};
		
		return downloadClient.get(genericType);
    }

    public List<DownloadInfo> getDownloads(String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.getDownloadInfo(List<DownloadInfo> downloadInfo)");
        }
    	
    	CacheKey key = new CacheKey(CACHE_DOWNLOADS_KEY);
     	List<DownloadInfo> downloadInfos = (List<DownloadInfo>) manager.get(key);
    	try {	
    		if (CollectionUtils.isEmpty(downloadInfos)) {
    			downloadInfos = getDownloadsFromServer(customerId);
    			manager.add(key, downloadInfos);
    		}
    	} catch(Exception e){
    		throw new PhrescoException(e);
    	}
    	
    	return downloadInfos;
    }
    
    public DownloadInfo getDownload(String downloadId, String customerId) throws PhrescoException {
    	if(isDebugEnabled){
    		S_LOGGER.debug("Entered into Restclient.getDownload(String downloadId, String customerId)");
    	}
    	
    	CacheKey key = new CacheKey(CACHE_DOWNLOADS_KEY);
    	List<DownloadInfo> downloadInfos = (List<DownloadInfo>) manager.get(key);
    	if (CollectionUtils.isEmpty(downloadInfos)) {
    		downloadInfos = getDownloadsFromServer(customerId);
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
    
    public ClientResponse createDownloads(List<DownloadInfo> downloadInfo, String customerId) throws PhrescoException {
    	if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createDownloadInfo(List<DownloadInfo> downloadInfo)");
        }
    	
    	RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_ADMIN + REST_API_DOWNLOADS);
    	ClientResponse response = downloadClient.create(downloadInfo);
    	CacheKey key = new CacheKey(CACHE_DOWNLOADS_KEY);
    	manager.add(key, getDownloadsFromServer(customerId));
    	
    	return response;
    }

    public void updateDownload(DownloadInfo downloadInfo, String downloadId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.updateDownload(DownloadInfo downloadInfo, String downloadId)");
        }
        
        RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_ADMIN + REST_API_DOWNLOADS);
        downloadClient.setPath(downloadId);
        GenericType<DownloadInfo> genericType = new GenericType<DownloadInfo>() {};
        downloadClient.updateById(downloadInfo, genericType);
        CacheKey key = new CacheKey(CACHE_DOWNLOADS_KEY);
        manager.add(key, getDownloadsFromServer(customerId));
    }

    public ClientResponse deleteDownloadInfo(String downloadId, String customerId) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.deleteDownloadInfo(String downloadId)");
        }

        RestClient<DownloadInfo> downloadClient = getRestClient(REST_API_ADMIN + REST_API_DOWNLOADS);
        downloadClient.setPath(downloadId);
        ClientResponse response = downloadClient.deleteById();
        CacheKey key = new CacheKey(CACHE_DOWNLOADS_KEY);
        manager.add(key, getDownloadsFromServer(customerId));

        return response;
    }

    public ClientResponse createProject(ProjectInfo projectInfo) throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entered into ServiceManagerImpl.createProject(ProjectInfo projectInfo)");
        }

        RestClient<ProjectInfo> projectClient = getRestClient(REST_API_PROJECT);

        return projectClient.create(projectInfo, MEDIATYPE_ZIP, MediaType.APPLICATION_JSON);
    }
}