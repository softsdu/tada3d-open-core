package com.zlp.platform.dataManagement.importExportDefinition;
 
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.constants.ZlpState;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.system.D_User;
import com.zlp.platform.model.sysmodel.Data;

//输入输出定义dao类
public class Dm_ImportExportDefinitionImpl extends DataBaseDao {	
	//根据json获取数据
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	
	@Override 
	public HashMap<String, Object> doOtherAction(INcpSession session, JSONObject requestObj) throws RuntimeException {
		try{
			String actionName = requestObj.getString("actionName");
			JSONObject customParam = requestObj.getJSONObject("customParam");
			if("updateRuntime".equals(actionName)){
				return updateRuntime(session, customParam);
			}
			return null;
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
	
	//根据data的定义，生成前端需要的js
	private HashMap<String, Object> updateRuntime(INcpSession session, JSONObject customParam) throws Exception{
		List<String> errors = new ArrayList<String>();
		
		String id = customParam.getString("importExportDefinitionId");	  
		ImportExportDefinition ieDef = this.generateImportExportDefinitionObject(id);
		
		//更新data模型和view模型
		//isMultiValue的字段，需要生成子表记录字段的keyvalue值
		
		//更新数据库，调用data模型更新数据库的方法实现
		//RefreshDBStructure(importDef);
		
		//自动生成一些js脚本，供客户端使用，例如下拉值（允许静态的listOption）

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
	
	@Override
	protected void beforeSave(INcpSession session, JSONObject requestObj) throws Exception{
	    //新增	    
	    JSONObject insertRowsObj = requestObj.getJSONObject("insert"); 
	    int insertRowCount = insertRowsObj.size();
	    Object[] insertRowIds = insertRowsObj.keySet().toArray();
	    for(int i = 0;i < insertRowCount; i++){	    	
	    	String insertRowId = (String)insertRowIds[i];
	    	JSONObject insertRowObj = insertRowsObj.getJSONObject(insertRowId); 

	    	String currentUserId = session.getUserId();
	    	String currentUserCode = session.getUserCode();
	    	insertRowObj.put("ownerid", currentUserId);
	    	
	    	String definitionName = insertRowObj.getString("name");  
	    	String definitionCode = currentUserCode + "_" + definitionName;
	    	insertRowObj.put("code", definitionCode);
	    	
	    	Date dt = new Date();
	    	String createTime = ValueConverter.convertToString(dt, ValueType.Time);
	    	insertRowObj.put("createtime", createTime);
	    }

	    //更新	    
	    JSONObject updateRowsObj = requestObj.getJSONObject("update"); 
	    int updateRowCount = updateRowsObj.size();
	    Object[] updateRowIds = updateRowsObj.keySet().toArray();
	    for(int i = 0;i < updateRowCount; i++){	    	
	    	String updateRowId = (String)updateRowIds[i];
	    	JSONObject updateRowObj = updateRowsObj.getJSONObject(updateRowId); 

	    	String id = updateRowObj.getString("id");
	    	String userId = getOwnerIdFromDb(id);
	    	String currentUserId = session.getUserId();
	    	if(userId == null){
	    		throw new Exception("当前记录不存在，或者原纪录没有指定所有者!");
	    	}
	    	else if(!userId.equals(currentUserId)){
	    		throw new Exception("当前用户不是此数据的数据所有者!");
	    	}
	    	else{
	    		//不允许重新定义编码
		    	//String currentUserCode = session.getUserCode();
		    	//String definitionName = updateRowObj.getString("name");  
		    	//String definitionCode = currentUserCode + "_" + definitionName;
		    	//updateRowObj.put("code", definitionCode);
		    	
		    	Date dt = new Date();
		    	String modifyTime = ValueConverter.convertToString(dt, ValueType.Time);
		    	updateRowObj.put("modifytime", modifyTime);
	    	}
	    }
	} 
	private String getOwnerIdFromDb(String definitionId){
	    String sql = "select d.ownerid as ownerid from dm_importexportdefinition d where d.id = " + SysConfig.getParamPrefix() + "id";
    	HashMap<String, Object> p2vs = new HashMap<String, Object>();
    	p2vs.put("id", definitionId);
    	String ownerId = (String)this.dBParserAccess.getSingleValue(this.getDBSession(), sql, p2vs);
    	return ownerId;
	}
	private DataRow getDefinitioRowFromDb(String definitionId){
	    String sql = "select d.id as id, d.code as code, d.definitionxml as definitionxml from dm_importexportdefinition d where d.id = " + SysConfig.getParamPrefix() + "id";
    	HashMap<String, Object> p2vs = new HashMap<String, Object>();
    	p2vs.put("id", definitionId);
    	DataTable dt = this.dBParserAccess.getMultiLineValues(this.getDBSession(), sql, p2vs, new String[]{"id", "code", "definitionxml"}, new ValueType[]{ValueType.String, ValueType.String, ValueType.String});
    	List<DataRow> rows = dt.getRows();
    	return rows.get(0);
	}
	
	//更新内存中存储的导入模型
	private ImportExportDefinition generateImportExportDefinitionObject(String id) throws Exception{
		DataRow row = getDefinitioRowFromDb(id);
		String code = row.getStringValue("code");
		String xml = row.getStringValue("definitionxml");
		ImportExportDefinition ieDef = new ImportExportDefinition(id, code, xml); 
		return ieDef;
	}
	
	//更新新题模型，包括data模型和view模型
	private void RefreshSystemModel(){
		
	}
	
	//更新数据库表结构（尚未完成的代码）
	private void RefreshDBStructure(ImportExportDefinition ieDef) throws Exception{
		
		String dbName = SysConfig.getPropertyFileValue("db.properties", "jdbc.db");
		String tableName = ieDef.getCode();
		//！！！！！！！！！！！！！！此处通过扩展data模型功能实现，即实现根据data模型更新数据库的功能。
		//获取表结构
		String sql = "select table_name as tablename,"
					+ "column_name as columnname, "
					+ "data_type as datatype, "
					+ "character_maximum_length as maxlength "
					+ "from information_schema.COLUMNS where table_name = '" + SysConfig.getParamPrefix() +"tableName"
					+ "and table_schema=" + SysConfig.getParamPrefix() +"dbName";

    	HashMap<String, Object> p2vs = new HashMap<String, Object>();
    	p2vs.put("tableName", tableName);
    	p2vs.put("dbName", dbName);

    	DataTable allDBFields = this.dBParserAccess.getMultiLineValues(this.getDBSession(), 
    			sql, 
    			p2vs, 
    			new String[]{"tablename", "columnname", "datatype", "maxlength"}, 
    			new ValueType[]{ValueType.String, ValueType.String, ValueType.Decimal});
    	
    	List<Field> allDefFields = ieDef.getFieldList();
    	
		//比较字段，更新数据库，其中multi的字段会自动生成子表
	}
	
	//生成jsp页面和js文件，用于查询数据
	private void GeneratePage(){
		
	}
	
	//生成移动终端页面，用于查询数据
	private void GenerateMobilePage(){
		
	}
	
}
