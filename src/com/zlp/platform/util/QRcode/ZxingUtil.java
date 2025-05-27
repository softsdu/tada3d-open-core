package com.zlp.platform.util.QRcode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.zlp.platform.dao.sys.ContextUtil;

/*
 * zxing生成条形码、二维码工具类
 * liyh 
 * 20181212
 * 
 */
public class ZxingUtil {

	private static final int BLACK = 0xFF000000;
	private static final int WHITE = 0xFFFFFFFF;

	private static BufferedImage toBufferedImage(BitMatrix matrix) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
			}
		}
		return image;
	}

	/**
	 * 输出文件
	 * 
	 * @param matrix
	 * @param format
	 * @param file
	 * @throws IOException
	 * @see [类、类#方法、类#成员]
	 */
	private static void writeToFile(BitMatrix matrix, String format, File file) throws IOException {
		BufferedImage image = toBufferedImage(matrix);
		if (!ImageIO.write(image, format, file)) {
			throw new IOException("Could not write an image of format " + format + " to " + file);
		}
	}

	/**
	 * 输入流
	 * 
	 * @param matrix
	 * @param format
	 * @param stream
	 * @throws IOException
	 * @see [类、类#方法、类#成员]
	 */
	private static void writeToStream(BitMatrix matrix, String format, OutputStream stream) throws IOException {
		BufferedImage image = toBufferedImage(matrix);
		if (!ImageIO.write(image, format, stream)) {
			throw new IOException("Could not write an image of format " + format);
		}
	}

	/**
	 * 
	 * 根据内容生成二维码并输出阿里云
	 * 
	 * @param fileName
	 *            需要带后缀名
	 * @param content
	 * @param width
	 * @param height
	 * @throws Exception
	 * @see [类、类#方法、类#成员]
	 */
	private static String generateCode(String fileName, String content, int width, int height) throws Exception {
		return generateCodeImg(fileName, content, width, height, BarcodeFormat.QR_CODE,"");
		//return generateCodeImg(fileName, content, width, height, BarcodeFormat.AZTEC, OssUtil.DISK_QRCODE);
	}

	private static String generateBarCode(String fileName, String content, int width,int height) throws Exception {
		//return generateCodeImg(fileName, content, height * 2, height, BarcodeFormat.CODABAR, OssUtil.DISK_BAR_CODE);
		return generateCodeImg(fileName, content, width, height, BarcodeFormat.CODE_93, "");
	}

	public static String generateCodeImg(String fileName, String content, int width, int height, BarcodeFormat fmt,
			String path) throws Exception {
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Integer ind = fileName.lastIndexOf(".");
		if (ind < 0) {
			throw new Exception("文件名称必须包含后缀名");
		}
		String suffix = fileName.substring(ind + 1);
		Map<EncodeHintType,String> hints = new HashMap<EncodeHintType,String>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		
		
		//BitMatrix bitMatrix = multiFormatWriter.encode(content, fmt, width, height, hints);
		BitMatrix bitMatrix = multiFormatWriter.encode(content, fmt, width, height);
		BufferedImage image = toBufferedImage(bitMatrix);
		
		File outputfile = new File(fileName);
        ImageIO.write(image, suffix, outputfile);
        return outputfile.getAbsolutePath();
	}

	public static void main(String[] args) {
		try {
			//String path = ZxingUtil.generateCode("meeting_333.png", "aaa", 120, 120);
			String path = ZxingUtil.generateBarCode("test_barcode_20181213.png", "20181213", 70, 24);
			//String path2 = ZxingUtil.generateCode("test_qrcode_20181213.png", "20181213", 48, 48);
			//String path2 = ZxingUtil.generateCode("test_qrcode_20181213.png", "20181213", 16, 16);
			//String path2 = ZxingUtil.generateCode("test_qrcode_20181213.png", "20181213", 24, 24);
			//String path2 = ZxingUtil.generateCode("test_qrcode_20181213.png", "A", 24, 24);
			String path2 = ZxingUtil.generateCode("test_qrcode_20181213.png", "A021", 16, 16);
			//String path2 = ZxingUtil.generateCode("test_qrcode_20181213.png", "20181213", 32, 32);
			System.out.println(path);
			System.out.println(path2);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static BufferedImage generateBarCodeImg(String content, int width, int height) throws Exception {
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Map<EncodeHintType,String> hints = new HashMap<EncodeHintType,String>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		//BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.CODE_93, width, height, hints);
		//BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.CODE_93, width, height, hints);
		BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.CODE_128, width, height, hints);
		BufferedImage image = toBufferedImage(bitMatrix);
		
		return image;
	}
	
	public static BufferedImage generateQrCodeImg(String content, int width, int height) throws Exception {
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Map<EncodeHintType,String> hints = new HashMap<EncodeHintType,String>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		//BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.CODE_93, width, height, hints);
		//BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.CODE_93, width, height, hints);
		BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
		//BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.AZTEC, width, height, hints);//最小15*15，识别度不高；
		
		BufferedImage image = toBufferedImage(bitMatrix);
		
		return image;
	}
	
	public static BufferedImage generateQrCodeImg(String parentDicName, String content, int width, int height,BarcodeFormat format) throws Exception {
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Map<EncodeHintType,String> hints = new HashMap<EncodeHintType,String>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		//BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.CODE_93, width, height, hints);
		//BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.CODE_93, width, height, hints);
		//BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
		//BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.AZTEC, width, height, hints);//最小15*15，识别度不高；
		
		
		//BitMatrix bitMatrix = multiFormatWriter.encode(content, format, width, height, hints);
		BitMatrix bitMatrix = multiFormatWriter.encode(content, format, width, height,hints);
		
		BufferedImage image = toBufferedImage(bitMatrix);
		
		
		//String fileName = ContextUtil.getAbsolutePath() + "\\uploadFiles\\" + parentDicName+"\\"+content+".jpg";
		//String fileName="E:\\work\\code\\Lims\\WebContent\\uploadFiles\\Image.jpg";
		String fileName="E:\\QRcode\\Image.jpg";
		
		File file = new File(fileName);  
		if(!file.exists()){  
		    file.mkdirs();  
		}

		
		File outputfile = new File(fileName);
        ImageIO.write(image, "jpg", outputfile);
		
		return image;
	}
	
}
