package com.zlp.platform.model.sysmodel.dbStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;

import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.SysConfig;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.DataRow;
import com.zlp.platform.dao.db.DataTable;
import com.zlp.platform.dao.db.IDBParserAccess;
import com.zlp.platform.dao.db.ValueType; 

public class DataModelToDBTableCompare extends TableCompare { 
	//执行数据库操作的通用类
	private IDBParserAccess dBParserAccess; 
	public void setDBParserAccess(IDBParserAccess dBParserAccess) {
		this.dBParserAccess = dBParserAccess;
	}  
	private IDBParserAccess getDBParserAccess() {
		return this.dBParserAccess;
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
	
	public Boolean updateDbTableStructure() throws Exception{
		try{
			String ddlString = this.getDDL();
			if(ddlString != null){
				Session dbSession = this.getDBSession();
				this.dBParserAccess.update(dbSession, ddlString, null);
				return true;
			}
			else{
				return false;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			throw new Exception("更改表结构失败. " + NcpException.getFullErrorInfo(ex));
		}
	}
	
	public void init(String dataName, String viewName) throws Exception{
		
		this.setTableName(dataName);

		DataRow dataRow = this.getDataStructure(dataName);
		DataRow viewRow = this.getViewStructure(viewName);
		if(dataRow == null){
			throw new Exception("None data model named " + dataName);
		}
		else{
			String dataId = dataRow.getStringValue("id");
			String viewId = viewRow.getStringValue("id");
			String idFieldName = dataRow.getStringValue("idfieldname").toLowerCase();
			List<DataRow> dataFieldRows = this.getDataFieldStructure(dataId);
			List<DataRow> viewDispunitRows = this.getViewDispunitStructure(viewId);
			List<DataRow> dbFieldRows = this.getDBTableStructure(dataName);
			List<DataRow> dbFieldIndexRows = this.getDBTableIndexStructure(dataName);
			
			for(DataRow dataFieldRow : dataFieldRows){
				String fieldName = dataFieldRow.getStringValue("name").toLowerCase();
				String dataType = this.getDBDataType(dataFieldRow.getStringValue("valuetype").toLowerCase());
				DataRow dbFieldRow = this.getDBFieldRow(dbFieldRows, fieldName);  
				DataRow viewDispunitRow = this.getViewDispunitRow(viewDispunitRows, fieldName);  
				Boolean dbFieldIsIndex = this.getDBFieldIsIndex(dbFieldIndexRows,  fieldName);  
				List<PropertyCompare> propertyList = new ArrayList<PropertyCompare>();				
				
				propertyList.add(new PropertyCompare(FieldPropertyType.Collation, this.getDataFieldCollation(dataFieldRow), dbFieldRow == null ? null : dbFieldRow.getStringValue("collation_name")));
				propertyList.add(new PropertyCompare(FieldPropertyType.DataType, dataType, dbFieldRow == null ? null : dbFieldRow.getStringValue("data_type").toLowerCase()));
				propertyList.add(new PropertyCompare(FieldPropertyType.FractionLength, this.getDataFieldFractionLength(dataFieldRow), dbFieldRow == null ? null : this.getDBFieldFractionLength(dbFieldRow)));
				propertyList.add(new PropertyCompare(FieldPropertyType.KeyType, (idFieldName.equals(fieldName) ? "pri": ""), dbFieldRow == null ? null : dbFieldRow.getStringValue("column_key").toLowerCase()));
				propertyList.add(new PropertyCompare(FieldPropertyType.Length, this.getDataFieldLength(dataFieldRow), dbFieldRow == null ? null : this.getDBFieldLength(dbFieldRow)));
				propertyList.add(new PropertyCompare(FieldPropertyType.CanSortable, viewDispunitRow.getBooleanValue("colsortable").toString(), (dbFieldIsIndex == null ? null : dbFieldIsIndex.toString())));
				
				//增加字段是否可为空、默认值和注释的自动对比生成功能 added by ls 20190729
				propertyList.add(new PropertyCompare(FieldPropertyType.NotNullable, this.getDataFieldNotNullable(dataFieldRow), dbFieldRow == null ? null : this.getDBFieldNotNullable(dbFieldRow)));
				propertyList.add(new PropertyCompare(FieldPropertyType.DefaultValue, this.getDataFieldDefaultValue(dataFieldRow), dbFieldRow == null ? null : this.getDBFieldDefaultValue(dbFieldRow)));
				propertyList.add(new PropertyCompare(FieldPropertyType.Comment, this.getDataFieldComment(dataFieldRow), dbFieldRow == null ? null : this.getDBFieldComment(dbFieldRow)));
				
				FieldCompare fc = new FieldCompare(fieldName, propertyList);
				this.setFieldCompare(fieldName, fc);	
			}
		}
	}
	
	public void init(String dataName) throws Exception{
		
		this.setTableName(dataName);

		DataRow dataRow = this.getDataStructure(dataName); 
		if(dataRow == null){
			throw new Exception("None data model named " + dataName);
		}
		else{
			String dataId = dataRow.getStringValue("id"); 
			String idFieldName = dataRow.getStringValue("idfieldname").toLowerCase();
			List<DataRow> dataFieldRows = this.getDataFieldStructure(dataId); 
			List<DataRow> dbFieldRows = this.getDBTableStructure(dataName);
			List<DataRow> dbFieldIndexRows = this.getDBTableIndexStructure(dataName);
			
			for(DataRow dataFieldRow : dataFieldRows){
				String fieldName = dataFieldRow.getStringValue("name").toLowerCase();
				boolean isSave = dataFieldRow.getBooleanValue("issave");
				if(isSave){
					String dataType = this.getDBDataType(dataFieldRow.getStringValue("valuetype").toLowerCase());
					DataRow dbFieldRow = this.getDBFieldRow(dbFieldRows, fieldName);     
					Boolean dbFieldIsIndex = this.getDBFieldIsIndex(dbFieldIndexRows,  fieldName);  
					List<PropertyCompare> propertyList = new ArrayList<PropertyCompare>();				
					
					propertyList.add(new PropertyCompare(FieldPropertyType.Collation, this.getDataFieldCollation(dataFieldRow), dbFieldRow == null ? null : dbFieldRow.getStringValue("collation_name")));
					propertyList.add(new PropertyCompare(FieldPropertyType.DataType, dataType, dbFieldRow == null ? null : dbFieldRow.getStringValue("data_type").toLowerCase()));
					propertyList.add(new PropertyCompare(FieldPropertyType.FractionLength, this.getDataFieldFractionLength(dataFieldRow), dbFieldRow == null ? null : this.getDBFieldFractionLength(dbFieldRow)));
					propertyList.add(new PropertyCompare(FieldPropertyType.KeyType, (idFieldName.equals(fieldName) ? "pri": ""), dbFieldRow == null ? null : dbFieldRow.getStringValue("column_key").toLowerCase()));
					propertyList.add(new PropertyCompare(FieldPropertyType.Length, this.getDataFieldLength(dataFieldRow), dbFieldRow == null ? null : this.getDBFieldLength(dbFieldRow)));
					propertyList.add(new PropertyCompare(FieldPropertyType.CanSortable, (idFieldName.equals(fieldName) ? "true": ""), (dbFieldIsIndex == null ? null : dbFieldIsIndex.toString())));
					
					//增加字段是否可为空、默认值和注释的自动对比生成功能 added by ls 20190729
					propertyList.add(new PropertyCompare(FieldPropertyType.NotNullable, this.getDataFieldNotNullable(dataFieldRow), dbFieldRow == null ? null : this.getDBFieldNotNullable(dbFieldRow)));
					propertyList.add(new PropertyCompare(FieldPropertyType.DefaultValue, this.getDataFieldDefaultValue(dataFieldRow), dbFieldRow == null ? null : this.getDBFieldDefaultValue(dbFieldRow)));
					propertyList.add(new PropertyCompare(FieldPropertyType.Comment, this.getDataFieldComment(dataFieldRow), dbFieldRow == null ? null : this.getDBFieldComment(dbFieldRow)));
					
					FieldCompare fc = new FieldCompare(fieldName, propertyList);
					this.setFieldCompare(fieldName, fc);	
				}
			}
		}
	}
	
	private DataRow getDBFieldRow(List<DataRow> dbFieldRows, String fieldName){
		for(DataRow dbFieldRow : dbFieldRows){
			if(fieldName.equals(dbFieldRow.getStringValue("columnname").toLowerCase())){
				return dbFieldRow;
			}
		}
		return null;
	}

	private DataRow getViewDispunitRow(List<DataRow> viewDispunitRows, String name){
		for(DataRow viewDispunitRow : viewDispunitRows){
			if(name.equals(viewDispunitRow.getStringValue("name").toLowerCase())){
				return viewDispunitRow;
			}
		}
		return null;
	}


	private Boolean getDBFieldIsIndex(List<DataRow> dbIndexRows, String fieldName){
		for(DataRow dbIndexRow : dbIndexRows){
			if(fieldName.equals(dbIndexRow.getStringValue("columnname").toLowerCase())){
				return true;
			}
		}
		return null;
	}
	
	private String getDBDataType(String valueType) throws Exception{
		switch(valueType){
			case "string":
			case "boolean":
				return "varchar";
			case "date":
			case "time":
			return "datetime";
			case "decimal":
			return "decimal";
			default:
				throw new Exception("Unknown value type named " + valueType);
		}
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
   					ValueType.Boolean, 
   					ValueType.String});
		List<DataRow> rows = dt.getRows();
		if(rows.size() != 0){
			return rows.get(0);
		}
		else{
			return null;
		}
	} 
	
