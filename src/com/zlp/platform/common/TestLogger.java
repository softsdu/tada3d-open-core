package com.zlp.platform.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//��¼������־
//added by ls 20180404 ������¼������־����
public class TestLogger {

	private static List<String> names = new ArrayList<String>();
	private static HashMap<String, Long> timeSpans = new HashMap<String, Long>();
	
	public static void clear(){
		names.clear();
		timeSpans.clear();
	}
	
	
	public static void plus(String name, long spanTime){
		if(timeSpans.containsKey(name)){
			timeSpans.put(name, timeSpans.get(name) + spanTime);					
		}
		else{
			names.add(name);
			timeSpans.put(name, spanTime);
		}
	}
	
	public static void print(){
		for(int i = 0; i < names.size(); i++){
			long s = timeSpans.get(names.get(i));
			System.out.println(names.get(i) + " = " + s);
		}
	}
}
