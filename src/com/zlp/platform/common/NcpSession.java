package com.zlp.platform.common;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest; 
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import com.alibaba.fastjson.JSONArray;
import com.zlp.platform.common.redis.RedisException;
import com.zlp.platform.common.redis.RedisSessionCacheClient;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dao.sys.Org;
import com.zlp.platform.dao.sys.Role; 

//从redis里获取session信息 added by ls 20190730
public class NcpSession implements INcpSession {
	 
	private String cSessionId;
	public String getCSessionId(){
		return this.cSessionId;
	} 
	
	public void setCSessionId(String cSessionId){
		this.cSessionId = cSessionId;
	}
  
	public RedisSessionCacheClient getRedisSessionCacheClient(){
		return (RedisSessionCacheClient)ContextUtil.getBean("redisSessionCacheClient");
	}  
	 
	public NcpSession(WebServiceContext wsContext,boolean isInitSessionValue) throws Exception{
		try
		{
			if(isInitSessionValue){
				MessageContext mc = wsContext.getMessageContext();
				HttpServletRequest request = (HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST); 
				cSessionId = getCookieSessionId(request.getCookies());
				RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
				if(jedisClient.getAttribute(cSessionId, "userId") == null){
					throw new Exception("尚未登录 或 系统超时(长时间没有操作)，请重新登录系统.");
				}
				else{
					this.userName = jedisClient.getAttribute(cSessionId, "userName");
					this.userCode = jedisClient.getAttribute(cSessionId, "userCode");
					this.userType = jedisClient.getAttribute(cSessionId, "userType");
					this.userId = jedisClient.getAttribute(cSessionId, "userId");
					this.companyId = jedisClient.getAttribute(cSessionId, "companyId");
					this.orgList = jedisClient.getOrgAttribute(cSessionId);
					this.roleList = jedisClient.getRoleAttribute(cSessionId); 
					this.cellList = jedisClient.getCellAttribute(cSessionId); 
				}
			}
			else {
				MessageContext mc = wsContext.getMessageContext();
				HttpServletRequest request = (HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST);
				String cSessionId = getCookieSessionId(request.getCookies());
				RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
				 
				if(jedisClient.getAttribute(cSessionId, "userId") != null){
					this.userName = jedisClient.getAttribute(cSessionId, "userName");
					this.userCode = jedisClient.getAttribute(cSessionId, "userCode");
					this.userType = jedisClient.getAttribute(cSessionId, "userType");
					this.userId = jedisClient.getAttribute(cSessionId, "userId");
					this.companyId = jedisClient.getAttribute(cSessionId, "companyId");
					this.orgList = jedisClient.getOrgAttribute(cSessionId);
					this.roleList = jedisClient.getRoleAttribute(cSessionId); 
					this.cellList = jedisClient.getCellAttribute(cSessionId); 
				} 
			}
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new Exception("无法获取当前用户信息, 请确定是否已登出或登录超时. ", ex);
		}
	}	 
	
	public static String getCookieSessionId(Cookie[] cookies) throws NcpException{
		String cSessionId = "";
		if(cookies != null){
			for(int i = 0; i < cookies.length; i++){
				Cookie c = cookies[i];
				String cName = c.getName();
				if(cName.equals("cSessionId")){
					cSessionId = c.getValue();
					break;
				}
			}
		} 
		return cSessionId;
	}
	 
	public NcpSession(Cookie[] cookies, boolean isInitSessionValue) throws Exception {
		try {
			cSessionId = getCookieSessionId(cookies);
			if (isInitSessionValue) {
				RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
				if(jedisClient.getAttribute(cSessionId, "userId") == null){
					throw new Exception("尚未登录 或 系统超时(长时间没有操作)，请重新登录系统.");
				}
				else{				
					this.userName = jedisClient.getAttribute(cSessionId, "userName");
					this.userCode = jedisClient.getAttribute(cSessionId, "userCode");
					this.userType = jedisClient.getAttribute(cSessionId, "userType");
					this.userId = jedisClient.getAttribute(cSessionId, "userId");
					this.companyId = jedisClient.getAttribute(cSessionId, "companyId");
					this.orgList = jedisClient.getOrgAttribute(cSessionId);
					this.roleList = jedisClient.getRoleAttribute(cSessionId); 
					this.cellList = jedisClient.getCellAttribute(cSessionId); 
				}
			}
			else {  
				RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();

				if(jedisClient.getAttribute(cSessionId, "userId") != null){
					this.userName = jedisClient.getAttribute(cSessionId, "userName");
					this.userCode = jedisClient.getAttribute(cSessionId, "userCode");
					this.userType = jedisClient.getAttribute(cSessionId, "userType");
					this.userId = jedisClient.getAttribute(cSessionId, "userId");
					this.companyId = jedisClient.getAttribute(cSessionId, "companyId");
					this.orgList = jedisClient.getOrgAttribute(cSessionId);
					this.roleList = jedisClient.getRoleAttribute(cSessionId); 
					this.cellList = jedisClient.getCellAttribute(cSessionId); 
				}
			}
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception("无法获取当前用户信息, 请确定是否已登出或登录超时. ", ex);
		}
	}
 
