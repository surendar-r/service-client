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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class EhCacheManager {

	/**
	 * The CacheManager provides us access to individual Cache instances
	 */
	private static final CacheManager CACHE_MANAGER = CacheManager.getInstance();

	private Cache cache;
	
	private static final String CACHE_CONFIG = "cacheConfig";
	
	public EhCacheManager() {
		// Load cache:
		cache = CACHE_MANAGER.getCache(CACHE_CONFIG);
	}

	public void add(CacheKey id, Object obj) {
		// Create an EHCache Element
		Element element = new Element(id, obj);
		// Add the element to the cache
		cache.put(element);
	}

	public Object get(CacheKey id) {
		// Retrieve the element that contains the requested id
		Element element = cache.get(id);
		if (element != null) {
			// Get the value out of the element and return
			return element.getValue();
		}

		// We don't have the object in the cache so return null
		return null;
	}
	
	public void resetCache() {
	    cache.removeAll();
	}
}