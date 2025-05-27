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
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.ITreeBaseDao;
import com.alibaba.fastjson.JSONObject;
import com.opensymphony.xwork2.ActionSupport;
 
public class TreeService extends NcpActionSupport implements ITreeService {

	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}   
	 
	protected Session openDBSession() throws SQLException{ 
		return this.transactionManager.getSessionFactory().openSession(); 
	}
	private ITreeBaseDao getTreeDao(JSONObject requestObj){
		//如果没有定义名为sheetName的bean，那么使用其基类 
		ITreeBaseDao treeDao = null;
		if(requestObj.containsKey("treeName")){
			String treeName = requestObj.getString("treeName");
			treeDao = ContextUtil.containsBean(treeName) ? (ITreeBaseDao)ContextUtil.getBean(treeName) :  (ITreeBaseDao)ContextUtil.getBean("treeBaseDao");
		}
		else{
			treeDao = (ITreeBaseDao)ContextUtil.getBean("treeBaseDao");
		}  
		return treeDao;
	} 
	
	@Override
	public String save(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			ITreeBaseDao treeDao = this.getTreeDao(requestObj); 
			dbSession = this.openDBSession();
			treeDao.setDBSession(dbSession); 
			HashMap<String, Object> resultHash = treeDao.saveWithTx(session, requestObj); 
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
			ITreeBaseDao treeDao = this.getTreeDao(requestObj); 
			dbSession = this.openDBSession();
			treeDao.setDBSession(dbSession); 
			HashMap<String, Object> resultHash = treeDao.deleteWithTx(session, requestObj); 
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
}
