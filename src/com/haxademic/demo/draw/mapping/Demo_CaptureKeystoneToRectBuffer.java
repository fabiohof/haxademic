package com.haxademic.demo.draw.mapping;

import com.haxademic.core.app.PAppletHax;
import com.haxademic.core.constants.AppSettings;
import com.haxademic.core.draw.image.ImageUtil;
import com.haxademic.core.draw.mapping.CaptureKeystoneToRectBuffer;
import com.haxademic.core.file.FileUtil;

import processing.core.PGraphics;
import processing.core.PImage;

public class Demo_CaptureKeystoneToRectBuffer
extends PAppletHax {
	public static void main(String args[]) { PAppletHax.main(Thread.currentThread().getStackTrace()[1].getClassName()); }

	protected PImage sourceTexture;
	protected PGraphics sourceBuffer;
	protected CaptureKeystoneToRectBuffer mappedCapture;
	protected boolean debug = true;

	protected void overridePropsFile() {
		p.appConfig.setProperty( AppSettings.WIDTH, 1280 );
		p.appConfig.setProperty( AppSettings.HEIGHT, 720 );
		p.appConfig.setProperty(AppSettings.WEBCAM_INDEX, 6 );
	}

	protected void setupFirstFrame() {
		// load source image
		sourceTexture = p.loadImage(FileUtil.getFile("images/_sketch/computers/macportable.jpg"));
		sourceBuffer = ImageUtil.imageToGraphics(sourceTexture);
		mappedCapture = new CaptureKeystoneToRectBuffer(sourceBuffer, 450, 200, "text/keystoning/capture-map-demo.txt");
	}
	
	public void keyPressed() {
		super.keyPressed();
		if(p.key == 'd') debug = !debug;
		if(p.key == 'r') mappedCapture.resetCorners();
	}

	public void drawApp() {
		p.background(0);
		
		// update mapped source to buffer
		mappedCapture.update(debug);
		
		// draw debug view
		if(p.webCamWrapper != null) sourceTexture = p.webCamWrapper.getImage();	// draw webcam if exists
		ImageUtil.cropFillCopyImage(sourceTexture, sourceBuffer, true);
		if(debug == true) {
			mappedCapture.drawDebug(sourceBuffer);
		}
		
		// draw mapped result
		p.image(sourceBuffer, 0, 0);
		p.image(mappedCapture.mappedBuffer(), 0, sourceBuffer.height);
	}

}
