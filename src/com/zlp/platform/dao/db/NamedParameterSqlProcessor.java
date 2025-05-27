package com.zlp.platform.dao.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NamedParameterSqlProcessor{
	
	private Map indexMap;
	
	private Object[] params;
	
	public Object[] getParams(){
		return params;
	}
	
	private String parsedSql;
	
	public String getParsedSql(){
		return parsedSql;
	}
	
	public NamedParameterSqlProcessor(String query, HashMap<String, Object> paramHash) {
		parsedSql = parse(query);
		params = processParams(paramHash);
	}
	
	private String parse(String query ) {
		indexMap = new HashMap();	
		int length = query.length();
		StringBuffer parsedQuery = new StringBuffer(length);
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int index = 1;
	 
		for (int i = 0; i < length; i++) {
			char c = query.charAt(i);
			if (inSingleQuote) {
				if (c == '\'') {
					inSingleQuote = false;
				}
			} 
			else if (inDoubleQuote) {
				if (c == '"') {
					inDoubleQuote = false;
				}
			} 
			else {
				if (c == '\'') {
					inSingleQuote = true;
				} 
				else if (c == '"') {
					inDoubleQuote = true;
				} 
				else if (c == ':' && i + 1 < length
						&& Character.isJavaIdentifierStart(query.charAt(i + 1))) {
					int j = i + 2;
					while (j < length
							&& Character.isJavaIdentifierPart(query.charAt(j))) {
						j++;
					}
					String name = query.substring(i + 1, j);
					c = '?';
					i += name.length();
	
					List indexList = (List) indexMap.get(name);
					if (indexList == null) {
						indexList = new LinkedList();
						indexMap.put(name, indexList);
					}
					indexList.add(new Integer(index));
	
					index++;
				}
			}
			parsedQuery.append(c);
		}
	
		for (Iterator itr = indexMap.entrySet().iterator(); itr.hasNext();) {
			Map.Entry entry = (Map.Entry) itr.next();
			List list = (List) entry.getValue();
			int[] indexes = new int[list.size()];
			int i = 0;
			for (Iterator itr2 = list.iterator(); itr2.hasNext();) {
				Integer x = (Integer) itr2.next();
				indexes[i++] = x.intValue();
			}
			entry.setValue(indexes);
		}
	
		return parsedQuery.toString();
	}
	
	private Object[] processParams( HashMap<String, Object> paramHash){
		int paramCount = 0;
		for(Object name : indexMap.keySet()) {
			paramCount += getIndexes((String)name).length;
		}
		Object[] paramObjects = new Object[paramCount];

		for(Object name : indexMap.keySet()) {
			int[] indexes = getIndexes((String)name);
			Object value = paramHash.get((String)name);
			if(indexes != null){
				for(int i=0;i<indexes.length;i++){
					paramObjects[indexes[i] - 1] = value;
				}
			}
		}
		return paramObjects;
	}
	
	private int[] getIndexes(String name) {
		int[] indexes = (int[]) indexMap.get(name); 
		return indexes;
	}
}