	private DataRow getViewStructure(String viewName){
		String dataSql = "select d.id as id, " 
						+ "d.name as name " 
						+ "from sys_view d where name = " + SysConfig.getParamPrefix() +"name";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("name", viewName);
		DataTable dt = this.dBParserAccess.getMultiLineValues(dbSession,
				dataSql,
				p2vs, 
   			new String[]{
   					"id", 
   					"name"}, 
   			new ValueType[]{
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
		String dataSql = "select df.id as id, " 
						+ "df.name as name, " 
						+ "df.displayname as displayname, " 
						+ "df.description as description, " 
						+ "df.notnullable as notnullable, " 
						+ "df.defaultvalue as defaultvalue, " 
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
    					"notnullable", 
    					"defaultvalue", 
    					"valuetype", 
    					"issave", 
    					"valuelength", 
    					"decimalnum"}, 
    			new ValueType[]{
    					ValueType.String, 
    					ValueType.String, 
    					ValueType.String,  
    					ValueType.String, 
    					ValueType.Boolean,
    					ValueType.String,
    					ValueType.String,
    					ValueType.Boolean, 
    					ValueType.Decimal, 
    					ValueType.Decimal});
		List<DataRow> rows = dt.getRows();
		return rows;
	}
	private List<DataRow> getViewDispunitStructure(String viewId){
		String dataSql = "select du.id as id, " 
						+ "du.name as name, " 
						+ "du.colsortable as colsortable " 
						+ "from sys_viewdispunit du where parentid = " + SysConfig.getParamPrefix() +"viewId";
		HashMap<String, Object> p2vs = new HashMap<String, Object>();
		p2vs.put("viewId", viewId);
		DataTable dt = this.dBParserAccess.getMultiLineValues(dbSession,
				dataSql,
				p2vs, 
    			new String[]{
    					"id", 
    					"name", 
    					"colsortable"}, 
    			new ValueType[]{
    					ValueType.String, 
    					ValueType.String, 
    					ValueType.Decimal});
		List<DataRow> rows = dt.getRows();
		return rows;
	}
	
