package com.haxademic.sketch.shader;

import com.haxademic.core.app.P;
import com.haxademic.core.app.PAppletHax;
import com.haxademic.core.constants.AppSettings;
import com.haxademic.core.draw.context.DrawUtil;
import com.haxademic.core.draw.filters.shaders.BadTVLinesFilter;
import com.haxademic.core.draw.filters.shaders.ColorDistortionFilter;
import com.haxademic.core.draw.filters.shaders.InvertFilter;
import com.haxademic.core.draw.filters.shaders.SphereDistortionFilter;
import com.haxademic.core.draw.filters.shaders.VignetteAltFilter;
import com.haxademic.core.draw.filters.shaders.VignetteFilter;
import com.haxademic.core.draw.filters.shaders.WobbleFilter;
import com.haxademic.core.file.FileUtil;
import com.haxademic.core.math.easing.Penner;

import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.opengl.PShader;

public class ShaderVertexTextureDeform2
extends PAppletHax {
	public static void main(String args[]) { PAppletHax.main(Thread.currentThread().getStackTrace()[1].getClassName()); }


	PGraphics texture;
	PShape mesh;
	float angle;
	PShader textureShader;
	PShader displacementShader;
	float _frames = 610;
	float displaceAmp = 200f; 


	protected void overridePropsFile() {
		p.appConfig.setProperty( AppSettings.WIDTH, 1600 );
		p.appConfig.setProperty( AppSettings.HEIGHT, 1000 );
		p.appConfig.setProperty( AppSettings.SMOOTHING, AppSettings.SMOOTH_HIGH );
		p.appConfig.setProperty( AppSettings.RENDERING_MOVIE, false );
		p.appConfig.setProperty( AppSettings.RENDERING_MOVIE_STOP_FRAME, (int)_frames );
		p.appConfig.setProperty( AppSettings.RENDERING_GIF, false );
		p.appConfig.setProperty( AppSettings.RENDERING_GIF_FRAMERATE, 40 );
		p.appConfig.setProperty( AppSettings.RENDERING_GIF_QUALITY, 15 );
		p.appConfig.setProperty( AppSettings.RENDERING_GIF_START_FRAME, 1 );
		p.appConfig.setProperty( AppSettings.RENDERING_GIF_STOP_FRAME, (int)_frames );

	}

	public void setup() {
		super.setup();	

		texture = createGraphics(p.height * 2, p.height * 2, P.P3D);
		
		textureShader = P.p.loadShader( FileUtil.getFile("shaders/textures/bw-clouds.glsl")); 
		textureShader.set("time", 0 );

		mesh = createSheet(450, texture);
		displacementShader = loadShader(
			FileUtil.getFile("shaders/vertex/brightness-displace-frag-texture.glsl"), 
			FileUtil.getFile("shaders/vertex/brightness-displace-sheet-vert.glsl")
		);
		displacementShader.set("displacementMap", texture);
		displacementShader.set("displaceStrength", displaceAmp);
	}

	public void drawApp() {
		background(0);
		
//		OpenGLUtil.setWireframe(p.g, true);
		
		// rendering
		float percentComplete = ((float)(p.frameCount%_frames)/_frames);
		float easedPercent = Penner.easeInOutCubic(percentComplete, 0, 1, 1);
		float easedPercentHard = Penner.easeInOutQuad(percentComplete, 0, 1, 1);

		angle = P.TWO_PI * percentComplete;

		// set center screen & rotate
		translate(width/2, height*0.4f, -width/3);
		scale(1.4f);
//		rotateX(P.PI * p.mouseY * 0.01f); 
		rotateX(1.09f); 
//		rotateZ(percentComplete * P.TWO_PI); 
		rotateZ(-P.PI/4f); 

		// update generative texture
		textureShader.set("time", 5+ 7f * percentComplete);
		texture.filter(textureShader);
		WobbleFilter.instance(p).setTime( P.sin( P.TWO_PI * percentComplete));
		WobbleFilter.instance(p).setStrength(0.02f * P.sin( P.TWO_PI * percentComplete));
		WobbleFilter.instance(p).setSpeed(2.01f * P.sin( P.TWO_PI * percentComplete));
		WobbleFilter.instance(p).applyTo(texture);
		
		texture.beginDraw();
		postProcessForRendering();
		texture.endDraw();

		// set shader properties & set on processing context
//		displacementShader.set("displaceStrength", displaceAmp + displaceAmp * P.sin(percentComplete * P.TWO_PI));
		displacementShader.set("displaceStrength", displaceAmp);
		shader(displacementShader);  
		shape(mesh);
		
		// unset shader deformation
		resetShader();
		
//		BadTVLinesFilter.instance(p).setTime(percentComplete * 3f);
//		BadTVLinesFilter.instance(p).applyTo(p.g);
		SphereDistortionFilter.instance(p).applyTo(p.g);
		
		InvertFilter.instance(p).applyTo(p.g);
	}

	protected void postProcessForRendering() {
		// overlay
		int transitionIn = 220;
		int transition = 220;
		DrawUtil.setDrawCorner(texture);
		int curFrame = (int) (p.frameCount % _frames);
		if(curFrame <= transitionIn) {
//			P.println(P.map(curFrame, 1f, transitionIn, 18f, 8f));
//			VignetteFilter.instance(p).setDarkness(P.map(curFrame, 1f, transitionIn, 18f, 8f));
//			VignetteFilter.instance(p).setSpread(P.map(curFrame, 1f, transitionIn, 0.05f, 0.4f));
//			VignetteFilter.instance(p).applyTo(texture);
			texture.fill(0, P.map(curFrame, 1f, transition, 255f, 0));
			texture.rect(0,0,texture.width, texture.height);
		} else if(curFrame >= _frames - transition) {
//			VignetteFilter.instance(p).setDarkness(P.map(curFrame, _frames - transition, _frames, 2f, 18f));
//			VignetteFilter.instance(p).setSpread(P.map(curFrame, _frames - transition, _frames, 0.4f, 0.05f));
//			VignetteFilter.instance(p).applyTo(texture);
			texture.fill(0, P.map(curFrame, _frames - transition, _frames, 0, 255f));
			texture.rect(0,0,texture.width, texture.height);
//		} else {
		}
		VignetteFilter.instance(p).setDarkness(2f);
		VignetteFilter.instance(p).setSpread(0.7f);
		VignetteFilter.instance(p).applyTo(texture);
	}


	PShape createSheet(int detail, PImage tex) {
		p.textureMode(NORMAL);
		PShape sh = p.createShape();
		sh.beginShape(QUADS);
		sh.noStroke();
		sh.texture(tex);
		float cellW = tex.width / detail;
		float cellH = tex.height / detail;
		int numVertices = 0;
		for (int col = 0; col < tex.width; col += cellW) {
			for (int row = 0; row < tex.height; row += cellH) {
				float xU = col;
				float yV = row;
				float x = -tex.width/2f + xU;
				float y = -tex.height/2f + yV;
				float z = 0;
				sh.normal(x, y, z);
				sh.vertex(x, y, z, P.map(xU, 0, tex.width, 0, 1), P.map(yV, 0, tex.height, 0, 1));
				sh.vertex(x, y + cellH, z, P.map(xU, 0, tex.width, 0, 1), P.map(yV + cellH, 0, tex.height, 0, 1));    
				sh.vertex(x + cellW, y + cellH, z, P.map(xU + cellW, 0, tex.width, 0, 1), P.map(yV + cellH, 0, tex.height, 0, 1));    
				sh.vertex(x + cellW, y, z, P.map(xU + cellW, 0, tex.width, 0, 1), P.map(yV, 0, tex.height, 0, 1));
				numVertices++;
			}
		}
		P.println(numVertices, "vertices");
		sh.endShape(); 
		return sh;
	}

}

