package com.zlp.constraintSolver.processor;

import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.ValueType;

public class MdlComponentParameter {	
	private String id = "";
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	private String name = "";
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	private ParameterType paramType = ParameterType.STRING;
	public ParameterType getParamType() {
		return paramType;
	}
	public void setParamType(ParameterType paramType) {
		this.paramType = paramType;
	}  
	
	private boolean isNullable = false;
	public boolean isNullable() {
		return isNullable;
	}
	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable;
	}

	private boolean isEditable = false;
	public boolean isEditable() {
		return isEditable;
	}
	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}
	
	private String defaultValue = null;
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public Object getDefaultValueObject() throws Exception{
		ValueType valueType = this.getValueType(this.getParamType());
		return ValueConverter.convertToObject(this.getDefaultValue(), valueType);
	}
	
	private String groupName = null;
	public String getGroupName(){
		return this.groupName;
	}
	public void setGroupName(String groupName){
		this.groupName = groupName;
	}

	private String listValues = null;
	public String getListValues() {
		return listValues;
	}
	public void setListValues(String listValues) {
		this.listValues = listValues;
	}
	
	private MdlComponentExp exp = null;
	public MdlComponentExp getExp() {
		return exp;
	}
	public void setExp(MdlComponentExp exp) {
		this.exp = exp;
	}
	
	//分类名称 added by ls 20230726
	private String categoryName = null;
	public String getCategoryName(){
		return this.categoryName;
	}
	public void setCategoryName(String categoryName){
		this.categoryName = categoryName;
	}

	
	//组内序号 added by ls 20230726
	private int indexInGroup = 1;
	public int getIndexInGroup(){
		return this.indexInGroup;
	}
	public void setIndexInGroup(int indexInGroup){
		this.indexInGroup = indexInGroup;
	}
	
	public MdlComponentParameter(String id, String name, ParameterType paramType, boolean isNullable, boolean isEditable, String defaultValue, String listValues, MdlComponentExp exp, String groupName, String indexName, String categoryName, int indexInGroup){
		this.id = id;
		this.name = name;
		this.paramType = paramType; 
		this.isNullable = isNullable;
		this.isEditable = isEditable;
		this.defaultValue = defaultValue;
		this.listValues = listValues;	
		this.exp = exp;
		this.groupName = groupName;
		//参数对应指标  added by liyh 20211129
		this.indexName = indexName;

		//分类名称 added by ls 20230726
		this.categoryName = categoryName;
		
		//组内序号 added by ls 20230804
		this.indexInGroup = indexInGroup;
	}
		
	public static ValueType getValueType(String paramTypeStr) throws Exception{
		ParameterType paramType = ParameterType.valueOf(paramTypeStr.toUpperCase());
		return MdlComponentParameter.getValueType(paramType);
	}
		
	public static ValueType getValueType(ParameterType paramType) throws Exception{ 
		switch(paramType){
			case STRING:{
				return ValueType.String; 
			}
			case DECIMAL:{
				return ValueType.Decimal; 
			}
			case BOOLEAN:{
				return ValueType.Boolean; 
			}
			case DATE:{
				return ValueType.Date; 
			}
			case TIME:{
				return ValueType.Time; 
			}
			case MATERIAL:

			//数组类型 added by liyh 20230606
			case ARRAY:

			case GLTFFILE:
			case BINFILE:
			case PATHFILE:
			case SURFACEFILE:

			//FBX文件 added by ls 20240112
			case FBXFILE:

			//辅助点文件 added by ls 20230418
			case ASSISTFILE:
				
			//增加参数类型 added by ls 20220607
			case POINT2D:
			case POINT3D:
			case POLYLINE2D:
			case POLYLINE3D:

			//线参数类型 added by ls 20230613
			case LINE2D:
			case LINE3D:
				
			//增加path参数类型 added by ls 20230105
			case PATH2D:
			case PATH3D:
			case PATHCLOSED2D:
			case PATHCLOSED3D:{
				return ValueType.String; 
			}
			default:{
				throw new Exception("Unknown param type = " + paramType.toString());
			}
		}
	}

	//参数对应指标  added by liyh 20211129
	private String indexName = "";
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
}
