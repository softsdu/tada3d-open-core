package com.zlp.platform.dao.system.impl;
 
import java.util.HashMap;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.system.D_RoleMenu;
import com.zlp.platform.model.sysmodel.PagePurviewHash;

public class D_RoleMenuImpl extends DataBaseDao  implements D_RoleMenu {

	private PagePurviewHash pagePurviewHash = null;
	public void setPagePurviewHash(PagePurviewHash pagePurviewHash){
		this.pagePurviewHash = pagePurviewHash;
	} 
	
	@Override
	protected void afterSave(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{
		pagePurviewHash.initFromDB();
	}
}
