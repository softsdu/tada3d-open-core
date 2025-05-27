package com.zlp.platform.dao.sys;
 
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession; 
import com.zlp.platform.model.sysmodel.Sheet;
import com.zlp.platform.model.sysmodel.SheetCollection;
import com.zlp.platform.model.sysmodel.SheetPart;
import com.zlp.platform.model.sysmodel.View;
import com.zlp.platform.model.sysmodel.ViewCollection;

public class SheetBaseDao implements ISheetBaseDao{

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

	protected IDataBaseDao getDataDao(String dataName){ 
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
	
	protected void afterSave(INcpSession session, JSONObject requestObj, HashMap<String, Object> resultHash) throws Exception{
		String sheetName = (String)requestObj.get("sheetName");
		Sheet sheet = SheetCollection.getSheet(sheetName);
		SheetPart mainPart = sheet.getMainPart();
		View view = ViewCollection.getView(mainPart.getViewName());
		String dataName = view.getDataName(); 
	}
	
	protected HashMap<String, Object> saveCore(INcpSession session, JSONObject requestObj) throws Exception{ 
		String sheetName = (String)requestObj.get("sheetName"); 
		Boolean isRefreshAfterSave = (Boolean) requestObj.get("isRefreshAfterSave"); 
		JSONObject mainArgsJson = requestObj.getJSONObject("mainArgs");
		JSONObject lineArgsJson = requestObj.getJSONObject("lineArgs"); 
		JSONObject rowId2Ids = requestObj.getJSONObject("rowId2Ids");  
		Sheet sheet = SheetCollection.getSheet(sheetName);
		SheetPart mainPart = sheet.getMainPart();
		View view = ViewCollection.getView(mainPart.getViewName());
		String dataName = view.getDataName();
		IDataBaseDao dDao = this.getDataDao(dataName);
		mainArgsJson.put("dataName", dataName);
		mainArgsJson.put("isRefreshAfterSave", isRefreshAfterSave);
		HashMap<String, Object> mainResultHash= dDao.save(session, mainArgsJson);
		JSONObject mainIdValueToRowIds = (JSONObject)mainResultHash.get("idValueToRowIds"); 
		String mainIdValue = "-1";
		for(Object idValue : mainIdValueToRowIds.keySet()){
			String rowId = (String)mainIdValueToRowIds.get(idValue);
			rowId2Ids.put(rowId, idValue);
			mainIdValue = idValue.toString();
		}
		List<SheetPart> middleParts = mainPart.getChildParts();
		for(SheetPart middlePart : middleParts){
			List<SheetPart> lastParts = middlePart.getChildParts();
			View middleView = ViewCollection.getView(middlePart.getViewName());
			String middleDataName = middleView.getDataName();
			IDataBaseDao mdDao = this.getDataDao(middleDataName);
			JSONObject mR = lineArgsJson.getJSONObject(middlePart.getName());
			
			//增加是否为空的判断，修改成fastJson后，可能返回时空 modified by ls 20221010
			if(mR != null){
				for(Object parentRowIdObj : mR.keySet()){
					String parentRowId = parentRowIdObj.toString();
					JSONObject mS = mR.getJSONObject(parentRowId);
					mS.put("dataName", middleDataName);
					
					//删除子表记录后删除本记录
					JSONObject deleteRowsObj = mS.getJSONObject("deleteRows");
					if(deleteRowsObj.size() != 0){
						for(Object idValueObj : deleteRowsObj.values().toArray()){
							for(SheetPart lastPart : lastParts){
								View lastView = ViewCollection.getView(lastPart.getViewName());
								String lastDataName = lastView.getDataName();
								IDataBaseDao ldDao = this.getDataDao(lastDataName);
								JSONObject deleteChildJSON = new JSONObject();
								JSONObject deleteByFieldValueJSON = new JSONObject(); 
								deleteChildJSON.put("dataName", lastDataName);
								deleteByFieldValueJSON.put("fieldName", lastPart.getParentPointerField());
								deleteByFieldValueJSON.put("operateStr", "=");
								deleteByFieldValueJSON.put("fieldValue", idValueObj.toString());
								deleteChildJSON.put("deleteByFieldValue", deleteByFieldValueJSON);
								ldDao.delete(session, deleteChildJSON);
							}
						}
						mdDao.delete(session, mS);
					}
					
					if(mS.getJSONObject("update").size()>0 ||mS.getJSONObject("insert").size()>0){
						mS.put("isRefreshAfterSave", false); 
						for(Object rowObj : mS.getJSONObject("update").values().toArray()){
							JSONObject row = (JSONObject)rowObj;
							row.put(middlePart.getParentPointerField(), mainIdValue);
						}
		
						for(Object rowObj : mS.getJSONObject("insert").values().toArray()){
							JSONObject row = (JSONObject)rowObj;
							row.put(middlePart.getParentPointerField(), mainIdValue);
						}
						HashMap<String, Object> mResultHash= mdDao.save(session, mS); 
						JSONObject mIdValueToRowIds = (JSONObject)mResultHash.get("idValueToRowIds");
						for(Object idValue : mIdValueToRowIds.keySet()){
							String rowId = (String)mIdValueToRowIds.get(idValue);
							rowId2Ids.put(rowId, idValue.toString()); 
						}
					}
				}
			}
			
			for(SheetPart lastPart : lastParts){
				View lastView = ViewCollection.getView(lastPart.getViewName());
				String lastDataName = lastView.getDataName();
				IDataBaseDao ldDao = this.getDataDao(lastDataName);
				JSONObject lR = lineArgsJson.getJSONObject(lastPart.getName());
				for(Object parentRowIdObj : lR.keySet()){
					String parentRowId = parentRowIdObj.toString();
					String mIdValue = (String)rowId2Ids.get(parentRowId);
					JSONObject lS = lR.getJSONObject(parentRowId);
					lS.put("dataName", lastDataName);
					if(lS.getJSONObject("update").size()>0 ||lS.getJSONObject("insert").size()>0){
						lS.put("isRefreshAfterSave", false);
						for(Object rowObj : lS.getJSONObject("update").values().toArray()){
							JSONObject row = (JSONObject)rowObj;
							row.put(lastPart.getParentPointerField(), mIdValue);
						}
	
						for(Object rowObj : lS.getJSONObject("insert").values().toArray()){
							JSONObject row = (JSONObject)rowObj;
							row.put(lastPart.getParentPointerField(), mIdValue);
						}
						ldDao.save(session, lS);
					}
					if(lS.getJSONObject("deleteRows").size()>0){
						ldDao.delete(session, lS);
					}
				}
			}
		} 
		return mainResultHash;
	} 
	
	//删除数据 
	public HashMap<String, Object> delete(INcpSession session, JSONObject requestObj) throws RuntimeException {  
		try{
			//调用表达式
			beforeDelete(session, requestObj);
			
			//onDelete 返回值为HashMap<String, Object> 参数为session, requestObj，如果返回值为空，那么调用saveCore
			HashMap<String, Object> resultObj = deleteCore(session, requestObj);

			//调用表达式
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
		String sheetName = (String)requestObj.get("sheetName");  
		JSONObject deleteRowsJSON = requestObj.getJSONObject("deleteRows");   
		Sheet sheet = SheetCollection.getSheet(sheetName);
		SheetPart mainPart = sheet.getMainPart();
		List<SheetPart> middleParts = mainPart.getChildParts();
		View mainView = ViewCollection.getView(mainPart.getViewName());
		String mainDataName = mainView.getDataName();
		IDataBaseDao dDao = this.getDataDao(mainDataName);  
		if(deleteRowsJSON.size() != 0){
			//删除子表
			//如果是非真删除，那么就别删除子表了 modified by ls 20200326
			if(!dDao.getHasIsDeletedMark()){
				for(Object mainIdValueObj : deleteRowsJSON.values().toArray()){
					for(SheetPart middlePart : middleParts){
						List<SheetPart> lastParts = middlePart.getChildParts();
						View middleView = ViewCollection.getView(middlePart.getViewName());
						String middleDataName = middleView.getDataName();
						IDataBaseDao mdDao = this.getDataDao(middleDataName);
						List<String> mIds = mdDao.getIds(session, middleDataName, middlePart.getParentPointerField(), "=", mainIdValueObj.toString());
						for(String mId : mIds){ 
							for(SheetPart lastPart : lastParts){
								View lastView = ViewCollection.getView(lastPart.getViewName());
								String lastDataName = lastView.getDataName();
								IDataBaseDao ldDao = this.getDataDao(lastDataName);
								JSONObject lDeleteChildJSON = new JSONObject();
								JSONObject lDeleteByFieldValueJSON = new JSONObject(); 
								lDeleteChildJSON.put("dataName", lastDataName);
								lDeleteByFieldValueJSON.put("fieldName", lastPart.getParentPointerField());
								lDeleteByFieldValueJSON.put("operateStr", "=");
								lDeleteByFieldValueJSON.put("fieldValue", mId.toString());
								lDeleteChildJSON.put("deleteByFieldValue", lDeleteByFieldValueJSON);
								ldDao.delete(session, lDeleteChildJSON);
							}
						}
						JSONObject mDeleteChildJSON = new JSONObject();
						JSONObject mDeleteByFieldValueJSON = new JSONObject(); 
						mDeleteChildJSON.put("dataName", middleDataName);
						mDeleteByFieldValueJSON.put("fieldName", middlePart.getParentPointerField());
						mDeleteByFieldValueJSON.put("operateStr", "=");
						mDeleteByFieldValueJSON.put("fieldValue", mainIdValueObj.toString());
						mDeleteChildJSON.put("deleteByFieldValue", mDeleteByFieldValueJSON);
						mdDao.delete(session, mDeleteChildJSON);
					}
				}
			} 
		}
		requestObj.put("dataName", mainDataName);
		return dDao.delete(session, requestObj);
	}
	
	protected void beforeDelete(INcpSession session, JSONObject requestObj) throws Exception{ 
	}
	
	protected void afterDelete(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{		
	} 
}
