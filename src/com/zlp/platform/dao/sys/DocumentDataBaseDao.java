package com.zlp.platform.dao.sys;

import java.util.Date;
import java.util.HashMap;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.ValueType;

public class DocumentDataBaseDao extends DataBaseDao {	
	@Override	
	protected void onAdd(INcpSession session, JSONObject requestObj, HashMap<String, Object> resultObj) throws Exception{
		super.onAdd(session, requestObj, resultObj);
		JSONArray defaultValues =  new JSONArray();
		int newRowCount = (Integer) requestObj.get("newRowCount");
		for(int i = 0; i < newRowCount; i++){
			JSONObject oneRowDefaultValue = new JSONObject();

			oneRowDefaultValue.put("createuserid", session.getUserId());
			oneRowDefaultValue.put("createusername", session.getUserName());

			oneRowDefaultValue.put("createtime", ValueConverter.convertToString(new Date(), ValueType.Time));
			defaultValues.add(oneRowDefaultValue);
		}
		resultObj.put("defaultValues", defaultValues);
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
}
