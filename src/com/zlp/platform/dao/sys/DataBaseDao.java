package com.zlp.platform.dao.sys;

import java.sql.SQLException;
import java.util.*;

import com.zlp.platform.dao.db.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.LicenseChecker;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import com.zlp.platform.model.sysmodel.DataField;
import com.zlp.platform.model.sysmodel.DownList;
import com.zlp.platform.model.sysmodel.DownListCollection;
import com.zlp.platform.model.sysmodel.DownListField;
import com.zlp.platform.service.IDataPurviewService;

public class DataBaseDao implements IDataBaseDao{

	/*可重载的	
	HashMap<String, Object> getInputStatus(INcpSession session, JSONObject requestObj ){}
	void onGetInputStatus(INcpSession session, JSONObject requestObj,HashMap<String, HashMap<String, Boolean>> defaultValues){}
	
	HashMap<String, Object> getList(INcpSession session, JSONObject requestObj){}
	void beforeGetList(INcpSession session, JSONObject requestObj){}
	void afterGetList(INcpSession session, HashMap<String,Object> resultHash){}
	
	HashMap<String, Object> add(INcpSession session, JSONObject requestObj){}
	void onAdd(INcpSession session, JSONObject requestObj,HashMap<String, Object> defaultValues){}
	
	HashMap<String, Object> select(INcpSession session, JSONObject requestObj){}
	void beforeSelect(INcpSession session, JSONObject requestObj){}
	void afterSelect(INcpSession session, HashMap<String,Object> resultHash){}
	 
	HashMap<String, Object> save(INcpSession session, JSONObject requestObj){}
	void beforeSave(INcpSession session, JSONObject requestObj){}
	void afterSave(INcpSession session, HashMap<String,Object> resultHash){}
	
	HashMap<String, Object> delete(INcpSession session, JSONObject requestObj){}
	void beforeDelete(INcpSession session, JSONObject requestObj){}
	void afterDelete(INcpSession session, HashMap<String,Object> resultHash){}
	
	HashMap<String, Object> doOtherAction(INcpSession session, JSONObject requestObj){}
	*/ 
	
	//统一创建记录时，给deletetime赋值
	private static String defaultDeleteTime = null;
	public static String getDefaultDeleteTime() throws Exception{
		if(defaultDeleteTime == null){
			defaultDeleteTime = "2999-12-31 23:59:59";
		}
		return defaultDeleteTime;
	}
	
	public static String getDefaultCompanyId(INcpSession ncpSesion) throws Exception{ 
		return ncpSesion.getCompanyId();
	}
	
	//added by ls 20200201是否指定了删除标记
	private boolean hasIsDeletedMark = false;
	@Override
	public boolean getHasIsDeletedMark(){
		return this.hasIsDeletedMark;
	}
	public void setHasIsDeletedMark(boolean hasIsDeletedMark){
		this.hasIsDeletedMark = hasIsDeletedMark;
	}
	
	//added by ls 201906111034 系统字段自动赋值
	private boolean autoAssignFieldValueBeforeSave = true;
	@Override
	public boolean getAutoAssignFieldValueBeforeSave(){
		return this.autoAssignFieldValueBeforeSave;
	}
	public void setAutoAssignFieldValueBeforeSave(boolean autoAssignFieldValueBeforeSave){
		this.autoAssignFieldValueBeforeSave = autoAssignFieldValueBeforeSave;
	}

	private String createTimeFieldName = "";
	@Override
	public String getCreateTimeFieldName() {
		return createTimeFieldName;
	}
	public void setCreateTimeFieldName(String createTimeFieldName) {
		this.createTimeFieldName = createTimeFieldName;
	}
	
	private String createUserIdFieldName = "";
	@Override
	public String getCreateUserIdFieldName() {
		return createUserIdFieldName;
	}
	public void setCreateUserIdFieldName(String createUserIdFieldName) {
		this.createUserIdFieldName = createUserIdFieldName;
	}
	
	private String modifyTimeFieldName = "";
	public String getModifyTimeFieldName() {
		return modifyTimeFieldName;
	}
	public void setModifyTimeFieldName(String modifyTimeFieldName) {
		this.modifyTimeFieldName = modifyTimeFieldName;
	}
	
	private String modifyUserIdFieldName = "";
	@Override
	public String getModifyUserIdFieldName() {
		return modifyUserIdFieldName;
	}
	public void setModifyUserIdFieldName(String modifyUserIdFieldName) {
		this.modifyUserIdFieldName = modifyUserIdFieldName;
	}
	
