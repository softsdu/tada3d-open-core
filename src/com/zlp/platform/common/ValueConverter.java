package com.zlp.platform.common;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.zlp.platform.dao.db.ValueType;

public class ValueConverter {

	//判断是否为数值类型 modified by ls 202205
	public static boolean CheckDecimal(String s){
		String regEx = "-?[0-9]+(\\.[0-9]+)?";
		return s == null ? false : s.matches(regEx);
		//return s == null ? false : s.matches("\\d+(.\\d+)?");
	}
	
	//判断是否为布尔类型
	public static boolean CheckBoolean(String s){
		if(s==null){
			return false;
		}
		else{
			s = s.toLowerCase();
			return "true".equals(s) || "false".equals(s);
		}
	}
	
	//转为字符串
	public static String convertToString(Object obj, String valueTypeStr) throws Exception{
		ValueType valueType = ValueType.String;
		try
		{
			valueType = ValueType.valueOf(valueTypeStr);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			throw new Exception("can not get value type = "+valueTypeStr+"."+ex.getMessage());
		}
		return ValueConverter.convertToString(obj, valueType);
	}
	
	//转为字符串
	public static String convertToString(Object obj, ValueType valueType) throws Exception{
		if(obj == null) {
			return null;
		}
		else {
			try
			{
				switch(valueType) {
					case Object:
						return obj.toString();
					case Decimal:
						return obj.toString(); 
					case String:
						return obj.toString();
					case Boolean:
						return (Boolean)obj ? "Y" : "N";
					case Time:
						SimpleDateFormat sdf =   new SimpleDateFormat(SysConfig.getTimeFormat());
						return sdf.format((Date)obj);
					case Date:
						SimpleDateFormat stf =   new SimpleDateFormat(SysConfig.getDateFormat());
						return stf.format((Date)obj);
					default:
						throw new Exception("unkown type = " + valueType.toString());
				}
			}
			catch(Exception ex){
            	ex.printStackTrace();
				throw new Exception("convertToString error."+ex.getMessage());
			}
		}
	}
	
	public static String dateTimeToString(Date date, String format){
		if(date == null){
			return "";
		}
		else{
			SimpleDateFormat stf =   new SimpleDateFormat(format);
			return stf.format(date);
		}
	}
	
	//字符串转为值
	public static Object convertToObject(String str, String valueTypeStr) throws Exception{
		ValueType valueType = ValueType.String;
		try
		{
			valueType = ValueType.valueOf(valueTypeStr);
		}
		catch(Exception ex) {
        	ex.printStackTrace();
			throw new Exception("can not get value type = "+valueTypeStr+"."+ex.getMessage());
		}
		return ValueConverter.convertToObject(str, valueType);
	}
	
	//字符串转为值
	public static Object convertToObject(String str, ValueType valueType) throws Exception{
		if(str == null || str.isEmpty()) {
			return null;
		}
		else {
			try
			{
				switch(valueType) {
					case String:
						return str;  
					case Decimal:
						return Double.parseDouble(str);  
					case Boolean:
						if(str == null || str.length() == 0){
							return null;
						}
						else{
							str = str.toLowerCase();
							if( "y".equals(str) || "Y".equals(str) || "true".equals(str) || "是".equals(str)) {
								return true;
							}
							else if("n".equals(str) || "N".equals(str) || "false".equals(str) || "否".equals(str)){
								return false;
							}
							else {
								return null;
							}
						} 
					case Time:
						SimpleDateFormat sdf =   new SimpleDateFormat(SysConfig.getTimeFormat());
						return sdf.parse(str);
					case Date:
						SimpleDateFormat stf =   new SimpleDateFormat(SysConfig.getDateFormat());
						return stf.parse(str);
					default:
						throw new Exception("unkown type = " + valueType.toString());
				}
			}
			catch(Exception ex){
            	ex.printStackTrace();
				throw new Exception("convertToObject error. str = "+ str +", valueType = "+valueType.toString() + ". "+ ex.getMessage());
			}
		}
	}

