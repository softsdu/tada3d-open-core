package com.zlp.platform.service;
 
public interface IDocumentService extends IServiceInterface  { 

	String save(); 
 
	String delete();  

	String submit(); 

	String getBack();

	String sendBack();

	String select();

	String getAllUserLogs();

	String getCanBackToSteps();

	String drive();

	String getDetailStatus(); 
}
