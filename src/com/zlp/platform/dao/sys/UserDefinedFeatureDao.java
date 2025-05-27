package com.zlp.platform.dao.sys;
 
import java.util.Date;
import java.util.HashMap; 
import org.hibernate.Session;
import com.zlp.platform.common.SysConfig; 
import com.zlp.platform.dao.db.IDBParserAccess; 
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection;

public class UserDefinedFeatureDao implements IUserDefinedFeatureDao{

	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	private IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess;
	}
	 
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
	
	@Override 
	public void saveFeature(String userId, String featureName, String modelName, String description, String content){
		IDBParserAccess dbAccess = this.getDBParserAccess();
		Session dbSession = this.getDBSession();
		String selectIdSql = "select udf.id as id from sys_userdefinedfeature udf where udf.userid = " + SysConfig.getParamPrefix()+"userid and udf.featurename = " + SysConfig.getParamPrefix()+"featurename and udf.modelname = " + SysConfig.getParamPrefix()+"modelname";
		HashMap<String, Object> selectIdP2vs = new HashMap<String, Object>();
		selectIdP2vs.put("userid", userId);
		selectIdP2vs.put("featurename", featureName);
		selectIdP2vs.put("modelname", modelName);
		Object idObj = dbAccess.getSingleValue(this.getDBSession(), selectIdSql, selectIdP2vs);
		HashMap<String, Object> values = new HashMap<String, Object>();
		values.put("userid", userId);
		values.put("featurename", featureName);
		values.put("modelname", modelName);
		values.put("description", description);
		values.put("content", content);
		
		Data udfData = DataCollection.getData("sys_UserDefinedFeature");
		if(idObj == null){
			//新增
			values.put("createtime", new Date());
			dbAccess.insertByData(dbSession, udfData, values);
		}
		else{
			//修改
			values.put("modifytime", new Date());
			dbAccess.updateByData(dbSession, udfData, values, (String)idObj);
		}		
	}
	
	@Override 
	public String getFeatureContent(String userId, String featureName, String modelName){
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String sql = "select udf.content as content from sys_userdefinedfeature udf where udf.userid = " + SysConfig.getParamPrefix()+"userid and udf.featurename = " + SysConfig.getParamPrefix()+"featurename and udf.modelname = " + SysConfig.getParamPrefix()+"modelname";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("userid", userId);
		p2vs.put("featurename", featureName);
		p2vs.put("modelname", modelName);
		Session dbSession = this.getDBSession();
		Object contentObj = dbAccess.getSingleValue(dbSession, sql, p2vs); 
		if(contentObj == null){
			return null;
		}
		else{
			return (String)contentObj;
		}		
	}
	 
	@Override
	public String getGlobalDefaultFeatureContent( String featureName, String modelName){
		IDBParserAccess dbAccess = this.getDBParserAccess();
		String sql = "select udf.content as content from sys_userdefinedfeature udf where udf.isglobaldefault = 'Y' and udf.featurename = " + SysConfig.getParamPrefix()+"featurename and udf.modelname = " + SysConfig.getParamPrefix()+"modelname";
		HashMap<String, Object> p2vs = new HashMap<String, Object>(); 
		p2vs.put("featurename", featureName);
		p2vs.put("modelname", modelName);
		Session dbSession = this.getDBSession();
		Object contentObj = dbAccess.getSingleValue(dbSession, sql, p2vs); 
		if(contentObj == null){
			return null;
		}
		else{
			return (String)contentObj;
		}		
	} 
}
