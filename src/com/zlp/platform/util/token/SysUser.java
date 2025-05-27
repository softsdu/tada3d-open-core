package com.zlp.platform.util.token;

import java.util.HashMap;
import java.util.List;
import com.alibaba.fastjson.JSONArray;
import com.zlp.platform.dao.sys.Org;
import com.zlp.platform.dao.sys.Role; 

public class SysUser {
	
	public SysUser() { 
		
	}
	
	public SysUser(String userId) { 
		this.userId = userId;  
	}

	private String userName;

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	private String userCode;

	public String getUserCode() {
		return this.userCode;
	}
	
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	//新增用户类型 usertype（默认为1）： 1-内部用户；2-外部用户（注册申请生成） added by liyh 20190103
	private String userType;
	public String getUserType() {
		return this.userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}

	
	private String userId;

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	private List<Org> orgList;

	public List<Org> getOrgList() {
		return this.orgList;
	}

	public void setOrgList(List<Org> orgList) {
		this.orgList = orgList;
	}

	private List<Role> roleList;

	public List<Role> getRoleList() {
		return this.roleList;
	}

	public void setRoleList(List<Role> roleList) {
		this.roleList = roleList;
	}
	
	public HashMap<String, Object> toHashMap() {
		HashMap<String, Object> sessionHash = new HashMap<String, Object>();
		sessionHash.put("userId", this.getUserId());
		sessionHash.put("userName", this.getUserName());
		sessionHash.put("userCode", this.getUserCode());

		JSONArray orgArray = new JSONArray();
		for (Org org : this.getOrgList()) {
			HashMap<String, Object> orgObj = new HashMap<String, Object>();
			orgObj.put("id", org.getId());
			orgObj.put("code", org.getCode());
			orgObj.put("name", org.getName());
			orgArray.add(orgObj);
		}
		sessionHash.put("orgList", orgArray);

		JSONArray roleArray = new JSONArray();
		for (Role role : this.getRoleList()) {
			HashMap<String, Object> roleObj = new HashMap<String, Object>();
			roleObj.put("id", role.getId());
			roleObj.put("code", role.getCode());
			roleObj.put("name", role.getName());
			roleArray.add(roleObj);
		}
		sessionHash.put("roleList", roleArray);

		return sessionHash;
	}

}
