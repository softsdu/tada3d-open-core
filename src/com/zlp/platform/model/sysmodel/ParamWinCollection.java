package com.zlp.platform.model.sysmodel;

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

public class ParamWinCollection implements InitializingBean{
	private static Logger logger=Logger.getLogger(ParamWinCollection.class);

	//根据json获取数据
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	public IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess ;
	}  

	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}    

	//sql配置
	private static SqlCollection sqls; 
	public void setSqls(SqlCollection sqls){
		ParamWinCollection.sqls = sqls;
	}
	public static SqlCollection getSqls( ){
		return ParamWinCollection.sqls;
	} 
	
	//Data对象
	private static HashMap<String, ParamWin> paramWinObjects ; 
	public static ParamWin getParamWin(String name){
		return ParamWinCollection.paramWinObjects.get(name); 
	}
	
	public void afterPropertiesSet() throws Exception{
		initFromDB();
	} 
 
	public ParamWin reloadParamWinFromDB(String paramWinId) throws Exception{
		SelectSqlParser sys_paramWinSql = this.getDBParserAccess().getSqlParser("sys.model.paramwin");
		SelectSqlParser sys_paramWinUnitSql = this.getDBParserAccess().getSqlParser("sys.model.paramwinunit");
		SelectSqlParser sys_paramWinUnitMapSql = this.getDBParserAccess().getSqlParser("sys.model.paramwinunitmap");

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("id", paramWinId); 
		Session dbSession = null;
		
		try{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
		   	DataTable paramWinDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_paramWinSql, -1, -1, params, "w.id = " + SysConfig.getParamPrefix() + "id", "");//"+SysConfig.getParamPrefix()+"paramWinId", "");
		   	DataTable paramWinUnitDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_paramWinUnitSql, -1, -1, params, "w.id = " + SysConfig.getParamPrefix() + "id", "");//"+SysConfig.getParamPrefix()+"paramWinId", "",  -1, -1);
		   	DataTable paramWinUnitMapDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_paramWinUnitMapSql, -1, -1, params, "w.id = " + SysConfig.getParamPrefix() + "id", "");//"+SysConfig.getParamPrefix()+"paramWinId", "", -1, -1);
		   	 List<DataRow> wRows = paramWinDt.getRows();
		   	if(wRows.size()==0){
		   		throw new Exception("none param win row, id = "+paramWinId);
		   	}
		   	else {
		   		ParamWin paramWin = this.createParamWinObject(wRows.get(0));
		   		 
			   	HashMap<String,ParamWinUnit> wuMap = new HashMap<String,ParamWinUnit>();
			   	for(DataRow wuRow : paramWinUnitDt.getRows()){
			   		ParamWinUnit unit = createParamWinUnitObject(wuRow); 
			   		wuMap.put(unit.getId(),unit);
			   		paramWin.setUnit(unit.getName(), unit);
			   	}
			   	 
			   	HashMap<String,ParamWinUnitMap> umMap = new HashMap<String,ParamWinUnitMap>();
			   	for(DataRow umRow : paramWinUnitMapDt.getRows()){
			   		ParamWinUnitMap um = createParamWinUnitMapObject(umRow);
			   		umMap.put(um.getId(), um);
			   		wuMap.get(um.getParentId()).addParamWinUnitMap(um);
			   	} 	   
	 
			   	ParamWinCollection.paramWinObjects.put(paramWin.getName(), paramWin);		
		   		return paramWin;
		   	}
		}
	   	catch(Exception ex){
	   		throw new Exception("Can not reload param win model from database.", ex);
	   	} 
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}  
	}
 
	public void initFromDB() throws Exception {
		logger.info("init Param Win Collection.");
		Session dbSession = null;
		try
		{
			SelectSqlParser sys_paramWinSql = this.getDBParserAccess().getSqlParser("sys.model.paramwin");
			SelectSqlParser sys_paramWinUnitSql = this.getDBParserAccess().getSqlParser("sys.model.paramwinunit");
			SelectSqlParser sys_paramWinUnitMapSql = this.getDBParserAccess().getSqlParser("sys.model.paramwinunitmap");

			dbSession = this.transactionManager.getSessionFactory().openSession();
		   	DataTable paramWinDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_paramWinSql, -1, -1, null, "", "");
		   	DataTable paramWinUnitDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_paramWinUnitSql, -1, -1, null, "", "" ); 
		   	DataTable paramWinUnitMapDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_paramWinUnitMapSql, -1, -1, null, "", "" );
		   		    
		   	HashMap<String, ParamWin> wMap = new HashMap<String, ParamWin>();
		    for(DataRow wRow : paramWinDt.getRows()){
		    	ParamWin paramWin = createParamWinObject(wRow);
		    	wMap.put(paramWin.getId(), paramWin); 
		    }	 
		   	 
		   	HashMap<String,ParamWinUnit> wuMap = new HashMap<String,ParamWinUnit>();
		   	for(DataRow wuRow : paramWinUnitDt.getRows()){
		   		ParamWinUnit wu = createParamWinUnitObject(wuRow);
		   		wuMap.put(wu.getId(), wu);
		   		wMap.get(wu.getParentId()).setUnit(wu.getName(), wu);
		   	}	 
		   	 
		   	HashMap<String,ParamWinUnitMap> umMap = new HashMap<String,ParamWinUnitMap>();
		   	for(DataRow umRow : paramWinUnitMapDt.getRows()){
		   		ParamWinUnitMap um = createParamWinUnitMapObject(umRow);
		   		umMap.put(um.getId(), um);
		   		wuMap.get(um.getParentId()).addParamWinUnitMap(um);
		   	} 	   
		    
		   	ParamWinCollection.paramWinObjects = new HashMap<String, ParamWin>();
		   	for(String id : wMap.keySet()){
		   		ParamWin w = wMap.get(id);
		   		ParamWinCollection.paramWinObjects.put(w.getName(), w);		
		   	}	
        }
        catch(Exception ex){
        	ex.printStackTrace();
    	    throw new Exception("init Param Win Collection error.", ex);
        }    
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}  
	}	 

	private ParamWin createParamWinObject(DataRow wRow){
    	ParamWin paramWin = new ParamWin();
    	paramWin.setId( wRow.getStringValue("id"));
    	paramWin.setName( wRow.getStringValue("name")); 
    	return paramWin;
    }
	
	private ParamWinUnitMap createParamWinUnitMapObject(DataRow umRow){
    	ParamWinUnitMap um = new ParamWinUnitMap();
    	um.setId( umRow.getStringValue("id"));
    	um.setParentId( umRow.getStringValue("parentid"));
    	um.setDestField( umRow.getStringValue("destfield"));
    	um.setSourceField( umRow.getStringValue("sourcefield")); 
    	return um;
    }
	
	private ParamWinUnit createParamWinUnitObject(DataRow wuRow) throws Exception{
		ParamWinUnit wu = new ParamWinUnit();
   		wu.setId( wuRow.getStringValue("id"));
   		wu.setParentId( wuRow.getStringValue("parentid")); 
   		wu.setName( wuRow.getStringValue("name"));
   		wu.setLabel( wuRow.getStringValue("label"));
   		wu.setValueType(ValueType.valueOf(wuRow.getStringValue("valuetype")));
   		wu.setIsMultiValue( wuRow.getBooleanValue("ismultivalue") );
   		wu.setInputHelpType( wuRow.getStringValue("inputhelptype"));
   		wu.setInputHelpName( wuRow.getStringValue("inputhelpname"));
   		wu.setIsNullable( wuRow.getBooleanValue("isnullable") );
   		wu.setIsEditable( wuRow.getBooleanValue("iseditable") );
   		wu.setValueLength( wuRow.getIntegerValue("valuelength"));  
   		wu.setDefaultValue( wuRow.getStringValue("defaultvalue"));  
   		wu.setDecimalNum(wuRow.getIntegerValue("decimalnum") == null? 0: wuRow.getIntegerValue("decimalnum"));		 
   		return wu;
	} 	
}