	private String isDeletedFieldName = "";
	@Override
	public String getIsDeletedFieldName() {
		return isDeletedFieldName;
	}
	public void setIsDeletedFieldName(String isDeletedFieldName) {
		this.isDeletedFieldName = isDeletedFieldName;
	}

	private String deleteTimeFieldName = "";
	@Override
	public String getDeleteTimeFieldName() {
		return deleteTimeFieldName;
	}
	public void setDeleteTimeFieldName(String deleteTimeFieldName) {
		this.deleteTimeFieldName = deleteTimeFieldName;
	}

	private String companyIdFieldName = "";
	@Override
	public String getCompanyIdFieldName() {
		return companyIdFieldName;
	}
	public void setCompanyIdFieldName(String companyIdFieldName) {
		this.companyIdFieldName = companyIdFieldName;
	}

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
	
	//获取只读必填项的状态
	public HashMap<String, Object> getInputStatus(INcpSession session, JSONObject requestObj ) {
		//request:
		/*
		 * {
		 * dataName:"d_user",
		 * fieldValues:{f1:1111,f2:"nihao"}
		 * }
		 */
		
		//response:
		/*
		 *{
		 *readonly:{f1:true,f2:false},
		 *visible:{f3:true,f4:false},
		 *nullable:{f3:false,f4:false}
		 *}  
		 */
		 
		HashMap<String, HashMap<String, Boolean>> statusObj =  new HashMap<String, HashMap<String, Boolean>>();
		statusObj.put("readonly", new HashMap<String, Boolean>());
		statusObj.put("visible", new HashMap<String, Boolean>());
		statusObj.put("nullable", new HashMap<String, Boolean>());
		
		//调用表达式
		
		onGetInputStatus(session, requestObj, statusObj); 
		HashMap<String, Object> resultHash = new HashMap<String, Object>();
		for(String key : statusObj.keySet()){
			resultHash.put(key, statusObj.get(key));
		}
		return resultHash;  	
	}
	
	protected void onGetInputStatus(INcpSession session, JSONObject requestObj,HashMap<String, HashMap<String, Boolean>> defaultValues){
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
		//调用表达式
		beforeGetList(session, requestObj);
		
		//调用自定义表达式 onGetList，如果返回值不为空，那么继续执行，否则跳过下面一句话
		
		HashMap<String, Object> resultObj = getListCore(session, requestObj);

		//调用表达式
		afterGetList(session, requestObj, resultObj); 
		return resultObj;  		
	}
	
	protected HashMap<String, Object> getListCore(INcpSession session, JSONObject requestObj) throws Exception{
		IDBParserAccess dbParserAccess = (IDBParserAccess)ContextUtil.getBean("dBParserAccess"); 
		String listName = requestObj.getString("listName");
		DownList list = DownListCollection.getDownList(listName);
		SelectSqlParser sqlParser = list.getDsSqlParser();
		JSONArray userWhereArray = requestObj.getJSONArray("where");
		JSONArray sysWhereArray = requestObj.containsKey("sysWhere")? requestObj.getJSONArray("sysWhere"):null;
		JSONArray orderbyArray = requestObj.getJSONArray("orderby");
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		String userWhere = DataBaseDao.getWhere(dbParserAccess, sqlParser, userWhereArray, p2vs, "and");
		String sysWhere = DataBaseDao.getWhere(dbParserAccess, sqlParser, sysWhereArray, p2vs, "and");
		String orderby = DataBaseDao.getOrderBy(orderbyArray);
		
		//获取数据权限过滤条件 
		String previousData = requestObj.containsKey("previousData")?requestObj.getString("previousData"):"";
		String previousField = requestObj.containsKey("previousField")?requestObj.getString("previousField"):"";
		String popDataField = requestObj.containsKey("popDataField")?requestObj.getString("popDataField"):"";  
		String dataPurviewFilter = this.getDataPurviewFilter(session, list, previousData, previousField, popDataField, p2vs);
		String allWhere = userWhere + (sysWhere.isEmpty() ? "" : ( userWhere.isEmpty() ? "" : " and ") + sysWhere);  
		if(!dataPurviewFilter.isEmpty()){
			allWhere = (allWhere.isEmpty() ? "" : allWhere + " and ") + "(" + dataPurviewFilter + ")";
		}

		HashMap<String,Object> resultHash = new HashMap<String, Object>(); 
		DataTable dt =	dbParserAccess.getDtBySqlParser(this.getDBSession(), sqlParser, -1, -1, p2vs, allWhere, orderby);
		resultHash.put("table", dt.toHashMap());
				
		return resultHash;
	}
	
