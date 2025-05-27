package com.zlp.platform.util;

import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @auther liangchuanxin
 * @create 2017/5/5
 */
public class OutPutUtil {

    public static void outString(String str) {
        HttpServletResponse response = ServletActionContext.getResponse();
        try {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("text/html;charset=utf-8");
            PrintWriter out = response.getWriter();
            out.write(str);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
}