	private String getDataFieldCollation(DataRow dataFieldRow) throws Exception{
		String valueType = dataFieldRow.getStringValue("valuetype").toLowerCase();
		switch(valueType){
			case "string":
			case "boolean":
				return "utf8_unicode_ci"; 
			case "date":
			case "time":
			case "decimal":
				return ""; 
			default:
				throw new Exception("Unknown value type named " + valueType);
		}
	}
	
	private String getDataFieldLength(DataRow dataFieldRow) throws Exception{
		String valueType = dataFieldRow.getStringValue("valuetype").toLowerCase();
		switch(valueType){
			case "string":
			case "decimal":
				return ((Integer)dataFieldRow.getBigDecimalValue("valuelength").intValue()).toString(); 
			case "date":
			case "time":
				return ""; 
			case "boolean":
				return "1"; 
			default:
				throw new Exception("Unknown value type named " + valueType);
		}				
	}
	
	private String getDataFieldNotNullable(DataRow dataFieldRow) throws Exception{
		String notNullable = dataFieldRow.getStringValue("notnullable").toLowerCase();
		switch(notNullable){
			case "y":
				return "y";
			case "n":
				return "n";
			default:
				return "n";
		}				
	}
	
	private String getDataFieldComment(DataRow dataFieldRow) throws Exception{
		String comment = dataFieldRow.getStringValue("description");
		return comment;
	}
	
