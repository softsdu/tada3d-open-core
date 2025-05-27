package com.zlp.platform.common.redis;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.dao.sys.Org;
import com.zlp.platform.dao.sys.Role;
import redis.clients.jedis.Jedis; 
import redis.clients.jedis.JedisPool;

//存取Redis里的session信息 added by ls 20190730
public class RedisSessionCacheClient {
	
	private JedisPool jedisPool = null;
	public JedisPool getJedisPool() {
		return jedisPool;
	}

	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool; 
	}
	
	//session过期时长（秒）
	private int expireSeconds = 30;
	public void setExpireSeconds(int expireSeconds){
		this.expireSeconds = expireSeconds;
	}
	public int getExpireSeconds(){
		return this.expireSeconds;
	}
	
	public void refreshExpire(String sessionId) throws RedisException{
		Jedis jedisClient = null;
		try{
			jedisClient = this.getJedisClient();
			jedisClient.expire(sessionId, this.getExpireSeconds());
		}
		catch(RedisException ex){
            ex.printStackTrace();
            throw ex;
		}
		finally{
			if(jedisClient != null){
				this.returnJedisClient(jedisClient);
			}
		} 
	}
	
	private Jedis getJedisClient() throws RedisException{
		try{
			return this.getJedisPool().getResource();
		}
		catch(Exception ex){
			throw new RedisException("GetRedisClientError", "Can not get redis client from pool." + ex.getMessage(), ex);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void returnJedisClient(Jedis jedisClient){
		  this.getJedisPool().returnResource(jedisClient);
	}
	
	public void invalidate(String sessionId) throws RedisException{
		Jedis jedisClient = null;
		try{
			jedisClient = this.getJedisClient();
			jedisClient.del(sessionId); 
		}
		catch(RedisException ex){
            ex.printStackTrace();
            throw ex;
		}
		finally{
			if(jedisClient != null){
				this.returnJedisClient(jedisClient);
			}
		} 
	}
		
	public void setAttribute(String key, String field, String value) throws RedisException{
		Jedis jedisClient = null;
		try{
			jedisClient = this.getJedisClient();
			jedisClient.hset(key, field, value);
		}
		catch(Exception ex){
            ex.printStackTrace();
            throw ex;
		}
		finally{
			if(jedisClient != null){
				this.returnJedisClient(jedisClient);
			}
		}
	}
	
	public String getAttribute(String key, String field) throws RedisException{
		Jedis jedisClient = null;
		try{
			jedisClient = this.getJedisClient();
			String value = jedisClient.hget(key, field);
			return value;
		}
		catch(Exception ex){
            ex.printStackTrace();
            throw ex;
		}
		finally{
			if(jedisClient != null){
				this.returnJedisClient(jedisClient);
			}
		}
	}

	public List<Org> getOrgAttribute(String sessionId) throws RedisException {
		String jsonStr = this.getAttribute(sessionId, "orgList");
		List<Org> orgList = new ArrayList<Org>();
		if(jsonStr != null && jsonStr.length() != 0){
			JSONArray jsonArray = JSONArray.parseArray(jsonStr);
			for(int i = 0; i < jsonArray.size(); i++){
				JSONObject orgObj = jsonArray.getJSONObject(i);
				Org org = new Org();
				org.setId(orgObj.getString("id"));
				org.setCode(orgObj.getString("code"));
				org.setName(orgObj.getString("name")); 
				orgList.add(org);
			}
		}
		return orgList;
	}

	public void setOrgAttribute(String sessionId, List<Org> orgList) throws RedisException { 
		JSONArray jsonArray = new JSONArray(); 
		for(int i = 0; i < orgList.size(); i++){ 
			Org org = orgList.get(i);
			JSONObject orgObj = new JSONObject();
			orgObj.put("id", org.getId());
			orgObj.put("code", org.getCode());
			orgObj.put("name", org.getName()); 
			jsonArray.add(orgObj);
		}
		this.setAttribute(sessionId, "orgList", jsonArray.toString());
	}

	public List<Role> getRoleAttribute(String sessionId) throws RedisException {
		String jsonStr = this.getAttribute(sessionId, "roleList");
		List<Role> roleList = new ArrayList<Role>();
		if(jsonStr != null && jsonStr.length() != 0){
			JSONArray jsonArray = JSONArray.parseArray(jsonStr);
			for(int i = 0; i < jsonArray.size(); i++){
				JSONObject roleObj = jsonArray.getJSONObject(i);
				Role role = new Role();
				role.setId(roleObj.getString("id"));
				role.setCode(roleObj.getString("code"));
				role.setName(roleObj.getString("name")); 
				roleList.add(role);
			}
		}
		return roleList;
	}

	public void setRoleAttribute(String sessionId, List<Role> roleList) throws RedisException { 
		JSONArray jsonArray = new JSONArray(); 
		for(int i = 0; i < roleList.size(); i++){ 
			Role role = roleList.get(i);
			JSONObject roleObj = new JSONObject();
			roleObj.put("id", role.getId());
			roleObj.put("code", role.getCode());
			roleObj.put("name", role.getName()); 
			jsonArray.add(roleObj);
		}
		this.setAttribute(sessionId, "roleList", jsonArray.toString());
	}

	public List<String> getCellAttribute(String sessionId) throws RedisException {
		String jsonStr = this.getAttribute(sessionId, "cellList");
		List<String> cellList = new ArrayList<String>();
		if(jsonStr != null && jsonStr.length() != 0){
			JSONArray jsonArray = JSONArray.parseArray(jsonStr);
			for(int i = 0; i < jsonArray.size(); i++){
				String str = jsonArray.getString(i); 
				cellList.add(str);
			}
		}
		return cellList;
	}

	public void setCellAttribute(String sessionId, List<String> cellList) throws RedisException { 
		JSONArray jsonArray = new JSONArray(); 
		for(int i = 0; i < cellList.size(); i++){ 
			String str = cellList.get(i);
			jsonArray.add(str);
		}
		this.setAttribute(sessionId, "cellList", jsonArray.toString());
	}
}
