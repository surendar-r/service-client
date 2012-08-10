package com.photon.phresco.service.client.impl;

import java.io.Serializable;

public class CacheKey implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String custId;

	private String api;
	
	public CacheKey(String custId, String api) {
		super();
		this.custId = custId;
		this.api = api;
	}
	
	public CacheKey(String api) {
		super();
		this.api = api;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((api == null) ? 0 : api.hashCode());
		result = prime * result + ((custId == null) ? 0 : custId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		CacheKey other = (CacheKey) obj;
		if (api == null) {
			if (other.api != null) {
				return false;
			}
		} else if (!api.equals(other.api)) {
			return false;
		}
		
		if (custId == null) {
			if (other.custId != null) {
				return false;
			}
		} else if (!custId.equals(other.custId)) {
			return false;
		}
		
		return true;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getApi() {
		return api;
	}

	public void setApi(String api) {
		this.api = api;
	}
	
}