package com.zlp.platform.dao.system.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.INcpSession;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.DataBaseDao;
import com.zlp.platform.dao.system.D_Role;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;
import com.zlp.platform.model.sysmodel.PagePurviewHash;

public class D_RoleImpl extends DataBaseDao implements D_Role {

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
			this.dBParserAccess.deleteByData(this.getDBSession(), rmData, "roleid", "=", id);
		}		 
		pagePurviewHash.initFromDB();
	}
	
	@Override
	protected void afterSave(INcpSession session, JSONObject requestObj, HashMap<String,Object> resultHash) throws Exception{
		List<String> newRoleIds = new ArrayList<String>();
	    JSONObject insertRowsObj = requestObj.getJSONObject("insert");
	    JSONObject id2RowIds = (JSONObject) resultHash.get("idValueToRowIds");
	    for(Object rowIdObj : insertRowsObj.keySet()){
	    	for(Object idValueObj : id2RowIds.keySet()){
	    		if(rowIdObj.equals(id2RowIds.get(idValueObj))){
	    			newRoleIds.add(idValueObj.toString());
	    		}
	    	}
	    }
	    
	    String menuSql = "select m.id as id, m.isdefaultenable as isdefaultenable from sys_menu m";
	    DataTable menuDt =this.dBParserAccess.getMultiLineValues(this.getDBSession(), menuSql, null,new String[]{"id", "isdefaultenable"}, new ValueType[]{ValueType.String, ValueType.Boolean}); 
    		    
	    List<HashMap<String, Object>> allRMs = new ArrayList<HashMap<String, Object>>();
	    //循环所有的新建行，
	    for(String roleId : newRoleIds){
		    for(DataRow menuRow : menuDt.getRows()){ 
		    	String menuid = menuRow.getStringValue("id");
		    	boolean isDefaultEnable = menuRow.getBooleanValue("isdefaultenable"); 
		    	HashMap<String, Object> rm = new HashMap<String, Object>();
	    		rm.put("menuid", menuid);
	    		rm.put("roleid", roleId);
	    		
	    		//修改逻辑：对于新建的角色，分配的所有菜单默认都是不可用的  modified by liyh 20180926 （之前根据每个菜单的默认是否可用，效果就是基本都可用；因为如果默认不可用，则所有角色都无法使用；）  
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
