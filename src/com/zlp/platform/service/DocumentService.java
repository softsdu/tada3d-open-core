package com.zlp.platform.service;

import java.sql.SQLException;
import java.util.HashMap; 

import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.JSONProcessor;
import com.zlp.platform.common.NcpActionSupport;
import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.ServiceResultProcessor;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.SelectSqlParser;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.IDataBaseDao;
import com.zlp.platform.dao.sys.IDocumentBaseDao;
import com.zlp.platform.dao.sys.ISheetBaseDao;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import com.alibaba.fastjson.JSONObject;
import com.opensymphony.xwork2.ActionSupport;
 
//单据服务类，用于编辑保存单据、执行流程等
public class DocumentService extends SheetService implements IDocumentService {
	private IDocumentBaseDao getDocumentDao(JSONObject requestObj){
		//如果没有定义名为sheetName的bean，那么使用其基类 
		IDocumentBaseDao documentDao = null;
		if(requestObj.containsKey("sheetName")){
			String sheetName = requestObj.getString("sheetName");
			documentDao = ContextUtil.containsBean(sheetName) ? (IDocumentBaseDao)ContextUtil.getBean(sheetName) :  (IDocumentBaseDao)ContextUtil.getBean("documentBaseDao");
		}
		else{
			documentDao = (IDocumentBaseDao)ContextUtil.getBean("documentBaseDao");
		}  
		return documentDao;
	}  
	
	//保存单据
	@Override
	public String save(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDocumentBaseDao documentDao = this.getDocumentDao(requestObj); 
			dbSession = this.openDBSession();
			documentDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = documentDao.saveWithTx(session, requestObj);
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

	//删除单据
	@Override
	public String delete(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDocumentBaseDao documentDao = this.getDocumentDao(requestObj); 
			dbSession = this.openDBSession();
			documentDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = documentDao.deleteWithTx(session, requestObj); 
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

	//筛选单据
	@Override
	public String select(){ 
		Session dbSession = null;
		try
		{ 
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDocumentBaseDao documentDao = this.getDocumentDao(requestObj); 
			dbSession = this.openDBSession();
			documentDao.setDBSession(dbSession); 
			HashMap<String, Object> resultHash = documentDao.select(session, requestObj);  
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

	//提交单据
	@Override
	public String submit(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDocumentBaseDao documentDao = this.getDocumentDao(requestObj); 
			dbSession = this.openDBSession();
			documentDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = documentDao.submitWithTx(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("submit", "提交单据未成功", ex);
			this.addResponse(ncpEx.toJsonString()); 	 	 
		}	
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		} 
		return ActionSupport.SUCCESS;	
	}   
	
	//单据审批通过
	@Override
	public String drive(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDocumentBaseDao documentDao = this.getDocumentDao(requestObj); 
			dbSession = this.openDBSession();
			documentDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = documentDao.driveWithTx(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("submit", "审批单据未成功", ex);
			this.addResponse(ncpEx.toJsonString()); 	 	 
		}	
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		} 
		return ActionSupport.SUCCESS;	
	}   
	
	//取回单据
	@Override
	public String getBack(){
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDocumentBaseDao documentDao = this.getDocumentDao(requestObj); 
			dbSession = this.openDBSession();
			documentDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = documentDao.getBackWithTx(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getBack", "取回单据未成功", ex);
			this.addResponse(ncpEx.toJsonString()); 	 	 
		}	
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		} 
		return ActionSupport.SUCCESS;	
	}     
	
	//退回单据
	@Override
	public String sendBack(){
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDocumentBaseDao documentDao = this.getDocumentDao(requestObj); 
			dbSession = this.openDBSession();
			documentDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = documentDao.sendBackWithTx(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("sendBack", "退回单据未成功", ex);
			this.addResponse(ncpEx.toJsonString()); 	 	 
		}	
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		} 
		return ActionSupport.SUCCESS;	
	}	 
	
	//获取对应的所有用户日志
	@Override
	public String getAllUserLogs(){
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDocumentBaseDao documentDao = this.getDocumentDao(requestObj); 
			dbSession = this.openDBSession();
			documentDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = documentDao.getAllUserLogs(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getAllUserLogs", "获取所有审批日志未成功", ex);
			this.addResponse(ncpEx.toJsonString()); 	 	 
		}	
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		} 
		return ActionSupport.SUCCESS;	
	}	
	
	//获取详细状态信息
	@Override
	public String getDetailStatus(){
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDocumentBaseDao documentDao = this.getDocumentDao(requestObj); 
			dbSession = this.openDBSession();
			documentDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = documentDao.getDetailStatus(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getDetailStatus", "获取单据详细状态", ex);
			this.addResponse(ncpEx.toJsonString()); 	 	 
		}	
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		} 
		return ActionSupport.SUCCESS;	
	}	
	
	//获取可以退回到的节点
	@Override
	public String getCanBackToSteps(){
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			IDocumentBaseDao documentDao = this.getDocumentDao(requestObj); 
			dbSession = this.openDBSession();
			documentDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = documentDao.getAllUserLogs(session, requestObj); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getCanBackToSteps", "获取可退回的步骤未成功", ex);
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