	private String getDataFieldDefaultValue(DataRow dataFieldRow) throws Exception{
		String defaultValue = dataFieldRow.getStringValue("defaultvalue");
		return defaultValue;
	}
	
	private String getDBFieldNotNullable(DataRow dbFieldRow) throws Exception{
		String isNullable = dbFieldRow.getStringValue("is_nullable").toLowerCase();
		switch(isNullable){
			case "yes":
				return "n"; 
			case "no":
				return "y"; 
			default:
				throw new Exception("Unknown is_nullable named " + isNullable);
		}				
	}
	
	private String getDBFieldDefaultValue(DataRow dbFieldRow) throws Exception{
		String defaultValue = dbFieldRow.getStringValue("column_default");
		return defaultValue;
	}
	
	private String getDBFieldComment(DataRow dbFieldRow) throws Exception{
		String comment = dbFieldRow.getStringValue("column_comment");
		return comment;
	}
	
	private String getDBFieldLength(DataRow dbFieldRow) throws Exception{
		String dataType = dbFieldRow.getStringValue("data_type").toLowerCase();
		switch(dataType){
			case "varchar":
				return dbFieldRow.getBigIntegerValue("character_maximum_length").toString(); 
			case "datetime":
				return null;
			case "decimal":
				return dbFieldRow.getBigIntegerValue("numeric_precision").toString();
			default:
				throw new Exception("Unknown data type named " + dataType);
		}				
	}
	
	private String getDataFieldFractionLength(DataRow dataFieldRow) throws Exception{
		String valueType = dataFieldRow.getStringValue("valuetype").toLowerCase();
		switch(valueType){
			case "string":
			case "boolean":
				return "";
			case "date":
				return "0";
			case "decimal":
			case "time":
				return ((Integer)dataFieldRow.getBigDecimalValue("decimalnum").intValue()).toString();
			default:
				throw new Exception("Unknown value type named " + valueType);
		}				
	}
	
	private String getDBFieldFractionLength(DataRow dbFieldRow) throws Exception{
		String dataType = dbFieldRow.getStringValue("data_type").toLowerCase();
		switch(dataType){
			case "varchar":
				return null; 
			case "datetime":
				return dbFieldRow.getBigIntegerValue("datetime_precision").toString(); 
			case "decimal":
				return dbFieldRow.getBigIntegerValue("numeric_scale").toString();
			default:
				throw new Exception("Unknown data type named " + dataType);
		}				
	}

