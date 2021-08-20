package com.example.demo;

import java.io.Serializable;

public class FileItemDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	private String name;
	private String id;
	private String thumbnailLink;
	private String mimetype;
	private boolean isShared;
	private String kind;
	private String webViewLink;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getThumbnailLink() {
		return thumbnailLink;
	}
	public void setThumbnailLink(String thumbnailLink) {
		this.thumbnailLink = thumbnailLink;
	}
	public String getMimetype() {
		return mimetype;
	}
	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}
	public boolean isShared() {
		return isShared;
	}
	public void setShared(boolean isShared) {
		this.isShared = isShared;
	}
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public String getWebViewLink() {
		return webViewLink;
	}
	public void setWebViewLink(String webViewLink) {
		this.webViewLink = webViewLink;
	}
	

	
}