	//获取数据权限过滤条件和参数值
	private String getDataPurviewFilter(INcpSession session, DownList list, String previousData, String previousField, String popDataField, HashMap<String, Object> returnSqlParams) throws Exception{
		HashMap<String, String> aliasToFactors = new HashMap<String, String>();
		List<DownListField> fields= list.getDownListFields();
		for(DownListField field : fields){
			String dfName = field.getName();
			String factorName = null;
			if(dfName.equals(popDataField)){ 
				Data pData = DataCollection.getData(previousData);
				factorName =  pData.getDataField(previousField).getDataPurviewFactor(); 
			}
			else{ 
				factorName = field.getDataPurviewFactor();
			}
			if(factorName != null && !factorName.trim().isEmpty()){
				String alias = list.getDsSqlParser().getSelectExpMap(dfName); 
				aliasToFactors.put(alias, factorName);
			}	
		} 
		if(aliasToFactors.size() > 0){
			IDataPurviewService dataPurviewService = (IDataPurviewService)ContextUtil.getBean("dataPurviewServiceBean");
			return dataPurviewService.getClause(this.getDBSession(), session.getUserId(), aliasToFactors, returnSqlParams);
		}
		else{
			return "";
		}
	}
	
	protected void beforeGetList(INcpSession session, JSONObject requestObj) throws Exception{  
	}
	
