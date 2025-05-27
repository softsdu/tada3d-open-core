package com.zlp.platform.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;

import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.ServiceResultProcessor;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.opensymphony.xwork2.ActionSupport;

public class DataPurviewService extends CustomService implements IDataPurviewService {   
	 	
	@Override
	public String getRoleAndDataPurviewFactor(){ 
		Session dbSession = null;
		try
		{  
			IDBParserAccess dbAccess = this.getDBParserAccess();
			HashMap<String, Object> resultHash = new HashMap<String, Object>();
			
			dbSession = this.openDBSession();
			String roleSql = "select r.id as id, r.code as code, r.name as name from d_role r";
			DataTable roleDt = dbAccess.getMultiLineValues(dbSession, roleSql, null, new String[]{"id","code","name"}, new ValueType[]{ValueType.Decimal,ValueType.String,ValueType.String});
			resultHash.put("roleDt", roleDt.toHashMap()); 
			
			String factorSql = "select f.id as roleid, f.name as name, f.valuetype as valuetype, f.isdefaultall as isdefaultall from sys_datapurviewfactor f";
			DataTable factorDt = dbAccess.getMultiLineValues(dbSession, factorSql, null, new String[]{"id","name", "valuetype","isdefaultall"}, new ValueType[]{ValueType.Decimal,ValueType.String, ValueType.String,ValueType.Boolean});
			resultHash.put("factorDt", factorDt.toHashMap());  

			String resultString = ServiceResultProcessor.createJsonResultStr(resultHash);
			this.addResponse(resultString);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getRoleAndDataPurviewFactor", "getRoleAndDataPurviewFactor error. ", ex);
			this.addResponse(ncpEx.toJsonString()); 	 	 
		}	 
		finally{
			dbSession.close();
		}
		return ActionSupport.SUCCESS;	
	}
	
	public String getSqlClause(String userId, HashMap<String, String> aliasToFactors, HashMap<String, Object> returnSqlParams) throws Exception{
		Session dbSession = null;
		try
		{  			 
			dbSession = this.openDBSession();
			return this.getClause(dbSession, userId, aliasToFactors, returnSqlParams);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			NcpException ncpEx = new NcpException("getClause", "getClause error. ", ex);
			return ncpEx.toJsonString();
		}		
		finally{
			if(dbSession != null){
				dbSession.close();
			}
		}
	}
	
