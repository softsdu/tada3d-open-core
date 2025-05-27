package com.zlp.platform.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;  
import com.zlp.platform.common.JSONProcessor;
import com.zlp.platform.common.NcpActionSupport;
import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.ServiceResultProcessor;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.IAccessoryDao;
import com.alibaba.fastjson.JSONObject;
import com.opensymphony.xwork2.ActionSupport;
 
public class AccessoryService extends NcpActionSupport implements IAccessoryService {
	private static Logger logger=Logger.getLogger(AccessoryService.class);

	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}   
	 
	protected Session openDBSession() throws SQLException{ 
		return this.transactionManager.getSessionFactory().openSession(); 
	}
	
	//附件处理
	private IAccessoryDao accessoryDao; 
	public void setAccessoryDao(IAccessoryDao accessoryDao) {
		this.accessoryDao = accessoryDao;
	}   
	
	@Override
	public String getFileCountByFilter(){
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);   
			String filterType = requestObj.getString("filterType");   
			String filterValue = requestObj.getString("filterValue");  
			dbSession = this.openDBSession();
			this.accessoryDao.setDBSession(dbSession);
			int fileCount = this.accessoryDao.getFileCountByFilter(filterType, filterValue);
			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			resultHash.put("fileCount", fileCount);
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getFileCountByFilter", "获取附件个数未成功", ex);
			this.addResponse(ncpEx.toJsonString()); 	 	 
		}	 
		finally{
			dbSession.close();
		}
		return ActionSupport.SUCCESS;	
	}
	
	@Override
	public String getAccessoryIds(){
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);   
			String filterType = requestObj.getString("filterType");   
			String filterValue = requestObj.getString("filterValue");  
			dbSession = this.openDBSession();
			this.accessoryDao.setDBSession(dbSession);
			String[] ids = this.accessoryDao.getAccessoryIds(filterType, filterValue);
			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			resultHash.put("ids", ids);
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getFileCountByFilter", "获取附件个数未成功", ex);
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
	public String deleteAccessory(){
		Session dbSession = null;
		try
		{
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);   
			String id = (String)ValueConverter.convertToObject(requestObj.getString("id"), ValueType.String);  
			dbSession = this.openDBSession();
			this.accessoryDao.setDBSession(dbSession);
			this.accessoryDao.deleteAccessory(id);
			HashMap<String, Object> resultHash = new HashMap<String, Object>(); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("deleteAccessory", "删除附件失败", ex);
			this.addResponse(ncpEx.toJsonString()); 	 	 
		}	 
		finally{ 
			if(dbSession != null){
				dbSession.close();
			}
		}
		return ActionSupport.SUCCESS;	
	}

	/**
	 * 下载附件  added by liyh 20190617
	 */
	public void downloadAccessory() {
		InputStream in = null;
		OutputStream out = null;
		HttpServletRequest request = this.getHttpRequest();
		Session dbSession = null;
		try {
			String accessoryId = request.getParameter("accessoryId");
			dbSession = this.openDBSession();
			this.accessoryDao.setDBSession(dbSession); 
			String path = this.accessoryDao.getFilePathById(accessoryId);
			File file = new File(path);
			// 取得文件名。
			String filename = file.getName();
			// 取得文件的后缀名。
			filename = new String(filename.getBytes(), "ISO-8859-1");
			// 以流的形式下载文件。
			this.getHttpResponse().reset();
			this.getHttpResponse().setContentType(this.getHttpRequest().getSession().getServletContext().getMimeType(filename));
			this.getHttpResponse().setHeader("Content-Disposition", "attachment;filename="+filename); 
	        in = new FileInputStream(file);  
	        out = this.getHttpResponse().getOutputStream();  
	              
            byte[] b = new byte[1024];
            int length = 0;
            while((length = in.read(b)) != -1)  {  
                out.write(b,0,length);  
            }

		}catch (Exception ex) {
			ex.printStackTrace();
			NcpException ncpEx = new NcpException("downloadAccessory", "下载文件出错!", ex);
			this.addResponse(ncpEx.toJsonString());
		} 
		finally{
			if(in != null){
				try{
					in.close();
					in = null;					
				}catch(Exception ex){
					
				}
			}
			if(out != null){
				try{
					out.close();
					out = null;			
				}catch(Exception ex){
					
				} 
			}
			if (dbSession != null) {
				dbSession.close();
			}
		}
	}

	//保存附件记录ID值到业务表 modified by ls 202203
	@Override
	public String saveAccessoryIdStrToDB(){
		Session dbSession = null;
		try {
			logger.info("saveAccessoryIdStrToDB:"+requestParam);

			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);

			String idValue = requestObj.getString("idValue");
			String tableName = requestObj.getString("tableName");
			String fieldName = requestObj.getString("fieldName");
			String fileIdsStr = requestObj.getString("fileIds");

			dbSession = this.openDBSession();
			this.accessoryDao.setDBSession(dbSession);
			this.accessoryDao.saveAccessoryIdStrToDB(idValue, tableName,fieldName,fileIdsStr);

			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch (Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
			NcpException ncpEx = new NcpException("saveAccessoryIdStrToDB", "保存附件记录ID值到业务表",ex);
			this.addResponse(ncpEx.toJsonString());
		}
		finally {
			if (dbSession != null) {
				dbSession.close();
			}
		}
		return ActionSupport.SUCCESS;
	}



	//删除没有关联的附件记录（打删除标记）
	@Override
	public String deleteUnrelatedAccessory(){
		Session dbSession = null;
		try {
			logger.info("deleteUnrelatedAccessory:" + requestParam);

			dbSession = this.openDBSession();
			this.accessoryDao.setDBSession(dbSession);
			this.accessoryDao.deleteUnrelatedAccessory();

			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch (Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
			NcpException ncpEx = new NcpException("deleteUnrelatedAccessory", "删除没有关联的附件记录（打删除标记）失败",ex);
			this.addResponse(ncpEx.toJsonString());
		}
		finally {
			if (dbSession != null) {
				dbSession.close();
			}
		}
		return ActionSupport.SUCCESS;
	}



	//备份所有附件
	@Override
	public String backupAllAccessoryFiles(){
		Session dbSession = null;
		try {
			logger.info("backupAllAccessoryFiles:" + requestParam);

			dbSession = this.openDBSession();
			this.accessoryDao.setDBSession(dbSession);
			JSONObject resultJson = this.accessoryDao.backupAllAccessoryFiles();

			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			resultHash.put("backup", resultJson);
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch (Exception ex) {
			logger.error(ex);
			ex.printStackTrace();
			NcpException ncpEx = new NcpException("backupAllAccessoryFiles", "备份附件失败",ex);
			this.addResponse(ncpEx.toJsonString());
		}
		finally {
			if (dbSession != null) {
				dbSession.close();
			}
		}
		return ActionSupport.SUCCESS;
	}

}
