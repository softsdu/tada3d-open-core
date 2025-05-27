package com.zlp.mdl.processor;

import java.sql.SQLException;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import org.hibernate.Session;

import com.zlp.platform.common.INcpSession;
import com.zlp.platform.dao.db.DataRow; 

public interface IMaterialProcessor {

	void setDBSession(Session dbSession);
 
	void generateStandardMaterialFile(INcpSession session) throws Exception;


	void generateAllStandardMaterialFiles() throws Exception;

	//根据材质的名称，获取材质的信息（从数据库中获取） added by ls 20220606
	DataRow getMaterialRow(INcpSession session, String materialName) throws SQLException;

	DataRow getMaterialRow(String userId, String materialName) throws SQLException;

	//获取材质JSON added by ls 20240109
	JSONObject getMaterialJson(String materialCode) throws Exception;

	JSONObject getMaterialJson(String userId, String materialCode) throws Exception;

	List<JSONObject> getAllMaterialJsons() throws Exception;
}
