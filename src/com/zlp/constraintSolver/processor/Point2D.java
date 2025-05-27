package com.zlp.constraintSolver.processor;
 
public class Point2D {
	protected double x;
	protected double y;
	
	public Point2D(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	public Point2D(int x, int y){
		this.x = x;
		this.y = y;
	}

	public double getX(){
		return this.x;
	}

	public void setX(double x){
		this.x = x;
	}

	public double getY(){
		return this.y;
	}

	public void setY(double y){
		this.y = y;
	}
}