	public static Object convertToDBObject(String str, ValueType valueType) throws RuntimeException{
		if(str == null || str.isEmpty()){
			return null;
		}
		else {
			try
			{
				switch(valueType) {
					case String:
						return str;  
					case Decimal:
						return Double.parseDouble(str);  
					case Boolean:
						if( "Y".equals(str) || "y".equals(str) || "true".equals(str)) {
							return "Y";
						}
						else if("N".equals(str) || "n".equals(str) || "false".equals(str)){
							return "N";
						}
						else{
							return "";
						} 
					case Time:
						SimpleDateFormat sdf =   new SimpleDateFormat(SysConfig.getTimeFormat());
						return sdf.parse(str);
					case Date:
						SimpleDateFormat stf =   new SimpleDateFormat(SysConfig.getDateFormat());
						return stf.parse(str);
					default:
						throw new RuntimeException("unkown type = " + valueType.toString());
				}
			}
			catch(Exception ex){
            	ex.printStackTrace();
				throw new RuntimeException("convertToObject error. str = "+ str +", valueType = "+valueType.toString() + ". "+ ex.getMessage());
			}
		}
	}
	
	public static Boolean convertToBoolean(String str, String trueValue, String falseValue) throws Exception{
		if(str == null || str.isEmpty()){
			return (Boolean) null;
		}
		else { 
			if(str.equals(trueValue)){
				return true;				
			}
			else if(str.equals(falseValue)){
				return false;
			}
			else{
				throw new Exception("数据转换为布尔类型失败, 值为'" + str + "'.");
			}
		}
	}
	
	public static BigDecimal convertToDecimal(String str, String format) throws Exception{
		if(str == null || str.isEmpty()){
			return (BigDecimal) null;
		}
		if(format == null){
			try{
				double value = Double.parseDouble(str);
				BigDecimal bdValue = BigDecimal.valueOf(value);
				return bdValue;
			}
			catch(Exception ex){
				throw new Exception("数据转换为数值类型失败, 值为'" + str + "'。", ex.getCause());
			}
		}
		else { 
			if(format.endsWith("%")){
				String newStr = str.replace("%", "");
				try{
					double value = Double.parseDouble(newStr) / 100;
					BigDecimal bdValue = BigDecimal.valueOf(value);
					return bdValue;
				}
				catch(Exception ex){
					throw new Exception("数据转换为数值类型失败, 值为'" + str + "'。", ex.getCause());
				}
			}
			else{
				try{
					double value = Double.parseDouble(str);
					BigDecimal bdValue = BigDecimal.valueOf(value);
					return bdValue;
				}
				catch(Exception ex){
					throw new Exception("数据转换为数值类型失败, 值为'" + str + "'。", ex.getCause());
				}
			}
		}
	}
	
	public static Date convertToTime(String str, String format) throws Exception{
		if(str == null || str.isEmpty()){
			return (Date) null;
		}
		else { 
			try{				
				SimpleDateFormat sdf =   new SimpleDateFormat(format == null || format.isEmpty() ? SysConfig.getTimeFormat() : format);
				Date date = sdf.parse(str);
				return date;
			}
			catch(Exception ex){
				throw new Exception("数据转换为时间类型失败, 值为'" + str + "'。", ex.getCause());
			}
		}
	}
	
	public static Date convertToDate(String str, String format) throws Exception{
		if(str == null || str.isEmpty()){
			return (Date) null;
		}
		else { 
			try{
				SimpleDateFormat sdf =   new SimpleDateFormat(format == null || format.isEmpty() ? SysConfig.getDateFormat() : format);
				Date date = sdf.parse(str);
				return date;
			}
			catch(Exception ex){
				throw new Exception("数据转换为日期类型失败, 值为'" + str + "'。", ex.getCause());
			}
		}
	}

	public static String arrayToString(String[] strs, String joinStr){
		StringBuilder ss = new StringBuilder();
		for(int i=0;i<strs.length;i++){
			ss.append((i == 0 ? "" : joinStr) + strs[i]);
		}
		return ss.toString();
	} 
	public static String arrayToString(List<String> strs, String joinStr){
		StringBuilder ss = new StringBuilder();
		for(int i=0;i<strs.size();i++){
			ss.append((i == 0 ? "" : joinStr) +strs.get(i));
		}
		return ss.toString();
	} 
	
	public static Integer DBBigDecimalToInteger(Object value){
		return  value == null ? null : ((BigDecimal)value).intValue();
	}
	
	public static ValueType getValueTypeByName(String valueTypeStr) throws Exception{
		String valueTypeStrLower =  valueTypeStr.toLowerCase();
		switch(valueTypeStrLower){
			case "string":
				return ValueType.String; 
			case "boolean":
				return ValueType.Boolean; 
			case "date":
				return ValueType.Date; 
			case "time":
				return ValueType.Time; 
			case "decimal":
				return ValueType.Decimal; 
			case "object": 
				return ValueType.Object; 
			default:
				throw new Exception("Unknown value type. ValueType = " + valueTypeStr);
		}
	}
}
