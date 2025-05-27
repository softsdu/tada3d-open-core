package com.zlp.platform.model.sysmodel.dbStructure;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.zlp.platform.common.FileOperate;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.constants.ZlpState;
import com.zlp.platform.core.ConfigContext;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType;
import com.zlp.platform.dao.sys.ContextUtil;
import com.zlp.platform.dataManagement.importExportDefinition.Field;
import com.zlp.platform.dataManagement.importExportDefinition.ImportExportDefinition;
import com.zlp.platform.model.sysmodel.Data;
import com.zlp.platform.model.sysmodel.DataCollection; 

public class ImportExportDefinitionToModelCompare extends TableCompare { 
	//执行数据库操作的通用类
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	private IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess;
	} 
	
	private String dataId = null;
	public String getDataId() {
		return dataId;
	}
	private void setDataId(String dataId) {
		this.dataId = dataId;
	}
	
	private String viewId = null;
	public String getViewId() {
		return viewId;
	}
	private void setViewId(String viewId) {
		this.viewId = viewId;
	}
	
	private String orderby = "";
	public String getOrderby() {
		return orderby;
	}
	private void setOrderby(String orderby) {
		this.orderby = orderby;
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
	 
	private DataRow getViewDispunit(List<DataRow> viewDispunitRows, String fieldName){
		if(viewDispunitRows != null){
			for(DataRow viewDispunitRow : viewDispunitRows){
				if(fieldName.equals(viewDispunitRow.getValue("name"))){
					return viewDispunitRow;
				}
			}
		}
		return null;
	}

	private DataRow getDataField(List<DataRow> dataFieldRows, String fieldName){
		if(dataFieldRows != null){
			for(DataRow dataFieldRow : dataFieldRows){
				if(fieldName.equals(dataFieldRow.getValue("name"))){
					return dataFieldRow;
				}
			}
		}
		return null;
	}
	 
	private DataRow getDataStructure(String dataName){
		String dataSql = "select d.id as id, " 
						+ "d.name as name, " 
						+ "d.dstype as dstype, " 
						+ "d.dsexp as dsexp, " 
						+ "d.savedest as savedest, " 
						+ "d.isusing as isusing, " 
						+ "d.idfieldname as idfieldname " 
						+ "from sys_data d where name = " + SysConfig.getParamPrefix() +"name";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("name", dataName);
		DataTable dt = this.dBParserAccess.getMultiLineValues(dbSession,
				dataSql,
				p2vs, 
   			new String[]{
   					"id", 
   					"name", 
   					"dstype", 
   					"dsexp", 
   					"savedest", 
   					"isusing", 
   					"idfieldname"}, 
   			new ValueType[]{
   					ValueType.String, 
   					ValueType.String, 
   					ValueType.String,  
   					ValueType.String, 
   					ValueType.String, 
   					ValueType.String, 
   					ValueType.String});
		List<DataRow> rows = dt.getRows();
		if(rows.size() != 0){
			return rows.get(0);
		}
		else{
			return null;
		}
	}

	private List<DataRow> getDataFieldStructure(String dataId){
		if(dataId == null){
			return null;
		}
		else{
			String dataSql = "select df.id as id, " 
							+ "df.name as name, " 
							+ "df.displayname as displayname, " 
							+ "df.description as description, " 
							+ "df.valuetype as valuetype, " 
							+ "df.issave as issave, " 
							+ "df.valuelength as valuelength, " 
							+ "df.decimalnum as decimalnum " 
							+ "from sys_datafield df where parentid = " + SysConfig.getParamPrefix() +"dataId";
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
			p2vs.put("dataId", dataId);
			DataTable dt = this.dBParserAccess.getMultiLineValues(dbSession,
					dataSql,
					p2vs, 
	    			new String[]{
	    					"id", 
	    					"name", 
	    					"displayname", 
	    					"description", 
	    					"valuetype", 
	    					"issave", 
	    					"valuelength", 
	    					"decimalnum"}, 
	    			new ValueType[]{
	    					ValueType.String, 
	    					ValueType.String, 
	    					ValueType.String,  
	    					ValueType.String, 
	    					ValueType.String, 
	    					ValueType.Boolean, 
	    					ValueType.Decimal, 
	    					ValueType.Decimal});
			List<DataRow> rows = dt.getRows();
			return rows;
		}
	}
	
	private DataRow getViewStructure(String dataName){
		if(dataName == null){
			return null;
		}
		else{
			String dataSql = "select v.id as id, " 
							+ "v.name as name, " 
							+ "v.dataname as dataname " 
							+ "from sys_view v where dataname = " + SysConfig.getParamPrefix() +"dataname";
			HashMap<String, Object> p2vs = new HashMap<String, Object>();
			p2vs.put("dataname", dataName);
			DataTable dt = this.dBParserAccess.getMultiLineValues(dbSession,
					dataSql,
					p2vs, 
	   			new String[]{
	   					"id", 
	   					"name", 
	   					"dataname"}, 
	   			new ValueType[]{
	   					ValueType.String, 
	   					ValueType.String, 
	   					ValueType.String});
			List<DataRow> rows = dt.getRows();
			if(rows.size() != 0){
				return rows.get(0);
			}
			else{
				return null;
			}
		}
	}

	private List<DataRow> getViewDispunitStructure(String viewId){
		String dataSql = "select vd.id as id, " 
						+ "vd.name as name, " 
						+ "vd.label as label, " 
						+ "vd.colwidth as colwidth, " 
						+ "vd.colvisible as colvisible, " 
						+ "vd.colsearch as colsearch, " 
						+ "vd.colindex as colindex "
						+ "from sys_viewdispunit vd where parentid = " + SysConfig.getParamPrefix() +"viewId";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("viewId", viewId);
		DataTable dt = this.dBParserAccess.getMultiLineValues(dbSession,
				dataSql,
				p2vs, 
    			new String[]{
    					"id", 
    					"name", 
    					"label", 
    					"colwidth", 
    					"colvisible",
    					"colsearch", 
    					"colindex"}, 
    			new ValueType[]{
    					ValueType.String, 
    					ValueType.String, 
    					ValueType.String,  
    					ValueType.Decimal, 
    					ValueType.Boolean, 
    					ValueType.Boolean, 
    					ValueType.Decimal});
		List<DataRow> rows = dt.getRows();
		return rows;
	}
	
	private String getDataFieldFractionLength(DataRow dataFieldRow) throws Exception{
		String valueType = dataFieldRow.getStringValue("valuetype").toLowerCase();
		switch(valueType){
			case "string":
			case "boolean":
			case "date":
				return "0";
			case "decimal":
			case "time":
				return ((Integer)dataFieldRow.getBigDecimalValue("decimalnum").intValue()).toString(); 
			default:
				throw new Exception("Unknown value type named " + valueType);
		}				
	} 
	 
	public Boolean updateModel() throws Exception { 
		
		Treatment t = this.getDataId() != null ? Treatment.Alter : this.getTreatment();
		
		switch(t){
			case Alter:{  
					HashMap<String, Object> dataValues = null;  
					HashMap<String, HashMap<String, Object>> allAddDataFieldValues = new HashMap<String, HashMap<String, Object>>();
					HashMap<String, HashMap<String, Object>> allAddDispunitValues = new HashMap<String, HashMap<String, Object>>();
					HashMap<String, HashMap<String, Object>> allUpdateDataFieldValues = new HashMap<String, HashMap<String, Object>>();
					HashMap<String, HashMap<String, Object>> allUpdateDispunitValues = new HashMap<String, HashMap<String, Object>>();
					for(FieldCompare fc : this.fields.values()){ 
						Treatment ft = fc.getTreatment();
						HashMap<String, Object> dataFieldValues = this.getDataFieldValues(fc);
						HashMap<String, Object> viewDispunitValues = this.getViewDispunitValues(fc);
						if(ft == Treatment.Alter){
							allUpdateDataFieldValues.put(fc.getFieldName(), dataFieldValues);
							allUpdateDispunitValues.put(fc.getFieldName(), viewDispunitValues);
						} 
						else if(ft == Treatment.Add){
							dataFieldValues.put("parentid", this.getDataId());
							viewDispunitValues.put("parentid", this.getViewId()); 
							allAddDataFieldValues.put(fc.getFieldName(), dataFieldValues);
							allAddDispunitValues.put(fc.getFieldName(), viewDispunitValues);
						}
					}    
					
					dataValues = new HashMap<String, Object>();
					dataValues.put("dsexp", this.getTableSelectSQL());
					
					Session dbSession = this.getDBSession();
					Transaction tran = null;
					try {
						tran = dbSession.beginTransaction();
						Data sysData = DataCollection.getData("sys_Data");
						Data sysDataField = DataCollection.getData("sys_DataField");
						String updateDataFieldSql = "update sys_datafield set name = " + SysConfig.getParamPrefix() + "name, "
								+ "valuetype = " + SysConfig.getParamPrefix() + "valuetype, "
								+ "displayname = " + SysConfig.getParamPrefix() + "displayname, "
								+ "issave = " + SysConfig.getParamPrefix() + "issave, "
								+ "decimalnum = " + SysConfig.getParamPrefix() + "decimalnum, "
								+ "isreadonly = " + SysConfig.getParamPrefix() + "isreadonly, "
								+ "issum = " + SysConfig.getParamPrefix() + "issum, "
								+ "valuelength = " + SysConfig.getParamPrefix() + "valuelength "
								+ "where parentid = " + SysConfig.getParamPrefix() + "parentid and name = " + SysConfig.getParamPrefix() + "name";
						this.dBParserAccess.updateByData(dbSession, sysData, dataValues, this.getDataId());
						for(String fieldName : allUpdateDataFieldValues.keySet()){
							HashMap<String, Object> dataFieldValues = allUpdateDataFieldValues.get(fieldName);
							dataFieldValues.put("parentid",  this.getDataId()); 
							this.dBParserAccess.update(dbSession, updateDataFieldSql, dataFieldValues);							
						}
						for(String fieldName : allAddDataFieldValues.keySet()){
							HashMap<String, Object> dataFieldValues = allAddDataFieldValues.get(fieldName);
							dataFieldValues.put("parentid",  this.getDataId()); 
							this.dBParserAccess.insertByData(dbSession, sysDataField, dataFieldValues);							
						}
						
						Data sysViewDispunitData = DataCollection.getData("sys_ViewDispunit");
						String updateViewDispunitSql = "update sys_viewdispunit set name = " + SysConfig.getParamPrefix() + "name, "
								+ "label = " + SysConfig.getParamPrefix() + "label, "
								+ "colwidth = " + SysConfig.getParamPrefix() + "colwidth, "
								+ "colsearch = " + SysConfig.getParamPrefix() + "colsearch, "
								+ "colvisible = " + SysConfig.getParamPrefix() + "colvisible, " 
								+ "colsortable = " + SysConfig.getParamPrefix() + "colsortable, " 
								+ "colindex = " + SysConfig.getParamPrefix() + "colindex "
								+ "where parentid = " + SysConfig.getParamPrefix() + "parentid and name = " + SysConfig.getParamPrefix() + "name";
						for(String fieldName : allUpdateDispunitValues.keySet()){
							HashMap<String, Object> dispunitValues = allUpdateDispunitValues.get(fieldName);
							dispunitValues.put("parentid",  this.getViewId()); 
							this.dBParserAccess.update(dbSession, updateViewDispunitSql, dispunitValues);							
						}
						for(String fieldName : allAddDispunitValues.keySet()){
							HashMap<String, Object> dispunitValues = allAddDispunitValues.get(fieldName);
							dispunitValues.put("parentid", this.getViewId()); 
							this.dBParserAccess.insertByData(dbSession, sysViewDispunitData, dispunitValues);							
						}
												
						tran.commit();
					}
					catch(Exception ex){
						tran.rollback();
						throw new Exception("更新模型出错" + this.getTableName(), ex);
					}			
					finally{
						
					}
				} 
				return true;
			case Add:{		
					HashMap<String, Object> dataValues = null;
					HashMap<String, Object> viewValues = null;
					HashMap<String, HashMap<String, Object>> allAddDataFieldValues = new HashMap<String, HashMap<String, Object>>();
					HashMap<String, HashMap<String, Object>> allAddDispunitValues = new HashMap<String, HashMap<String, Object>>();	
					String dataId  = this.dBParserAccess.getSequenceGenerator().getIdSequence("sys_Data", 1).get(0);
					String viewId  = this.dBParserAccess.getSequenceGenerator().getIdSequence("sys_View", 1).get(0);
					this.setDataId(dataId);
					this.setViewId(viewId);
					
					dataValues = new HashMap<String, Object>();
					dataValues.put("id", dataId);
					dataValues.put("name", this.getTableName());
					dataValues.put("savedest", this.getTableName());
					dataValues.put("dstype", "sql");
					dataValues.put("savetype", "db");
					dataValues.put("isusing", "Y");
					dataValues.put("idfieldname", "id");
					dataValues.put("dsexp", this.getTableSelectSQL());
					
					viewValues = new HashMap<String, Object>();
					viewValues.put("id", viewId);
					viewValues.put("name", this.getTableName());
					viewValues.put("dataname", this.getTableName());
					
					for(FieldCompare fc : this.fields.values()){  
						HashMap<String, Object> dataFieldValues = this.getDataFieldValues(fc);
						HashMap<String, Object> viewDispunitValues = this.getViewDispunitValues(fc);
						dataFieldValues.put("parentid", this.getDataId());
						viewDispunitValues.put("parentid", this.getViewId());
						allAddDataFieldValues.put(fc.getFieldName(), dataFieldValues);
						allAddDispunitValues.put(fc.getFieldName(), viewDispunitValues);
					}
					allAddDataFieldValues.put("id",  this.getIdDataFieldValues());
					allAddDispunitValues.put("id", this.getIdViewDispunitValues());   
					allAddDataFieldValues.put("parentid",  this.getParentIdDataFieldValues());
					allAddDispunitValues.put("parentid", this.getParentIdViewDispunitValues()); 
					allAddDataFieldValues.put("createtime",  this.getCreateTimeDataFieldValues());
					allAddDispunitValues.put("createtime", this.getCreateTimeViewDispunitValues());  
					allAddDataFieldValues.put("isdeleted",  this.getIsDeletedDataFieldValues());
					allAddDispunitValues.put("isdeleted", this.getIsDeletedViewDispunitValues());   

					Session dbSession = this.getDBSession();
					Transaction tran = null;
					try {
						tran = dbSession.beginTransaction();
						Data sysData = DataCollection.getData("sys_Data");
						Data sysDataField = DataCollection.getData("sys_DataField");
						this.dBParserAccess.insertByData(dbSession, sysData, dataValues, dataId);
						for(String fieldName : allAddDataFieldValues.keySet()){
							HashMap<String, Object> dataFieldValues = allAddDataFieldValues.get(fieldName);
							dataFieldValues.put("parentid", dataId); 
							this.dBParserAccess.insertByData(dbSession, sysDataField, dataFieldValues);							
						}

						Data sysViewData = DataCollection.getData("sys_View");
						Data sysViewDispunitData = DataCollection.getData("sys_ViewDispunit");
						this.dBParserAccess.insertByData(dbSession, sysViewData, viewValues, viewId);
						for(String fieldName : allAddDispunitValues.keySet()){
							HashMap<String, Object> dispunitValues = allAddDispunitValues.get(fieldName);
							dispunitValues.put("parentid", viewId); 
							this.dBParserAccess.insertByData(dbSession, sysViewDispunitData, dispunitValues);							
						}
												
						tran.commit();
					}
					catch(Exception ex){
						tran.rollback();
						throw new Exception("生成模型出错" + this.getTableName(), ex);
					}			
					finally{
						
					}
				} 
				return true;
			default:
				return false;
		} 
	}

	
	private String getTableSelectSQL() {
		List<String> fieldSqlParts = new ArrayList<String>();
		for(FieldCompare fc : this.fields.values()){  
			fieldSqlParts.add("t." + fc.getFieldName() + " as " + fc.getFieldName()); 
		}
		
		return "select t.id as id, t.createtime as createtime, t.isdeleted as isdeleted, " + ValueConverter.arrayToString(fieldSqlParts, ", ")
		+ " from " + this.getTableName() + " t" 
		+ (this.getOrderby().length() == 0 ? "" : " order by " + this.getOrderby());
	}
	private HashMap<String, Object> getDataFieldValues(FieldCompare fc) {
		String ieDataType = fc.getPropertyCompare(FieldPropertyType.DataType).getSourceValue();
		String ieFractionLength = fc.getPropertyCompare(FieldPropertyType.FractionLength).getSourceValue();
		String ieLength = fc.getPropertyCompare(FieldPropertyType.Length).getSourceValue(); 
		String ieShowName = fc.getPropertyCompare(FieldPropertyType.ShowName).getSourceValue();  
		HashMap<String, Object> dataFieldValues = new HashMap<String, Object>(); 
		dataFieldValues.put("name", fc.getFieldName());
		dataFieldValues.put("valuetype", ieDataType);
		dataFieldValues.put("displayname", ieShowName);
		dataFieldValues.put("issave", "Y");
		dataFieldValues.put("isreadonly", "N");
		dataFieldValues.put("issum", "N"); 
		dataFieldValues.put("decimalnum", BigDecimal.valueOf(Double.valueOf(ieFractionLength)));
		dataFieldValues.put("valuelength", BigDecimal.valueOf(Double.valueOf(ieLength)));  
		return dataFieldValues;
	}
	private HashMap<String, Object> getIdDataFieldValues() {
		HashMap<String, Object> dataFieldValues = new HashMap<String, Object>(); 
		dataFieldValues.put("name", "id");
		dataFieldValues.put("valuetype", "String");
		dataFieldValues.put("displayname", "id");
		dataFieldValues.put("issave", "Y");
		dataFieldValues.put("isreadonly", "N"); 
		dataFieldValues.put("issum", "N"); 
		dataFieldValues.put("decimalnum", BigDecimal.valueOf(0));
		dataFieldValues.put("valuelength", BigDecimal.valueOf(40));  
		return dataFieldValues;
	}
	private HashMap<String, Object> getParentIdDataFieldValues() {
		HashMap<String, Object> dataFieldValues = new HashMap<String, Object>(); 
		dataFieldValues.put("name", "parentid");
		dataFieldValues.put("valuetype", "String");
		dataFieldValues.put("displayname", "parentid");
		dataFieldValues.put("issave", "Y");
		dataFieldValues.put("isreadonly", "N"); 
		dataFieldValues.put("issum", "N"); 
		dataFieldValues.put("decimalnum", BigDecimal.valueOf(0));
		dataFieldValues.put("valuelength", BigDecimal.valueOf(40));  
		return dataFieldValues;
	}
	private HashMap<String, Object> getCreateTimeDataFieldValues() {
		HashMap<String, Object> dataFieldValues = new HashMap<String, Object>(); 
		dataFieldValues.put("name", "createtime");
		dataFieldValues.put("valuetype", "Time");
		dataFieldValues.put("displayname", "记录创建时间");
		dataFieldValues.put("issave", "Y");
		dataFieldValues.put("isreadonly", "N"); 
		dataFieldValues.put("issum", "N"); 
		dataFieldValues.put("decimalnum", BigDecimal.valueOf(3));
		dataFieldValues.put("valuelength", BigDecimal.valueOf(20));  
		return dataFieldValues;
	}
	private HashMap<String, Object> getIsDeletedDataFieldValues() {
		HashMap<String, Object> dataFieldValues = new HashMap<String, Object>(); 
		dataFieldValues.put("name", "isdeleted");
		dataFieldValues.put("valuetype", "Boolean");
		dataFieldValues.put("displayname", "已删除");
		dataFieldValues.put("issave", "Y");
		dataFieldValues.put("isreadonly", "N"); 
		dataFieldValues.put("issum", "N"); 
		dataFieldValues.put("decimalnum", BigDecimal.valueOf(0));
		dataFieldValues.put("valuelength", BigDecimal.valueOf(1));  
		return dataFieldValues;
	}

	private HashMap<String, Object> getViewDispunitValues(FieldCompare fc) {
		String ieShowName = fc.getPropertyCompare(FieldPropertyType.ShowName).getSourceValue();  
		Boolean ieIsHidden = Boolean.valueOf(fc.getPropertyCompare(FieldPropertyType.IsHidden).getSourceValue());
		BigDecimal ieShowWidth = BigDecimal.valueOf(Integer.parseInt(fc.getPropertyCompare(FieldPropertyType.ShowWidth).getSourceValue())); 
		BigDecimal ieShowIndex = BigDecimal.valueOf(Integer.parseInt(fc.getPropertyCompare(FieldPropertyType.ShowIndex).getSourceValue()));
		Boolean ieCanQuery = Boolean.valueOf(fc.getPropertyCompare(FieldPropertyType.CanQuery).getSourceValue());  
		HashMap<String, Object> dataFieldValues = new HashMap<String, Object>();
		dataFieldValues.put("name", fc.getFieldName());
		dataFieldValues.put("label", ieShowName);
		dataFieldValues.put("colwidth", ieShowWidth);
		dataFieldValues.put("colsearch", ieCanQuery  ? "Y" : "N" );
		dataFieldValues.put("colsortable", ieCanQuery  ? "Y" : "N" );
		dataFieldValues.put("colvisible", ieIsHidden ? "N" : "Y" ); 
		dataFieldValues.put("colindex", ieShowIndex); 
		return dataFieldValues;
	}
	private HashMap<String, Object> getIdViewDispunitValues() {
		HashMap<String, Object> dataFieldValues = new HashMap<String, Object>();
		dataFieldValues.put("name", "id");
		dataFieldValues.put("label", "ID");
		dataFieldValues.put("colwidth", 200);
		dataFieldValues.put("colsearch", "N" );
		dataFieldValues.put("colvisible", "Y" ); 
		dataFieldValues.put("colsortable", "N" );
		dataFieldValues.put("colindex", 100); 
		return dataFieldValues;
	}
	private HashMap<String, Object> getParentIdViewDispunitValues() {
		HashMap<String, Object> dataFieldValues = new HashMap<String, Object>();
		dataFieldValues.put("name", "parentid");
		dataFieldValues.put("label", "parentid");
		dataFieldValues.put("colwidth", 0);
		dataFieldValues.put("colsearch", "N" );
		dataFieldValues.put("colvisible", "N" ); 
		dataFieldValues.put("colsortable", "Y" );
		dataFieldValues.put("colindex", 101); 
		return dataFieldValues;
	}
	private HashMap<String, Object> getCreateTimeViewDispunitValues() {
		HashMap<String, Object> dataFieldValues = new HashMap<String, Object>();
		dataFieldValues.put("name", "createtime");
		dataFieldValues.put("label", "记录创建时间");
		dataFieldValues.put("colwidth", 0);
		dataFieldValues.put("colsearch", "N" );
		dataFieldValues.put("colvisible", "N" ); 
		dataFieldValues.put("colsortable", "Y" );
		dataFieldValues.put("colindex", 102); 
		return dataFieldValues;
	}
	private HashMap<String, Object> getIsDeletedViewDispunitValues() {
		HashMap<String, Object> dataFieldValues = new HashMap<String, Object>();
		dataFieldValues.put("name", "isdeleted");
		dataFieldValues.put("label", "已删除");
		dataFieldValues.put("colwidth", 0);
		dataFieldValues.put("colsearch", "N" );
		dataFieldValues.put("colvisible", "N" ); 
		dataFieldValues.put("colsortable", "N" );
		dataFieldValues.put("colindex", 103); 
		return dataFieldValues;
	}

}
