package com.zlp.platform.dao.sys; 
import java.util.HashMap;

import org.hibernate.Session;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter; 
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.SelectSqlParser;
import com.zlp.platform.model.sysmodel.DownList;
import com.zlp.platform.model.sysmodel.DownListCollection;

public class ParamWinBaseDao implements IParamWinBaseDao{
 
	/*可重载的	 
	HashMap<String, Object> getList(INcpSession session, JSONObject requestObj){}
	void beforeGetList(INcpSession session, JSONObject requestObj){}
	void afterGetList(INcpSession session, HashMap<String,Object> resultHash){}
	
	HashMap<String, Object> doOtherAction(INcpSession session, JSONObject requestObj){}
	*/
	private Session dbSession = null;
	protected Session getDBSession(){ 
		if(this.dbSession == null){
			throw new RuntimeException("none db session.");
		}
		return this.dbSession;
	} 
	public void setDBSession(Session dbSession){
		this.dbSession = dbSession;
	}
 
	//获取下拉数据
	public HashMap<String, Object> getList(INcpSession session, JSONObject requestObj) throws Exception { 
		//request:
		/*
		 * {
		 * listName:"userList", 
		 * paramValues:{f1:1111,f2:"nihao"}
		 * }
		 */
		
		//response:
		/*
		 *{
		 *list:[{f1:true,f2:false},
		 *		{f1:true,f2:false}]
		 *}  
		 */
		
		beforeGetList(session, requestObj);
		HashMap<String, Object> resultObj = getListCore(session, requestObj);
		afterGetList(session, requestObj, resultObj); 
		return resultObj;  		
	}
	
	protected HashMap<String, Object> getListCore(INcpSession session, JSONObject requestObj) throws Exception{
		IDBParserAccess dbParserAccess = (IDBParserAccess)ContextUtil.getBean("dBParserAccess");
		String listName = requestObj.getString("listName");
		DownList list = DownListCollection.getDownList(listName);
		SelectSqlParser sqlParser = list.getDsSqlParser();
		JSONArray whereArray = requestObj.getJSONArray("where");
		JSONArray orderbyArray = requestObj.getJSONArray("orderby");
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		String where = this.getWhere(dbParserAccess, sqlParser, whereArray, p2vs, "and");
		String orderby = this.getOrderBy(orderbyArray);

		HashMap<String,Object> resultHash = new HashMap<String, Object>(); 
		DataTable dt =	dbParserAccess.getDtBySqlParser(this.getDBSession(), sqlParser, -1, -1, p2vs, where, orderby);
		resultHash.put("table", dt.toHashMap());
				
		return resultHash;
	}
	
	protected void beforeGetList(INcpSession session, JSONObject requestObj) throws Exception{ 
	}
	
	protected void afterGetList(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{		
	}	
	 
	private String getWhere(IDBParserAccess dbParserAccess, SelectSqlParser sqlParser, JSONArray whereArray, HashMap<String, Object> p2vs, String joinStr) throws Exception{
		StringBuilder s = new StringBuilder();
		for(int i=0;i<whereArray.size();i++){
			JSONObject partObj = whereArray.getJSONObject(i);
			String parttype = partObj.getString("parttype");
			String partWhere = "";
			if("or".equals(parttype)){
				partWhere = this.getWhere(dbParserAccess, sqlParser, partObj.getJSONArray("value"), p2vs, "or");				
			}
			else if("and".equals(parttype)){
				partWhere = this.getWhere(dbParserAccess, sqlParser, partObj.getJSONArray("value"), p2vs, "and");				
			}
			else if("field".equals(parttype)){
				//{parttype:"field",field:"id", operator:"=", value:"1111111"}
				String fieldName = partObj.getString("field");
				String operator = partObj.getString("operator");
				String valueStr = partObj.getString("value");
				String selectExp = sqlParser.getSelectExpMap(fieldName);
			    Object value = ValueConverter.convertToDBObject(valueStr, sqlParser.getFieldTypeMap(fieldName));
				String pName = "p" + p2vs.size();				
				partWhere = selectExp + " "	+ operator + " " + SysConfig.getParamPrefix() + pName;
				p2vs.put(pName, value);
			}
			else if("clause".equals(parttype)){
				String clauseStr = partObj.getString("clause");
				partWhere = clauseStr;
			}
			s.append((i == 0 ? "" : " " + joinStr + " ") + "(" + partWhere + ")"); 
		}
		return s.toString();
	}
	
	private String getOrderBy(JSONArray orderbyArray){
		//orderby:[{name:"f1",direction:"desc"},{name:"f2",direction:"asc"}]
		StringBuilder s = new StringBuilder();
		for(int i=0;i<orderbyArray.size();i++){
			JSONObject partObj = orderbyArray.getJSONObject(i);
			String name = partObj.getString("name");
			String direction = partObj.getString("direction");
			s.append((i == 0 ? "" : ", ") + name + " " + direction); 
		}
		return s.toString();
	}
 
	//其他操作，用于扩展其他功能
	public HashMap<String,Object> doOtherAction(INcpSession session, JSONObject requestObj) throws RuntimeException{
		try{
			return null;  	
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
}
