package com.zlp.platform.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession; 
import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.NcpSession; 
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.common.redis.RedisException;
import com.zlp.platform.common.util.CommonFunction;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.SelectSqlParser;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.Org;
import com.zlp.platform.dao.sys.Role;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import com.zlp.platform.util.token.UserToken; 


/**
 * App开发所需  
 * 20190519
 * @author liyh
 *
 */
public class SystemUserProcessor {

	//DBParserAccess
	private IDBParserAccess dBParserAccess;
	public void setDBParserAccess(IDBParserAccess dBParserAccess){ 
		this.dBParserAccess = dBParserAccess;
	}
	public IDBParserAccess getDBParserAccess(){ 
		return this.dBParserAccess;
	}
	
	public void logout(INcpSession session) throws Exception {
		session.invalidate();		
	}
	
	//数据库Session（需要Service类里的方法把dbSession传递过来）
	private Session dbSession = null;
	protected Session getDBSession(){ 
		if(this.dbSession == null){
			throw new RuntimeException("none db session.");
		}
		return this.dbSession;
	} 
	public void setDBSession(Session dbSession){
		this.dbSession = dbSession;
	} 
 
	public JSONArray getMenu(INcpSession session){
		Session dbSession = this.getDBSession(); 

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("userid", session.getUserId()); 
		
		String sql = "select m.id as id, m.name as name, m.icon as icon, m.actionexp as actionexp, m.parentid as parentid "
					+"from sys_menu m where exists " 
					+"(select * from d_rolemenu rm left outer join d_userrole ur on ur.roleid = rm.roleid "
					+"where  m.id = rm.menuid and rm.isenable = 'Y' and ur.userid = "+ SysConfig.getParamPrefix() + "userid) "
					+"order by m.code asc "; 

		String[] fieldNames =  {"id", "name", "icon", "actionexp", "parentid"};
		ValueType[] fieldTypes =  {ValueType.String, ValueType.String, ValueType.String, ValueType.String, ValueType.String};
		DataTable dt = dBParserAccess.getMultiLineValues(dbSession, sql, params, fieldNames, fieldTypes); 
		JSONArray menuItems = getMenuItems(null, dt.getRows());	 
		return menuItems;
	}
	
	private JSONArray getMenuItems(String parentMenuItemId, List<DataRow> allRows){
		List<DataRow> childRows = new ArrayList<DataRow>();
		for(int i=0;i<allRows.size();i++){
			DataRow row = allRows.get(i);
			if(parentMenuItemId == null){
				if(row.getStringValue("parentid") == null){
					childRows.add(row);
				} 
			}
			else if(parentMenuItemId.equals(row.getStringValue("parentid"))){
				childRows.add(row);
			} 
		}

		if(childRows.size()>0){

			for(DataRow row : childRows){
				allRows.remove(row);
			}
			
			JSONArray menuItems = new JSONArray();
			for(DataRow row : childRows){ 
				JSONObject menuItem = getMenuItem(row);
				JSONArray childItems = getMenuItems( row.getStringValue("id"), allRows);
				if(childItems != null){
					menuItem.put("children", childItems);
					menuItem.put("state", "open");
				}
				
				menuItems.add(menuItem);
			}
			return menuItems;
		}
		else{
			return null;
		}
	}
	
	private JSONObject getMenuItem(DataRow row){
		JSONObject menuItem = new JSONObject();
		menuItem.put("text", row.getStringValue("name"));
		menuItem.put("id", row.getStringValue("id"));
		JSONObject attrObj = new JSONObject(); 
		attrObj.put("parentid", row.getStringValue("parentid"));
		attrObj.put("icon", row.getStringValue("icon"));
		attrObj.put("actionexp", row.getStringValue("actionexp"));
		menuItem.put("attributes", attrObj);
		return menuItem;
	} 
	
	private DataRow getUserByCodeAndPassword(INcpSession session, String code, String password) throws NcpException{
		Session dbSession = this.getDBSession();
		//验证用户名密 
		SelectSqlParser getUserSqlParser = dBParserAccess.getSqlParser("sys.login.getuser");
		HashMap<String, Object> userParams = new HashMap<String, Object>();
		userParams.put("code", code); 
		DataTable userDt = dBParserAccess.getDtBySqlParser(dbSession, getUserSqlParser, -1, -1, userParams, "", "");
		List<DataRow> userRows = userDt.getRows();
		if(userRows.size()==0){
			NcpException ncpEx = new NcpException("002", "不存在此用户.", null); 
			throw ncpEx;
		}
		else{
			DataRow userRow = userRows.get(0); 
			return userRow;
		}
	}

