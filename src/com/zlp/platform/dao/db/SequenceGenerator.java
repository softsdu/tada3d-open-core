package com.zlp.platform.dao.db;
 
import java.math.BigDecimal; 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List; 
import java.util.UUID;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate4.HibernateTransactionManager; 

import com.zlp.platform.common.SysConfig;

public class SequenceGenerator implements ISequenceGenerator {   
	
	private HibernateTransactionManager transactionManager; 
	public void setTransactionManager(HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}     

	// 获取Id字段的sequence	
    // 关于这里使用的事务的说明：REQUIRES_NEW不管是否存在事务，该方法总汇为自己发起一个新的事务。如果方法已经运行在一个事务中，则原有事务挂起，新的事务被创建。  
    public List<String> getIdSequence(String tableName, int count) { 
    	List<String> ids = new ArrayList<String>();
    	for(int i=0;i<count;i++){
        	UUID uuid = UUID.randomUUID();  
        	ids.add(uuid.toString()); 
    	}
    	return ids;
        /*
    	try{ 
    		JdbcUtils.beginTransaction();    
        	List<Integer> ids = this.getSequence(tableName, null, "", "", count);
        	JdbcUtils.commitTransaction();
        	return ids;
    	}    	
    	catch(Exception ex){
    		JdbcUtils.rollbackTransaction();
    		throw ex;
    	}
    	finally{ 
    	}*/
	}
 
    // 获取字段sequence，包含前后缀的非id字段
    // 关于这里使用的事务的说明：REQUIRES_NEW不管是否存在事务，该方法总汇为自己发起一个新的事务。如果方法已经运行在一个事务中，则原有事务挂起，新的事务被创建。  
    public List<String> getFieldSequence( String tableName, String fieldName, String prefix, String postfix,int seqNumLength, int count) throws RuntimeException {

    	Session dbSession = null; 
    	Transaction tx = null;
    	try{ 
    		dbSession = this.transactionManager.getSessionFactory().openSession();   
    		tx = dbSession.beginTransaction();
	    	prefix = prefix == null? "": prefix;
	    	postfix = postfix == null? "": postfix;
	    	List<Integer> seqNums = this.getSequence(dbSession, tableName, fieldName, prefix, postfix, count);
	    	List<String> seqValues = new ArrayList<String>();
	    	
	    	//位数不足的，前面补零  modified by liyh 20190114
	    	//String formatStr = "%1$" + seqNumLength + "d";
	    	String formatStr = "%0" + seqNumLength + "d";
	    	
	    	for(int seqNum : seqNums){
	    		seqValues.add(String.format(formatStr, seqNum));
	    	}
	    	tx.commit();
	    	return seqValues;
    	}    	
    	catch(Exception ex){
    		if(tx != null){
    			tx.rollback();
    		}
    		throw new RuntimeException(ex);
    	} 
    	finally{
    		if(dbSession != null){
    			dbSession.close();
    		}
    	}
	}

    private String idSequenceSql = null;
    private String getIdSequenceSql(){
    	if(this.idSequenceSql == null){ 
        	this.idSequenceSql ="select sq.id as id, sq.currentnum as currentnum from sys_sequence sq "
        						+ "where sq.seqname = " + SysConfig.getParamPrefix()+"seqname for update";
    	}
    	return this.idSequenceSql;
    }

    private String fieldSequenceSql = null;
    private String getFieldSequenceSql(){
    	if(this.fieldSequenceSql == null){
        	this.fieldSequenceSql ="select sq.id as id, sq.currentnum as currentnum from sys_sequence sq "
					+ "where sq.seqname = " + SysConfig.getParamPrefix()+"seqname "
					+"and sq.prefix = " + SysConfig.getParamPrefix() + "prefix "
					+ "and sq.postfix = " + SysConfig.getParamPrefix() + "postfix for update";
    	}
    	return this.fieldSequenceSql;
    }

