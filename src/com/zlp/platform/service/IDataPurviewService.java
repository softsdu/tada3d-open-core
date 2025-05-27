package com.zlp.platform.service;

import java.util.HashMap;

import org.hibernate.Session;  
 
public interface IDataPurviewService extends IServiceInterface {   
	
	String getRoleAndDataPurviewFactor();
	
	String getClause(Session dbSession, String userId, HashMap<String, String> aliasToFactors, HashMap<String, Object> returnSqlParams) throws Exception;
 
	String getSqlClause(String userId, HashMap<String, String> aliasToFactors, HashMap<String, Object> returnSqlParams) throws Exception;
}
