package com.zlp.platform.dao.system.impl;
import com.alibaba.fastjson.JSONObject;
import com.nova.frame.utils.SecurityUtils;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.constants.ZlpState;
import com.zlp.platform.core.ConfigContext;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.system.D_User;

public class D_UserImpl extends DataBaseDao implements D_User {	
	@Override
	protected void beforeSave(INcpSession session, JSONObject requestObj) throws Exception{
	    //插入到数据库	    
	    JSONObject insertRowsObj = requestObj.getJSONObject("insert");
	    String defaultPwd = ConfigContext.getConfigMap().get(ZlpState.PLATFORM_DEFAULT_PASSWORD);
	    int insertRowCount = insertRowsObj.size();
	    Object[] insertRowIds = insertRowsObj.keySet().toArray();
	    for(int i = 0;i < insertRowCount; i++){	    	
	    	String insertRowId = (String)insertRowIds[i];
	    	JSONObject insertRowObj = insertRowsObj.getJSONObject(insertRowId); 
	    	insertRowObj.put("password", SecurityUtils.novaEnCryption(defaultPwd));
	    }
	}	
}
