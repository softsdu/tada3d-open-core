package com.zlp.platform.workflow;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession; 
import com.zlp.platform.dao.sys.DataBaseDao; 

//我的任务（审批窗口）Dao类
public class Wf_DriveInstanceListImpl extends DataBaseDao {	
	@Override
	protected void beforeSelect(INcpSession session, JSONObject requestObj) throws Exception{
		JSONObject userObj = new JSONObject();
		userObj.put("parttype", "field");
		userObj.put("field", "instuserid");
		userObj.put("operator", "=");
		userObj.put("value", session.getUserId());
		
		JSONArray sysWhere = new JSONArray();
		sysWhere.add(userObj);
		
		requestObj.put("sysWhere", sysWhere);
	}	
}
