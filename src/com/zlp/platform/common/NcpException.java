package com.zlp.platform.common;

import java.util.HashMap;

public class NcpException extends Exception {
	private static final long serialVersionUID = 2316634619402200746L;

	private String errorCode = "";

	public String getErrorCode() {
		return this.errorCode;
	}

	public NcpException(String errorCode, String errorInfo) {
		super(errorInfo);
		this.errorCode = errorCode;
	}

	public NcpException(String errorCode, String errorInfo, Exception ex) {
		super(errorInfo, ex);
		this.errorCode = errorCode;
	}
	
	//递归获取所有的报错信息 added by ls 20190618
	public static String getFullErrorInfo(Exception ex){
		StringBuilder message = new StringBuilder(ex.getMessage());

		Throwable tempEx = ex.getCause(); 
		while(tempEx != null){
			message.append("\r\n" + tempEx.getLocalizedMessage());
			tempEx = tempEx.getCause();
		}
		return message.toString();
	}

	public String toJsonString() {
		//StringBuilder message = new StringBuilder(this.toString());
		StringBuilder message = new StringBuilder(this.getMessage());

		Throwable tempEx = this.getCause();
		while(tempEx != null){
			message.append("\r\n\r\n" + tempEx.getMessage());
			tempEx = tempEx.getCause();
		}

		HashMap<String, Object> json = new HashMap<String, Object>();
		json.put("code", this.getErrorCode());
		json.put("message", this.getMessage());
		json.put("detailMessage", message.toString());
        return JSONProcessor.mapToStr(json);
	}

}