package com.zlp.platform.dao.sys;
 
import org.hibernate.Session; 

public interface IUserDefinedFeatureDao { 
	void setDBSession(Session dbSession);
	void saveFeature(String userId, String featureName, String modelName, String description, String content);
	String getGlobalDefaultFeatureContent(String featureName, String modelName);
	String getFeatureContent(String userId, String featureName, String modelName);  
}
