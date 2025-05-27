package com.zlp.platform.workflow;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.HashMap; 
import com.zlp.platform.common.INcpSession; 
import com.zlp.platform.dao.sys.DataBaseDao; 
import com.zlp.platform.workflow.definition.StepStatusType;

//可退回节点Dao类
public class Wf_InstanceStepForBackListImpl extends DataBaseDao {		
	
	@Override
	protected void afterSelect(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{
		JSONArray rows = (JSONArray)((HashMap)resultHash.get("table")).get("rows");

		for(int i=0;i<rows.size();i++){
			JSONObject stepRow = rows.getJSONObject(i);
			StepStatusType statusType  = Enum.valueOf(StepStatusType.class,stepRow.getString("statustype"));
			if(statusType == StepStatusType.active || statusType == StepStatusType.suspended) {
				boolean canBackFrom = "Y".equals(stepRow.getString("canbackfrom"));
				if(!canBackFrom){
					rows.clear();
				}
			}
		}
		 
		JSONArray canBackToStepRows = new JSONArray();
		for(int i=0;i<rows.size();i++){
			JSONObject stepRow = rows.getJSONObject(i);
			StepStatusType statusType  = Enum.valueOf(StepStatusType.class,stepRow.getString("statustype"));
			if(statusType == StepStatusType.passed){
				boolean canBackFrom = "Y".equals(stepRow.getString("canbackfrom"));
				boolean canBackTo = "Y".equals(stepRow.getString("canbackto"));
				if(canBackTo){
					canBackToStepRows.add(stepRow);
					if(!canBackFrom){
						break;
					}
				}
				else{
					break;
				}				
			}
		}
		
		((HashMap)resultHash.get("table")).put("rows", canBackToStepRows);		
	}	
}
