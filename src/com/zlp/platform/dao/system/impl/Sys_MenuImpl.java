package com.zlp.platform.dao.system.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.common.SysConfig; 
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.system.Sys_Menu;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection; 
import com.zlp.platform.model.sysmodel.PagePurviewHash;

public class Sys_MenuImpl extends DataBaseDao implements Sys_Menu {

	private PagePurviewHash pagePurviewHash = null;
	public void setPagePurviewHash(PagePurviewHash pagePurviewHash){
		this.pagePurviewHash = pagePurviewHash;
	} 
	
	//根据json获取数据
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	
	@Override
	protected void afterDelete(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{

		//删除所有角色关于此菜单项的权限设置
		JSONObject rowIdToIdValues = requestObj.getJSONObject("deleteRows");    
		Data rmData = DataCollection.getData("d_RoleMenu"); 
		for(Object id : rowIdToIdValues.values().toArray()){ 
			this.dBParserAccess.deleteByData(this.getDBSession(), rmData, "menuid", "=", id);
		}	 
		pagePurviewHash.initFromDB();
	}
	
	@Override
	protected void afterSave(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{
		List<String> newMenuItemIds = new ArrayList<String>();
	    JSONObject insertRowsObj = requestObj.getJSONObject("insert");
	    JSONObject id2RowIds = (JSONObject) resultHash.get("idValueToRowIds");
	    for(Object rowIdObj : insertRowsObj.keySet()){
	    	for(Object idValueObj : id2RowIds.keySet()){
	    		if(rowIdObj.equals(id2RowIds.get(idValueObj))){
	    			newMenuItemIds.add(idValueObj.toString());
	    		}
	    	}
	    }
	    
	    //对于手工新增的菜单，只分配给手工添加的角色（ishidden='N'，其他角色未系统自动创建的各组织机构报表填报控制角色）  modified by liyh 20180906
	    //String roleSql = "select r.id as id from d_role r";
	    String roleSql = "select r.id as id from d_role r where ishidden='N' ";
	    
	    DataTable roleDt = this.dBParserAccess.getMultiLineValues(this.getDBSession(), roleSql, null,new String[]{"id"}, new ValueType[]{ValueType.String});
	    List<String> roleIds = new ArrayList<String>();
	    for(DataRow roleRow : roleDt.getRows()){
	    	roleIds.add(roleRow.getStringValue("id"));
	    }
    	
	    
	    List<HashMap<String, Object>> allRMs = new ArrayList<HashMap<String, Object>>();
	    //循环所有的新建行，
	    for(String menuItemId : newMenuItemIds){
	    	String menuSql = "select m.id as id, m.isdefaultenable as isdefaultenable from sys_menu m where m.id = " + SysConfig.getParamPrefix() + "id";
	    	HashMap<String, Object> p2vs = new HashMap<String, Object>();
	    	p2vs.put("id", menuItemId);
	    	DataTable menuDt = this.dBParserAccess.getMultiLineValues( this.getDBSession(), menuSql, p2vs, new String[]{"id","isdefaultenable"}, new ValueType[]{ValueType.String, ValueType.String});
	    	DataRow menuRow = menuDt.getRows().get(0); 
	    	boolean isDefaultEnable =  menuRow.getBooleanValue("isdefaultenable");
	    	for(String roleId : roleIds){
		    	HashMap<String, Object> rm = new HashMap<String, Object>();
	    		rm.put("menuid", menuItemId);
	    		rm.put("roleid", roleId);
	    		
	    		//所有新增菜单，保存时只分给角色，但是不应该自动启用    modified by liyh 20180906
	    		//rm.put("isenable", isDefaultEnable ? "Y" : "N");
	    		rm.put("isenable", "N");
	    		
	    		allRMs.add(rm);
	    	}	    	
	    } 
		Data rmData = DataCollection.getData("d_RoleMenu"); 
		this.dBParserAccess.insertByData(this.getDBSession(), rmData, allRMs);
		 
		pagePurviewHash.initFromDB();
	}	
}