    private String insertSequenceSql = null;
    private String getInsertSequenceSql(){
    	if(this.insertSequenceSql == null){
        	this.insertSequenceSql ="insert into sys_sequence(id, seqname, currentnum, prefix, postfix) "
					+ "values("
        			+ SysConfig.getParamPrefix() + "id, " 
                	+ SysConfig.getParamPrefix() + "seqname, "  
                 	+ SysConfig.getParamPrefix() + "currentnum, "  
                	+ SysConfig.getParamPrefix() + "prefix, "  
                    + SysConfig.getParamPrefix() + "postfix"  
        			+ ")";
    	}
    	return this.insertSequenceSql;
    }

    private String maxSequenceIdSql = null;
    private String getMaxSequenceIdSql(){
    	if(this.maxSequenceIdSql == null){
        	this.maxSequenceIdSql ="select max(sq.id) as maxid from sys_sequence sq";
    	}
    	return this.maxSequenceIdSql;
    }
    
    private String updateSequenceSql = null;
    private String getUpdateSequenceSql(){
    	if(this.updateSequenceSql == null){
        	this.updateSequenceSql ="update sys_sequence set currentnum = " + SysConfig.getParamPrefix() + "currentnum "
					+" where id = " +SysConfig.getParamPrefix() + "id";
    	}
    	return this.updateSequenceSql;
    }

    /*
     * 获取sequence
     */
    private List<Integer> getSequence(Session session, String tableName, String fieldName, String prefix, String postfix,  int count) throws Exception {
    	boolean isId = fieldName == null || fieldName.isEmpty();    	
    	String getSeqSql = isId ? this.getIdSequenceSql(): this.getFieldSequenceSql(); 
    	  
		SQLQuery getSeqQuery = session.createSQLQuery(getSeqSql.toString());
		String seqName = isId ? tableName : tableName+"."+fieldName;
		getSeqQuery.setParameter("seqname", seqName);
		if(!isId){  
			getSeqQuery.setParameter("prefix", prefix);
			getSeqQuery.setParameter("postfix", postfix);
    	}  	 

		List<Object[]> seqRows = getSeqQuery.list();
		if(seqRows.size()==0){
			this.initSeqRowInDB(session, tableName, seqName, prefix, postfix);
			return this.getSequence(session, tableName, fieldName, prefix, postfix, count);
		}
		else {		
			Object[] seqRow = (Object[])seqRows.get(0);
			int id = ((BigDecimal)seqRow[0]).intValue();
			int currentNum = ((BigDecimal)seqRow[1]).intValue();
			int newNum = currentNum + count;
			
			
			//将新的currentnum更新到数据库   modified by liyh 20190114 
			String updateSequenceSql = this.getUpdateSequenceSql();
			/* 下面语句执行有问题，更改执行方式
			HashMap<String, Object> updateSeqP2vs = new HashMap<String, Object>();
			updateSeqP2vs.put("currentnum", BigDecimal.valueOf(newNum));
			updateSeqP2vs.put("id", BigDecimal.valueOf(id));
			session.update(updateSequenceSql, updateSeqP2vs);*/
			SQLQuery updateSql=session.createSQLQuery(updateSequenceSql);
			updateSql.setParameter("currentnum", BigDecimal.valueOf(newNum));
			updateSql.setParameter("id", BigDecimal.valueOf(id));
			updateSql.executeUpdate();		
			
	    	List<Integer> sequenceNums= new ArrayList<Integer>(); 
			for(int i=currentNum+1; i<=newNum; i++){
				sequenceNums.add(i);
			}			
			return sequenceNums;
		}
	}
 
	private void initSeqRowInDB(Session session, String tableName, String seqName, String prefix, String postfix) throws Exception{ 
		SQLQuery maxSeqQuery = session.createSQLQuery(this.getMaxSequenceIdSql()); 
	   	Object results = maxSeqQuery.uniqueResult();
	   	int newId = 1;
	   	if(results  != null){
	   		newId = Integer.parseInt(results.toString()) + 1;
	   	}
		SQLQuery insertSeqQuery = session.createSQLQuery(this.getInsertSequenceSql()); 
		insertSeqQuery.setParameter("id", newId);
		insertSeqQuery.setParameter("seqname", seqName);
		insertSeqQuery.setParameter("currentnum", 0);
		insertSeqQuery.setParameter("prefix", prefix);
		insertSeqQuery.setParameter("postfix", postfix); 
		insertSeqQuery.executeUpdate();			
	}
}
