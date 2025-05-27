package com.zlp.platform.asynService;
   
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.JSONProcessor;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess; 
import com.zlp.platform.dao.sys.SheetBaseDao;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;

//异步处理定义表单
public class AisServiceImpl extends SheetBaseDao{
 
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	
	//保存异步任务定义时，根据子表记录，自动生成parameterconfig字段值
	@Override
	protected void afterSave(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{
		Data aisServiceData = DataCollection.getData("ais_Service");
		Data aisServiceParameterData = DataCollection.getData("ais_ServiceParameter");
		Session dbSession = this.getDBSession(); 
	    JSONObject id2RowIds = (JSONObject) resultHash.get("idValueToRowIds"); 
    	for(Object idValueObj : id2RowIds.keySet()){
    		String idValue = (String)idValueObj;  
	    
    		JSONObject parameterConfigObj = new JSONObject();
    		JSONArray parameterArray = new JSONArray();
    		DataTable spTable = this.dBParserAccess.getDtByFieldValue(dbSession, aisServiceParameterData, "serviceid", "=", idValue);
    		List<DataRow> spRows = spTable.getRows();
    		for(DataRow spRow : spRows){
    			String name = spRow.getStringValue("name");
    			String valueType = spRow.getStringValue("valuetype");
    			String description = spRow.getStringValue("description");
        		JSONObject parameterObj = new JSONObject();
        		parameterObj.put("name", name);
        		parameterObj.put("valueType", valueType);
        		parameterObj.put("description", description);
        		parameterArray.add(parameterObj);
    		}
    		parameterConfigObj.put("parameterConfig", parameterArray);
    		
    		String parameterConfigStr = JSONProcessor.jsonToStr(parameterConfigObj);
    		
    		HashMap<String, Object> fieldValues = new HashMap<String, Object>();
    		fieldValues.put("parameterconfig", parameterConfigStr);
    		fieldValues.put("id", idValue);
    		this.dBParserAccess.updateByData(dbSession, aisServiceData, fieldValues, idValue);   
    		
    		this.refreshBackToClientInfo(idValue, parameterConfigStr, resultHash);
    	} 
	}	 
	
	//返回值中，增加parameterconfig字段值
	private void refreshBackToClientInfo(String id, String parameterConfigStr, HashMap<String,Object> resultHash){
		JSONArray rowJSONArray = (JSONArray)((HashMap)resultHash.get("table")).get("rows");
		for(Object rowObj : rowJSONArray){
			JSONObject rowJsonObj = (JSONObject)rowObj;
			String idValue = rowJsonObj.getString("id");
			if(id.equals(idValue)){
				rowJsonObj.put("parameterconfig", DataTable.processQuotes(parameterConfigStr));
			}
		}
	}
}
