package com.zlp.mdl.processor;

public interface IPictureProcessor {

	byte[] getImageData(String imgFileName, double width, double height, double x1, double x2, double y1, double y2)
			throws Exception;

}
