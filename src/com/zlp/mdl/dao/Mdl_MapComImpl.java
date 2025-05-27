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

//构件关系 add by ls 20230731
public class Mdl_MapComImpl extends DataBaseDao implements IMdl_MapComImpl {
	
  	private static Logger logger = Logger.getLogger(Mdl_MapComImpl.class);  
	
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
			if("generateMapComFile".equals(actionName)){
				return generateMapComFile(session, customParam);
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

	private String generateMapComJs(INcpSession session, DataRow mapComRow, List<String> errors) throws Exception{
		String mapComId = mapComRow.getStringValue("id");
		Session dbSession = this.getDBSession();
		IDBParserAccess dbAccess = this.getDBParserAccess();
		Data mdl_MapComPropertyData = DataCollection.getData("mdl_MapComProperty");

		StringBuilder jsStr = new StringBuilder();
		jsStr.append("  {\r\n");
		jsStr.append("    id: \"" + mapComId + "\",\r\n");
		jsStr.append("    mapTypeCode: \"" + mapComRow.getStringValue("maptypecode") + "\",\r\n");
		jsStr.append("    fromComName: \"" + CommonFunction.generateInnerStr(mapComRow.getStringValue("fromcomname")) + "\",\r\n");
		jsStr.append("    fromComCode: \"" + mapComRow.getStringValue("fromcomcode") + "\",\r\n");
		jsStr.append("    fromComVersionNum: \"" + mapComRow.getStringValue("fromcomversionnum") + "\",\r\n");
		if(mapComRow.getStringValue("frompointname") != null && mapComRow.getStringValue("frompointname").length() != 0){
			jsStr.append("    fromPointName: \"" + CommonFunction.generateInnerStr(mapComRow.getStringValue("frompointname")) + "\",\r\n");
		}
		jsStr.append("    toComName: \"" + CommonFunction.generateInnerStr(mapComRow.getStringValue("tocomname")) + "\",\r\n");
		jsStr.append("    toComCode: \"" + mapComRow.getStringValue("tocomcode") + "\",\r\n");
		jsStr.append("    toComVersionNum: \"" + mapComRow.getStringValue("tocomversionnum") + "\",\r\n");
		if(mapComRow.getStringValue("topointname") != null && mapComRow.getStringValue("topointname").length() != 0){
			jsStr.append("    toPointName: \"" + CommonFunction.generateInnerStr(mapComRow.getStringValue("topointname")) + "\",\r\n");
		}
		jsStr.append("    properties:[\r\n");		
		DataTable propertyDt = dbAccess.getDtByFieldValue(dbSession, mdl_MapComPropertyData, "parentid", "=", mapComId);
		List<DataRow> propertyRows = propertyDt.getRows();
		for(int i = 0; i < propertyRows.size(); i++){
			DataRow propertyRow = propertyRows.get(i);
			jsStr.append("      {\r\n");
			jsStr.append("        from: \"" + CommonFunction.generateInnerStr(propertyRow.getStringValue("fromname")) + "\",\r\n");
			jsStr.append("        to: \"" + CommonFunction.generateInnerStr(propertyRow.getStringValue("toname")) + "\",\r\n");
			jsStr.append("      },\r\n");
		}
		jsStr.append("    ]\r\n");
		
		jsStr.append("  },\r\n");	
		return jsStr.toString();
	}

	private HashMap<String, Object> generateMapComFile(INcpSession session, JSONObject customParam) throws Exception{
		List<String> errors = this.generateMapComponentFile(session);
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
	public void generateMapComFile(INcpSession session) throws Exception{
		List<String> errors = this.generateMapComponentFile(session);
		if(errors.size() > 0){
			logger.error(CommonFunction.listToString(errors, ".\r\n"));
		}
	}

	private List<String> generateMapComponentFile(INcpSession session) throws Exception{
		List<String> errors = new ArrayList<String>();
		Session dbSession = this.getDBSession();
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String mapComSql = "select t.id as id,"
				+ " mt.code as maptypecode,"
				+ " mt.name as maptypename,"
				+ " t.maptypeid as maptypeid,"
				+ " t.fromcomid as fromcomid,"
				+ " fc.name as fromcomname,"
				+ " fc.code as fromcomcode,"
				+ " fc.versionnum as fromcomversionnum,"
				+ " t.frompointname as frompointname,"
				+ " t.tocomid as tocomid,"
				+ " tc.name as tocomname,"
				+ " tc.code as tocomcode,"
				+ " tc.versionnum as tocomversionnum,"
				+ " t.topointname as topointname,"
				+ " t.description as description"
				+ " from mdl_MapCom t"
				+ " left outer join mdl_component fc on fc.id = t.fromcomid"
				+ " left outer join mdl_component tc on tc.id = t.tocomid"
				+ " left outer join mdl_maptype mt on mt.id = t.maptypeid"
				+ " where t.isdeleted = 'N' and t.isactive = 'Y'";
		HashMap<String, ValueType> fieldValueTypes = new HashMap<String, ValueType>();
		fieldValueTypes.put("id", ValueType.String);
		fieldValueTypes.put("maptypecode", ValueType.String);
		fieldValueTypes.put("maptypename", ValueType.String);
		fieldValueTypes.put("maptypeid", ValueType.String);
		fieldValueTypes.put("fromcomid", ValueType.String);
		fieldValueTypes.put("fromcomname", ValueType.String);
		fieldValueTypes.put("fromcomversionnum", ValueType.String);
		fieldValueTypes.put("frompointname", ValueType.String);
		fieldValueTypes.put("tocomcode", ValueType.String);
		fieldValueTypes.put("tocomid", ValueType.String);
		fieldValueTypes.put("tocomname", ValueType.String);
		fieldValueTypes.put("tocomcode", ValueType.String);
		fieldValueTypes.put("tocomversionnum", ValueType.String);
		fieldValueTypes.put("topointname", ValueType.String);
		fieldValueTypes.put("description", ValueType.String);
		List<String> alias = new ArrayList<String>();
		alias.add("id");
		alias.add("maptypecode");
		alias.add("maptypename");
		alias.add("maptypeid");
		alias.add("fromcomid");
		alias.add("fromcomname");
		alias.add("fromcomcode");
		alias.add("fromcomversionnum");
		alias.add("frompointname");
		alias.add("tocomid");
		alias.add("tocomname");
		alias.add("tocomcode");
		alias.add("tocomversionnum");
		alias.add("topointname");
		alias.add("description");
		DataTable mapComDt = dbAccess.selectList(dbSession, mapComSql, null, alias, fieldValueTypes);
		List<DataRow> mapComRows = mapComDt.getRows();
		StringBuilder jsText = new StringBuilder();
		jsText.append("var js3MapComs = [\r\n");
		for(int i = 0; i < mapComRows.size(); i++) {
			DataRow mapComRow = mapComRows.get(i);
			String mapComJs = this.generateMapComJs(session, mapComRow, errors);
			jsText.append(mapComJs);
		}
		jsText.append("];");
		String jsFilePath = ContextUtil.getAbsolutePath() + ZlpState.PAGE_PATH + "/design/common/js/js3MapComs.js";			
		FileOperate fileOperate = new FileOperate();
		fileOperate.createFile(jsFilePath, jsText.toString());
		return errors;
	} 
	
}