	protected void afterGetList(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{		
	}
	
	public List<String> getIds(INcpSession session, String dataName, String fieldName, String operateStr, String fieldValueStr) throws Exception{ 
		IDBParserAccess dbParserAccess = (IDBParserAccess)ContextUtil.getBean("dBParserAccess");
		Data data = DataCollection.getData(dataName);
		String idFieldName = data.getIdFieldName();
		Object fieldValue = ValueConverter.convertToDBObject(fieldValueStr, data.getDataField(fieldName).getValueType());
		List<String> ids =	dbParserAccess.getIds(this.getDBSession(), idFieldName, data, fieldName, operateStr, fieldValue);
		return ids;
	} 
	
	//当新建时，获取字段默认值
	public HashMap<String, Object> add(INcpSession session, JSONObject requestObj) throws Exception { 
		//request:
		/*
		 * {
		 * dataName:"d_user" 
		 * }
		 */
		
		//response:
		/*
		 *{
		 *defaultValues:{f1:"nihao",f2:222}
		 *}  
		 */
		 
		HashMap<String, Object> resultObj =  new HashMap<String, Object>();
		onAdd(session, requestObj, resultObj);
		//调用自定义表达式 onAdd
		return resultObj;  	
	}
	
	protected void onAdd(INcpSession session, JSONObject requestObj, HashMap<String, Object> resultObj) throws Exception{
		JSONArray defaultValues =  new JSONArray();
		int newRowCount = (Integer) requestObj.get("newRowCount");
		for(int i=0;i<newRowCount;i++){
			defaultValues.add(new JSONObject());
		}
		resultObj.put("defaultValues", defaultValues);
	}
	
	//查询数据
	public HashMap<String, Object> select(INcpSession session, JSONObject requestObj) throws Exception {
		//request:
		/*
			dataName:"d_user",
			getDataType:"all",
			fromIndex:1,
			onePageRowCount:20,
			isGetSum:"Y",
			isGetCount:"N",
			//where的最顶级，是用and连接的，然后按照树形递归下去
			where:[{parttype:"or",value:[
										{parttype:"field",field:"name",operator:"like",value:"zhang%"},
										{parttype:"clause",clause:"1=1"}
										]},
					{parttype:"field",field:"id", operator:"=", value:"1111111"}
				],
			orderby:[{name:"f1",direction:"desc"},{name:"f2",direction:"asc"}]
		 */
		
		//response:
		/*
		 *{
		 *table:{
		 *			fields:[{name:"f1",valueType:"String"},{name:"f2",valueType:"Decimal"}],
		 *			rows:[{f1:"asdf",f2:"1111"},{f1:"asdf11",f2:"33"}]
		 *			},
		 *sumRow:{f2:1123,f3:3232},
		 *rowCount:100
		 *}  
		 */
		
		//调用表达式
		beforeSelect(session, requestObj);
		
		//调用自定义表达式  onSelect 返回值为HashMap<String, Object> 参数为session, requestObj，如果返回值为空，那么执行系统自带的selectCore
		HashMap<String, Object> resultObj = selectCore(session, requestObj);

		//调用表达式
		afterSelect(session, requestObj, resultObj); 
		return resultObj;  	
	}
	
	protected HashMap<String, Object> selectCore(INcpSession session, JSONObject requestObj) throws Exception{ 	
		//验证License added by ls 20190618	
		LicenseChecker.check(); 
		
		IDBParserAccess dbParserAccess = (IDBParserAccess)ContextUtil.getBean("dBParserAccess");
		String dataName = requestObj.getString("dataName");
		Data data = DataCollection.getData(dataName);
		SelectSqlParser sqlParser = data.getDsSqlParser();
		int currentPage = requestObj.getIntValue("currentPage");
		boolean isGetCount = requestObj.getBoolean("isGetCount");
		//boolean isGetSum = requestObj.getBoolean("isGetSum");
		int pageSize = requestObj.getIntValue("pageSize");
		JSONArray userWhereArray = requestObj.getJSONArray("where");
		JSONArray sysWhereArray = requestObj.containsKey("sysWhere")? requestObj.getJSONArray("sysWhere"):null;
		JSONArray orderbyArray = requestObj.getJSONArray("orderby");
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		String userWhere = DataBaseDao.getWhere(dbParserAccess, sqlParser, userWhereArray, p2vs, "and");
		String sysWhere = DataBaseDao.getWhere(dbParserAccess, sqlParser, sysWhereArray, p2vs, "and");
		String orderby = DataBaseDao.getOrderBy(orderbyArray);
		 
		//获取数据权限过滤条件 
		String previousData = requestObj.containsKey("previousData")?requestObj.getString("previousData"):"";
		String previousField = requestObj.containsKey("previousField")?requestObj.getString("previousField"):"";
		String popDataField = requestObj.containsKey("popDataField")?requestObj.getString("popDataField"):"";  
		String dataPurviewFilter = DataBaseDao.getDataPurviewFilter(session, this.getDBSession(), data, previousData, previousField, popDataField, p2vs);
		String allWhere = userWhere + (sysWhere.isEmpty() ? "" :( userWhere.isEmpty()? "" :" and ") + sysWhere);  
		if(!dataPurviewFilter.isEmpty()){
			allWhere = (allWhere.isEmpty() ? "" : allWhere + " and ") + "(" + dataPurviewFilter + ")";
		}

		HashMap<String,Object> resultHash = new HashMap<String, Object>();
		DataTable dt =	dbParserAccess.getDtBySqlParser( this.getDBSession(), sqlParser, currentPage, pageSize, p2vs, allWhere, orderby);
		resultHash.put("table", dt.toHashMap());
		
		if(isGetCount){
			int rowCount = dbParserAccess.getRecordCountBySqlParser( this.getDBSession(), sqlParser, p2vs, allWhere);
			resultHash.put("rowCount", rowCount);
//			if(dt.getRows()!=null&&dt.getRows().size()>0){
//				resultHash.put("rowCount", dt.getRows().size());
//			}else{
//				resultHash.put("rowCount", 0);
//			}
//			
			
		}
		
		return resultHash;
	}
	
	//获取数据权限过滤条件和参数值
	public static String getDataPurviewFilter(INcpSession session, Session dbSession, Data data, String previousData, String previousField, String popDataField, HashMap<String, Object> returnSqlParams) throws Exception{
		HashMap<String, String> aliasToFactors = new HashMap<String, String>();
		HashMap<String, DataField> dfs= data.getDataFields();
		for(String dfName : dfs.keySet()){
			String factorName = null;
			if(dfName.equals(popDataField)){ 
				Data pData = DataCollection.getData(previousData);
				factorName =  pData.getDataField(previousField).getDataPurviewFactor(); 
			}
			else{
				DataField df = dfs.get(dfName);
				factorName = df.getDataPurviewFactor();
			}
			if(factorName != null && !factorName.trim().isEmpty()){
				String alias = data.getDsSqlParser().getSelectExpMap(dfName); 
				aliasToFactors.put(alias, factorName);
			}	
		} 
		if(aliasToFactors.size() > 0){
			IDataPurviewService dataPurviewService = (IDataPurviewService)ContextUtil.getBean("dataPurviewServiceBean");
			return dataPurviewService.getClause(dbSession, session.getUserId(), aliasToFactors, returnSqlParams);
		}
		else{
			return "";
		}
	}
	
	public static String getWhere(IDBParserAccess dbParserAccess, SelectSqlParser sqlParser, JSONArray whereArray, HashMap<String, Object> p2vs, String joinStr) throws Exception{
		if(whereArray == null){
			return "";
		}
		else{

			StringBuilder s = new StringBuilder();
			for(int i=0;i<whereArray.size();i++){
				JSONObject partObj = whereArray.getJSONObject(i);
				String parttype = partObj.getString("parttype");
				String partWhere = "";
				if("or".equals(parttype)){
					partWhere = DataBaseDao.getWhere(dbParserAccess, sqlParser, partObj.getJSONArray("value"), p2vs, "or");				
				}
				else if("and".equals(parttype)){
					partWhere = DataBaseDao.getWhere(dbParserAccess, sqlParser, partObj.getJSONArray("value"), p2vs, "and");				
				}
				else if("field".equals(parttype)) {
					//{parttype:"field",field:"id", operator:"=", value:"1111111"}
					String fieldName = partObj.getString("field");
					String operator = partObj.getString("operator");
					String valueStr = partObj.getString("value");
					String selectExp = sqlParser.getSelectExpMap(fieldName);

					//解决前端各模型窗口模糊查询时，日期、时间等类型转换出错的问题（统一保持字符串进行like匹配，无需转换类型）  modified by liyh 20180903
					//Object value = ValueConverter.convertToDBObject(valueStr, sqlParser.getFieldTypeMap(fieldName));
					Object value;
					switch (operator) {
						case "is null":
						case "is not null": {
							partWhere = selectExp + " " + operator;
							break;
						}
						case "like": {
							String pName = "p" + p2vs.size();
							partWhere = selectExp + " " + operator + " " + SysConfig.getParamPrefix() + pName;
							value = valueStr;
							p2vs.put(pName, value);
							break;
						}
						default: {
							String pName = "p" + p2vs.size();


							//达梦兼容改动 modified by liyh 20231108 Start
							//partWhere = selectExp + " "	+ operator + " " + SysConfig.getParamPrefix() + pName;
							//value = ValueConverter.convertToDBObject(valueStr, sqlParser.getFieldTypeMap(fieldName));
							//p2vs.put(pName, value);
							//break;

							partWhere = selectExp + " " + operator + " ";
							value = ValueConverter.convertToDBObject(valueStr, sqlParser.getFieldTypeMap(fieldName));
							if ("is null".equals(operator) || "is not null".equals(operator)) {
								//如果操作符为 is null 或者 is not null，则不需要后面的值  added by liyh 20191219
							} else if (!operator.toLowerCase().equals("is")) {////sql 脚本中进行 判断is null时，在mysql中没有错，但在oracle或者达梦中，用?传值会出现错误，
								//所以当sql中有is时，不再采用?传入null(org.parentid is :p0)，而直接将null拼拉到is 后(如org.parentid is null)
								partWhere += SysConfig.getParamPrefix() + pName;
								if ("1".equals(value)) {
									System.out.println(value);
								}
								p2vs.put(pName, value);
							} else {
								partWhere += value;
							}
							//达梦兼容改动 modified by liyh 20231108 End

							break;


						}
					}
				}
				else if("clause".equals(parttype)){
					String clauseStr = partObj.getString("clause");
					partWhere = clauseStr;
				}
				s.append((i == 0 ? "" : " " + joinStr + " ") + "(" + partWhere + ")"); 
			}
			return s.toString();
		}
	}
	
	public static String getOrderBy(JSONArray orderbyArray){
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

	protected void beforeSelect(INcpSession session, JSONObject requestObj) throws Exception{ 	 
	}
	
	protected void afterSelect(INcpSession session, JSONObject requestObj,  HashMap<String,Object> resultHash) throws Exception{
 	}   
	 
	//保存数据 
	public HashMap<String, Object> save(INcpSession session, JSONObject requestObj) throws RuntimeException { 
		//request:
		/*
		 * {
		 * dataName:"d_user",
		 * isRefreshAfterSave:true,//是否返回保存后的行记录
		 * update:{"row1":{id:11,f1:1111,f2:"232"},"row2":{f1:1111,f2:"232"}},
		 * insert:{"row1":{id:11,f1:1111,f2:"232"},"row2":{f1:1111,f2:"232"}}
		 * }
		 */
		
		//response:
		/*
		 *{
		 * idValueToRowIds:{"11":"r1","12":"r2"}
		 * table:{
		 *			fields:[{name:"f1",valueType:"String"},{name:"f2",valueType:"Decimal"}],
		 *			rows:[{f1:"asdf",f2:"1111"},{f1:"asdf11",f2:"33"}]
		 *			},
		 */ 
		try{ 
			//调用表达式
			beforeSave(session, requestObj);
			
			//调用表达式，onSave 返回值为resultHash，参数为session, requestObj，如果返回值为空，那么调用saveCore
			
			HashMap<String, Object> resultHash = saveCore(session, requestObj);
			
			//调用表达式
			afterSave(session, requestObj, resultHash);  
			return resultHash;  	
		}
		catch(Exception ex){ 
        	ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
	
	public HashMap<String, Object> saveWithTx(INcpSession session, JSONObject requestObj) throws RuntimeException, SQLException {  
		Session dbSession = this.getDBSession(); 
		Transaction tx = null;
		try{  
			tx = dbSession.beginTransaction();
			HashMap<String, Object> resultObj =  this.save(session, requestObj);
			tx.commit();
			return resultObj;  
		}
		catch(RuntimeException ex){ 
			if(tx != null){
				tx.rollback();
			}
			throw ex;
		} 
	}
	
	protected void beforeSave(INcpSession session, JSONObject requestObj) throws Exception{ 
	}

	protected void afterSave(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{
 
 	}
	
	//added by ls 20190611 保存前对系统字段自动赋值
	protected void autoAssignFieldValueBeforeInsert(INcpSession session, JSONObject rowObj, Data data) throws Exception{
		if(this.getAutoAssignFieldValueBeforeSave()){
			String createTimeFieldName = this.getCreateTimeFieldName();
			String createUserIdFieldName = this.getCreateUserIdFieldName();
			String isDeletedFieldName = this.getIsDeletedFieldName();
			String deleteTimeFieldName = this.getDeleteTimeFieldName();
			String companyIdFieldName = this.getCompanyIdFieldName();
			if(createTimeFieldName.length() != 0 && data.getDataField(createTimeFieldName) != null){
				rowObj.put(createTimeFieldName, ValueConverter.convertToString(new Date(), ValueType.Time));
			}
			if(createUserIdFieldName.length() != 0 && data.getDataField(createUserIdFieldName) != null){
				rowObj.put(createUserIdFieldName, session.getUserId());
			}
			if(isDeletedFieldName.length() != 0 && data.getDataField(isDeletedFieldName) != null){
				rowObj.put(isDeletedFieldName, "N");
			}
			if(isDeletedFieldName.length() != 0 && data.getDataField(isDeletedFieldName) != null){
				rowObj.put(isDeletedFieldName, "N");
			}
			if(deleteTimeFieldName.length() != 0 && data.getDataField(deleteTimeFieldName) != null){
				rowObj.put("deletetime", getDefaultDeleteTime());
			}
			if(companyIdFieldName.length() != 0 && data.getDataField(companyIdFieldName) != null){
				rowObj.put("companyid", getDefaultCompanyId(session));
			}
		}
	}

	//added by ls 20190611 保存前对系统字段自动赋值
	protected void autoAssignFieldValueBeforeUpdate(INcpSession session, JSONObject rowObj, Data data) throws Exception{
		if(this.getAutoAssignFieldValueBeforeSave()){
			String modifyTimeFieldName = this.getModifyTimeFieldName();
			String modifyUserIdFieldName = this.getModifyUserIdFieldName(); 
			if(modifyTimeFieldName.length() != 0 && data.getDataField(modifyTimeFieldName) != null){
				rowObj.put(modifyTimeFieldName, ValueConverter.convertToString(new Date(), ValueType.Time));
			}
			if(modifyUserIdFieldName.length() != 0 && data.getDataField(modifyUserIdFieldName) != null){
				rowObj.put(modifyUserIdFieldName, session.getUserId());
			} 
		}
	}
	
	protected HashMap<String, Object> saveCore(INcpSession session, JSONObject requestObj) throws Exception{
		IDBParserAccess dbParserAccess = (IDBParserAccess)ContextUtil.getBean("dBParserAccess");
		String dataName = (String)requestObj.get("dataName"); 
		Boolean isRefreshAfterSave = (Boolean) requestObj.get("isRefreshAfterSave");
		Data data = DataCollection.getData(dataName);
	    JSONObject idValueToRowIds = new JSONObject(); 
	    //更新到数据库	    
	    JSONObject updateRowsObj = requestObj.getJSONObject("update");
	    for(Object updateRowIdObj : updateRowsObj.keySet()){
	    	String updateRowId = (String)updateRowIdObj;
	    	JSONObject updateRowObj = updateRowsObj.getJSONObject(updateRowId);

	    	//added by ls 20190611 保存前对系统字段自动赋值
	    	this.autoAssignFieldValueBeforeUpdate(session, updateRowObj, data);
	    	
	    	String idValue =(String)updateRowObj.get(data.getIdFieldName());    
	    	dbParserAccess.updateByData(this.getDBSession(), data, updateRowObj, idValue);   
	    	idValueToRowIds.put(idValue,updateRowId);
	    }

	    //插入到数据库	    
	    JSONObject insertRowsObj = requestObj.getJSONObject("insert");
	    int insertRowCount = insertRowsObj.size();
	    //先为新插入数据批量获取id值
	    List<String> idSeqValues = dbParserAccess.getSequenceGenerator().getIdSequence(data.getSaveDest(), insertRowCount);
	    Object[] insertRowIds = insertRowsObj.keySet().toArray();
	    for(int i = 0;i < insertRowCount; i++){	    	
	    	String insertRowId = (String)insertRowIds[i];
	    	String idValue = idSeqValues.get(i);
	    	JSONObject insertRowObj = insertRowsObj.getJSONObject(insertRowId);  

	    	//added by ls 20190611 保存前对系统字段自动赋值
	    	this.autoAssignFieldValueBeforeInsert(session, insertRowObj, data);
	    	
    		dbParserAccess.insertByData(this.getDBSession(), data, insertRowObj, idValue); 
	    	idValueToRowIds.put(idValue, insertRowId);
	    }
	    
	    //构造返回值
	    HashMap<String, Object> resultHash=new HashMap<String, Object>();
	    resultHash.put("idValueToRowIds", idValueToRowIds);
	    
	    //获取更新插入后的记录值
	    if(isRefreshAfterSave){
	        Object[] idObjects = idValueToRowIds.keySet().toArray(); 
	        DataTable dt = null;
    		if(idObjects.length == 1){
    			dt = dbParserAccess.getDtById(this.getDBSession(), data,(String)idObjects[0]);
    		}
    		else{
    			List<String> ids = new ArrayList<String>();
		        for(int i=0; i<idObjects.length; i++){ 
		        	ids.add((String)idObjects[i]);
		        }	
    			dt = dbParserAccess.getDtByIds(this.getDBSession(), data, ids);    	
    		}
	    	resultHash.put("table", dt.toHashMap());
	    }    
		
		return resultHash;
	} 
	
	//删除数据 
	public HashMap<String, Object> delete(INcpSession session, JSONObject requestObj) throws RuntimeException { 
		//request:
		/*
		 * {
		 * dataName:"d_user" 
		 * deleteRows:{r1:11,r2:12}
		 * deleteByFieldValue:{fieldName:"",operateStr:"",fieldValue:""}
		 * }
		 */
		
		//response:
		/*
		 *{
		 * success:true
		 *}  
		 */  
		try{ 
			//调用表达式beforeDelete
			beforeDelete(session, requestObj);
			//返回值为HashMap<String, Object> 参数为session, requestObj，如果返回值为空，那么调用saveDelete
			
			HashMap<String, Object> resultObj = deleteCore(session, requestObj);
			
			//调用表达式afterDelete
			afterDelete(session, requestObj, resultObj);  
			return resultObj;  
		}
		catch(Exception ex){ 
        	ex.printStackTrace();
			throw new RuntimeException(ex);
		}	
	}
	
	public HashMap<String, Object> deleteWithTx(INcpSession session, JSONObject requestObj) throws RuntimeException, SQLException { 
		Session dbSession = this.getDBSession(); 
		Transaction tx = null;
		try{  
			tx = dbSession.beginTransaction();
			HashMap<String, Object> resultObj =  this.delete(session, requestObj);
			tx.commit();
			return resultObj;  
		}
		catch(RuntimeException ex){ 
			if(tx != null){
				tx.rollback();
			}
			throw ex;
		}	
	}
	
	protected HashMap<String, Object> deleteCore(INcpSession session, JSONObject requestObj) throws Exception{ 
		IDBParserAccess dbParserAccess = (IDBParserAccess)ContextUtil.getBean("dBParserAccess");
		String dataName = requestObj.getString("dataName");
		if(this.getHasIsDeletedMark()){
			if(requestObj.containsKey("deleteRows")) {
				
				StringBuilder deleteSql = new StringBuilder();
				deleteSql.append("update " + dataName + " set isdeleted = 'Y', deletetime= " + SysConfig.getParamPrefix() + "deletetime where id in (");
				
				JSONObject rowIdToIdValues = requestObj.getJSONObject("deleteRows");    	
				Object[] idArray = rowIdToIdValues.values().toArray();
				for(int i = 0; i < idArray.length; i++){
					if(i > 0){
						deleteSql.append(", ");
					}
					deleteSql.append("'" + (String)idArray[i] + "'");
				}
				deleteSql.append(")");
				
				HashMap<String, Object> p2vs = new HashMap<String, Object>();
				p2vs.put("deletetime", new Date());
								
				dbParserAccess.update(getDBSession(), deleteSql.toString(), p2vs);
			}
			else{
				//支持子表打删除标签 modified by ls 20240110
				Data data =	DataCollection.getData(dataName);
				JSONObject deleteByFieldValue = requestObj.getJSONObject("deleteByFieldValue");
				String fieldName = deleteByFieldValue.getString("fieldName");
				DataField field = data.getDataField(fieldName);
				String operateStr = deleteByFieldValue.getString("operateStr");
				String fieldValueStr = deleteByFieldValue.getString("fieldValue");
				Object fieldValue = ValueConverter.convertToDBObject(fieldValueStr, field.getValueType());
				DataTable dt = dbParserAccess.getDtByFieldValue(this.getDBSession(), data, fieldName, operateStr, fieldValue);
				List<DataRow> rows = dt.getRows();
				if(!rows.isEmpty()) {
					StringBuilder deleteSql = new StringBuilder();
					deleteSql.append("update " + dataName + " set isdeleted = 'Y', deletetime= " + SysConfig.getParamPrefix() + "deletetime where id in (");
					for (int i = 0; i < rows.size(); i++) {
						DataRow row = rows.get(i);
						String id = row.getStringValue(data.getIdFieldName());
						if (i > 0) {
							deleteSql.append(", ");
						}
						deleteSql.append("'" + id + "'");
					}
					deleteSql.append(")");

					HashMap<String, Object> p2vs = new HashMap<String, Object>();
					p2vs.put("deletetime", new Date());

					dbParserAccess.update(getDBSession(), deleteSql.toString(), p2vs);
				}
			}
		}
		else{
			Data data =	DataCollection.getData(dataName); 
			if(requestObj.containsKey("deleteRows")) {
				JSONObject rowIdToIdValues = requestObj.getJSONObject("deleteRows");    	
				List<String> ids = new ArrayList<String>(); 
				for(Object id : rowIdToIdValues.values().toArray()){
					ids.add((String)id);
				} 
				if(ids.size()==1){
					dbParserAccess.deleteByData(this.getDBSession(), data, ids.get(0));	
				}
				else {		
					dbParserAccess.deleteByData(this.getDBSession(), data, ids);	
				}
			}
			else{
				JSONObject deleteByFieldValue = requestObj.getJSONObject("deleteByFieldValue"); 
				String fieldName = deleteByFieldValue.getString("fieldName");
				DataField field = data.getDataField(fieldName);
				String operateStr = deleteByFieldValue.getString("operateStr");
				String fieldValueStr = deleteByFieldValue.getString("fieldValue");
				Object fieldValue = ValueConverter.convertToDBObject(fieldValueStr, field.getValueType());
				dbParserAccess.deleteByData(this.getDBSession(), data,fieldName,operateStr,fieldValue);
			}
		}
				
	   HashMap<String, Object> resultHash=new HashMap<String, Object>();
	    return resultHash;
	}

	protected void beforeDelete(INcpSession session, JSONObject requestObj) throws Exception{
        String dataName = (String) requestObj.get("dataName");
        if(dataName.equals("d_UserOrgPost")){
        	//实验室项目Mysql数据库-暂用不到如下处理，并且也不支持mysql，后续添加对mysql的支持 
            /*budgetApplicationDao.setDbSession(this.getDBSession());
            Iterator iterator = JSONObject.fromObject(requestObj.get("deleteRows")).keySet().iterator();
            while(iterator.hasNext()) {
                String grid_id = (String) iterator.next();
                String delete_id = (String) JSONObject.fromObject(requestObj.get("deleteRows")).get(grid_id);
                budgetApplicationDao.deleteOrgRole(delete_id);
            }*/
        }
	}
	
	protected void afterDelete(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{
        String dataName = (String) requestObj.get("dataName");
        if(dataName.equals("d_UserOrgPost")){
        	
        	//实验室项目Mysql数据库-暂用不到如下处理，并且也不支持mysql，后续添加对mysql的支持     deleted by liyh 20181225
            /*budgetApplicationDao.setDbSession(this.getDBSession());
            Iterator iterator = JSONObject.fromObject(requestObj.get("deleteRows")).keySet().iterator();
            while(iterator.hasNext()) {
                String grid_id = (String) iterator.next();
                String delete_id = (String) JSONObject.fromObject(requestObj.get("deleteRows")).get(grid_id);
                budgetApplicationDao.createOrgRole(delete_id);
            }*/
        }
	}
	
	//其他操作，用于扩展其他功能
	public HashMap<String,Object> doOtherAction(INcpSession session, JSONObject requestObj) throws RuntimeException{
		try{
			//调用表达式 
			HashMap<String,Object> resultObj = onDoOtherAction(session, requestObj);
			
			if(resultObj == null){
				return onDoOtherAction(session,requestObj);
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
	
	//其他操作，用于扩展其他功能
	protected HashMap<String,Object> onDoOtherAction(INcpSession session, JSONObject requestObj) throws RuntimeException{ 
		return null;  	
	} 
}
