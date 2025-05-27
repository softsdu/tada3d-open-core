package com.zlp.platform.schedule;
 
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;

import com.alibaba.fastjson.JSONObject;
import com.nova.frame.utils.SecurityUtils;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.constants.ZlpState;
import com.zlp.platform.core.ConfigContext;
import com.zlp.platform.dao.db.IDBParserAccess; 
import com.zlp.platform.dao.sys.DataBaseDao; 

public class Skd_JobImpl extends DataBaseDao {

	private IJobManager jobManager = null;
	public IJobManager getJobManager() {
		return jobManager;
	}
	public void setJobManager(IJobManager jobManager) {
		this.jobManager = jobManager;
	}
	
 	
	//根据json获取数据
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	
	@Override
	protected HashMap<String, Object> deleteCore(INcpSession session, JSONObject requestObj) throws Exception{ 
		if(requestObj.containsKey("deleteRows")) {
			JSONObject rowIdToIdValues = requestObj.getJSONObject("deleteRows");     
			for(Object id : rowIdToIdValues.values().toArray()){
				String jobId = (String)id;
				String updateJobSql = "update skd_job set isactive = 'N', isdeleted = 'Y' where id = " + SysConfig.getParamPrefix() + "jobid";
				HashMap<String, Object> p2vs = new HashMap<String, Object>();
				p2vs.put("jobid", jobId);
				this.dBParserAccess.update(this.getDBSession(), updateJobSql, p2vs);
				this.jobManager.deleteWaitingJobInstance(this.getDBSession(), jobId);
			}
		}
			
	    HashMap<String, Object> resultHash=new HashMap<String, Object>();
	    return resultHash;
	}
	
	@Override
	protected void beforeSave(INcpSession session, JSONObject requestObj) throws Exception{ 
	    JSONObject insertRowsObj = requestObj.getJSONObject("insert");
	    int insertRowCount = insertRowsObj.size();
	    Object[] insertRowIds = insertRowsObj.keySet().toArray();
	    for(int i = 0;i < insertRowCount; i++){	    	
	    	String insertRowId = (String)insertRowIds[i];
	    	JSONObject insertRowObj = insertRowsObj.getJSONObject(insertRowId); 
	    	insertRowObj.put("isdeleted", "N");
	    }
	}
	
	@Override
	protected void afterSave(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{ 
	    JSONObject id2RowIds = (JSONObject) resultHash.get("idValueToRowIds");
	    Session dbSession = this.getDBSession();
	    for(Object jobIdObj : id2RowIds.keySet()){ 
	    	String jobId = (String)jobIdObj;
	    	
	    	this.jobManager.deleteWaitingJobInstance(dbSession, jobId);	    	
	    	boolean canRun = this.jobManager.checkCanRun(dbSession, jobId);
	    	if(canRun){
		    	String timeConfig = this.jobManager.getTimeConfig(dbSession, jobId);
		    	List<String> errors = this.jobManager.checkTimeConfig(timeConfig);	  
		    	if(errors.size() > 0){
		    		throw new Exception("TimeConfig error, timeConfig = " + timeConfig + ", jobId = " + jobId);
		    	}
		    	else {
		    		this.jobManager.createJobInstance(dbSession, jobId);
		    	}
	    	}
	    }
	}
}
