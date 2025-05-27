package com.zlp.platform.common.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONObject;
import com.zlp.platform.common.NcpException;
import com.zlp.platform.common.ValueConverter;
import com.zlp.platform.dao.db.ValueType;

//一般通用方法
public class CommonFunction {
	//将正则表达式的特殊保留字符转义 added by ls 20170912
	public static String encodeRegExpString(String s){
		if(s.length() != 0){
			String newStr = "";
			for(int i = 0; i < s.length(); i++){
				switch(s.charAt(i)){
					case '*':
					case '.':
					case '?':
					case '+':
					case '$':
					case '^':
					case '[':
					case ']':
					case '(':
					case ')':
					case '{':
					case '}':
					case '|':
					case '\\':
						newStr += "\\"+s.charAt(i);
						break;
					default:
						newStr += s.charAt(i);
				}
			}
			return newStr;
		}
		else{
			return "";
		}
	}
	
	public static String getRepeatString(String str, int num){
		StringBuilder s = new StringBuilder();
		for(int i=0;i<num;i++){
			s.append(str);
		}
		return s.toString();
	}

	public static String encode(String str) throws UnsupportedEncodingException{
		if(str == null){
			return "";
		}
		else{
			String s = URLEncoder.encode(str, "utf-8");
			s =  s.replaceAll("\\+","%20");
			return s;
		}
	}
	public static String decode(String str) throws UnsupportedEncodingException{
		String s = str == null || str.length() == 0 ? "" : URLDecoder.decode(str, "utf-8"); 
		return s;		
	}
	
	public static String generateInnerStr(String sourceStr){
		if(sourceStr == null){
			return null;
		}
		else{
			return sourceStr.replaceAll("\\\\","\\\\\\\\").replaceAll("\"","\\\\\"");
		}
	}
	
	//字符串数组排序
	public static List<String> sort(Set<String> sourceStrs){
		List<String> newStrs = new ArrayList<String>();
		Object[] arr = sourceStrs.toArray();		
		for(int i=0;i<arr.length;i++){
			String ss = (String)arr[i];
			for(int j=0;j<=newStrs.size();j++){
				if(j == newStrs.size()){
					newStrs.add(ss);
					break;
				}
				else{
					String ns = newStrs.get(j);
					if(ns.compareTo(ss) >= 0){ 
						newStrs.add(j, ss);
						break;
					}
				}
			}			
		}
		return newStrs;
	} 
	
	public static ValueType getValueType(String valueTypeName){
		if(valueTypeName == null || valueTypeName.length() == 0){
			return null;
		}
		else{
			String vtn = valueTypeName.toLowerCase();
			if(vtn.equals("string")){
				return ValueType.String;
			}
			else if(vtn.equals("decimal")){
				return ValueType.Decimal;
			}
			else if(vtn.equals("date")){
				return ValueType.Date;
			}
			else if(vtn.equals("time")){
				return ValueType.Time;
			}
			else if(vtn.equals("boolean")){
				return ValueType.Boolean;
			}
			else if(vtn.equals("object")){
				return ValueType.Object;
			}
			else{
				throw new RuntimeException("None value type named " + valueTypeName);
			}
		}
	}
	
	public static Object getValueFromJson(JSONObject jsonObj, String propertyName, ValueType valueType, boolean isEncoded, boolean errorNoneProperty) throws Exception{
		if(jsonObj.containsKey(propertyName)){
			String pValueStr = jsonObj.getString(propertyName);
			if(valueType == ValueType.String){
				if(isEncoded){
					pValueStr = CommonFunction.decode(pValueStr);
				} 
				return pValueStr; 
			}
			else{
				Object pValue = ValueConverter.convertToObject(pValueStr, valueType);
				return pValue;
			}
		}
		else{
			if(errorNoneProperty){
				throw new Exception("None property. PropertyName = " + propertyName);
			}
			else{
				return null;
			}
		}
	}

