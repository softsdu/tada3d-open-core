package com.zlp.platform.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 文件解压工具
 */
public class ZipUtils {
   private final static Log logger = LogFactory.getLog( ZipUtils.class ) ;
   
   /**
    * 解压缩,注意:文件名不能包含中文.
    * @param in 
    * @param outputDirectory
    * @throws Exception
    */
   public static void unzip(final ZipInputStream in, String outputDirectory)throws Exception{
	   if ( in == null ){//输入流为空
	     return;
       }
   if ( outputDirectory == null ){
	   final String MSG = "输出目录为空null,无法解压";
	   logger.debug(MSG);
	   return;
   }
	   
	   File dirFile = new File( outputDirectory );
	   if ( dirFile.exists() == false ){
	      dirFile.mkdirs();
	   }
	   
		ZipEntry z;
		while ((z=in.getNextEntry() )!= null)
		{
			if (z.isDirectory())
			{
				String name=z.getName();
				name=name.substring(0,name.length()-1);
				final String dir = outputDirectory+File.separator+name; 
				File f=new File(dir);
				if ( f.exists() == false)
				if ( f.mkdir() == false ){
					final String MSG = "创建目录失败!"  + dir;
					logger.error(MSG);
					throw new Exception(MSG);
				}
			}
			else{
				final String file = outputDirectory+File.separator+z.getName(); 
				File f=new File(file);
				if ( f.exists() == true ){
 				  f.delete();
				}
				FileOutputStream fos=new FileOutputStream(f);
				try{
				final int cache = 2048;
		   	     byte[] b = new byte[cache];
			   	 int aa = 0;
					while ((aa = in.read(b)) != -1) {
					    fos.write(b, 0, aa);
				   }
				}finally{
					fos.close();
				}
			}
		}
	}


   public static void main(String[] args)throws Exception{
//	    ZipFile a;
//	   try {
//		   ZipInputStream zis = new ZipInputStream(
//				     new FileInputStream("i:/xtree.zip"));
//
//		   ZipUtils.unzip(zis, "i:\\x");
//		   zis.close();
//		  } catch (Exception e) {
//		   e.printStackTrace();
//		  }
       //boolean isok = false;
       //String xString = "ma[or]1.jsp";
       //String mString = "/war/ma/upload/1.jsp";


       //System.out.println("-----"+xString.substring(0, xString.indexOf("[or]")));
       //System.out.println("-----"+xString.substring(xString.indexOf("[or]")+4, xString.length()));
       //System.out.println("--------"+"sdfsdf/sdfsdf.jsp".indexOf("3"));

   }
}
