package com.zlp.platform.common.validatecode;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CreateValidateCode extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	// 验证码图片的宽度。
    private int width = 80;

    // 验证码图片的高度。
    private int height = 20;

    // 验证码字符个数
    private int codeCount = 4;

    private int x = 0;
    
    // 字体高度
    private int fontHeight;

    private int codeY;

    char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P',
            'Q', 'R','S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', 
            '9', 'a', 'b', 'c','d', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x',
            'y', 'z' };

    public void init() throws ServletException {
        // 从web.xml中获取初始信息
        // 宽度
        String strWidth = this.getInitParameter("width");
        // 高度
        String strHeight = this.getInitParameter("height");
        // 字符个数
        String strCodeCount = this.getInitParameter("codeCount");

        // 将配置的信息转换成数值
        try {
            if (strWidth != null && !strWidth.isEmpty()) {
                width = Integer.parseInt(strWidth);
            }
            if (strHeight != null && !strHeight.isEmpty()) {
                height = Integer.parseInt(strHeight);
            }
            if (strCodeCount != null && !strCodeCount.isEmpty()) {
                codeCount = Integer.parseInt(strCodeCount);
            }
        } catch (NumberFormatException e) {
        	///logger.error("", e);
        }

        x = 10;
        fontHeight = height - 2;
        codeY = height - 4;
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 定义图像buffer
        BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffImg.createGraphics();
        // 创建一个随机数生成器类
        Random random = new Random();

        // 将图像填充为白色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 创建字体，字体的大小应该根据图片的高度来定。
        Font font = new Font("Utopia", Font.PLAIN, fontHeight);
        // 设置字体。
        g.setFont(font);

        // 画边框。
        g.setColor(Color.lightGray);
        g.drawRect(0, 0, width - 1, height - 1);

        StringBuilder randomCode = new StringBuilder();
        int red = 0, green = 0, blue = 0;
        String strRand = null;
        
        // 随机产生codeCount数字的验证码。
        for (int i = 0; i < codeCount; i++) {
            // 得到随机产生的验证码数字。
            strRand = String.valueOf(codeSequence[random.nextInt(52)]);
            // 产生随机的颜色分量来构造颜色值，这样输出的每位数字的颜色值都将不同。
            red = random.nextInt(255);
            green = random.nextInt(255);
            blue = random.nextInt(255);

            // 用随机产生的颜色将验证码绘制到图像中。
            g.setColor(new Color(red, green, blue));
            g.drawString(strRand, (i * 16) + x, codeY);

            // 将产生的四个随机数组合在一起。
            randomCode.append(strRand);
        }
        // 将四位数字的验证码保存到Session中。
        HttpSession session = req.getSession();        
        session.setAttribute("validateCode", randomCode.toString());

        // 禁止图像缓存。
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", 0);
        resp.setContentType("image/jpeg");

        // 将图像输出到Servlet输出流中。
        ServletOutputStream sos = resp.getOutputStream();
        ImageIO.write(buffImg, "jpeg", sos);
        sos.close();
    }
        
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