	public static String listToString(List<String> listStrings, String splitString){
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i < listStrings.size();i++){
			String str = listStrings.get(i);
			sb.append((i == 0 ? "" : splitString) + str);
		}
		return sb.toString();
	}

	public static String listToString(String[] listStrings, String splitString){
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i < listStrings.length;i++){
			String str = listStrings[i];
			sb.append((i == 0 ? "" : splitString) + str);
		}
		return sb.toString();
	}
	

	//added by liyh 20181128
    public static boolean checkIsEmail(String email){
        boolean flag = false;
        try{
                String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                Pattern regex = Pattern.compile(check);
                Matcher matcher = regex.matcher(email);
                flag = matcher.matches();
            }catch(Exception e){
                flag = false;
            }
        return flag;
    }

    public static List<List<String>> splitGroups(List<String> allStrings, int oneGroupCount){
    	List<List<String>> groupStrings = new ArrayList<List<String>>();
    	if(allStrings.size() != 0){ 
	    	List<String> oneGroup = null;
	    	for(int i = 0; i < allStrings.size(); i++){
	    		if( i % oneGroupCount == 0){
	    			if(oneGroup != null){
	    				groupStrings.add(oneGroup);
	    			}
	    			oneGroup = new ArrayList<String>();
	    		}
	    		oneGroup.add(allStrings.get(i));
	    	}
			groupStrings.add(oneGroup);
    	}
    	return groupStrings;
    }    
	
	//获取指定长度的随机码  added by liyh 20181213
	public static String getRandomString(int length){
		
		String randomCharSource = "abcdefghijklmnopqrstuvwxyz123456789";
		
		StringBuilder ss= new StringBuilder();
		int sourceLength = randomCharSource.length();
        Random random = new Random();
        for(int i = 0; i < length; i++){
            int rInt = random.nextInt(sourceLength);
            ss.append(randomCharSource.charAt(rInt));
        }
		return ss.toString();
	}

	public static String getMd5(String inputText) throws NoSuchAlgorithmException{
		byte[] secretBytes = MessageDigest.getInstance("md5").digest(inputText.getBytes());
		String md5Code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5Code.length(); i++) {
            md5Code = "0" + md5Code;
        }
        return md5Code;
	}
	
	//替换文件名里的特殊字符 added by ls 20220623
	public static String processSpecialCharInFileName(String name, String toChar){ 
        Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
        Matcher matcher = pattern.matcher(name);
        String newName = matcher.replaceAll(toChar);
        return newName;
	}

	/**
	 *   密码长度 8-16位；密码含有数字和字母； 抛出异常
	 * */
	public static boolean checkPassword(String password,String userCode) throws NcpException {
		if(password.toLowerCase().contains(userCode.toLowerCase())){
			throw new NcpException("passwordCheck","密码不允许包含用户名!");
		}

		boolean validity=false;
		int maxLength = 20;
		int minLength = 6;
		if(password.length()>maxLength){
			throw new NcpException("passwordCheck","密码长度超过"+maxLength +"位！");
		}

		boolean lengthLimit=false;
		if(password.length()>=minLength)lengthLimit=true;
		boolean containLetters=false;
		boolean containsNumbers=false;
		boolean containsSpecialCharactersrs=false;//特殊字符

		for (int i = 0; i < password.length(); i++) {
			char c = password.charAt(i);
			if((c>='a'&&c<='z')||(c>='A'&&c<='Z')){
				containLetters=true;
			}
			if(c>='0'&&c<='9'){
				containsNumbers=true;
			}
			if ((c >= 33 && c <= 47) ||(c >= 58 && c <= 64) ||(c >= 91 && c <= 96) ||(c >= 123 && c <= 126)) {
				containsSpecialCharactersrs = true;
			}
		}

		//合法校验
		if(lengthLimit&&containLetters&&containsNumbers&&containsSpecialCharactersrs){
			validity=true;
		}else {
			throw new NcpException("passwordCheck","密码长度不得少于"+minLength+"位，必须包含字母、数字和特殊符号！");
		}
		return validity;
	}
}
