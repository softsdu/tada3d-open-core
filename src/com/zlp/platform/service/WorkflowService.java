package com.zlp.platform.service;

import java.io.UnsupportedEncodingException;
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
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.model.sysmodel.DataField;
import com.zlp.platform.workflow.definition.IWorkflowDefineDao;
import com.zlp.platform.workflow.definition.Wf_Workflow;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opensymphony.xwork2.ActionSupport;
 
public class WorkflowService extends NcpActionSupport implements IWorkflowService {
 
	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}   
	 
	protected Session openDBSession() throws SQLException{ 
		return this.transactionManager.getSessionFactory().openSession(); 
	}  

	private IWorkflowDefineDao workflowDefineDao; 
	public void setWorkflowDefineDao(IWorkflowDefineDao workflowDefineDao) {
		this.workflowDefineDao = workflowDefineDao;
	}   
		
	@Override
	public String saveWorkflow(){
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam); 
			JSONObject workflowJson = requestObj.getJSONObject("workflow");
			
			Wf_Workflow workflow = new Wf_Workflow();
			workflow.initByJson(workflowJson);			

			dbSession = this.openDBSession();  
			this.workflowDefineDao.setDBSession(dbSession);
			try{
				String workflowId = this.workflowDefineDao.saveWorkflow(session, workflow);
				
				HashMap<String, Object> resultHash = new HashMap<String, Object>();
				resultHash.put("succeed", true);
				resultHash.put("workflowId", workflowId);
				String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
				this.addResponse(resultString);
			}
			catch(Exception e){
	        	e.printStackTrace();
				HashMap<String, Object> resultHash = new HashMap<String, Object>();
				resultHash.put("succeed", false);
				resultHash.put("errorMessage", e.getMessage());
				String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
				this.addResponse(resultString);
			} 
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("saveWorkflow", "保存流程信息失败", ex);
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
	public String getWorkflow(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);  
			String workflowId = requestObj.getString("workflowId");    
			dbSession = this.openDBSession();  
			this.workflowDefineDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = new HashMap<String, Object>(); 
			
			Wf_Workflow newWorkflow = this.workflowDefineDao.getWorkflow(workflowId);
			JSONObject newWorkflowJson = newWorkflow.toJson();
			
			JSONArray fieldObjArray = this.getDocFields(newWorkflow.getDocTypeId());  
			resultHash.put("docFields", fieldObjArray);			
 
			resultHash.put("workflow", newWorkflowJson);			
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getWorkflow", "获取流程信息失败", ex);
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
	public String deleteWorkflow(){ 
		Session dbSession = null;
		try
		{
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);  
			String workflowId = requestObj.getString("workflowId");    
			dbSession = this.openDBSession();  
			this.workflowDefineDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = new HashMap<String, Object>(); 
			this.workflowDefineDao.deleteWorkflow(workflowId);			
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("deleteWorkflow", "删除流程失败", ex);
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
	public String getDocFields(){
		Session dbSession = null;
		try
		{
			dbSession = this.openDBSession();  
			this.workflowDefineDao.setDBSession(dbSession);
			
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);  
			String docTypeId = requestObj.getString("docTypeId");  
			this.workflowDefineDao.setDBSession(dbSession);
			HashMap<String, Object> resultHash = new HashMap<String, Object>(); 
			JSONArray fieldObjArray = this.getDocFields(docTypeId);  
			resultHash.put("docFields", fieldObjArray);			
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getDocFieldTypes", "获取单据字段列表失败", ex);
			this.addResponse(ncpEx.toJsonString()); 	 	 
		}	  
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}
		return ActionSupport.SUCCESS;	
	} 
	
	public JSONArray getDocFields(String docTypeId) throws UnsupportedEncodingException{  
		HashMap<String, DataField> fields = this.workflowDefineDao.getDocFields(docTypeId); 
		JSONArray fieldObjArray = new JSONArray();
		if(fields != null){
			for(DataField field : fields.values()){
				JSONObject fieldObj = new JSONObject();
				fieldObj.put("name", CommonFunction.encode(field.getName()));
				fieldObj.put("valueType", field.getValueType());
				fieldObj.put("description", CommonFunction.encode(field.getDisplayName()));
				fieldObjArray.add(fieldObj);
			}
		}
		return fieldObjArray; 
	} 
}