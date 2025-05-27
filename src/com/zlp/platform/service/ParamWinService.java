package com.zlp.platform.service;

import java.sql.SQLException;
import java.util.HashMap;

import javax.annotation.Resource; 
import javax.xml.ws.WebServiceContext;

import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.JSONProcessor;
import com.zlp.platform.common.NcpActionSupport;
import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.ServiceResultProcessor; 
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.IParamWinBaseDao;
import com.alibaba.fastjson.JSONObject;
import com.opensymphony.xwork2.ActionSupport;
 
public class ParamWinService  extends NcpActionSupport implements IParamWinService {

	@Resource    
    private WebServiceContext wsContext;   	 

	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}   
	 
	protected Session openDBSession() throws SQLException{ 
		return this.transactionManager.getSessionFactory().openSession(); 
	}
		
	private IParamWinBaseDao getParamWinDao(JSONObject requestObj){
		//如果没有定义名为dataName的bean，那么使用其基类 
		IParamWinBaseDao paramWinDao = null;
		if(requestObj.containsKey("paramWinName")){
			String paramWinName = requestObj.getString("paramWinName");
			paramWinDao = ContextUtil.containsBean(paramWinName) ? (IParamWinBaseDao)ContextUtil.getBean(paramWinName) :  (IParamWinBaseDao)ContextUtil.getBean("paramWinBaseDao");
		}
		else{
			paramWinDao = (IParamWinBaseDao)ContextUtil.getBean("paramWinBaseDao");
		}  
		return paramWinDao;
	} 

	 
	@Override
	public String getList() { 
		Session dbSession = null;
		try
		{
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IParamWinBaseDao paramWinBaseDao = this.getParamWinDao(requestObj); 
			dbSession = this.openDBSession();
			paramWinBaseDao.setDBSession(dbSession);
			
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			INcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			HashMap<String, Object> resultHash = paramWinBaseDao.getList(session, requestObj); 
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
	public String doOtherAction(){ 
		Session dbSession = null;
		try
		{
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IParamWinBaseDao paramWinBaseDao = this.getParamWinDao(requestObj); 
			dbSession = this.openDBSession();
			paramWinBaseDao.setDBSession(dbSession);
			
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			INcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			HashMap<String, Object> resultHash = paramWinBaseDao.doOtherAction(session, requestObj); 
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
