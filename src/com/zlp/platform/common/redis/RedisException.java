package com.zlp.platform.common.redis;

//Redis异常 added by ls 20190801
public class RedisException extends Exception {
	private String errorCode = null;
	public String getErrorCode(){
		return this.errorCode;
	}
	private void setErrorCode(String errorCode){
		this.errorCode = errorCode; 
	}
	public RedisException(String errorCode, String errorInfo){
		super(errorInfo);
		this.setErrorCode(errorCode);
	}
	public RedisException(String errorCode, String errorInfo, Exception ex){
		super(errorInfo, ex);
		this.setErrorCode(errorCode);
	}
}
