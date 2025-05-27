package com.zlp.platform.util.QRcode;

import java.awt.Graphics;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.zxing.BarcodeFormat;

/*
 * 打印类:打印二维码和条形码类测试
 * liyh
 * 20181212
 * 
 */
public class PrintUtil implements Printable{
	
	private String billCode="";
	
	public PrintUtil (String billCode){
		this.billCode = billCode;
	}
	
	
   /** 
   * @param Graphic指明打印的图形环境 
   * @param PageFormat指明打印页格式（页面大小以点为计量单位，1点为1英才的1/72，1英寸为25.4毫米。A4纸大致为595×842点） 
   * @param pageIndex指明页号 
   **/  
   public int print(Graphics gra, PageFormat pf, int pageIndex) throws PrinterException {  
       
	try {
			
       System.out.println("pageIndex="+pageIndex);   
       Component c = null;  
       
      //print string    
      //String str = "YP20181212001";
      String billcode = billCode;
        
      Graphics2D g2 = (Graphics2D) gra;//转换成Graphics2D  
      g2.setColor(Color.black);//设置打印颜色为黑色  
  
      //打印起点坐标  
      double x = pf.getImageableX();  
      double y = pf.getImageableY();  
   
      switch(pageIndex){  
         case 0:{
        	 //设置打印字体（字体名称、样式和点大小）（字体名称可以是物理或者逻辑名称）  
             //Java平台所定义的五种字体系列：Serif、SansSerif、Monospaced、Dialog 和 DialogInput  
             //Font font = new Font("新宋体", Font.PLAIN, 9);
             Font font = new Font("新宋体", Font.BOLD, 6);  
             g2.setFont(font);//设置字体  
      
             float[] dash1 = {2.0f};   
    
             //设置打印线的属性。   
             //1.线宽 2、3、不知道，4、空白的宽度，5、虚线的宽度，6、偏移量  
             g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f, dash1, 0.0f));    
             float heigth = font.getSize2D();//字体高度   
             System.out.println("x="+x);  
             
             //BufferedImage bufImage3 = ZxingUtil.generateQrCodeImg("serverPrintTest",billcode, 20, 20,BarcodeFormat.DATA_MATRIX);//实际生成：14*14
             //BufferedImage bufImage3 = ZxingUtil.generateQrCodeImg("serverPrintTest",billcode, 160, 160,BarcodeFormat.QR_CODE);//RFID编号
             BufferedImage bufImage3 = ZxingUtil.generateQrCodeImg("serverPrintTest",billcode, 120, 120,BarcodeFormat.QR_CODE);//身份证号
             Image src3 = (Image)bufImage3;
             //g2.drawImage(src,(int)x,(int)y,c); //绘制二维码
             g2.drawImage(src3,(int)x,(int)y+5,c); //绘制二维码AAA
             int img_Height=src3.getHeight(c);  
             int img_width=src3.getWidth(c);
             System.out.println(" DATA_MATRIX ：img_Height="+img_Height+"img_width="+img_width) ;
             
             //打印二维码对应的文本测试
/*             g2.drawString("产品编号："+str, (float)x+img_width+15, (float)y+heigth);
             g2.drawString("打印日期：2018-12-13", (float)x+img_width+15, (float)y+heigth*2+5);*/
             g2.drawString("实际内容："+billcode, (float)x+img_width+18, (float)y+heigth+4);
             
             SimpleDateFormat stf =   new SimpleDateFormat("yyyyMMdd");
     		 String currentYYYYMMDD=stf.format(new Date());
     		
             g2.drawString("打印日期："+currentYYYYMMDD, (float)x+img_width+18, (float)y+heigth*2+3+4);             
             
 
      
             return PAGE_EXISTS; 
         }  
         default:{
        	 return NO_SUCH_PAGE;
         }  
      	}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return NO_SUCH_PAGE;  
	}
         
   }
    
   public static void main(String[] args) {  
	         
	    //通俗理解就是书、文档   
	    Book book = new Book();  
	  
	    //设置成竖打   
	    PageFormat pf = new PageFormat(); 
	  
	    //通过Paper设置页面的空白边距和可打印区域。必须与实际打印纸张大小相符。  
	    Paper p = new Paper();
//	     p.setSize(122,27);//纸张大小:伯杰标签纸
//	     p.setImageableArea(18,1,122,27);//伯杰标签纸  
	     pf.setPaper(p);  
	    
	    //把 PageFormat和 Printable添加到书中，组成一个页面    
	     //book.append(new PrintUtil("A00001"), pf);//A055//A0001-识别效果差；
	     //book.append(new PrintUtil("SD20190606123456"), pf);//bsp：RFID编号示例
	     book.append(new PrintUtil("370923198310020658"), pf);//bsp：身份证号示例
	    
	    
	    
	     //获取打印服务对象  
	     PrinterJob job = PrinterJob.getPrinterJob();    
	     
	
	     // 设置打印类   
	     job.setPageable(book);
	     	         
	     try {  
	         //可以用printDialog显示打印对话框，在用户确认后打印；也可以直接打印  
	         boolean a=job.printDialog();
	         if(a){       
	             job.print();   
	         }else{
	             job.cancel();
	         }
	     } catch (PrinterException e) {  
	         e.printStackTrace();  
	     }
	  
    }  
  
}