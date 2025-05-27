package com.zlp.platform.common;

import java.util.HashMap;

import com.alibaba.fastjson.JSONObject;

public class ServiceResultProcessor {

	private static HashMap<String,Object> getResultHashMap(Object o){
		HashMap<String,Object> json = new HashMap<String,Object>();
		json.put("code", "000");
		json.put("message", "");
		json.put("result", o);
		return json;
	}

	private static String createBaseJsonResultStr(Object o){
		return JSONProcessor.mapToStr(getResultHashMap(o));
	}

	//处理webservice的返回值
	public static String createJsonResultStr(String returnInfo) {
		return createBaseJsonResultStr(returnInfo);
	}
	//处理webservice的返回值
	public static String createJsonResultStr(HashMap<String, Object> resultMap) {
		return createBaseJsonResultStr(resultMap);
	}
	//处理webservice的返回值
	public static String createJsonResultStr(JSONObject resultJson) {
		return createBaseJsonResultStr(resultJson);
	} 
}
