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

public class SheetCollection implements InitializingBean{
	private static Logger logger=Logger.getLogger(SheetCollection.class);

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
		SheetCollection.sqls = sqls;
	}
	public static SqlCollection getSqls( ){
		return SheetCollection.sqls;
	} 
	
	private static HashMap<String, Sheet> sheetObjects ; 
	public static Sheet getSheet(String name){
		return SheetCollection.sheetObjects.get(name); 
	}
	
	public void afterPropertiesSet() throws Exception{
		initFromDB();
	} 
 
	public Sheet reloadSheetFromDB(String sheetId) throws Exception{
		SelectSqlParser sys_sheetSql = this.getDBParserAccess().getSqlParser("sys.model.sheet");
		SelectSqlParser sys_sheetPartSql = this.getDBParserAccess().getSqlParser("sys.model.sheetpart");

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("id", sheetId); 
		Session dbSession = null;
		try
		{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
		   	DataTable sheetDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_sheetSql, -1, -1, params, "s.id = " + SysConfig.getParamPrefix() + "id", "");//+SysConfig.getParamPrefix()+"sheetId", "",);
		   	DataTable sheetPartDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_sheetPartSql, -1, -1, params, "sp.parentid = " + SysConfig.getParamPrefix() + "id", "");//"+SysConfig.getParamPrefix()+"sheetId", "",  -1, -1);
		   	
		   	List<DataRow> sRows = sheetDt.getRows();
		   	if(sRows.size()==0){
		   		throw new Exception("none sheet row, id = " + sheetId);
		   	}
		   	else {
		   		Sheet sheet = this.createSheetObject(sRows.get(0));
		   		
			    //读取Sys_ViewDispunit 
			   	for(DataRow spRow : sheetPartDt.getRows()){
			   		SheetPart sp = createSheetPartObject(spRow);  
			   		sheet.setSheetPart(sp.getName(), sp);
			   	} 
	
		   		sheet.init();
		   		SheetCollection.sheetObjects.put(sheet.getName(), sheet);		
		   		return sheet;
		   	}
        }
        catch(Exception ex){
        	ex.printStackTrace();
    	    throw new Exception("init sheet Collection error.", ex);
        }     
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}  
	}
 
	public void initFromDB() throws Exception {
		logger.info("init Data Collection.");
		Session dbSession = null;
		try
		{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
			SelectSqlParser sys_sheetSql = this.getDBParserAccess().getSqlParser("sys.model.sheet");
			SelectSqlParser sys_sheetPartSql = this.getDBParserAccess().getSqlParser("sys.model.sheetpart");
 
		   	DataTable sheetDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_sheetSql, -1, -1, null, "", "");
		   	DataTable sheetPartDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_sheetPartSql, -1, -1, null, "", ""); 
		   	HashMap<String, Sheet> sMap = new HashMap<String, Sheet>();
		    for(DataRow sRow : sheetDt.getRows()){
		    	Sheet sheet = createSheetObject(sRow);
		    	sMap.put(sheet.getId(), sheet); 
		    }	 
		   	 
		   	for(DataRow spRow : sheetPartDt.getRows()){
		   		SheetPart sp = createSheetPartObject(spRow); 
		   		sMap.get(sp.getParentId()).setSheetPart(sp.getName(), sp);
		   	}	 
		   	 
		    
		   	SheetCollection.sheetObjects = new HashMap<String, Sheet>();
		   	for(String id : sMap.keySet()){
		   		Sheet sheet = sMap.get(id);
		   		sheet.init();
		   		SheetCollection.sheetObjects.put(sheet.getName(), sheet);
		   	}	
        }
        catch(Exception ex){
        	ex.printStackTrace();
    	    throw new Exception("init Sheet Collection error.", ex);
        }    
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}   
	}	 
	
	private Sheet createSheetObject(DataRow sRow){
    	Sheet sheet = new Sheet();
    	sheet.setId( sRow.getStringValue("id"));
    	sheet.setName( sRow.getStringValue("name")); 
    	sheet.setTitle( sRow.getStringValue("title")); 
    	return sheet;
    }
	
	private SheetPart createSheetPartObject(DataRow spRow) throws Exception{
		SheetPart sp = new SheetPart();		
		sp.setId(spRow.getStringValue("id"));
		sp.setParentId( spRow.getStringValue("parentid")); 
		sp.setViewName( spRow.getStringValue("viewname")); 
   		sp.setName( spRow.getStringValue("name"));
   		sp.setTitle( spRow.getStringValue("title"));
   		sp.setLabelField( spRow.getStringValue("labelfield"));
   		sp.setParentPartName( spRow.getStringValue("parentpartname"));
   		sp.setParentPointerField( spRow.getStringValue("parentpointerfield"));
   		return sp;
	}
}
