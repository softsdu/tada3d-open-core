package com.zlp.platform.expression.expFunction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.zlp.constraintSolver.processor.Circle;
import com.zlp.constraintSolver.processor.CircleIntersect;
import com.zlp.constraintSolver.processor.Point2D;
import com.zlp.platform.common.ValueConverter;
 
public class ExpGeometry {
	//根据三条边长度，构造三角形
	public String triangle(BigDecimal param1, BigDecimal param2, BigDecimal param3) throws Exception {
		if(param3.doubleValue() - param2.doubleValue() >= param1.doubleValue()){
			throw new Exception("两边之和小于第三边");
		}
		else if(param3.doubleValue() + param2.doubleValue() <= param1.doubleValue()){
			throw new Exception("两边之和小于第三边");
		}
		else{
			Point2D point1 = new Point2D(0, 0);
			Point2D point2 = new Point2D(param1.doubleValue(), 0);
			Circle cA = new Circle(point1.getX(), point1.getY(), param2.doubleValue());
			Circle cB = new Circle(point2.getX(), point2.getY(), param3.doubleValue());
			CircleIntersect circleIntersect = new CircleIntersect(cA, cB);
			double[] values = circleIntersect.intersect();
			Point2D point3 = new Point2D(values[0], values[1]); 
			return this.convertToPath(point1, point2, point3);
		} 
	}  
	
	//直角三角形
	public String rightTriangle(BigDecimal param1, BigDecimal param2) throws Exception {
		Point2D point1 = new Point2D(0, 0);
		Point2D point2 = new Point2D(param1.doubleValue(), 0);
		Point2D point3 = new Point2D(0, param2.doubleValue()); 
		return this.convertToPath(point1, point2, point3);
	}  
	
	//等腰三角形	
	public String isoscelesTriangle(BigDecimal param1, BigDecimal param2) throws Exception {
		Point2D point1 = new Point2D(0, 0);
		Point2D point2 = new Point2D(param2.doubleValue(), 0);
		Point2D point3 = new Point2D(param2.doubleValue() / 2, param1.doubleValue()); 
		return this.convertToPath(point1, point2, point3);
	}
	
	private String convertToPath(Point2D point1, Point2D point2, Point2D point3){
		return point1.getX() + "," + point1.getY() + ";" + point2.getX() + "," + point2.getY() + ";" + point3.getX() + "," + point3.getY();
	}
	
	private String convertToPath(List<Point2D> points){
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < points.size(); i++){
			Point2D p = points.get(i);
			s.append((s.length() == 0 ? "" : ";") + p.getX() + "," + p.getY());
		}
		return s.toString();
	}
	
	//获取X坐标值 added by ls 202205
	public BigDecimal getX(String location) throws Exception{
		BigDecimal[] values = this.getXYZ(location);
		return values[0];
	}
	
	//获取Y坐标值 added by ls 202205
	public BigDecimal getY(String location) throws Exception{
		BigDecimal[] values = this.getXYZ(location);
		return values[1];
	}
	
	//获取Z坐标值 added by ls 202205
	public BigDecimal getZ(String location) throws Exception{
		BigDecimal[] values = this.getXYZ(location);
		return values[2];
	}

	//获取坐标值数值 added by ls 202205
	private BigDecimal[] getXYZ(String location) throws Exception{
		BigDecimal[] values = new BigDecimal[3];
		values[0] = BigDecimal.valueOf(0);
		values[1] = BigDecimal.valueOf(0);
		values[2] = BigDecimal.valueOf(0);
		if(location != null && location.trim().length() != 0){
			String[] parts = location.trim().split(","); 
			for(int i = 0; i < parts.length; i++){
				String part = parts[i].trim();
				if(!ValueConverter.CheckDecimal(part)){
					values[i] = BigDecimal.valueOf(0);
				}
				else{
					values[i] = ValueConverter.convertToDecimal(part, null);
				}
			} 
		}
		return values;
	}
	
	//线段长度 added by ls 20230615
	public BigDecimal getLineLength(String pointAStr, String pointBStr) throws Exception{
		BigDecimal[] pointA = this.getXYZ(pointAStr);
		BigDecimal[] pointB = this.getXYZ(pointBStr);
		double length = Math.sqrt((pointA[0].doubleValue() - pointB[0].doubleValue()) * (pointA[0].doubleValue() - pointB[0].doubleValue()) + (pointA[1].doubleValue() - pointB[1].doubleValue()) * (pointA[1].doubleValue() - pointB[1].doubleValue()) + (pointA[2].doubleValue() - pointB[2].doubleValue()) * (pointA[2].doubleValue() - pointB[2].doubleValue()));
		return BigDecimal.valueOf(length);
	}
	
	//获取点坐标 added by ls 20230615
	public List<BigDecimal[]> getXYZs(String location) throws Exception{
		String[] locs = location.trim().split(";"); 
		List<BigDecimal[]> points = new ArrayList<BigDecimal[]>();
		for(int i = 0; i < locs.length; i++){
			points.add(this.getXYZ(locs[i]));
		}
		return points;
	}
	
	//获取点坐标 added by ls 20230613
	private BigDecimal[] getPointArray(String[] locs, int index) throws Exception{
		 if(index >= locs.length){
			 throw new Exception("仅包含" + locs.length + "个点, 超出范围.");
		 }
		 else{
			 List<BigDecimal[]> points = new ArrayList<BigDecimal[]>();
			 for(int i = 0; i < locs.length; i++){
				 points.add(this.getXYZ(locs[i]));
			 }
			 return points.get(index);
		 }
	}
	public String getPoint(String location, BigDecimal index) throws Exception{
		 String[] locs = location.trim().split(";"); 
		 BigDecimal[] point = this.getPointArray(locs, index.intValue());
		 return point[0] + "," + point[1] + "," + point[2]; 
	}
	public String getFirstPoint(String location) throws Exception{
		 String[] locs = location.trim().split(";"); 
		 BigDecimal[] point = this.getPointArray(locs, 0);
		 return point[0] + "," + point[1] + "," + point[2]; 
	}
	public String getLastPoint(String location) throws Exception{
		 String[] locs = location.trim().split(";"); 
		 BigDecimal[] point = this.getPointArray(locs, locs.length - 1);
		 return point[0] + "," + point[1] + "," + point[2]; 
	}
}
