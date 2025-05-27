package com.zlp.mdl.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.FileOperate;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.util.CommonFunction;
import com.zlp.platform.constants.ZlpState;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;

//构件参数分类 add by ls 20230731
public class Mdl_PropertyCategoryImpl extends DataBaseDao implements IMdl_PropertyCategoryImpl {
	
  	private static Logger logger = Logger.getLogger(Mdl_PropertyCategoryImpl.class);  
	
	private IDBParserAccess dBParserAccess;

	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}

	private IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess;
	}
	
	@Override 
	public HashMap<String, Object> doOtherAction(INcpSession session, JSONObject requestObj) throws RuntimeException {
		try{
			String actionName = requestObj.getString("actionName");
			JSONObject customParam = requestObj.getJSONObject("customParam");
			if("generateCategoryFile".equals(actionName)){
				return generateCategoryFile(session, customParam);
			}
			else{
				return null;
			}
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	private HashMap<String, Object> generateCategoryFile(INcpSession session, JSONObject customParam) throws Exception{
		List<String> errors = this.generateCategoryJsFile(session);
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
	public void generateCategoryFile(INcpSession session) throws Exception{
		List<String> errors = this.generateCategoryJsFile(session);
		if(errors.size() > 0){
			logger.error(CommonFunction.listToString(errors, ".\r\n"));
		}
	}

	private List<String> generateCategoryJsFile(INcpSession session) throws Exception{
		List<String> errors = new ArrayList<String>();
		Session dbSession = this.getDBSession();
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String catSql = "select t.sortindex as sortindex,"
				+ " t.name as name"
				+ " from mdl_propertycategory t"
				+ " order by t.sortindex asc";
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("sortindex", ValueType.Decimal);
		fieldValueTypes.put("name", ValueType.String);
		List<String> alias = new ArrayList<String>();
		alias.add("sortindex");
		alias.add("name");
		DataTable catDt = dbAccess.selectList(dbSession, catSql, null, alias, fieldValueTypes);
		List<DataRow> catRows = catDt.getRows();
		StringBuilder jsText = new StringBuilder();
		jsText.append("var js3PropertyCategories = [\r\n");
		for(int i = 0; i < catRows.size(); i++) {
			DataRow catRow = catRows.get(i);
			jsText.append("  {\r\n");
			jsText.append("    sortIndex: " + catRow.getIntegerValue("sortindex") + ",\r\n");
			jsText.append("    name: \"" + CommonFunction.generateInnerStr(catRow.getStringValue("name")) + "\"\r\n");
			jsText.append("  },\r\n");
		}
		jsText.append("];");
		String jsFilePath = ContextUtil.getAbsolutePath() + ZlpState.PAGE_PATH + "/design/common/unitComponents/common/js3PropertyCategories.js";			
		FileOperate fileOperate = new FileOperate();
		fileOperate.createFile(jsFilePath, jsText.toString());
		return errors;
	} 
	
}
