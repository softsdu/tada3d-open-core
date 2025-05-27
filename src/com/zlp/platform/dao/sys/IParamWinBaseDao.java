package com.zlp.platform.dao.sys;

import java.util.HashMap;

import org.hibernate.Session;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;

public interface IParamWinBaseDao {  
	void setDBSession( Session dbSession);
	HashMap<String, Object> getList(INcpSession session, JSONObject requestObj) throws Exception;
	HashMap<String, Object> doOtherAction(INcpSession session, JSONObject requestObj) throws RuntimeException;
}
