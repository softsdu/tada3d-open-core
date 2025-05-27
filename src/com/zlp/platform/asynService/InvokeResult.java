package com.zlp.platform.asynService;

//异步调用结果
public class InvokeResult {
	private InvokeStatusType statusType;

	public InvokeStatusType getStatusType() {
		return statusType;
	} 
 
	public void setStatusType(InvokeStatusType statusType) {
		this.statusType = statusType;
	}
	
	
	private String message;
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public InvokeResult(){
	}
	
	public InvokeResult(InvokeStatusType statusType, String message){
		this.setStatusType(statusType);
		this.setMessage(message);
	}
	
	public InvokeResult(InvokeStatusType statusType, Exception ex){
		this.setStatusType(statusType);
		if(ex != null){
			StringBuilder message = new StringBuilder(ex.toString());
			StackTraceElement[] traces = ex.getStackTrace();
			for(StackTraceElement trace : traces){
				message.append("\r\n>" + trace.toString());
			}
			this.setMessage(message.toString());
		}
	}
}