	public void login(NcpSession session, String code, String password) throws NcpException {  
		Session dbSession = this.getDBSession();
		DataRow userRow = this.getUserByCodeAndPassword(session, code, password);
		if(password.equals(userRow.getStringValue("password"))){
			String userId = userRow.getStringValue("id");
			SelectSqlParser getRoleSqlParser = dBParserAccess.getSqlParser("sys.login.getrole");
			HashMap<String, Object> roleParams = new HashMap<String, Object>();
			roleParams.put("userid", userId);
			DataTable roleDt = dBParserAccess.getDtBySqlParser(dbSession, getRoleSqlParser, -1, -1, roleParams, "", "");
			List<DataRow> roleRows = roleDt.getRows();
			if(roleRows.size()==0){
				NcpException ncpEx = new NcpException("004", "没有给此用户指定角色.", null);
				throw ncpEx;
			}
			else{
				String userCode = userRow.getStringValue("code");
				String userName = userRow.getStringValue("name");
				String companyId = userRow.getStringValue("companyid");
				
				SelectSqlParser getOrgSqlParser = dBParserAccess.getSqlParser("sys.login.getorg");
				HashMap<String, Object> orgParams = new HashMap<String, Object>();
				orgParams.put("userid", userId);
				DataTable orgDt = dBParserAccess.getDtBySqlParser(dbSession, getOrgSqlParser, -1, -1, orgParams, "", "");
				List<DataRow> orgRows = orgDt.getRows();
				
				//added by liyh 20190612
				if(orgRows.size()==0){
					NcpException ncpEx = new NcpException("005", "没有给此用户指定所属机构.", null);
					throw ncpEx;
				}

				List<Org> orgList=new ArrayList<Org>();
				for(DataRow orgRow : orgRows){
					Org org = new Org();
					org.setId(orgRow.getStringValue("orgid"));
					org.setCode(orgRow.getStringValue("orgcode"));
					org.setName(orgRow.getStringValue("orgname"));
					orgList.add(org);
				}
				
				List<Role> roleList=new ArrayList<Role>();
				for(DataRow roleRow : roleRows){
					Role r = new Role();
					r.setId(roleRow.getStringValue("roleid"));
					r.setCode(roleRow.getStringValue("rolecode"));
					r.setName(roleRow.getStringValue("rolename"));
							roleList.add(r);
					}
 
				try{
					session.setUserId(userId);
					session.setUserCode(userCode);
					session.setUserName(userName);
					session.setCompanyId(companyId);
					session.setOrgList(orgList);
					session.setRoleList(roleList);
				}
				catch(RedisException ex){
					throw new NcpException("SaveSessionError", "写入sessoin信息出错", ex);
				}  
			}
		}
		else { 
			NcpException ncpEx = new NcpException("003", "密码错误", null);  
			throw ncpEx;
		} 
	}
 
	public void changePassword(UserToken userToken, String oldpassword, String newpassword) throws NcpException{

		Session dbSession = this.getDBSession();  
		String userid = userToken.getUserid(); 
 
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("userid", userid);
		
		String sql = "select u.password as password from d_user u where u.id = " + SysConfig.getParamPrefix() + "userid"; 
		String dbUserPassword = (String)dBParserAccess.getSingleValue(dbSession, sql, params);
		if(dbUserPassword.equals(oldpassword)){
			HashMap<String, Object> updateParams = new HashMap<String, Object>();
			updateParams.put("password", newpassword);
			updateParams.put("userid", userid);
			
			String updateSql = "update d_user set password = " + SysConfig.getParamPrefix() + "password where id = " + SysConfig.getParamPrefix() + "userid"; 
			dBParserAccess.update(dbSession, updateSql, updateParams);
		}
		else{
			NcpException ncpEx = new NcpException("ChangePassword", "原密码输入错误.", null);
			throw ncpEx;
		}  	  
	}
	
	//标记为已读
	public void markUserMessage(NcpSession session, List<String> messageIdList){  
		Session dbSession = this.getDBSession();
		String userId = session.getUserId();
		String messageIdStr = ValueConverter.arrayToString(messageIdList, ",");
		messageIdStr = messageIdStr.replace("'", "''");
		String updateSql = "update d_usermessage set status = 'read' where touserid = " + SysConfig.getParamPrefix() + "userId and id in ('" + messageIdStr + "')";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("userId", userId);
		dBParserAccess.update(dbSession, updateSql, p2vs);
	} 
	
	public void sendUserMessage(String fromUserId, String toUserId, String content){
		Date currentTime = new Date();
		Data memberData = DataCollection.getData("d_UserMessage");
		HashMap<String, Object> f2vs = new HashMap<String, Object>();
		f2vs.put("fromuserid", fromUserId);
		f2vs.put("touserid", toUserId);
		f2vs.put("content", content);
		f2vs.put("createtime", currentTime);
		f2vs.put("status", "unread");
		this.getDBParserAccess().insertByData(dbSession, memberData, f2vs); 
	}
	
	public DataRow getUserInfo(String userId) throws NcpException{ 
		Data userData = DataCollection.getData("d_User");
		Session dbSession = this.getDBSession();
		DataTable userDt = this.getDBParserAccess().getDtById(dbSession, userData, userId);
		List<DataRow> userRows = userDt.getRows();
		if(userRows.size() == 0){
			throw new NcpException("getUserInfo_002", "不存在的用户, userId = " + userId);
		}
		else{
			return userRows.get(0);
		}
	}
	
	public DataRow getUserInfo(INcpSession session) throws NcpException {
		String userId = session.getUserId();
		return this.getUserInfo(userId);		
	}
	public void changeUserInfo(INcpSession session, String password, String name, String email) throws NcpException {
		
		if(!CommonFunction.checkIsEmail(email)){
			NcpException ncpEx = new NcpException("changeUserInfo_002", "Email地址错误", null);  
			throw ncpEx;
		}
		else{
			Session dbSession = this.getDBSession(); 
			String userId = session.getUserId();	
			String userCode = session.getUserCode();	
			
			DataRow userRow = this.getUserByCodeAndPassword(session, userCode, password);
			if(password.equals(userRow.getStringValue("password"))){			
				Data userData = DataCollection.getData("d_User");
				HashMap<String, Object> f2vs = new HashMap<String, Object>();
				f2vs.put("name", name);
				f2vs.put("email", email);
				this.getDBParserAccess().updateByData(dbSession, userData, f2vs, userId); 
			}
			else { 
				NcpException ncpEx = new NcpException("changeUserInfo_003", "密码错误", null);  
				throw ncpEx;
			} 
		}
	}
}
