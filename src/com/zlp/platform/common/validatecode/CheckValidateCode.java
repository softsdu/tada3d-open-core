package com.zlp.platform.common.validatecode;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 该类是验证码的返回servlet类
 * 
 * 该类实现验证输入的验证码是否正确
 * 
 * @author  于采兴
 * @version Ver 1.0 2015-3-11 新建
 */
@SuppressWarnings("serial")
public class CheckValidateCode extends HttpServlet {
	
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 设置编码格式
        response.setContentType("text/html;charset=utf-8");
        HttpSession session = request.getSession();
        String validateC = (String) session.getAttribute("validateCode");
        String veryCode = request.getParameter("c");
        PrintWriter out = response.getWriter();
        
        // 如果条件成立，则说明验证错误
//        if(FiaConstants.FLAG_1.equals(FiaConstants.getTurnonStressTest())){//如果开启压力测试，则不验证验证码
//            out.print("true");
//        }
        
//        else 
    	if (validateC == null || "".equals(validateC) || veryCode == null || "".equals(veryCode)) {
            out.print("false");
        } else if (validateC.equalsIgnoreCase(veryCode)) {
            out.print("true");
        } else {
            out.print("false");
        }

        // 刷新缓存
        out.flush();
        out.close();
    }

}
