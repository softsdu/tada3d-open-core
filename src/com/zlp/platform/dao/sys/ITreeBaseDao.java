package com.zlp.platform.dao.sys;

import java.sql.SQLException;
import java.util.HashMap;

import org.hibernate.Session;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;

public interface ITreeBaseDao {  
	void setDBSession(Session dbSession);
	HashMap<String, Object> saveWithTx(INcpSession session, JSONObject requestObj) throws RuntimeException, SQLException ;
	HashMap<String, Object> deleteWithTx(INcpSession session, JSONObject requestObj) throws RuntimeException, SQLException; 
	HashMap<String, Object> save(INcpSession session, JSONObject requestObj) throws RuntimeException ;
	HashMap<String, Object> delete(INcpSession session, JSONObject requestObj) throws RuntimeException; 
}
