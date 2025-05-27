package com.zlp.platform.dataManagement.commonFunction;

import java.io.BufferedReader; 
import java.io.File;
import java.io.FileInputStream; 
import java.io.IOException; 
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern; 

public class TextProcessor { 
	//读取文本文件
    public String readTxtFile(String fileFullPath) throws IOException { 
    	BufferedReader bis = null;
        try {  
            bis = new BufferedReader(new InputStreamReader(new FileInputStream( new File(fileFullPath))));  
            String szContent="";  
            String szTemp;  
              
            while ( (szTemp = bis.readLine()) != null) {  
                szContent+=szTemp+"\r\n";  
            }  
            return szContent;  
        }  
        catch(IOException e ) {  
            throw new IOException("读取文件出错, fileFullPath");  
        }  
        finally{
        	if(bis != null){
                bis.close();         		
        	} 
        }
    }
    
    public List<String> splitByRegex(String txt, String regTxt){    	
    	Pattern p = Pattern.compile(regTxt);
        Matcher m = p.matcher(txt);
        int start = 0;
        int lastEnd = 0;
        List<String> strList = new ArrayList<String>();
        while(m.find()) {
		  start = m.start();
		  String str = txt.substring(lastEnd, start);
		  lastEnd = m.end(); 
		  strList.add(str);
        }
        if(txt.length() > lastEnd){
  		  String str = txt.substring(lastEnd, txt.length());
		  strList.add(str);
        }
        return strList;
    }
    
    public List<String> splitAndReplaceByRegex(String txt, String splitRegTxt, String replaceRegTxt, String replacement){    	
    	Pattern p = Pattern.compile(splitRegTxt);
        Matcher m = p.matcher(txt);
        int start = 0;
        int lastEnd = 0;
        List<String> strList = new ArrayList<String>();
        while(m.find()) {
		  start = m.start();
		  String str = txt.substring(lastEnd, start).replaceAll(replaceRegTxt, replacement);
		  lastEnd = m.end(); 
		  strList.add(str);
        }
        if(txt.length() > lastEnd){
  		  String str = txt.substring(lastEnd, txt.length()).replaceAll(replaceRegTxt, replacement);
		  strList.add(str);
        }
        return strList;
    } 
}
