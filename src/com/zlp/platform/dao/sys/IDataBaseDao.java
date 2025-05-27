package com.zlp.platform.dao.sys;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;

public interface IDataBaseDao { 
	void setDBSession( Session dbSession);
	HashMap<String, Object> getInputStatus(INcpSession session, JSONObject requestObj ) throws Exception;
	HashMap<String, Object> getList(INcpSession session, JSONObject requestObj) throws Exception;
	HashMap<String, Object> add(INcpSession session, JSONObject requestObj) throws RuntimeException, Exception;
	HashMap<String, Object> select(INcpSession session, JSONObject requestObj) throws Exception; 
	HashMap<String, Object> save(INcpSession session, JSONObject requestObj) throws RuntimeException ;
	HashMap<String, Object> delete(INcpSession session, JSONObject requestObj) throws RuntimeException;
	HashMap<String, Object> doOtherAction(INcpSession session, JSONObject requestObj) throws RuntimeException;
	List<String> getIds(INcpSession session, String dataName, String fieldName, String operateStr, String fieldValueStr) throws Exception;
	HashMap<String, Object> deleteWithTx(INcpSession session, JSONObject requestObj) throws RuntimeException, SQLException;
	HashMap<String, Object> saveWithTx(INcpSession session, JSONObject requestObj) throws RuntimeException, SQLException ;
	boolean getHasIsDeletedMark();
	String getDeleteTimeFieldName();
	String getIsDeletedFieldName();
	String getModifyUserIdFieldName();
	String getCreateUserIdFieldName();
	boolean getAutoAssignFieldValueBeforeSave();
	String getCreateTimeFieldName();
	String getCompanyIdFieldName();
}
