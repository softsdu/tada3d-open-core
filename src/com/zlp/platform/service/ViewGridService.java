package com.zlp.platform.service;

import java.sql.SQLException;
import java.util.HashMap; 

import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager; 
import com.zlp.platform.common.JSONProcessor;
import com.zlp.platform.common.NcpActionSupport;
import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.ServiceResultProcessor;
import com.zlp.platform.common.util.CommonFunction;
import com.zlp.platform.dao.sys.ContextUtil; 
import com.zlp.platform.dao.sys.ISheetBaseDao;
import com.zlp.platform.dao.sys.IUserDefinedFeatureDao;
import com.alibaba.fastjson.JSONObject;
import com.opensymphony.xwork2.ActionSupport;
 
public class ViewGridService extends NcpActionSupport implements IViewGridService {
 
	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}   
	 
	protected Session openDBSession() throws SQLException{ 
		return this.transactionManager.getSessionFactory().openSession(); 
	} 

	private IUserDefinedFeatureDao userDefinedFeatureDao; 
	public void setUserDefinedFeatureDao(IUserDefinedFeatureDao userDefinedFeatureDao) {
		this.userDefinedFeatureDao = userDefinedFeatureDao;
	}   
	
	
	@Override
	public String saveComplexQuery(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam); 
			
			String userId = session.getUserId();
			String featureName = CommonFunction.decode(requestObj.getString("featureName"));   
			String modelName = CommonFunction.decode(requestObj.getString("modelName"));   
			String description = CommonFunction.decode(requestObj.getString("description"));   
			String content = requestObj.getString("content");   

			dbSession = this.openDBSession(); 
			this.userDefinedFeatureDao.setDBSession(dbSession);
			this.userDefinedFeatureDao.saveFeature(userId, featureName, modelName, description, content);
			//在此处实现保存复杂查询条件及值

			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("saveComplexQuery", "保存复杂查询条件", ex);
			this.addResponse(ncpEx.toJsonString()); 	 	 
		}	 
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}
		return ActionSupport.SUCCESS;	
	}
	
	@Override
	public String getComplexQuery(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam); 

			String userId = session.getUserId();
			String featureName = CommonFunction.decode(requestObj.getString("featureName"));   
			String modelName = CommonFunction.decode(requestObj.getString("modelName"));    

			dbSession = this.openDBSession(); 
			this.userDefinedFeatureDao.setDBSession(dbSession);
			String content = this.userDefinedFeatureDao.getFeatureContent(userId, featureName, modelName);
			
			if(content == null){
				content = this.userDefinedFeatureDao.getGlobalDefaultFeatureContent(featureName, modelName);
			} 
			
			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			resultHash.put("complexQueryContent", content);
			
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("saveComplexQuery", "保存复杂查询条件", ex);
			this.addResponse(ncpEx.toJsonString()); 	 	 
		}	 
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}
		return ActionSupport.SUCCESS;	
	} 
}
