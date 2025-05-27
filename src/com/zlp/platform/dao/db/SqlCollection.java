package com.zlp.platform.dao.db;
 
import java.util.HashMap;
import java.util.List; 
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate4.HibernateTransactionManager; 

public class SqlCollection implements InitializingBean{ 
	private static Logger logger=Logger.getLogger(SqlCollection.class);
  	
	//select sql的解析对象
	private static HashMap<String, SelectSqlParser> selectSqlObjects ;
	
	//sql列表
	private static HashMap<String, String> sqls;   

	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}     
	
	public void afterPropertiesSet() throws Exception{
		initFromDB();
	} 
 
	public void initFromDB() throws Exception {
		logger.info("init Sql Collection.");
		String sys_sql = "select s.id as id, s.name as name, s.sqlcontext as sqlcontext, s.sqltype as sqltype from sys_sql s";
		String sys_sqlfieldtype = "select ft.id as id,ft.parentid as parentid,ft.name as name,ft.valuetype as valuetype from sys_sqlfieldtype ft";
		Session session = this.transactionManager.getSessionFactory().openSession();
	    List<Object[]> sqlList = session.createSQLQuery(sys_sql).list();
	    List<Object[]> sqlFieldList = session.createSQLQuery(sys_sqlfieldtype).list();  
	    selectSqlObjects = new HashMap<String, SelectSqlParser>();
	    sqls = new HashMap<String, String>();
	    for(Object[] sqlItem:sqlList){ 
	       String id = (String)sqlItem[0];
	       String name = (String)sqlItem[1];
	       String sql = (String)sqlItem[2];
	       String sqltype = (String)sqlItem[3];
	       if("select".equals(sqltype)) {
		       SelectSqlParser sqlParser = new SelectSqlParser(sql);
		       try
		       {
		    	   sqlParser.parser(); 
		    	   selectSqlObjects.put(name,sqlParser);
		    	   HashMap<String, ValueType> fieldTypeMap = new HashMap<String, ValueType>();

			       for(Object[] sqlFieldItem : sqlFieldList){ 
			    	   String parentId = (String)sqlFieldItem[1];
			    	   if(id.equals(parentId)){
				    	   String fieldname = (String)sqlFieldItem[2];
				    	   String valuetypeStr = (String)sqlFieldItem[3];
				    	   ValueType valuetype = Enum.valueOf(ValueType.class, valuetypeStr);
			    		   fieldTypeMap.put(fieldname, valuetype);
			    	   }
			       }
	    		   sqlParser.setFieldTypeMap(fieldTypeMap);
		       }
		       catch(Exception ex){
	            	ex.printStackTrace();
		    	   throw ex;
		       }
	       }
	       sqls.put(name, sql);	       
	    }	    
	}
	
	//获取sql语句
	public static String getSql(String name) throws Exception{
		String sql = sqls.get(name);
		if(sql==null){
			throw new Exception("can not find sql "+name+".");
		}
		return sql;
	}
	
	//获取select sql对象
	public static SelectSqlParser getSelectObject(String name) throws RuntimeException{		
		if(selectSqlObjects.containsKey(name)){
			return selectSqlObjects.get(name);
		}
		else if(sqls.containsKey(name)) {
			try
			{
				String sql = sqls.get(name);
				SelectSqlParser sqlParser = new SelectSqlParser(sql);
				selectSqlObjects.put(name, sqlParser);
				return sqlParser;
			}
			catch(Exception ex){
            	ex.printStackTrace();
				throw new RuntimeException("can not parse select sql named " + name + ". " + ex.getMessage());
			}
		}
		else {
			throw new RuntimeException("none sql named " + name +".");
		}
	}
}