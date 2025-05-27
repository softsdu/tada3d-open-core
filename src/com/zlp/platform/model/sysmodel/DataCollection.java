package com.zlp.platform.model.sysmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List; 
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

import com.zlp.platform.common.SysConfig; 
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.SelectSqlParser;
import com.zlp.platform.dao.db.SqlCollection; 
import com.zlp.platform.dao.db.ValueType;

public class DataCollection implements InitializingBean {
	private static Logger logger=Logger.getLogger(DataCollection.class);

	//根据json获取数据
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	public IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess;
	}  

	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}    

	//sql配置
	private static SqlCollection sqls; 
	public void setSqls(SqlCollection sqls){
		DataCollection.sqls = sqls;
	}
	public static SqlCollection getSqls( ){
		return DataCollection.sqls;
	} 
	
	//Data对象
	private static HashMap<String, Data> dataObjects ; 
	public static Data  getData(String name){
		return DataCollection.dataObjects.get(name); 
	}
	
	public void afterPropertiesSet() throws Exception{
		initFromDB();
	} 
	
	//重新加载某个data 
	public Data reloadDataFromDB(String dataId) throws Exception{		
		SelectSqlParser sys_dataSql = this.getDBParserAccess().getSqlParser("sys.model.data");
		SelectSqlParser sys_datafieldSql = this.getDBParserAccess().getSqlParser("sys.model.datafield");
		SelectSqlParser sys_datafieldmapSql = this.getDBParserAccess().getSqlParser("sys.model.datafieldmap");
		SelectSqlParser sys_eventexpressionSql = this.getDBParserAccess().getSqlParser("sys.model.eventexpression");

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("id", dataId); 
		Session dbSession = null;		
		try{
			dbSession = this.transactionManager.getSessionFactory().openSession();		
		   	DataTable dataDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_dataSql, 0, -1, params, "d.id = " + SysConfig.getParamPrefix() + "id", "");
		   	DataTable dataFieldDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_datafieldSql, 0, -1, params, "d.id = " + SysConfig.getParamPrefix() + "id", "");
		   	DataTable dataFieldMapDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_datafieldmapSql, 0, -1, params, "d.id = " + SysConfig.getParamPrefix() + "id", "");
		   	DataTable eventExpDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_eventexpressionSql, 0, -1, params, "ee.parentid = " + SysConfig.getParamPrefix() + "id", "");
		   	List<DataRow> dRows = dataDt.getRows();
		   	if(dRows.size()==0){
		   		throw new Exception("none data row, id = " + dataId);
		   	}
		   	else {
		   		Data data = this.createDataObject(dRows.get(0));

			    //读取DataField
			   	HashMap<String,DataField> dfMap = new HashMap<String,DataField>();
			   	for(DataRow dfRow : dataFieldDt.getRows()){
			   		DataField df = createDataFieldObject(dfRow); 
			   		dfMap.put(df.getId(),df);
			   		data.setDataField(df.getName(), df);
			   	}
			   	
			    //读取EventExpression 
			   	for(DataRow eeRow : eventExpDt.getRows()){
			   		EventServerExpression ee = createEventExpressionObject(eeRow); 
			   		List<EventServerExpression> eeList = data.getEventToExps(ee.getEventName());
			   		if(eeList == null){
			   			eeList = new ArrayList<EventServerExpression>();
			   			data.setEventToExps(ee.getEventName(), eeList);
			   		} 
			   		eeList.add(ee); 
			   	} 
			   	
			   	//读取DataFieldMap
			   	HashMap<String,DataFieldMap> dfmMap = new HashMap<String,DataFieldMap>();
			   	for(DataRow dfmRow : dataFieldMapDt.getRows()){
			   		DataFieldMap dfm = createDataFieldMapObject(dfmRow);
			   		dfmMap.put(dfm.getId(), dfm);
			   		dfMap.get(dfm.getParentId()).addDataFieldMap(dfm);
			   	} 	   
	
		   		initSqlParser(data);
		   		DataCollection.dataObjects.put(data.getName(), data);
		   		return data;	
			}
		}
	   	catch(Exception ex){
	   		throw new Exception("Can not reload data model from database.", ex);
	   	} 
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}
	}
	 
	//从数据库中加载全部data 
	public void initFromDB() throws Exception {
		logger.info("init Data Collection.");
		Session dbSession = null;
		try
		{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
			SelectSqlParser sys_dataSql = this.getDBParserAccess().getSqlParser("sys.model.data");
			SelectSqlParser sys_datafieldSql = this.getDBParserAccess().getSqlParser("sys.model.datafield");
			SelectSqlParser sys_datafieldmapSql = this.getDBParserAccess().getSqlParser("sys.model.datafieldmap");
			SelectSqlParser sys_eventexpressionSql = this.getDBParserAccess().getSqlParser("sys.model.eventexpression");
 
		   	DataTable dataDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_dataSql, -1, -1, null, "", "");
		   	DataTable dataFieldDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_datafieldSql, -1, -1, null, "", "");
		   	DataTable dataFieldMapDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_datafieldmapSql, -1, -1, null, "", "");
		   	DataTable eventExpDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_eventexpressionSql, -1, -1, null, "e.category = 'dataServer'", "");
		   		
		    //读取DataFieldMap
		   	HashMap<String, Data> vMap = new HashMap<String, Data>();
		    for(DataRow vRow : dataDt.getRows()){
		    	Data data = createDataObject(vRow);
		    	vMap.put(data.getId(), data); 
		    }	 
		   	
		    //读取DataField
		   	HashMap<String,DataField> vfMap = new HashMap<String,DataField>();
		   	for(DataRow vfRow : dataFieldDt.getRows()){
		   		DataField vf = createDataFieldObject(vfRow);
		   		vfMap.put(vf.getId(), vf);
		   		String parentId = vf.getParentId();
		   		Data data = vMap.get(parentId);
		   		String vfName = vf.getName(); 
		   		data.setDataField(vfName, vf);
		   	}	 
		   	
		    //读取EventExpression 
		   	for(DataRow eeRow : eventExpDt.getRows()){
		   		EventServerExpression ee = createEventExpressionObject(eeRow); 
		   		String parentId = ee.getParentId();
		   		Data data = vMap.get(parentId);
		   		List<EventServerExpression> eeList = data.getEventToExps(ee.getEventName());
		   		if(eeList == null){
		   			eeList = new ArrayList<EventServerExpression>();
		   			data.setEventToExps(ee.getEventName(), eeList);
		   		} 
		   		eeList.add(ee); 
		   	} 
		   	
		   	//读取DataFieldMap
		   	HashMap<String,DataFieldMap> vfmMap = new HashMap<String,DataFieldMap>();
		   	for(DataRow vfmRow : dataFieldMapDt.getRows()){
		   		DataFieldMap vfm = createDataFieldMapObject(vfmRow);
		   		vfmMap.put(vfm.getId(), vfm);
		   		vfMap.get(vfm.getParentId()).addDataFieldMap(vfm);
		   	} 	   
		    
		   	DataCollection.dataObjects = new HashMap<String, Data>();
		   	for(String id : vMap.keySet()){
		   		Data data = vMap.get(id);
		   		DataCollection.dataObjects.put(data.getName(), data);		   		
		    	//构造SelectSqlParser
		   		initSqlParser(data);
		   	}	
        }
        catch(Exception ex){
        	ex.printStackTrace();
    	    throw new Exception("init Data Collection error.", ex);
        }    
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}
	}	 
	
	private Data createDataObject(DataRow vRow){
    	Data data = new Data();
    	data.setId(vRow.getStringValue("id"));
    	data.setDsExp(vRow.getStringValue("dsexp"));
    	data.setDsType(vRow.getStringValue("dstype"));
    	data.setName(vRow.getStringValue("name")); 
    	data.setSaveDest(vRow.getStringValue("savedest")); 
    	data.setSaveType(vRow.getStringValue("savetype"));  
    	data.setIdFieldName(vRow.getStringValue("idfieldname"));  
    	return data;
    }
	
	private EventServerExpression createEventExpressionObject(DataRow eeRow){
		EventServerExpression ee = new EventServerExpression();
    	ee.setId(eeRow.getStringValue("id"));
    	ee.setDescription(eeRow.getStringValue("description"));
    	ee.setEventCategory(eeRow.getStringValue("eventcategory"));
    	ee.setEventId(eeRow.getStringValue("eventid")); 
    	ee.setEventName(eeRow.getStringValue("eventname")); 
    	ee.setEventResultValueType(eeRow.getStringValue("eventresultvaluetype"));  
    	ee.setExp(eeRow.getStringValue("exp"));  
    	ee.setEventDescription(eeRow.getStringValue("eventdescription"));  
    	ee.setParentId(eeRow.getStringValue("parentid"));   
    	return ee;
    }
	
	private DataField createDataFieldObject(DataRow dfRow) throws Exception{
   		DataField df = new DataField();
   		df.setId( dfRow.getStringValue("id"));
   		df.setParentId( dfRow.getStringValue("parentid")); 
   		df.setDecimalNum(dfRow.getIntegerValue("decimalnum") == null? 0 : dfRow.getIntegerValue("decimalnum"));
   		df.setDisplayName( dfRow.getStringValue("displayname"));
   		df.setForeignKeyName( dfRow.getStringValue("foreignkeyname"));
   		df.setInputHelpName( dfRow.getStringValue("inputhelpname"));
   		df.setInputHelpType( dfRow.getStringValue("inputhelptype"));
   		df.setIsComma(dfRow.getBooleanValue("iscomma"));
   		df.setIsReadonly( dfRow.getBooleanValue("isreadonly") );
   		df.setIsSave( dfRow.getBooleanValue("issave") ); 
   		df.setIsSum( dfRow.getBooleanValue("issum") ); 
   		df.setIsMulti( dfRow.getBooleanValue("ismulti") ); 
   		df.setName( dfRow.getStringValue("name"));
   		df.setValueLength( dfRow.getIntegerValue("valuelength"));
   		df.setValueType( dfRow.getStringValue("valuetype").trim());
   		df.setDataPurviewFactor( dfRow.getStringValue("datapurviewfactor"));
   		return df;
	}
	
	private DataFieldMap createDataFieldMapObject(DataRow vfmRow){
   		DataFieldMap vfm = new DataFieldMap();
   		vfm.setId(vfmRow.getStringValue("id"));
   		vfm.setParentId(vfmRow.getStringValue("parentid"));
   		vfm.setDestField(vfmRow.getStringValue("destfield"));
   		vfm.setSourceField(vfmRow.getStringValue("sourcefield"));
   		return vfm;
	}
	
	private void initSqlParser(Data data) throws Exception{
    	if("sql".equals(data.getDsType())) {
    		try
    		{
	    		SelectSqlParser sqlParser = new SelectSqlParser(data.getDsExp());
				sqlParser.parser(); 
				data.setDsSqlParser(sqlParser);
				HashMap<String, ValueType> fieldTypeMap = new HashMap<String, ValueType>();						
			    for(DataField dataField : data.getDataFields().values()){   
		    		fieldTypeMap.put(dataField.getName(), dataField.getValueType());
			    }
			    sqlParser.setFieldTypeMap(fieldTypeMap);
    		}
    		catch(Exception ex){
            	ex.printStackTrace();
        	    throw new Exception("init Data Collection error. dataId = " + data.getId(), ex);
    		}
    	}
	}
	
}
