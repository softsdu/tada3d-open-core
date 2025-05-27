package com.zlp.platform.expression.expFunction.excelFunction;
 
import java.math.BigDecimal;
import java.util.List; 

//Excel文本
public class ExcelText {
	//拼接字符串
	public String concatenate(List<String> strs){
		StringBuilder ss = new StringBuilder();
		for(String s : strs){
			ss.append(s);
		}
		return ss.toString();
	} 
	//拼接字符串
	public String concatenate(BigDecimal d1, BigDecimal d2, BigDecimal d3, List<String> strs1, List<String> strs2){
		StringBuilder ss = new StringBuilder();
		ss.append(d1.toString() + ", ");
		ss.append(d2.toString() + ", ");
		ss.append(d3.toString() + ", ");
		for(String s : strs1){
			ss.append(s + ", ");
		}
		for(String s : strs2){
			ss.append(s + ", ");
		}
		return ss.toString();
	} 
}
