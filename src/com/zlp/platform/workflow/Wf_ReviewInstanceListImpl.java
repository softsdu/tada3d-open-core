package com.zlp.platform.workflow;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession; 
import com.zlp.platform.dao.sys.DataBaseDao; 

//个人审批历史记录Dao类
public class Wf_ReviewInstanceListImpl extends DataBaseDao {	
	@Override
	protected void beforeSelect(INcpSession session, JSONObject requestObj) throws Exception{
		JSONObject userObj = new JSONObject();
		userObj.put("parttype", "field");
		userObj.put("field", "instloguserid");
		userObj.put("operator", "=");
		userObj.put("value", session.getUserId());
		
		JSONArray sysWhere = new JSONArray();
		sysWhere.add(userObj);
		
		requestObj.put("sysWhere", sysWhere);
	}	
}
