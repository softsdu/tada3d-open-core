package com.zlp.platform.common; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject; 

public class JSONProcessor {

	//哈希表转字符串
	public static String mapToStr(HashMap<String, Object> map){
		JSONObject json = new JSONObject();
		for(String key : map.keySet()){
			json.put(key, map.get(key));
		} 
		JSONArray jsonArray = new JSONArray();
		jsonArray.add(json);		
    	String str = jsonArray.toString();
        return str;
	}
	//JSON转字符串
	public static String jsonToStr(JSONObject json){  
    	String str=json.toString();
        return str;
	}
	
	//字符串转json
	public static JSONObject strToJSON(String str) throws Exception{
		try
		{
	    	JSONObject json = JSONObject.parseObject(str); 
	    	return json;
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new Exception("can not convert string to json. str = "+ str);
		}
	}  
	
	//字符串转jsonArray
	public static JSONArray strToJSONArray(String str) throws Exception{
		try
		{
			JSONArray json = JSONArray.parseArray(str); 
	    	return json;
		}
		catch(Exception ex){
        	ex.printStackTrace();
			throw new Exception("can not convert string to json. str = "+ str);
		}
	}  
	
	//哈希表转字符串(fastjson)
	public static String mapToFastStr(HashMap<String, Object> map){
		List<HashMap<String, Object>> list = new ArrayList<>();
		list.add(map);
		return JSON.toJSONString(list);
	}
}
