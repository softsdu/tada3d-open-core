package com.zlp.platform.dao.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map; 

//解析sql
public class SelectSqlParser {
	private String sql = ""; 
	public String getInitSql(){
		return this.sql;
	}
	
	//构造获取记录数的sql
	public String getCountSql(String join, String otherWhere){
		//拼接join部分
		String fromClause = ""; 
		if(join.isEmpty()) {
			fromClause = " from " + this.getFrom();
		}
		else {
			fromClause = " from " + this.getFrom() + " " + join;
		}
		
		//拼接where部分
		String whereClause = "";
		if(this.getWhere().isEmpty()) {
			if(!otherWhere.isEmpty()) {
				whereClause = " where " + otherWhere;
			}
		}
		else{
			if(otherWhere.isEmpty()) {
				whereClause = " where " + this.getWhere();
			}
			else {
				whereClause = " where (" + this.getWhere() + ") and (" + otherWhere + ")";
			}
		}
		
		//groupby部分
		String groupbyClause = "";
		if(!this.getGroupby().isEmpty()) {
			groupbyClause = " group by " + this.getGroupby();
		}		
		
		//构造count sql
		String newSql = "select count(*) as recordCount"
				+ fromClause
				+ whereClause
				+ groupbyClause;
		return newSql;
		
	}
	
	//构造获取数据的sql
	public String getSql(String otherWhere, String otherOrderby){	
		return this.getSql("", otherWhere, otherOrderby);
	} 
	
	//构造获取数据的sql
	public String getSqlByParts(String otherJoin, String otherWhere, String otherOrderby){	
		return this.getSqlByParts("", otherJoin, otherWhere, otherOrderby);
	} 
	
	//构造获取数据的sql
	public String getSqlByParts(String otherSelect, String otherJoin, String otherWhere, String otherOrderby){	
		//拼接join部分
		String fromClause = ""; 
		if(otherJoin.isEmpty()) {
			fromClause = " from " + this.getFrom();
		}
		else {
			fromClause = " from " + this.getFrom() + " " + otherJoin + "";
		}
		
		//拼接where部分
		String whereClause = "";
		if(this.getWhere().isEmpty()) {
			if(!otherWhere.isEmpty()) {
				whereClause = " where " + otherWhere;
			}
		}
		else{
			if(otherWhere.isEmpty()) {
				whereClause = " where " + this.getWhere();
			}
			else {
				whereClause = " where (" + this.getWhere() + ") and (" + otherWhere + ")";
			}
		}

		//拼接orderby部分
		String orderbyClause = "";
		if(this.getOrderby().isEmpty()) {
			if(!otherOrderby.isEmpty()) {
				orderbyClause = " order by " + otherOrderby;
			}
		}
		else {
			if(otherOrderby.isEmpty()) {
				orderbyClause = " order by " + this.getOrderby();
			}
			else {
				orderbyClause = " order by " + otherOrderby + ", " +  this.getOrderby() ;
			}
		}
		
		//groupby部分
		String groupbyClause = "";
		if(!this.getGroupby().isEmpty()) {
			groupbyClause = " group by " + this.getGroupby();
		}		
		
		//合并
		String newSql = "select " + this.getSelect() 
				+ (otherSelect.isEmpty() ? "" : (", " + otherSelect + " ") )
				+ fromClause
				+ whereClause
				+ groupbyClause
				+ orderbyClause;
		return newSql;
	}
	
	//构造获取数据的sql
	public String getSql(String otherJoin, String otherWhere, String otherOrderby){		
		if(otherJoin.isEmpty() && otherWhere.isEmpty() && otherOrderby.isEmpty()){
			return this.sql;
		}
		else {
			return this.getSqlByParts(otherJoin, otherWhere, otherOrderby);
		}
	}
	
	//包含的字段别名(定义sql时，必须有别名)
	private List<String> selectAlias = new ArrayList<String>();
	public List<String> getSelectAlias(){
		return this.selectAlias;
	}
	
	//字段别名和字段表达式
	private HashMap<String, String> selectExpMap =new HashMap<String,String>();
	public String getSelectExpMap(String alias){
		return this.selectExpMap.get(alias);
	}
	
	
	//字段类型
	private HashMap<String, ValueType> fieldTypeMap ;
	public ValueType getFieldTypeMap(String alias){
		return this.fieldTypeMap.get(alias);
	}
	public void setFieldTypeMap( HashMap<String, ValueType> fieldTypeMap){
		this.fieldTypeMap= fieldTypeMap;
	}
	public Map<String, ValueType> getFieldTypeMaps(){
		return fieldTypeMap;
	}
	
	//select部分
	private String select = "";
	public String getSelect() {
		return select;
	} 
	protected void setSelect(String select) {
		this.select = select;
	} 
	
	//from部分
	private String from = "";
	public String getFrom() {
		return from;
	} 
	
	//where部分
	private String where = "";
	public String getWhere() {
		return where;
	} 
	
	private String orderby = "";
	public String getOrderby() {
		return orderby;
	} 
	
	//groupby部分
	private String groupby = "";
	public String getGroupby() {
		return groupby;
	}
	
	//构造
	public SelectSqlParser(String sql){
		this.sql=sql.trim();
	}
	
