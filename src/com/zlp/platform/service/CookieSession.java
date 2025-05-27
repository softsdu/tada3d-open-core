package com.zlp.platform.service;

//Cookie里存储的Session信息对象 added by ls 20190801
public class CookieSession {
	private String cSessionValue = "";
	public String GetCSessionValue(){
		return this.cSessionValue;
	}
	
	public CookieSession(String cSessionValue){
		this.cSessionValue = cSessionValue;
	}
}
