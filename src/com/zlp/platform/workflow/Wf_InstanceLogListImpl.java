package com.zlp.platform.workflow;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;  
import com.zlp.platform.common.INcpSession; 
import com.zlp.platform.dao.sys.DataBaseDao; 
import com.zlp.platform.workflow.definition.OperateType;

//审批日志Dao类
public class Wf_InstanceLogListImpl extends DataBaseDao {	 
	@Override
	protected void beforeSelect(INcpSession session, JSONObject requestObj) throws Exception{
		String instanceId = requestObj.getString("otherRequestParam");
		
		JSONObject userObj = new JSONObject();
		userObj.put("parttype", "field");
		userObj.put("field", "instanceid");
		userObj.put("operator", "=");
		userObj.put("value", instanceId);
		
		JSONArray sysWhere = new JSONArray();
		sysWhere.add(userObj);
		
		requestObj.put("sysWhere", sysWhere);
	}	
	
	@Override
	protected void afterSelect(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{
		JSONArray rows = (JSONArray)((HashMap)resultHash.get("table")).get("rows");

		for(int i=0;i<rows.size();i++){
			JSONObject logRow = rows.getJSONObject(i);
			OperateType operateType = Enum.valueOf(OperateType.class, logRow.getString("operatetype"));
			String operateName = "";
			switch(operateType){
				case drive:{
					operateName = "审批";
				}
				break;
				case sendBack:{
					operateName = "退回";
				}
				break;
				case getBack:{
					operateName = "取回";
				}
				break;
				case submit:{
					operateName = "提交";
				}
				break;
				case delete:{
					operateName = "删除";
				}
				break;
				case autoDrive:{
					operateName = "自动处理";
				}
				break;
			}
			logRow.put("operatetype", operateName);
		} 
		
	}	
}