	private List<DataRow> getDBTableStructure(String tableName) throws Exception{

		String dbName = SysConfig.getPropertyFileValue("system.properties", "jdbc.db"); 
		//！！！！！！！！！！！！！！此处通过扩展data模型功能实现，即实现根据data模型更新数据库的功能。
		//获取表结构

		//获取字段是否可为空、默认值和注释  added by ls 20190729
		String sql = "select table_name as tablename,"
					+ "column_name as columnname, "
					+ "data_type as data_type, "
					+ "is_nullable as is_nullable, "
					+ "column_default as column_default, "
					+ "column_comment as column_comment, "
					+ "numeric_precision as numeric_precision, "//数值类型字段长度
					+ "numeric_scale as numeric_scale, "//数值类型小数位数
					+ "character_maximum_length as character_maximum_length, "//字符串最大长度
					+ "datetime_precision as datetime_precision, "//日期类型小数位数
					+ "collation_name as collation_name, "//编码方式类型
					+ "column_key as column_key "//日期类型小数位数
					+ "from information_schema.COLUMNS where table_name = " + SysConfig.getParamPrefix() +"tableName "
					+ "and table_schema=" + SysConfig.getParamPrefix() +"dbName";

    	HashMap<String, Object> p2vs = new HashMap<String, Object>();
    	p2vs.put("tableName", tableName);
    	p2vs.put("dbName", dbName);

    	DataTable structureTable = this.dBParserAccess.getMultiLineValues(this.getDBSession(), 
    			sql, 
    			p2vs, 
    			new String[]{
    					"tablename", 
    					"columnname", 
    					"data_type", 
    					"is_nullable", 
    					"column_default", 
    					"column_comment", 
    					"numeric_precision", 
    					"numeric_scale", 
    					"character_maximum_length", 
    					"datetime_precision", 
    					"collation_name",
    					"column_key"}, 
    			new ValueType[]{
    					ValueType.String, 
    					ValueType.String, 
    					ValueType.String,  
    					ValueType.String, 
    					ValueType.String, 
    					ValueType.String, 
    					ValueType.Decimal, 
    					ValueType.Decimal, 
    					ValueType.Decimal, 
    					ValueType.Decimal, 
    					ValueType.String, 
    					ValueType.String});
    	
    	return structureTable.getRows(); 
	}

	private List<DataRow> getDBTableIndexStructure(String tableName) throws Exception{

		String dbName = SysConfig.getPropertyFileValue("system.properties", "jdbc.db"); 
		//！！！！！！！！！！！！！！此处通过扩展data模型功能实现，即实现根据data模型更新数据库的功能。
		//获取表结构
		String sql = "select table_name as tablename,"
					+ "column_name as columnname, "
					+ "index_name as indexname "
					+ "from information_schema.STATISTICS where table_name = " + SysConfig.getParamPrefix() +"tableName "
					+ "and table_schema=" + SysConfig.getParamPrefix() +"dbName";

    	HashMap<String, Object> p2vs = new HashMap<String, Object>();
    	p2vs.put("tableName", tableName);
    	p2vs.put("dbName", dbName);

    	DataTable structureTable = this.dBParserAccess.getMultiLineValues(this.getDBSession(), 
    			sql, 
    			p2vs, 
    			new String[]{
    					"tablename", 
    					"columnname", 
    					"indexname" }, 
    			new ValueType[]{
    					ValueType.String, 
    					ValueType.String, 
    					ValueType.String});
    	
    	return structureTable.getRows(); 
	}
	 
