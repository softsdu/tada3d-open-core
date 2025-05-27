package com.zlp.platform.common;

import java.util.HashMap;
import java.util.List;

import com.zlp.platform.common.redis.RedisException;
import com.zlp.platform.dao.sys.Org;
import com.zlp.platform.dao.sys.Role;

public interface INcpSession {
	void invalidate() throws RedisException; 
	String getUserName();
	String getUserCode();
	
	//新增用户类型 usertype（默认为1）： 1-内部用户；2-外部用户（注册申请生成） added by liyh 20190103
	String getUserType();

	String getUserId();

	String getCompanyId();
	
	boolean getIsOnline() throws RedisException;
	List<Org> getOrgList();
	void setOrgList(List<Org> orgList) throws RedisException;
	List<Role> getRoleList();
	HashMap<String,Object> toHashMap();
	List<String> getCellList();
	void setCellList(List<String> cellList) throws RedisException;

	//added by liyh 20221129
	void setUserId(String userId) throws RedisException;
	void setCompanyId(String companyId) throws RedisException;
}
