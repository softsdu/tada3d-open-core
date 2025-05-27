package com.zlp.platform.expression.expFunction;

import java.math.BigDecimal; 
import java.util.ArrayList; 
import java.util.List; 
import com.zlp.constraintSolver.processor.Point2D; 
 
public class ExpModel {

	public String buildStairway(BigDecimal height, BigDecimal width, BigDecimal stepCount) {
		List<Point2D> allPoints = new ArrayList<Point2D>();
		int stepCountInt = (int)Math.ceil(stepCount.doubleValue());
		double stepHeight = height.doubleValue() / stepCountInt;
		double stepWidth = width.doubleValue() / stepCountInt;

		allPoints.add(new Point2D(stepWidth / 2, 0)); 
		allPoints.add(new Point2D(0, 0)); 

		for(int i = 0; i < stepCountInt; i++){
			Point2D ponitA = new Point2D(i * stepWidth, (i + 1) * stepHeight + stepHeight /2);
			allPoints.add(ponitA);
			Point2D ponitB = new Point2D((i + 1) * stepWidth, (i + 1) * stepHeight + stepHeight /2);
			allPoints.add(ponitB);
		}
 
		allPoints.add(new Point2D(stepCountInt * stepWidth, (stepCountInt - 1) * stepHeight + stepHeight /2));
		  
		return this.getPointsStr(allPoints);
	}  
	public String buildHandrail(BigDecimal height, BigDecimal width, BigDecimal handWidth, String railPartType){
		List<Point2D> allPoints = new ArrayList<Point2D>(); 
		double heightDouble = height.doubleValue(); 
		double widthDouble = width.doubleValue(); 
		double handWithDouble = handWidth.doubleValue();  

		switch(railPartType){
			case "middle":{ 
				allPoints.add(new Point2D(0, 0)); 
				allPoints.add(new Point2D(0, handWithDouble));  
				allPoints.add(new Point2D(widthDouble, heightDouble + handWithDouble)); 
				allPoints.add(new Point2D(widthDouble, heightDouble)); 
				break;
			}
			case "bottom":
			default:{
				allPoints.add(new Point2D(0, 0)); 
				allPoints.add(new Point2D(0, -handWithDouble)); 
				allPoints.add(new Point2D(-handWithDouble, -handWithDouble)); 
				allPoints.add(new Point2D(-handWithDouble, (widthDouble * 2* handWithDouble / heightDouble - handWithDouble) * heightDouble / widthDouble - handWithDouble));
				allPoints.add(new Point2D(widthDouble, heightDouble + handWithDouble)); 
				allPoints.add(new Point2D(widthDouble, heightDouble)); 
				break;
			} 
		}  
		return this.getPointsStr(allPoints);
	}
	public String buildRightTriangle(BigDecimal edgeLength1, BigDecimal edgeLength2){
		List<Point2D> allPoints = new ArrayList<Point2D>(); 
		double edgeLength1Double = edgeLength1.doubleValue(); 
		double edgeLength2Double = edgeLength2.doubleValue();   
		allPoints.add(new Point2D(0, 0)); 
		allPoints.add(new Point2D(0, edgeLength1Double)); 
		allPoints.add(new Point2D(edgeLength2Double, 0));  
		return this.getPointsStr(allPoints);
	}
	
	private String getPointsStr(List<Point2D> allPoints){
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < allPoints.size(); i++){
			Point2D p = allPoints.get(i);
			s.append(p.getX() + "," + p.getY() + ";");
		}
		return s.toString();
	}
}