	//解析
	public void parser() throws Exception{
		try
		{
			int length = this.sql.length();
			HashMap<String, Integer> partMap = getPartMap();
			String currentPart = "";	
			String nextPart = "";
			int lastBegin = 0;
			while(!partMap.isEmpty()){
				int min=length;			
				for(String partName : partMap.keySet()){
					int index = partMap.get(partName);
					if(partName!=currentPart && min>index){
						min = index;
						nextPart = partName;
					}
				}
				
				if(lastBegin!=min){
					String clause = this.sql.substring(lastBegin, min);
					setCaluse(currentPart, clause.trim());
				}
				lastBegin=min + nextPart.length();
				currentPart = nextPart;
				partMap.remove(currentPart);
			}
			String clause = this.sql.substring(lastBegin, length);
			setCaluse(currentPart, clause.trim());
		}
		catch(Exception ex){ 
        	ex.printStackTrace();
			throw new Exception(ex.getMessage()+" sql="+this.sql);
		}
	} 
	
	//设置字句
	private void setCaluse(String partName, String clause) throws Exception{
		if(partName.equals("select ")){
			this.setSelect(clause);
			this.initSelect();
		}
		else if(partName.equals(" from ")){
			this.from = clause;
		}
		else if(partName.equals(" where ")){
			this.where = clause;
		}
		else if(partName.equals(" order by ")){
			this.orderby = clause;
		}
		else if(partName.equals(" group by ")){
			this.groupby = clause;
		} 
	}
	 
	//初始化select部分
	private void initSelect() throws Exception{ 
		String selectPart = this.getSelect();
		String lowerSql = selectPart.toLowerCase();
		lowerSql.replaceAll("''", "##"); 
		
		//用逗号给select分段
		int tempIndex = 0;
		List<Integer> fieldIndexes=new ArrayList<Integer>();
		boolean isStr  = false;
		int bracketCount = 0;
		int length = lowerSql.length();
		while(tempIndex < length){
			char ch = lowerSql.charAt(tempIndex);
			if(ch == '\''){
				isStr = !isStr;
			}
			if(isStr){ 
				tempIndex++; 
				continue;
			}
			if(ch=='('){
				bracketCount++;
				tempIndex++;
				continue;
			}
			if(ch==')'){
				bracketCount--;
				tempIndex++;
				continue;
			}
			if(bracketCount>0){
				tempIndex++;
				continue;
			}
			else {
				if(ch==','){
					fieldIndexes.add(tempIndex);
				}
				tempIndex++;
				continue;
			}			
		} 
		
		//截取各段
		List<String> selectParts = new ArrayList<String>();
		int lastIndex=0;
		int partCount = fieldIndexes.size();
		for(int i=0;i<partCount;i++){ 
			int tIndex = fieldIndexes.get(i);
			selectParts.add(selectPart.substring(lastIndex,fieldIndexes.get(i)));
			lastIndex = tIndex+1;
		}
		selectParts.add(selectPart.substring(lastIndex,length));
		
		//截取as
		for(String part : selectParts){
			int index = part.toLowerCase().lastIndexOf(" as ");
			if(index<0){
				throw new Exception("can not find as in " + part + ". " );
			}
			String alias = part.substring(index+4).trim();
			String exp = part.substring(0,index).trim();
			selectAlias.add(alias);
			selectExpMap.put(alias, exp);
		}
	}
	
	//获取每部分的起点位置
	private HashMap<String, Integer> getPartMap() throws Exception{
		String lowerSql = this.sql.toLowerCase();
		lowerSql.replaceAll("''", "##"); 
		HashMap<String, Integer> partMap=new HashMap<String, Integer>();
		boolean isStr  = false;
		int bracketCount = 0;
		
		int tempIndex = 0;
		int length = lowerSql.length();
		while(tempIndex < length){
			char ch = lowerSql.charAt(tempIndex);
			if(ch=='\''){
				isStr = !isStr;
			}
			if(isStr){ 
				tempIndex++; 
				continue;
			}
			if(ch=='('){
				bracketCount++;
				tempIndex++;
				continue;
			}
			if(ch==')'){
				bracketCount--;
				tempIndex++;
				continue;
			}
			if(bracketCount>0){
				tempIndex++;
				continue;
			}
			else {
				String subStr = lowerSql.substring(tempIndex);
				if(findClause(subStr,"select ", tempIndex,partMap)){
					tempIndex = tempIndex + 7;
				}
				else if(findClause(subStr," from ", tempIndex,partMap)){
					tempIndex = tempIndex + 6;
				}
				else if(findClause(subStr," where ", tempIndex,partMap)){
					tempIndex = tempIndex + 7;
				}
				else if(findClause(subStr," order by ", tempIndex,partMap)){
					tempIndex = tempIndex + 10;
				}
				else if(findClause(subStr," group by ", tempIndex,partMap)){
					tempIndex = tempIndex + 10;
				}
				else {
					tempIndex++; 
				}
				continue;
			}			
		}
		return partMap;
	} 
	
	//判断是否为某部分
	private boolean findClause(String subStr,String startStr, int index, HashMap<String, Integer> partMap ) throws Exception{
		if(subStr.startsWith(startStr)){
			if(partMap.containsKey(subStr)){
				throw new Exception("more than one "+subStr+" part. " );
			}
			else{
				partMap.put(startStr, index);
				return true;
			}
		}
		return false;
	}
}
