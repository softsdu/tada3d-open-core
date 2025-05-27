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

public class DocTypeCollection implements InitializingBean{
	private static Logger logger=Logger.getLogger(DocTypeCollection.class);

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
	
	//Tree对象
	private static HashMap<String, DocType> docTypeObjects ; 
	public static DocType getDocType(String name){
		return DocTypeCollection.docTypeObjects.get(name); 
	}
	
	public void afterPropertiesSet() throws Exception{
		initFromDB();
	} 
 
	public DocType reloadDocTypeFromDB(String docTypeId) throws Exception{
		SelectSqlParser sys_docTypeSql = this.getDBParserAccess().getSqlParser("sys.workflow.docType");
		 
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("id", docTypeId); 
		Session dbSession = null;
		
		try{
			dbSession = this.transactionManager.getSessionFactory().openSession();		
		   	DataTable docTypeDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_docTypeSql, -1, -1, params, "dt.id = " + SysConfig.getParamPrefix() + "id", "");
		   	List<DataRow> dtRows = docTypeDt.getRows();
		   	if(dtRows.size()==0){
		   		throw new Exception("none document type row, id = " + docTypeId);
		   	}
		   	else {
		   		DataRow row = dtRows.get(0); 
		   		DocType docType = this.createDocTypeObject(row); 		
		   		DocTypeCollection.docTypeObjects.put(docType.getName(), docType);	 
		   		return docType;
		   	}
		}
	   	catch(Exception ex){
	   		throw new Exception("Can not reload document type model from database.", ex);
	   	} 
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}  
	} 
	
	public void initFromDB() throws Exception {
		logger.info("init DocType Collection.");
		Session dbSession = null;
		try
		{
			dbSession = this.transactionManager.getSessionFactory().openSession();	
			SelectSqlParser sys_docTypeSql = this.getDBParserAccess().getSqlParser("sys.workflow.docType");
 
		   	DataTable docTypeDt = this.getDBParserAccess().getDtBySqlParser(dbSession, sys_docTypeSql, -1, -1, null, "", "");
		    
		   	DocTypeCollection.docTypeObjects = new HashMap<String, DocType>(); 
		    for(DataRow dtRow : docTypeDt.getRows()){
		    	DocType docType = createDocTypeObject(dtRow); 
		    	DocTypeCollection.docTypeObjects.put(docType.getName(), docType); 
		    }		    
        }
        catch(Exception ex){
        	ex.printStackTrace();
    	    throw new Exception("init DocType Collection error.", ex);
        }    
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}    
	}	 
	
	private DocType createDocTypeObject(DataRow dtRow){
		DocType docType = new DocType();
		docType.setId( dtRow.getStringValue("id"));
		docType.setName( dtRow.getStringValue("name")); 
		docType.setSheetName( dtRow.getStringValue("sheetname"));  
		docType.setUserIdFieldName( dtRow.getStringValue("useridfieldname"));  
		docType.setOrgIdFieldName( dtRow.getStringValue("orgidfieldname"));  
		docType.setIsDeletedFieldName(dtRow.getStringValue("isdeletedfieldname")); 
		docType.setCreatePageUrl(dtRow.getStringValue("createpageurl")); 
		docType.setQueryPageUrl(dtRow.getStringValue("querypageurl"));  
    	return docType;
    } 
	
}