	public String getDDL() throws Exception { 
		Treatment t = this.getTreatment();
		switch(t){
			case Alter:{ 
					List<String> fieldTreatStrings = new ArrayList<String>();
					for(FieldCompare fc : this.fields.values()){ 
						Treatment ft = fc.getTreatment();
						if(ft == Treatment.Alter){
							fieldTreatStrings.add("CHANGE COLUMN " + fc.getFieldName() + " " + fc.getFieldName() + " " + this.getFieldDataTypeString(fc) + " " + this.getFieldNullableString(fc) + " " + this.getFieldDefaultValueString(fc) + " " + this.getFieldCommentString(fc)) ;
						} 
						else if(ft == Treatment.Add){
							fieldTreatStrings.add("ADD COLUMN " + fc.getFieldName() + " " + this.getFieldDataTypeString(fc) + " " + this.getFieldNullableString(fc) + " " + this.getFieldDefaultValueString(fc) + " " + this.getFieldCommentString(fc));
						}
					}  
					String newPriKey = null;
					for(FieldCompare fc : this.fields.values()){ 
						PropertyCompare pc = fc.getPropertyCompare(FieldPropertyType.KeyType);
						if(!pc.isSame()){
							if("pri".equals(pc.getSourceValue())){
								newPriKey = fc.getFieldName();
							}
						}
					}
					if(newPriKey != null){
						fieldTreatStrings.add("DROP PRIMARY KEY");
						fieldTreatStrings.add("ADD PRIMARY KEY (" + newPriKey + ")");
					}
					for(FieldCompare fc : this.fields.values()){ 
						String fieldName = fc.getFieldName();
						if(!fieldName.equals("id")){
							PropertyCompare pc = fc.getPropertyCompare(FieldPropertyType.CanSortable); 
							if(pc != null && !pc.isSame() && "true".equals(pc.getSourceValue())){
								fieldTreatStrings.add("add index " + fc.getFieldName() + " (" + fc.getFieldName() + " " + (fc.getFieldName().equals("createtime") ? "desc" : "asc") + ")");
							}
						}
					} 
					
					return ("ALTER TABLE " + this.getTableName() + " \r\n" + ValueConverter.arrayToString(fieldTreatStrings, ",\r\n")) + ";";
				} 
			case Add:{
					List<String> fieldTreatStrings = new ArrayList<String>();
					for(FieldCompare fc : this.fields.values()){  
						fieldTreatStrings.add(fc.getFieldName() + " " + this.getFieldDataTypeString(fc) + " " + this.getFieldNullableString(fc) + " " + this.getFieldDefaultValueString(fc) + " " + this.getFieldCommentString(fc)); 
					}   
					for(FieldCompare fc : this.fields.values()){ 
						PropertyCompare pc = fc.getPropertyCompare(FieldPropertyType.KeyType);
						if(!pc.isSame()){
							if("pri".equals(pc.getSourceValue())){
								fieldTreatStrings.add("PRIMARY KEY (" + fc.getFieldName() + ")");
							}
						}
					} 
					for(FieldCompare fc : this.fields.values()){ 
						String fieldName = fc.getFieldName();
						if(!fieldName.equals("id")){
							PropertyCompare pc = fc.getPropertyCompare(FieldPropertyType.CanSortable); 
							if(pc != null && "true".equals(pc.getSourceValue())){
								fieldTreatStrings.add("index " + fc.getFieldName() + " (" + fc.getFieldName() + " " + (fc.getFieldName().equals("createtime") ? "desc" : "asc") + ")");
							}
						}
					} 
					
					return "CREATE TABLE " + this.getTableName() + " (\r\n" + ValueConverter.arrayToString(fieldTreatStrings, ",\r\n") + ");"; 
				} 
			default:
				return null; 
		} 
	}
	
	public String getFieldDataTypeString(FieldCompare fc) throws Exception{
		String pDataType = fc.getPropertyCompare(FieldPropertyType.DataType).getSourceValue();
		String pLength = fc.getPropertyCompare(FieldPropertyType.Length).getSourceValue();
		String pFractionLength = fc.getPropertyCompare(FieldPropertyType.FractionLength).getSourceValue(); 
		switch(pDataType.toLowerCase()){
			case "varchar":
			case "string":
				return "varchar(" + pLength + ") CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
			case "decimal":
				return "decimal(" + pLength + "," + pFractionLength + ")";
			case "datetime":
				return "datetime" + (pFractionLength == null || pFractionLength.length() ==0 ? "" : ("(" + pFractionLength + ")"));
			default:
				throw new Exception("Unknown data type, DataType = " + pDataType);
		}
	}
	
	public String getFieldNullableString(FieldCompare fc) throws Exception{  
		String notNullable = fc.getPropertyCompare(FieldPropertyType.NotNullable).getSourceValue(); 
		switch(notNullable.toLowerCase()){
			case "y": 
				return "NOT NULL";
			case "n":
				return "NULL"; 
			default:
				throw new Exception("Unknown notNullable, NotNullable = " + notNullable);
		}
	}
	
	public String getFieldCommentString(FieldCompare fc) throws Exception{  
		String comment = fc.getPropertyCompare(FieldPropertyType.Comment).getSourceValue(); 
		if(comment == null || comment.length() == 0){
			return "";
		}
		else{
			if(comment.contains("'")){
				throw new Exception("字段注释里不要带单引号");
			}
			else{
				return "COMMENT '" + comment +"'";
			}
		}
	}
	
	public String getFieldDefaultValueString(FieldCompare fc) throws Exception{   
		String defaultValue = fc.getPropertyCompare(FieldPropertyType.DefaultValue).getSourceValue(); 
		if(defaultValue == null || defaultValue.length() == 0){
			return "";
		}
		else{
			if(defaultValue.contains("'")){
				throw new Exception("字段默认值不要带单引号");
			}
			else{
				return "DEFAULT '" + defaultValue +"'";
			} 
		}
	}

}
