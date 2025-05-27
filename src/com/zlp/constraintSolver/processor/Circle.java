package com.zlp.constraintSolver.processor;

import java.util.ArrayList;
import java.util.List;

public class Circle {
    private double x;
    private double y;
    private double r;
    public Circle(double X,double Y,double R){
        x = X;
        y = Y;
        r = R;
    }
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public double getR(){
        return r;
    } 

    //获取圆上的点 added by ls 20230606
    public static List<Point2D> getPoints(double radius, int sphereSegmentCount){
		double hPreAngle = Math.PI * 2 / sphereSegmentCount;		
		
		List<Point2D> allPoints = new ArrayList<Point2D>(); 
		
		//圆周上的点
		for(int j = 0; j < sphereSegmentCount; j++){
			double xValue = radius * Math.cos(j * hPreAngle);
			double zValue = radius * Math.sin(j * hPreAngle);
			Point2D point = new Point2D(xValue, zValue);
			allPoints.add(point);
		}
		return allPoints;
    }
}
