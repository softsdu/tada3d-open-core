package com.zlp.platform.dao.system.impl;
  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.system.Sys_DownList; 
import com.zlp.platform.model.sysmodel.DownList;
import com.zlp.platform.model.sysmodel.DownListCollection;
import com.zlp.platform.model.sysmodel.DownListField;

public class Sys_DownListImpl extends DataBaseDao  implements Sys_DownList{

	private DownListCollection downListCollection = null;
	public void setDownListCollection(DownListCollection downListCollection){
		this.downListCollection = downListCollection;
	} 
	
	@Override 
	public HashMap<String, Object> doOtherAction(INcpSession session, JSONObject requestObj) throws RuntimeException{
		try{
			String actionName = requestObj.getString("actionName");
			JSONObject customParam = requestObj.getJSONObject("customParam");
			if("updateRuntimeModel".equals(actionName)){
				return generateJs(session, customParam);
			}
			return null;
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	private HashMap<String, Object> generateJs(INcpSession session, JSONObject customParam) throws Exception{
		String downListId = customParam.getString("downListId");	 
		DownList dl = downListCollection.reloadDownListFromDB(downListId);	 
		List<String> errors = this.validateDownListModel(dl);
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		if(errors.size() == 0) {	 
			resultMap.put("succeed", "true");
		}
		else { 		
			resultMap.put("succeed", "false");	
			resultMap.put("errors", errors);
		}
		return resultMap; 
	}
	
	private List<String> validateDownListModel(DownList dl){
		List<String> errors = new ArrayList<String>(); 		 		 
		List<String> fieldNames = new ArrayList<String>();
		List<DownListField> dlfs = dl.getDownListFields(); 
		for(int i=0;i<dlfs.size();i++){
			DownListField dlf = dlfs.get(i);
			if(fieldNames.contains(dlf.getName())){
				errors.add("定义了重名字段 "+dlf.getName()); 
			}
			else{
				fieldNames.add(dlf.getName());
			}
		}
		return errors;
	}	
	 
}
