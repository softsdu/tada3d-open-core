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

public class ReportCollection implements InitializingBean {
	private static Logger logger=Logger.getLogger(ReportCollection.class);

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
		ReportCollection.sqls = sqls;
	}
	public static SqlCollection getSqls( ){
		return ReportCollection.sqls;
	} 
	
	//Tree对象
	private static HashMap<String, Report> reportObjects ; 
	public static Report getReport(String name){
		return ReportCollection.reportObjects.get(name); 
	}
	
	public void afterPropertiesSet() throws Exception{
		initFromDB();
	}
 
	public Report reloadSheetFromDB(String reportId) throws Exception{
		SelectSqlParser sys_reportSql = this.getDBParserAccess().getSqlParser("sys.model.report");
		 
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("id", reportId); 
		Session dbSession = null;		
		try{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
		   	DataTable reportDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_reportSql, -1, -1, params, "r.id = " + SysConfig.getParamPrefix() + "id", "");//"+SysConfig.getParamPrefix()+"reportId", "");
		   	List<DataRow> rRows = reportDt.getRows();
		   	if(rRows.size()==0){
		   		throw new Exception("none report row, id = " + reportId);
		   	}
		   	else {
		   		Report report = this.createReportObject(rRows.get(0)); 
	
		   		ReportCollection.reportObjects.put(report.getName(), report);		
		   		return report;
		   	}
		}
	   	catch(Exception ex){
	   		throw new Exception("Can not reload report model from database.", ex);
	   	} 
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}  
	}
	
	public void initFromDB() throws Exception {
		logger.info("init Report Collection.");
		Session dbSession = null;
		try
		{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
			SelectSqlParser sys_reportSql = this.getDBParserAccess().getSqlParser("sys.model.report");
			DataTable reportDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_reportSql, -1, -1, null, "", "");
		   	
			ReportCollection.reportObjects = new HashMap<String, Report>(); 
		    for(DataRow rRow : reportDt.getRows()){
		    	Report report = createReportObject(rRow); 
		    	ReportCollection.reportObjects.put(report.getName(), report); 
		    }
        }
        catch(Exception ex){
        	ex.printStackTrace();
    	    throw new Exception("init Report Collection error.", ex);
        } 
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}  
	}
	
	private Report createReportObject(DataRow rRow) throws Exception{
    	Report report = new Report();
    	report.setId( rRow.getStringValue("id"));
    	report.setName( rRow.getStringValue("name")); 
    	report.setReportName( rRow.getStringValue("reportname")); 
    	report.setParamWinName( rRow.getStringValue("paramwinname")); 
    	report.setIsAuto( rRow.getBooleanValue("isauto") );
    	return report;
    }
}