	public String getClause(Session dbSession, String userId, HashMap<String, String> aliasToFactors, HashMap<String, Object> returnSqlParams) throws Exception{
		String resultString = "";
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String getRoleSql = "select ur.roleid as roleid from d_userrole ur where ur.userid = " + SysConfig.getParamPrefix() + "userid";
		HashMap<String, Object> getRoleParams = new HashMap<String, Object>();
		getRoleParams.put("userid",userId); 
	    DataTable roleDt = dbAccess.getMultiLineValues(dbSession, getRoleSql, getRoleParams, new String[]{"roleid"}, new ValueType[]{ValueType.String});
	     
	    StringBuilder getFactorSql = new StringBuilder();
	    getFactorSql.append("select f.id as id, f.name as name, f.valuetype as valuetype, f.isdefaultall as isdefaultall from sys_datapurviewfactor f where ");

		HashMap<String, Object> getFactorParams = new HashMap<String, Object>();
		HashMap<String, ValueType> factorTypes = new HashMap<String,ValueType>(); 
		HashMap<String, String> factorToIds = new HashMap<String, String>();
		HashMap<String, Boolean> factorToDefaultAll = new HashMap<String, Boolean>();
		List<String> factorNameList = new ArrayList<String>();
		for(String alias : aliasToFactors.keySet()){
			String factorName = aliasToFactors.get(alias);
			if(!factorNameList.contains(factorName)){
				factorNameList.add(factorName);
			}
		}  
	    for(int i=0;i<factorNameList.size();i++){
	    	String factorName = factorNameList.get(i);
	    	getFactorSql.append((i == 0 ? "" : " or ") + "f.name = " + SysConfig.getParamPrefix() + "name" + i);
	    	getFactorParams.put("name" + i, factorName); 
	    }     
		DataTable factorDt = dbAccess.getMultiLineValues(dbSession, getFactorSql.toString(), getFactorParams,new String[]{"id", "name", "valuetype","isdefaultall"}, new ValueType[]{ValueType.String, ValueType.String, ValueType.String, ValueType.Boolean});
		List<DataRow> factorRows = factorDt.getRows();  
		for(DataRow factorRow :factorRows){
		   String factorName = factorRow.getStringValue("name");
		   String factorId = factorRow.getStringValue("id");	
		   Boolean isDefaultAll = factorRow.getBooleanValue("isdefaultall");	
		   factorToIds.put(factorName, factorId);
		   factorToDefaultAll.put(factorName, isDefaultAll);
		   factorNameList.remove(factorName);
		}
		if(factorNameList.size() != 0){			
			throw new NcpException("getFactor", "none data purview factor named '" + ValueConverter.arrayToString((String[])factorNameList.toArray(), ",") + "' in table sys_datapurviewfactor.", null);			
		}
		else{ 
			StringBuilder getRoleFactorSql = new StringBuilder();
			getRoleFactorSql.append("select rf.id as id, rf.roleid as roleid, rf.datapurviewfactorid as datapurviewfactorid, rf.valuea as valuea, rf.valueb as valueb from d_roledatapurview rf where (");
			HashMap<String,HashMap<String, List<String>>> roleToDataPurview = new HashMap<String,HashMap<String, List<String>>>();
			
			HashMap<String, Object> getRoleFactorParams = new HashMap<String, Object>();
			List<DataRow> roleRows = roleDt.getRows();
			for(int i=0;i<roleRows.size();i++){
				DataRow roleRow = roleRows.get(i);
				String roleId = roleRow.getStringValue("roleid");
				getRoleFactorParams.put("roleid" + i, roleId);
				getRoleFactorSql.append((i==0?"":" or ") + "rf.roleid = " + SysConfig.getParamPrefix() + "roleid" + i);
				HashMap<String, List<String>> aliasToFilters = new HashMap<String, List<String>>();
				roleToDataPurview.put(roleId, aliasToFilters);

				for(int j=0;j<factorRows.size();j++){ 
					DataRow factorRow = factorRows.get(j);
					String factorId = factorRow.getStringValue("id");			
					for(String alias : aliasToFactors.keySet()){
						String factorName = aliasToFactors.get(alias);
						if(factorId.equals(factorToIds.get(factorName))){
							List<String> valueFilters = new ArrayList<String>();
							aliasToFilters.put(alias, valueFilters); 
						}
					}
				}
			}
			getRoleFactorSql.append(") and (");

			for(int i=0;i<factorRows.size();i++){
				DataRow factorRow = factorRows.get(i);
				String factorId = factorRow.getStringValue("id");
				String factorName = factorRow.getStringValue("name");
				getRoleFactorParams.put("factorid" + i, factorId);
				getRoleFactorSql.append((i==0?"":" or ") + "rf.datapurviewfactorid = " + SysConfig.getParamPrefix() + "factorid" + i);
				
				ValueType valueType = (ValueType)Enum.valueOf(ValueType.class, factorRow.getStringValue("valuetype"));
				factorTypes.put(factorId, valueType);
				factorToIds.put(factorName, factorId);
			}
			getRoleFactorSql.append(")");
			
			DataTable roleFactorDt = dbAccess.getMultiLineValues(dbSession, getRoleFactorSql.toString(), getRoleFactorParams, new String[]{"id", "roleid", "datapurviewfactorid", "valuea", "valueb"}, new ValueType[]{ValueType.String, ValueType.String, ValueType.String, ValueType.String,ValueType.String});
			List<DataRow> roleFactorRows = roleFactorDt.getRows(); 
			for(int i=0;i<roleFactorRows.size();i++ ){
				DataRow roleFactorRow = roleFactorRows.get(i);
				String factorId = roleFactorRow.getStringValue("datapurviewfactorid");
				String roleId = roleFactorRow.getStringValue("roleid");
				String valueAStr = roleFactorRow.getStringValue("valuea");
				String valueBStr = roleFactorRow.getStringValue("valueb");	
				int l = 0;
				for(String alias : aliasToFactors.keySet()){
					String factorName = aliasToFactors.get(alias);
					if(factorId.equals(factorToIds.get(factorName))){
						String filterStr = null;
						List<String> valueFilters = roleToDataPurview.get(roleId).get(alias);
						String paramName = "dp" + i + "_" + l;
						if(valueAStr.isEmpty()){
							//小于
							Object valueB = ValueConverter.convertToObject(valueBStr, factorTypes.get(factorId));
							returnSqlParams.put(paramName, valueB);
							filterStr = alias + " <= " + SysConfig.getParamPrefix() + paramName; 
						}
						else if(valueBStr.isEmpty()){
							//大于
							Object valueA = ValueConverter.convertToObject(valueAStr, factorTypes.get(factorId));
							returnSqlParams.put(paramName, valueA);
							filterStr = alias + " >= " + SysConfig.getParamPrefix() + paramName; 
						}
						else if(valueAStr.equals(valueBStr)){
							//等于
							Object valueA = ValueConverter.convertToObject(valueAStr, factorTypes.get(factorId));
							returnSqlParams.put(paramName, valueA);
							filterStr = alias + " = " + SysConfig.getParamPrefix() + paramName; 
						}
						else{
							//范围内
							Object valueA = ValueConverter.convertToObject(valueAStr, factorTypes.get(factorId));
							returnSqlParams.put(paramName + "_1", valueA);
							Object valueB = ValueConverter.convertToObject(valueBStr, factorTypes.get(factorId));
							returnSqlParams.put(paramName + "_2", valueB);
							filterStr = "(" + alias + " >= " + SysConfig.getParamPrefix() + paramName + "_1"
									+ " and " + alias + " <= " + SysConfig.getParamPrefix() + paramName + "_2" + ")";
						}
						valueFilters.add(filterStr);
						l++;
					}
				}				
			}

			List<String> roleFilterStrs = new ArrayList<String>();
			for(int i=0;i<roleRows.size();i++){
				DataRow roleRow = roleRows.get(i);
				String roleId = roleRow.getStringValue("roleid"); 
				
				HashMap<String, List<String>> aliasToFilters = roleToDataPurview.get(roleId); 
				
				List<String> aliasFilterStrs = new ArrayList<String>();
				for(String alias : aliasToFactors.keySet()){ 
					List<String> valueFilters = aliasToFilters.get(alias);
					if(valueFilters.size()>0){
						StringBuilder ss = new StringBuilder();
						for(int k=0;k<valueFilters.size();k++){
							ss.append((k==0?"":" or ") + valueFilters.get(k));
						}
						aliasFilterStrs.add(ss.toString());
					}
					else{
						if(factorToDefaultAll.get(aliasToFactors.get(alias))){
							aliasFilterStrs.add("1 = 1");
						}
						else{
							aliasFilterStrs.add("1 > 1");
						}
					}
				}
				if(aliasFilterStrs.size()>0){
					StringBuilder ss = new StringBuilder();
					for(int j=0;j<aliasFilterStrs.size();j++){
						ss.append((j==0?"":" and ") + "(" + aliasFilterStrs.get(j) + ")");
					}
					roleFilterStrs.add(ss.toString());
				}
			}
			if(roleFilterStrs.size()>0){
				StringBuilder clauseStr = new StringBuilder();
				for(int i=0;i<roleFilterStrs.size();i++){
					clauseStr.append((i==0?"":" or ") + "(" + roleFilterStrs.get(i) + ")");
				} 
				
				resultString = clauseStr.toString();	
			} 	
		} 
		return resultString;
	} 
}