	public NcpSession(Cookie[] cookies) throws Exception {
		try { 
			cSessionId = getCookieSessionId(cookies);
			RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
			this.userName = jedisClient.getAttribute(cSessionId, "userName");
			this.userCode = jedisClient.getAttribute(cSessionId, "userCode");
			this.userType = jedisClient.getAttribute(cSessionId, "userType");
			this.userId = jedisClient.getAttribute(cSessionId, "userId");
			this.companyId = jedisClient.getAttribute(cSessionId, "companyId");
			this.orgList = jedisClient.getOrgAttribute(cSessionId);
			this.roleList = jedisClient.getRoleAttribute(cSessionId);  
			this.cellList = jedisClient.getCellAttribute(cSessionId); 
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception("无法获取当前用户信息, 请确定是否已登出或登录超时. ", ex);
		}
	}

	public NcpSession(String userId) { 
		this.userId = userId;  
	}

	// 作废
	public void invalidate() throws RedisException {
		String cSessionId = this.getCSessionId();
		RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
		jedisClient.invalidate(cSessionId);
	}
	
	public void refreshExpire() throws RedisException{
		String cSessionId = this.getCSessionId();
		RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
		jedisClient.refreshExpire(cSessionId);
	}

	private String userName;

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) throws RedisException {
		String cSessionId = this.getCSessionId();
		RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
		jedisClient.setAttribute(cSessionId, "userName", userName);
		this.userName = userName;
	}
	
	private String companyId;

	public String getCompanyId() {
		return this.companyId;
	}

	public void setCompanyId(String companyId) throws RedisException {
		String cSessionId = this.getCSessionId();
		RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
		jedisClient.setAttribute(cSessionId, "companyId", companyId);
		this.companyId = companyId;
	}

	private String userCode;

	public String getUserCode() {
		return this.userCode;
	}
	
	public void setUserCode(String userCode) throws RedisException {
		String cSessionId = this.getCSessionId();
		RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
		jedisClient.setAttribute(cSessionId, "userCode", userCode); 
		this.userCode = userCode;
	}

	private String userType;
	public String getUserType() {
		return this.userType;
	}
	public void setUserType(String userType) throws RedisException {
		String cSessionId = this.getCSessionId();
		RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
		jedisClient.setAttribute(cSessionId, "userType", userType);  
		this.userType = userType;
	}

	
	private String userId;

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) throws RedisException {
		String cSessionId = this.getCSessionId();
		RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
		jedisClient.setAttribute(cSessionId, "userId", userId);  
		this.userId = userId;
	}

	public boolean getIsOnline() throws RedisException {
		String cSessionId = this.getCSessionId();
		if(cSessionId == null){
			return false;
		}
		else{ 
			RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
			String userId = jedisClient.getAttribute(cSessionId, "userId");  
			return userId != null;
		}
	}

	private List<Org> orgList;

	public List<Org> getOrgList() {
		return this.orgList;
	}

	public void setOrgList(List<Org> orgList) throws RedisException {
		String cSessionId = this.getCSessionId();
		RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
		jedisClient.setOrgAttribute(cSessionId, orgList); 
		this.orgList = orgList;
	}

	private List<Role> roleList;

	public List<Role> getRoleList() {
		return this.roleList;
	}

	public void setRoleList(List<Role> roleList) throws RedisException {
		String cSessionId = this.getCSessionId();
		RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
		jedisClient.setRoleAttribute(cSessionId, roleList);  
		this.roleList = roleList;
	}

	private List<String> cellList;

	public List<String> getCellList() {
		return this.cellList;
	}

	public void setCellList(List<String> cellList) throws RedisException {
		String cSessionId = this.getCSessionId();
		RedisSessionCacheClient jedisClient = this.getRedisSessionCacheClient();
		jedisClient.setCellAttribute(cSessionId, cellList);  
		this.cellList = cellList;
	}

	public HashMap<String, Object> toHashMap() {
		HashMap<String, Object> sessionHash = new HashMap<String, Object>();
		sessionHash.put("cSessionId", this.getCSessionId());
		sessionHash.put("userId", this.getUserId());
		sessionHash.put("userName", this.getUserName());
		sessionHash.put("userCode", this.getUserCode());
		
		//增加token(目前只有APP登录会生成token) added by liyh 20190604
		sessionHash.put("token", this.getToken());

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

	//新增APP登录后生成并返回Token added by liyh 20190604
	private String token;
	public String getToken() {
		return this.token;
	}
	public void setToken(String token) {
		this.token = token;
	} 
}
