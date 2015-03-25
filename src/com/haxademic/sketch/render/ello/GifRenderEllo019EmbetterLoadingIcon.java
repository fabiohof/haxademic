package com.haxademic.sketch.render.ello;

import processing.core.PGraphics;
import processing.core.PImage;

import com.haxademic.core.app.P;
import com.haxademic.core.app.PAppletHax;
import com.haxademic.core.draw.util.OpenGLUtil;
import com.haxademic.core.image.AnimatedGifEncoder;
import com.haxademic.core.math.easing.Penner;
import com.haxademic.core.render.GifRenderer;
import com.haxademic.core.system.FileUtil;

@SuppressWarnings("serial")
public class GifRenderEllo019EmbetterLoadingIcon
extends PAppletHax{
	
	AnimatedGifEncoder encoder;
	PImage _icon;
	PGraphics _pg;
	float _frames = 30;
	boolean rendering = false;
	
	protected void overridePropsFile() {
		_appConfig.setProperty( "width", "100" );
		_appConfig.setProperty( "height", "110" );
	}
	
	public void setup() {
		super.setup();
		p.smooth(OpenGLUtil.SMOOTH_HIGH);
		_icon = p.loadImage(FileUtil.getHaxademicDataPath()+"images/play-arrow.png");
		
		// special rendering situation since applet won't go as small as 110
		_pg = p.createGraphics(46, 50, P.OPENGL);
		if(rendering == true) {
			_gifRenderer = new GifRenderer(40, 15);
			_gifRenderer.startGifRender(this);
		}
	}

	public void drawApp() {
		p.background(0);
		p.noStroke();
		
		_pg.beginDraw();
		_pg.clear();
		
		float percentComplete = ((float)(p.frameCount%_frames)/_frames);
		float easedPercent = Penner.easeInOutSine(percentComplete, 0, 1, 1);

		// draw play icon
		_pg.image(_icon, 0, 0, _pg.width, _pg.height);
		
		// drawmask
		_pg.fill(0);
		_pg.noStroke();
		if(easedPercent < 0.5f) {
			_pg.rect(0, 0, _pg.width * easedPercent * 2f, _pg.height);
		} else {
			_pg.rect(_pg.width * easedPercent * 2f - _pg.width, 0, _pg.width, _pg.height);
		}
		
		_pg.endDraw();
		p.image(_pg, 0, 0);
			
		// render
		if(rendering == true) {
			if(p.frameCount <= _frames) _gifRenderer.renderGifFrame(_pg);
			if(_frames == p.frameCount) {
				P.println("should finish: ", p.frameCount);
				_gifRenderer.finish();
			}
		}
	}
}



