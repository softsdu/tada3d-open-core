package com.zlp.platform.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.zlp.platform.common.util.CommonFunction;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nova.frame.utils.SecurityUtils; 
import com.zlp.platform.common.JSONProcessor;
import com.zlp.platform.common.NcpActionSupport;
import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.NcpSession;
import com.zlp.platform.common.ServiceResultProcessor;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.constants.ZlpState;
import com.zlp.platform.core.ConfigContext;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.SelectSqlParser;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.Org;
import com.zlp.platform.dao.sys.Role;
import com.zlp.platform.dao.sys.SystemContext;
import com.opensymphony.xwork2.ActionSupport;

import com.zlp.platform.util.LogUtil;

//@WebService
//@SOAPBinding(style = Style.RPC)
public class UserService extends NcpActionSupport implements IUserService {
	/**
     *
     */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(UserService.class);

	private List<Object[]> list = null;
	// DBParserAccess
	private IDBParserAccess dBParserAccess;

	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}

	private HibernateTransactionManager transactionManager;

	public void setTransactionManager(
			HibernateTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	protected Session openDBSession() throws SQLException {
		return this.transactionManager.getSessionFactory().openSession();
	}

	@Override
	public String logout() {
		try {

	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession beforeLogoutSession = new NcpSession(this.getHttpCookies(), true);
			
			String currentUserName=beforeLogoutSession.getUserName();
			String currentUserId=beforeLogoutSession.getUserId();

			NcpSession session = new NcpSession(this.getHttpCookies(), false);
			session.invalidate();
			 
			logger.info("用户" + currentUserId + "退出系统!"); 
			HashMap<String, Object> resultHash = new HashMap<String, Object>(); 
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ActionSupport.SUCCESS;
	}

	@Override
	public String getSysParam() {
		try {
			logger.info(requestParam);
			
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);

			HashMap<String, Object> resultHash = session.toHashMap();
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		} catch (Exception ex) {
			ex.printStackTrace();
			NcpException ncpEx = new NcpException("getSysParam", "获取系统参数失败", ex);
			this.addResponse(ncpEx.toJsonString());
		}
		return ActionSupport.SUCCESS;
	}

	@Override
	public String getMenu() {
		Session dbSession = null;
		try { 
			NcpSession session = new NcpSession(this.getHttpCookies(), true);

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("userid", session.getUserId());

			StringBuffer sql = new StringBuffer();
 
			sql.append("select m.id as id, m.code as code, m.name as name, m.icon as icon, m.actionexp as actionexp, m.parentid as parentid ");
			sql.append(" from sys_menu m where exists ");
			sql.append("      (select * from d_rolemenu rm left outer join d_userrole ur on ur.roleid = rm.roleid ");
			sql.append("         where  m.id = rm.menuid and rm.isenable = 'Y' and ur.userid = ")
					.append(SysConfig.getParamPrefix()).append("userid) ");
			sql.append("      and m.isdefaultenable = 'Y' and m.ishidden='N' "); 
			sql.append(" order by m.code asc "); 
			
			String[] fieldNames = { "id", "code", "name", "icon", "actionexp", "parentid" };
			ValueType[] fieldTypes = { ValueType.String, ValueType.String, ValueType.String, ValueType.String, ValueType.String, ValueType.String };
			dbSession = this.openDBSession();
			DataTable menuItemDt = dBParserAccess.getMultiLineValues(dbSession, sql.toString(), params, fieldNames, fieldTypes);
			
			List<DataRow> menuItemRows = menuItemDt.getRows();
			JSONArray menuItemJsonArray = new JSONArray();
			for(int i = 0; i < menuItemRows.size(); i++){
				DataRow menuItemRow = menuItemRows.get(i);
				JSONObject menuItemJson = new JSONObject();
				menuItemJson.put("id", menuItemRow.getStringValue("id"));
				menuItemJson.put("code", menuItemRow.getStringValue("code"));
				menuItemJson.put("name", menuItemRow.getStringValue("name"));
				menuItemJson.put("icon", menuItemRow.getStringValue("icon"));
				menuItemJson.put("actionExp", menuItemRow.getStringValue("actionexp"));
				menuItemJson.put("parentId", menuItemRow.getStringValue("parentid"));
				menuItemJsonArray.add(menuItemJson);
			}
			
			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			resultHash.put("menuItems", menuItemJsonArray);
			String resultString = ServiceResultProcessor
					.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		} catch (Exception ex) {
			ex.printStackTrace();
			NcpException ncpEx = new NcpException("getMenu", "获取菜单失败", ex);
			this.addResponse(ncpEx.toJsonString());
		} finally {
			if (dbSession != null) {
				dbSession.close();
			}
		}
		return ActionSupport.SUCCESS;
	}
	
	private List<DataRow> resetMenuParentid(List<DataRow> allRows) {
		List<DataRow> return_list = new ArrayList<>();
		List<String> mainMenuIdList = new ArrayList<>();
		for (int i = 0; i < allRows.size(); i++) {
			DataRow row = allRows.get(i);
			if (row.getStringValue("parentid") == null) {
				mainMenuIdList.add(row.getStringValue("id"));
			} else {
				return_list.add(row);
			}
		}

		for (int i = 0; i < return_list.size(); i++) {
			DataRow row = return_list.get(i);
			if (mainMenuIdList.contains(row.getStringValue("parentid"))) {
				row.setValue("parentid", null);
			}
		}
		return return_list;
	} 

	private String findMenuItem(DataRow row) {
		String res = "";
		String id = row.getStringValue("id");
		if (list != null) {
			for (int i = 0, size = list.size(); i < size; i++) {
				Object[] obj = list.get(i);
				if (id.equals(obj[0])) {
					res = obj[1] + "";
					break;
				}
			}
		}
		return res;
	}

	private JSONObject getMenuItem(DataRow row) {
		JSONObject menuItem = new JSONObject();
		String text = findMenuItem(row);
		if (text != "") {
			menuItem.put("text", text);
		} else {
			menuItem.put("text", row.getStringValue("name"));
		}
		menuItem.put("id", row.getStringValue("id"));
		JSONObject attrObj = new JSONObject();
		attrObj.put("parentid", row.getStringValue("parentid"));
		attrObj.put("icon", row.getStringValue("icon"));
		attrObj.put("actionexp", row.getStringValue("actionexp"));
		menuItem.put("attributes", attrObj);

		return menuItem;
	}

	@Override
	public String login() {
		Session dbSession = null;
		try {
			
			//deleted by liyh 20190111
			//logger.info(requestParam);

			// 输入参数
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);
			String code = requestObj.getString("code");
			
			// 密码加密
			String password = SecurityUtils.novaEnCryption(requestObj
					.getString("password"));

			// 验证用户名密
			SelectSqlParser getUserSqlParser = dBParserAccess
					.getSqlParser("sys.login.getuser");
			HashMap<String, Object> userParams = new HashMap<String, Object>();
			userParams.put("code", code);
			dbSession = this.openDBSession();
			DataTable userDt = dBParserAccess.getDtBySqlParser(dbSession,
					getUserSqlParser, -1, -1, userParams, "", "");
			List<DataRow> userRows = userDt.getRows();
			if (userRows.size() == 0) {
				NcpException ncpEx = new NcpException("login", "用户名或密码错误.", null);// “没有指定的用户” 此提示太明确，存在与户名枚举风险
				this.addResponse(ncpEx.toJsonString());
				
			} 
			else {
				DataRow userRow = userRows.get(0);
				
				//增加外部客户首次登录需要先激活的功能  added by liyh 20181213
				String status = userRow.getStringValue("status"); 
	 
				if (password.equals(userRow.getStringValue("password"))) {
					String userId = userRow.getStringValue("id");
					SelectSqlParser getRoleSqlParser = dBParserAccess
							.getSqlParser("sys.login.getrole");
					HashMap<String, Object> roleParams = new HashMap<String, Object>();
					roleParams.put("userid", userId);
					DataTable roleDt = dBParserAccess.getDtBySqlParser(dbSession, getRoleSqlParser, -1, -1, roleParams, "", "");
					List<DataRow> roleRows = roleDt.getRows();
					if (roleRows.size() == 0) {
						NcpException ncpEx = new NcpException("login", "没有给此用户指定角色.", null);
						this.addResponse(ncpEx.toJsonString());
					} 
					else {
						String userCode = userRow.getStringValue("code");
						String userName = userRow.getStringValue("name");
						String companyId = userRow.getStringValue("companyid");
	
						SelectSqlParser getOrgSqlParser = dBParserAccess.getSqlParser("sys.login.getorg");
						HashMap<String, Object> orgParams = new HashMap<String, Object>();
						orgParams.put("userid", userId);
						DataTable orgDt = dBParserAccess.getDtBySqlParser(dbSession, getOrgSqlParser, -1, -1, orgParams,"", "");
						List<DataRow> orgRows = orgDt.getRows();
	
						List<Org> orgList = new ArrayList<Org>();
						for (DataRow orgRow : orgRows) {
							Org org = new Org();
							org.setId(orgRow.getStringValue("orgid"));
							org.setCode(orgRow.getStringValue("orgcode"));
							org.setName(orgRow.getStringValue("orgname"));
							org.setIspublic(orgRow.getStringValue("ispublic"));
							orgList.add(org);
	
						}
						List<Role> roleList = new ArrayList<Role>();
						for (DataRow roleRow : roleRows) {
							Role r = new Role();
							r.setId(roleRow.getStringValue("roleid"));
							r.setCode(roleRow.getStringValue("rolecode"));
							r.setName(roleRow.getStringValue("rolename"));
							roleList.add(r);
	
						}
						
						//改变session存储方式 modified by ls 20190801
						Cookie[] cookies = this.getHttpRequest().getCookies();	
						NcpSession session = new NcpSession(cookies, false);							
						String cSessionId =  "UserSession_" + UUID.randomUUID().toString();
						session.setCSessionId(cSessionId); 
						
						//设置动态 
						session.setUserId(userId);
						session.setUserCode(userCode);

						session.setUserName(userName);
						session.setCompanyId(companyId);
						session.setOrgList(orgList);
						session.setRoleList(roleList);
						session.refreshExpire(); 
						String resultString = ServiceResultProcessor.createJsonResultStr(session.toHashMap());
						
						this.addResponse(resultString);
	
						HttpServletResponse response = this.getHttpResponse(); 
						
						String cookWebPath = ConfigContext.getConfigMap().get(ZlpState.COOKIE_WEB_PATH); 
						
						response.addHeader("Set-Cookie", "cSessionId=" + session.getCSessionId() + ";Path=/" + cookWebPath + "; HttpOnly");
						
						//新增登录日志记录  added by liyh 20190104
						logger.info("用户" + userName + "登录成功!");
						LogUtil.log(userId, "", "登录成功!", "sys");
					}
				} 
				else {
					NcpException ncpEx = new NcpException("login", "用户名或密码错误", null);
					this.addResponse(ncpEx.toJsonString());
				} 
			}
		} 
		catch (Exception ex) {
			NcpException ncpEx = new NcpException("login", "登录错误", ex);
			this.addResponse(ncpEx.toJsonString());
		} 
		finally {
			if (dbSession != null) {
				dbSession.close();
			}
		}
		return ActionSupport.SUCCESS;
	}

	@Override
	public String changePassword() {

		Session dbSession = null;
		try {
			logger.info(requestParam);
			// 输入参数
			JSONObject requestObj = JSONProcessor.strToJSON(requestParam);

	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			String userid = session.getUserId();
			String userCode = session.getUserCode();

			//新增密码强度校验 added by liyh 20240126
			String newpassword_noEn = requestObj.getString("newpassword");
			CommonFunction.checkPassword(newpassword_noEn,userCode);//增加校验密码

			String oldpassword = SecurityUtils.novaEnCryption(requestObj
					.getString("oldpassword"));
			String newpassword = SecurityUtils.novaEnCryption(requestObj
					.getString("newpassword"));

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("userid", userid);

			String sql = "select u.password as password from d_user u where u.id = "
					+ SysConfig.getParamPrefix() + "userid";
			dbSession = this.openDBSession();
			String dbUserPassword = (String) dBParserAccess.getSingleValue(
					dbSession, sql, params);
			if (dbUserPassword.equals(oldpassword)) {
				HashMap<String, Object> updateParams = new HashMap<String, Object>();
				updateParams.put("password", newpassword);
				updateParams.put("userid", userid);

				String updateSql = "update d_user set password = "
						+ SysConfig.getParamPrefix() + "password where id = "
						+ SysConfig.getParamPrefix() + "userid";
				dBParserAccess.update(dbSession, updateSql, updateParams);
				HashMap<String, Object> resultHash = new HashMap<String, Object>();
				resultHash.put("info", "密码修改成功.");
				String resultString = ServiceResultProcessor
						.createJsonResultStr(resultHash);
				this.addResponse(resultString);
			} else {
				NcpException ncpEx = new NcpException("ChangePassword",
						"原密码输入错误.", null);
				this.addResponse(ncpEx.toJsonString());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			NcpException ncpEx = new NcpException("ChangePassword", "修改密码失败",
					ex);
			this.addResponse(ncpEx.toJsonString());
		} finally {
			if (dbSession != null) {
				dbSession.close();
			}
		}
		return ActionSupport.SUCCESS;
	}

	//获取主菜单，同时获取主菜单的actionexp modified by ls 20190725
	public String getMainMenuList() {
		Session dbSession = null;
		try {
	    	//通过Cookie获取cSessionId，在线信息存储在redis，实现session共享 modified by ls 20190801
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			
			StringBuffer sb = new StringBuffer();
			sb.append("	select id, code, name, actionexp");
			sb.append(" from sys_menu m ");
			sb.append(" where parentid is null");
			sb.append(" and isdefaultenable = 'Y'");
			sb.append(" and ishidden = 'N'");
			sb.append(" and exists");
			sb.append("  (select * ");
			sb.append("    from d_rolemenu rm");
			sb.append("    left outer join d_userrole ur on ur.roleid = rm.roleid");
			sb.append("    where m.id = rm.menuid");
			sb.append("    and rm.isenable = 'Y'");
			sb.append("    and ur.userid = '" + session.getUserId() + "')");
			sb.append(" order by code asc");

			String[] fieldNames = { "id", "code", "name", "actionexp" };
			ValueType[] fieldTypes = {ValueType.String, ValueType.String, ValueType.String, ValueType.String};
			dbSession = this.openDBSession();
			DataTable dt = dBParserAccess.getMultiLineValues(dbSession, sb.toString(), null, fieldNames, fieldTypes);

			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			resultHash.put("mainMenuItems", dt.getRows());
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			NcpException ncpEx = new NcpException("getMainMenuList", "获取主菜单失败", ex);
			this.addResponse(ncpEx.toJsonString());
		} 
		finally {
			if (dbSession != null) {
				dbSession.close();
			}
		}
		return SUCCESS;
	}

	public String checkDefaultConfig() {
		Session dbSession = null;

		try {
			logger.info(this.requestParam);
			JSONObject requestObj = JSONProcessor.strToJSON(this.requestParam);
			NcpSession session = new NcpSession(this.getHttpCookies(), true);
			String userid = session.getUserId();
			String defaultPassword = SysConfig.getPropertyFileValue("system.properties", "default.password").trim();
			String defaultPasswordEn = SecurityUtils.novaEnCryption(defaultPassword);

			HashMap<String, Object> params = new HashMap();
			params.put("userid", userid);
			String sql = "select u.password as password from d_user u where u.id = " + SysConfig.getParamPrefix() + "userid";
			dbSession = this.openDBSession();
			String dbUserPassword = (String)this.dBParserAccess.getSingleValue(dbSession, sql, params);
			HashMap<String, Object> resultHash = new HashMap();

			if (dbUserPassword.equals(defaultPasswordEn)) {
				resultHash.put("isDefault", true);
			} else {
				resultHash.put("isDefault", false);
			}
			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		} catch (Exception ex) {
			ex.printStackTrace();
			NcpException ncpEx = new NcpException("checkDefaultConfig", "登录提示", ex);
			this.addResponse(ncpEx.toJsonString());
		} finally {
			if (dbSession != null) {
				dbSession.close();
			}

		}
		return SUCCESS;
	}
}
