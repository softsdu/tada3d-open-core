package com.zlp.platform.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class HttpGetRequest {
    /**
     * Post Request
     * @return
     * @throws Exception
     */
    public static String doGet(String url, String parameterData) throws Exception { 
        
        URL localURL = new URL(url + (parameterData != null && parameterData.length() > 0 ? ("?" + parameterData) : ""));
        URLConnection connection = localURL.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection)connection;
        
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
        httpURLConnection.setRequestProperty("Content-Type", "text/xml"); 
        
        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;
        
        try { 
            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            } 
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            reader = new BufferedReader(inputStreamReader);
            
            while ((tempLine = reader.readLine()) != null) {
            	
                resultBuffer.append(tempLine);
            }
            
        }
        catch(Exception ex){
        	throw new Exception("调用" + url + "出错.", ex);
        }
        finally {
            
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
            
            if (outputStream != null) {
                outputStream.close();
            }
            
            if (reader != null) {
                reader.close();
            }
            
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            
            if (inputStream != null) {
                inputStream.close();
            }
            
        }

        return resultBuffer.toString();
    }

}
