package com.zlp.platform.dao.sys;

import java.io.Serializable;

public class Role implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2874790605714356390L;
	private String id;
	private String code;
	private String name;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
}
