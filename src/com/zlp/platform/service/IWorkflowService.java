package com.zlp.platform.service;
 
public interface IWorkflowService extends IServiceInterface  { 
 
	String saveWorkflow();
 
	String getWorkflow(); 
	
	String getDocFields();

	String deleteWorkflow();
}
