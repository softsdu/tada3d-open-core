package com.zlp.platform.common; 

import javax.servlet.ServletException;

public class NcpNonePurviewException extends ServletException {    
	private static final long serialVersionUID = 2931745272902787118L;
	
	private String errorCode ="";
	public String getErrorCode(){
		return this.errorCode;
	}
	
	public NcpNonePurviewException(String errorCode, String errorInfo, Exception ex){
		super(errorInfo, ex);
		this.errorCode = errorCode;
	} 	
}
