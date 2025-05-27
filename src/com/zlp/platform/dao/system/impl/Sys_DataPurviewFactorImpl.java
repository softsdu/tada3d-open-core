package com.zlp.platform.dao.system.impl;
 
import java.util.HashMap;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.system.Sys_DataPurviewFactor;

public class Sys_DataPurviewFactorImpl extends DataBaseDao implements Sys_DataPurviewFactor { 
	@Override
	protected void afterSave(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{
	}
}
