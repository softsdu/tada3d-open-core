package com.zlp.platform.dao.sys;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.SysConfig; 
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import com.zlp.platform.model.sysmodel.Tree;
import com.zlp.platform.model.sysmodel.TreeCollection;

public class TreeBaseDao implements ITreeBaseDao{

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
	
	private IDataBaseDao getDataDao(String dataName){ 
		IDataBaseDao dataDao = ContextUtil.containsBean(dataName) ? (IDataBaseDao)ContextUtil.getBean(dataName) :  (IDataBaseDao)ContextUtil.getBean("dataBaseDao");
		dataDao.setDBSession(this.getDBSession());
		return dataDao;
	}
	
	/*可重载的	
	HashMap<String, Object> save(INcpSession session, JSONObject requestObj){}
	void beforeSave(INcpSession session, JSONObject requestObj){}
	void afterSave(INcpSession session, HashMap<String,Object> resultHash){}
	
	HashMap<String, Object> delete(INcpSession session, JSONObject requestObj){}
	void beforeDelete(INcpSession session, JSONObject requestObj){}
	void afterDelete(INcpSession session, HashMap<String,Object> resultHash){}
	*/
  
	//保存数据 
	public HashMap<String, Object> save(INcpSession session, JSONObject requestObj) throws RuntimeException {  
		try{
			//调用表达式
			beforeSave(session, requestObj);
			
			//调用表达式 onSave 返回值为HashMap<String, Object> 参数为session, requestObj，如果返回值为空，那么调用saveCore
			HashMap<String, Object> resultHash = saveCore(session, requestObj);

			//调用表达式
			afterSave(session, resultHash);  
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
	
	protected void beforeSave(INcpSession session, JSONObject requestObj){ 
	}
	
	protected void afterSave(INcpSession session, HashMap<String, Object> resultHash){		
	}	
	
	protected HashMap<String, Object> saveCore(INcpSession session, JSONObject requestObj) throws Exception{
		IDBParserAccess dbParserAccess = (IDBParserAccess)ContextUtil.getBean("dBParserAccess");
		String treeName = (String)requestObj.get("treeName"); 
		String dataName = (String)requestObj.get("dataName"); 
		String parentIdValueStr = (String)requestObj.get("parentIdValue");
		//Boolean isRefreshAfterSave = (Boolean) requestObj.get("isRefreshAfterSave");  
		Data data = DataCollection.getData(dataName);
		Tree tree = TreeCollection.getTree(treeName);
	    IDataBaseDao dataDao = getDataDao(dataName);
	    HashMap<String, Object> resultHash = dataDao.save(session, requestObj);

	    //更新父节点isLeaf值
	    if(!parentIdValueStr.isEmpty()){
	    	JSONObject updateParentRowObj = new JSONObject();
	    	updateParentRowObj.put(data.getIdFieldName(), parentIdValueStr);
	    	updateParentRowObj.put(tree.getIsLeafField(), "N"); 
	    	dbParserAccess.updateByData(this.getDBSession(), data, updateParentRowObj, parentIdValueStr);    
	    }
	    
		return resultHash;
	} 
	
	//删除数据 
	public HashMap<String, Object> delete(INcpSession session, JSONObject requestObj) throws RuntimeException {  
		try{
			//调用表达式
			beforeDelete(session, requestObj);
			
			//onDelete 返回值为HashMap<String, Object> 参数为session, requestObj，如果返回值为空，那么调用saveCore
			HashMap<String, Object> resultObj = deleteCore(session, requestObj);
			
			//调用表达式
			afterDelete(session, resultObj); 
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
	
	protected void getNextLevelIds(Data data, Tree tree, IDBParserAccess dbParserAccess, String parentIdValue, List<String> ids) throws Exception{
		List<String> childrenIds =	dbParserAccess.getIds(this.getDBSession(), data.getIdFieldName(), data, tree.getParentPointerField(), "=", parentIdValue);
		for(String childId : childrenIds){
			if(!ids.contains(childId)){
				ids.add(childId);
				getNextLevelIds(data, tree, dbParserAccess, parentIdValue, ids);
			}
		}
	}
	
	protected HashMap<String, Object> deleteCore(INcpSession session, JSONObject requestObj) throws Exception{ 
		IDBParserAccess dbParserAccess = (IDBParserAccess)ContextUtil.getBean("dBParserAccess"); 
		String dataName = requestObj.getString("dataName");	
		String treeName = requestObj.getString("treeName");
		Data data =	DataCollection.getData(dataName);		
		Tree tree =	TreeCollection.getTree(treeName);		
		JSONObject rowIdToIdValues = requestObj.getJSONObject("deleteRows");  
		List<String> selectIds = new ArrayList<String>();
		List<String> allIds = new ArrayList<String>();
		for(Object id : rowIdToIdValues.values().toArray()){
			selectIds.add((String)id); 
		} 
		
		List<String> parentIds = getParentIds(dbParserAccess, data, tree, selectIds);		
		
		for(String id : selectIds){
			if(!allIds.contains(id)){
				allIds.add(id);
				getNextLevelIds(data, tree, dbParserAccess, id, allIds);
			}
		}
		for(String id : allIds){
			if(!selectIds.contains(id)){
				rowIdToIdValues.put(id.toString(), id);
			}
		}
	    IDataBaseDao dataDao = getDataDao(dataName);
	    HashMap<String, Object> resultHash = dataDao.delete(session, requestObj);
		
	    //更新父节点的isleaf值
	    for(String parentId : parentIds){
	    	HashMap<String, Object> p2vs = new HashMap<String, Object>();
	    	p2vs.put(tree.getParentPointerField(), parentId);
	    	Object countObj = dbParserAccess.getSingleValue(this.getDBSession(), "select count(*) from " + data.getName() + " where " + tree.getParentPointerField() + " = " + SysConfig.getParamPrefix()+tree.getParentPointerField(), p2vs);
	    	HashMap<String, Object> fieldValues = new HashMap<String, Object>();
	    	fieldValues.put(tree.getIsLeafField(), Integer.parseInt(countObj.toString()) == 0 ? "Y" : "N");
	    	dbParserAccess.updateByData(this.getDBSession(), data, fieldValues, parentId);    	
	    }
	    
	    return resultHash;
	}
	
	protected List<String> getParentIds(IDBParserAccess dbParserAccess, Data data, Tree tree, List<String> ids) throws Exception{
		List<String> parentIds = new ArrayList<String>();
		for(String id : ids){
			Object parentId = getParentId(dbParserAccess, data, tree, id);
			if(parentId != null && !parentIds.contains(parentId)){
				parentIds.add((String)parentId);
			}
		}
		return parentIds;
	}
	
	private Object getParentId(IDBParserAccess dbParserAccess, Data data, Tree tree, String id) throws Exception{
		String sql = "select " + tree.getParentPointerField() + " from " + data.getName() + " where " + data.getIdFieldName() + " = " +SysConfig.getParamPrefix() + data.getIdFieldName();
    	HashMap<String, Object> p2vs = new HashMap<String, Object>();
    	p2vs.put(data.getIdFieldName(), id);
		Object parentId = dbParserAccess.getSingleValue(this.getDBSession(), sql, p2vs);
		return parentId;
	}
	
	protected void beforeDelete(INcpSession session, JSONObject requestObj){ 
	}
	
	protected void afterDelete(INcpSession session, HashMap<String,Object> resultHash){		
	} 
}
