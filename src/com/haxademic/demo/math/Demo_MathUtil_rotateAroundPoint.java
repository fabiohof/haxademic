package com.haxademic.demo.math;

import com.haxademic.core.app.PAppletHax;
import com.haxademic.core.draw.context.PG;
import com.haxademic.core.math.MathUtil;

import processing.core.PVector;

public class Demo_MathUtil_rotateAroundPoint 
extends PAppletHax {
	public static void main(String args[]) { PAppletHax.main(Thread.currentThread().getStackTrace()[1].getClassName()); }
	
	protected PVector mouse = new PVector();
	protected PVector otherPoint = new PVector();
	
	public void setupFirstFrame() {
		mouse.set(p.width/3, p.height/3);
		otherPoint.set(p.width/2, p.height/2);
	}
	
	public void drawApp() {
		p.background(0);
		PG.setDrawCenter(p);
		
		// update points
		mouse.set(p.mouseX, p.mouseY);
		MathUtil.rotateAroundPoint(otherPoint, mouse, 0.01f);
				
		// draw points
		p.stroke(255);
		p.fill(255, 0, 0);
		p.circle(mouse.x, mouse.y, 20);
		
		p.fill(0, 255, 0);
		p.circle(otherPoint.x, otherPoint.y, 20);
		
		p.text("otherPoint: "+otherPoint.x+", "+otherPoint.y, 10, 10);
		p.text("mouse: "+mouse.x+", "+mouse.y, 10, 30);
	}
}
