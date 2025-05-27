package com.zlp.platform.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager; 
import com.zlp.platform.common.JSONProcessor;
import com.zlp.platform.common.NcpActionSupport;
import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.ServiceResultProcessor;  
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.IDataBaseDao;
import com.alibaba.fastjson.JSONObject;
import com.opensymphony.xwork2.ActionSupport;

//added by liyh 20190605
import com.zlp.platform.util.token.UserToken;
import com.zlp.platform.util.token.TokenUtil;
  
public class DataService extends NcpActionSupport implements IDataService {
 

	private static final long serialVersionUID = 1L;
	private IDataBaseDao getDataDao(JSONObject requestObj){
		//如果没有定义名为dataName的bean，那么使用其基类 
		IDataBaseDao dataDao = null;
		if(requestObj.containsKey("dataName")){
			String dataName = requestObj.getString("dataName");
			dataDao = ContextUtil.containsBean(dataName) ? (IDataBaseDao)ContextUtil.getBean(dataName) :  (IDataBaseDao)ContextUtil.getBean("dataBaseDao");
		}
		else{
			dataDao = (IDataBaseDao)ContextUtil.getBean("dataBaseDao");
		}  
		return dataDao;
	}

	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}   
	 
	protected Session openDBSession() throws SQLException{ 
		return this.transactionManager.getSessionFactory().openSession(); 
	}
		
	@Override
	public String getInputStatus(){
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam); 
			IDataBaseDao dataDao = this.getDataDao(requestObj);  
			dbSession = this.openDBSession();
			dataDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = dataDao.getInputStatus(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getInputStatus", "更新录入框状态未成功", ex);
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
	public String getList() { 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), false);//modified by liyh 兼容APP访问
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDataBaseDao dataDao = this.getDataDao(requestObj); 
			dbSession = this.openDBSession();
			dataDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = dataDao.getList(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getList", "获取下拉数据未成功", ex);
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
	public String add(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDataBaseDao dataDao = this.getDataDao(requestObj); 
			dbSession = this.openDBSession();
			dataDao.setDBSession(dbSession); 
			HashMap<String, Object> resultHash = dataDao.add(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("add", "创建新纪录未成功", ex);
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
	public String select(){ 
		Session dbSession = null;
		try
		{ 
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDataBaseDao dataDao = this.getDataDao(requestObj); 
			dbSession = this.openDBSession();
			dataDao.setDBSession(dbSession); 
			HashMap<String, Object> resultHash = dataDao.select(session, requestObj);  
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("select", "查询数据未成功", ex);
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
	public String save(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDataBaseDao dataDao = this.getDataDao(requestObj); 
			dbSession = this.openDBSession();
			dataDao.setDBSession(dbSession); 
			HashMap<String, Object> resultHash = dataDao.saveWithTx(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("save", "保存记录未成功", ex);
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
	public String delete(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDataBaseDao dataDao = this.getDataDao(requestObj); 
			dbSession = this.openDBSession();
			dataDao.setDBSession(dbSession); 
			HashMap<String, Object> resultHash = dataDao.deleteWithTx(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("delete", "删除记录未成功", ex);
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
	public String doOtherAction(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDataBaseDao dataDao = this.getDataDao(requestObj); 
			dbSession = this.openDBSession();
			dataDao.setDBSession(dbSession); 
			HashMap<String, Object> resultHash = dataDao.doOtherAction(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("doOtherAction", "调用服务", ex);
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
	
