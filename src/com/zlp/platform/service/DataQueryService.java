package com.zlp.platform.service;
 
import java.sql.SQLException;
import java.util.HashMap;

import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.JSONProcessor;
import com.zlp.platform.common.NcpActionSupport;
import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.ServiceResultProcessor;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter; 
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.SelectSqlParser;
import com.zlp.platform.dao.db.ValueType;
 
public class DataQueryService implements IDataQueryService {

	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}   
	 
	protected Session openDBSession() throws SQLException{ 
		return this.transactionManager.getSessionFactory().openSession(); 
	}
			
	//值转换
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  

	//暂时未启用此方法
	//@Override
	public String getTableData(String requestParam){ 
		Session dbSession = null;
		try
		{
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			String sqlName = requestObj.getString("sqlName");
			JSONArray orderbyObjectList = requestObj.getJSONArray("orderby");
			JSONObject whereObject = requestObj.getJSONObject("where");
			String getDataType = requestObj.getString("getDataType");
			String currentPageStr = requestObj.getString("currentPage");
			String pageSizeStr = requestObj.getString("pageSize");
			
			SelectSqlParser sqlParser = this.dBParserAccess.getSqlParser(sqlName);
			if("page".equals(getDataType)){
				int currentPage = Integer.parseInt(currentPageStr);
				int pageSize = Integer.parseInt(pageSizeStr);				
				String orderbyStr = this.jsonToOrderby(orderbyObjectList);		 
				HashMap<String, Object> p2vs  = new HashMap<String, Object>();		
				String whereStr = this.jsonToWhere(sqlParser, whereObject, p2vs);	 
				dbSession = this.openDBSession();
				int recordCount = this.dBParserAccess.getRecordCountBySqlParser(dbSession, sqlParser, p2vs, whereStr);
				DataTable dt = this.dBParserAccess.getDtBySqlParser(dbSession, sqlParser,currentPage, pageSize, p2vs, orderbyStr, whereStr);
			    HashMap<Object, Object> dtMap =	dt.toHashMap();
			    HashMap<String, Object> returnMap = new HashMap<String, Object>();
			    returnMap.put("recordCount",recordCount);
			    returnMap.put("dt", dtMap);
			    return ServiceResultProcessor.createJsonResultStr(returnMap);
			}
			else if("all".equals(getDataType)){
				return "";	
			}
			else {
				return "";
			}			
		}
		catch(Exception ex){
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getData", "", ex);
			return ncpEx.toJsonString();
		} 
	}
	
	//根据select sql配置获取数据
	public DataTable getDtBySqlParser(SelectSqlParser sqlParser,  JSONArray orderbyObjectList, JSONObject whereObject, int currentPage, int pageSize) throws Exception {
		String orderbyStr = this.jsonToOrderby(orderbyObjectList);		 
		HashMap<String, Object> params  = new HashMap<String, Object>();		
		String whereStr = this.jsonToWhere(sqlParser, whereObject, params);	
		Session dbSession = null;
		try
		{
			dbSession = this.openDBSession();
			DataTable dt = this.dBParserAccess.getDtBySqlParser(dbSession,  sqlParser, currentPage, pageSize, params, orderbyStr, whereStr);
			return dt;
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new Exception("getDtBySqlParser error", ex);
		} 
	}  
	
	//根据条件获取记录数
	public int getRecordCountBySqlParser(SelectSqlParser sqlParser, JSONObject whereObject) throws Exception{
		HashMap<String, Object> params  = new HashMap<String, Object>();	
		String whereStr = this.jsonToWhere(sqlParser, whereObject, params);	 
		Session dbSession = null;
		try
		{
			dbSession = this.openDBSession();
			int count =  this.dBParserAccess.getRecordCountBySqlParser(dbSession, sqlParser, params, whereStr);  
			return count;
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new Exception("getDtBySqlParser error", ex);
		} 
	} 

	
	//将json中定义的orderby变成字符串
	private String jsonToOrderby(JSONArray orderbyObjectList ) throws Exception{
		try
		{
			if(orderbyObjectList == null || orderbyObjectList.size()==0){
				return "";
			}
			else {
				StringBuilder orderbyStr =new StringBuilder();
				for(int i=0;i<orderbyObjectList.size();i++) {
					JSONObject orderbyObj = orderbyObjectList.getJSONObject(i);
					String fieldName = orderbyObj.getString("name");
					String direction = orderbyObj.getString("direction");
					if(i>0) {
						orderbyStr.append(", ");
					}
					orderbyStr.append(fieldName + " " +direction );
				}
				return orderbyStr.toString();
			}
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new Exception("can not convert json to order by。 " + ex.getMessage());
		}
	}
	
	//将json中定义的where变成字符串
	private String jsonToWhere(SelectSqlParser sqlParser, JSONObject whereObject, HashMap<String, Object> partWhereParams) throws Exception{
		try
		{
			if(whereObject == null ){
				return "";
			}
			else {
				StringBuilder whereStr = new StringBuilder();
				this.partJsonToWhere(sqlParser, whereObject, whereStr, partWhereParams);
				return whereStr.toString();
			}
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new Exception("can not convert json to order by。 " + ex.getMessage());
		}
	}

	//递归，将json中定义的where变成字符串
	private void partJsonToWhere(SelectSqlParser sqlParser, JSONObject partWhereObject, StringBuilder whereStr, HashMap<String, Object> partWhereParams) throws Exception{
		/* {
		 * 		parttype:"or",
		 * 		list:[
		 * 				{
		 * 					parttype:"field",
		 * 					field:"name",
		 * 					operator:"like",
		 * 					value:"zhang%"
		 * 				},
		 * 				{
		 * 					parttype:"clause",
		 * 					clause:"1=1"
		 * 				}
		 * 			 ]
		 * } 
		 * */
		String parttype = partWhereObject.getString("parttype");
		//or子句
		if("or".equals(parttype)){
			whereStr.append("(");
			JSONArray jsonArray =  partWhereObject.getJSONArray("list");
			for(int i=0;i<jsonArray.size();i++) {
				if(i>0) {
					whereStr.append(" or ");
				}
				JSONObject childObj = jsonArray.getJSONObject(i);
				this.partJsonToWhere(sqlParser, childObj, whereStr, partWhereParams);
			}
			whereStr.append(")");
		}
		//and子句
		else if("and".equals(parttype)){
			whereStr.append("(");
			JSONArray jsonArray =  partWhereObject.getJSONArray("list");
			for(int i=0;i<jsonArray.size();i++) {
				if(i>0) {
					whereStr.append(" and ");
				}
				JSONObject childObj = jsonArray.getJSONObject(i);
				this.partJsonToWhere(sqlParser, childObj, whereStr, partWhereParams);
			}
			whereStr.append(")");
		}
		//字段比较
		else if("field".equals(parttype)){
			String fieldName = partWhereObject.getString("field");
			String operator = partWhereObject.getString("operator");
			String value = partWhereObject.getString("value");
			ValueType valueType = sqlParser.getFieldTypeMap(fieldName); 
			if(valueType == null) {
				throw new Exception("can not find field type. fieldname = "+ fieldName);
			}

			String exp = sqlParser.getSelectExpMap(fieldName); 
			if(exp==null|| exp.isEmpty()) {
				throw new Exception("can not find field exp. fieldname = "+ fieldName);
			}
			
			int paramCount = partWhereParams.size() + 1;
			String paramName = SysConfig.getParamPrefix() + "pp_" + Integer.toString(paramCount);
			
			whereStr.append(exp + " " + operator + paramName);
			Object paramValue = ValueConverter.convertToObject(value, valueType);
			partWhereParams.put(paramName, paramValue);
			//用传递参数的方式，构造sql，参数名命名为wp1,wp2,wp3等，需要编写获取sql参数前缀的函数，字段值的类型转换等通用函数
			
		}
		//其他
		else {
			String partClause = partWhereObject.getString("clause");
			whereStr.append(partClause);
		}
	} 
}
