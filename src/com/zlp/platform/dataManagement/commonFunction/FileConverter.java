package com.zlp.platform.dataManagement.commonFunction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException; 
import java.io.InputStreamReader; 
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper; 
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document; 

public class FileConverter {	

	//将文献文件转换成txt文本
	public String sourceToTxtFile(String sourceDir, String sourceFilePath, boolean needRewrite, String exportDir, String fileType) throws Exception{
		String txtFilePath = sourceFilePath + ".txt";
        try {
			String txtFileFullPath = exportDir + txtFilePath;
			File txtFile = new File(txtFileFullPath); 
			boolean needGenerate = txtFile.exists() ? needRewrite : true;
			if(needGenerate){ 
				String sourceFileFullPath = sourceDir + sourceFilePath;
            	if(fileType.equals("pdf")){
            		this.pdf2TxtFile(sourceFileFullPath, txtFileFullPath);
            	}
            	else if(fileType.equals("html") || fileType.equals("xml")){
                	this.html2TxtFile(sourceFileFullPath, txtFileFullPath);
                } 
            	else if(fileType.equals("unknown")){
            		//未知类型
            		return null;
            	} 
            	else
            	{
        			throw new Exception("转换文件出错, 无法识别的文件类型. fileType = " + fileType);
            	}
			}
        }
        catch(Exception ex) 
        { 
			throw new Exception("转换文件出错. fileType = " + fileType, ex);
        }
		return txtFilePath;
	}
	
	//pdf转文本
	private void pdf2TxtFile(String sourceFileFullPath, String txtFileFullPath) throws Exception{ 
		File sourceFile = new File(sourceFileFullPath);   
		File txtFile = new File(txtFileFullPath);   
		PDDocument pdDoc = null;
        try {
            pdDoc = PDDocument.load(sourceFile);
            PDFTextStripper  pdfStripper = new PDFTextStripper();  
            /*
            Map<COSName, PDFont> allFonts = new  HashMap<COSName, PDFont>();
            PDPageTree pages = pdDoc.getDocumentCatalog().getPages(); 
            for(PDPage page : pages) {
            	PDResources resource = page.getResources();
            	Iterable<COSName> fontNames = resource.getFontNames(); 
            	for(COSName key : fontNames){
            		if(!allFonts.containsKey(key)){
            			allFonts.put(key, resource.getFont(key));
            		}
            	}
            } 
            */
            String content = pdfStripper.getText(pdDoc);
    		if(content == null || content.trim().isEmpty()){
    			throw new Exception("得到的文本文件为空.");
    		}
    		pdDoc.close();
            saveTxt(txtFile, content);
        }
        catch(Exception ex) 
        { 
			throw new Exception("转换pdf文件出错. pdfFileFullPath = " + sourceFileFullPath, ex);
        } 
        finally{
        	if(pdDoc != null){
        		pdDoc.close();
        	}
        }
	}
	
	//保存文本文件
	private void saveTxt(File txtFile, String content) throws Exception{
		BufferedWriter out = null;
		try{ 
			txtFile.createNewFile();
	        out = new BufferedWriter(new FileWriter(txtFile));  
	        out.write(content);  
	        out.flush();     
		}
		catch(IOException ex){
			throw new IOException("写入文本文件出错. txtFileFullPath = " + txtFile.getPath(), ex);
		}
		finally{
			if(out != null){
		        out.close(); 
			}
		}
	}

	//网页转文本
	private void html2TxtFile(String sourceFileFullPath, String txtFileFullPath) throws Exception{ 
		File txtFile = new File(txtFileFullPath);   
        try { 
    		File htmlFile = new File(sourceFileFullPath); 
        	Document doc = Jsoup.parse(htmlFile, "UTF-8");
        	String textInPage = doc.text();
    		if(textInPage == null || textInPage.trim().isEmpty()){
    			throw new Exception("得到的文本文件为空.");
    		}
    		saveTxt(txtFile, textInPage);
        }
        catch(Exception ex) 
        { 
			throw new Exception("转换html文件出错. htmlFileFullPath = " + sourceFileFullPath, ex);
        } 
	}
	
	//打开文本文件
    private static String openTxtFile(String fileFullPath, String encoding) throws IOException { 
    	BufferedReader bis = null;
        try {  
            bis = new BufferedReader(new InputStreamReader(new FileInputStream( new File(fileFullPath)), encoding) );  
            String szContent="";  
            String szTemp;  
              
            while ( (szTemp = bis.readLine()) != null) {  
                szContent+=szTemp+"\n";  
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
}
