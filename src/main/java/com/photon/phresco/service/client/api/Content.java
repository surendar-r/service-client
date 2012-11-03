package com.photon.phresco.service.client.api;

import java.util.Date;

import com.sun.jersey.core.header.ContentDisposition;

public class Content extends ContentDisposition {
	
	public enum Type {
		JSON, ARCHETYPE, JAR, ZIP, FEATURE, ICON
	}

    public Content(Type type, String fileName, Date creationDate,
            Date modificationDate, Date readDate, long size) {
        super(type.name(), fileName, creationDate, modificationDate, readDate, size);
    }
   
    
//    @Override
//    public boolean equals(Object obj) {
//        Content content = (Content) obj;
//        if (getType().equals(content.getType()) && getFileName().equals(content.getFileName())) {
//            return true;
//        }
//        return super.equals(obj);
//    }
//    
//    @Override
//    public String toString() {
//        return "Content [ type: " + getType() + ", fileName: " + getFileName() + "];";
//    }
}