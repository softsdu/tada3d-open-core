package com.zlp.platform.asynService;

import java.util.HashMap;

public interface IAsynInvokeBase {
	void setParameter(HashMap<String, Object> parameterValues);

	void setInvokeId(String invokeId);
	
	InvokeResult run() throws Exception ;
	
	InvokeResult check() throws Exception ;
	
	int getCheckWaitingSecond();
	
	int getMaxRuningSecond();
}
