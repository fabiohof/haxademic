package com.haxademic.demo.draw.shapes.shader;

import com.haxademic.core.app.P;
import com.haxademic.core.app.PAppletHax;
import com.haxademic.core.camera.CameraUtil;
import com.haxademic.core.constants.AppSettings;
import com.haxademic.core.constants.PRenderers;
import com.haxademic.core.draw.context.DrawUtil;
import com.haxademic.core.draw.filters.shaders.BlurProcessingFilter;
import com.haxademic.core.draw.image.PerlinTexture;
import com.haxademic.core.draw.shapes.PShapeUtil;
import com.haxademic.core.draw.textures.pshader.TextureShader;
import com.haxademic.core.file.DemoAssets;
import com.haxademic.core.file.FileUtil;

import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.opengl.PShader;

public class Demo_VertexShader_LinesTunnelDeform 
extends PAppletHax {
	public static void main(String args[]) { PAppletHax.main(Thread.currentThread().getStackTrace()[1].getClassName()); }

	protected PShape shape;
	protected PGraphics noiseBuffer;
	protected TextureShader noiseTexture;
	protected PShader displacementShader;
	protected float shapeExtent = 100;

	protected void overridePropsFile() {
		int FRAMES = 358;
		p.appConfig.setProperty(AppSettings.LOOP_FRAMES, FRAMES);
		p.appConfig.setProperty(AppSettings.RENDERING_MOVIE, false);
		p.appConfig.setProperty(AppSettings.RENDERING_MOVIE_START_FRAME, 1 + FRAMES);
		p.appConfig.setProperty(AppSettings.RENDERING_MOVIE_STOP_FRAME, 1 + FRAMES * 2);
	}
	
	protected void setupFirstFrame() {
		// load texture
//		perlin = new PerlinTexture(p, 256, 256);
//		texture = perlin.texture();
		noiseBuffer = p.createGraphics(p.width, p.height, PRenderers.P2D);
		noiseTexture = new TextureShader(TextureShader.noise_simplex_2d_iq, 0.0005f);
		p.debugView.setTexture(noiseBuffer);
		
		// build sheet mesh
		shape = p.createShape(P.GROUP);
		int rows = 200;
		int circleSegments = 200;
		float radius = 200;
		float segmentRads = P.TWO_PI / (float) circleSegments;
		for (int y = 0; y < rows; y++) {
			PShape line = P.p.createShape();
			line.beginShape();
			line.stroke(255);
			line.strokeWeight(1);
			line.noFill();
			for (int i = 0; i <= circleSegments; i++) {
				line.vertex(radius * P.sin(segmentRads * i), y * 10f, radius * P.cos(segmentRads * i));
			}
			line.endShape();
			shape.addChild(line);
		}
		PShapeUtil.centerShape(shape);
		PShapeUtil.scaleShapeToHeight(shape, p.height * 2f);
		PShapeUtil.addTextureUVSpherical(shape, noiseBuffer);
		shapeExtent = PShapeUtil.getMaxExtent(shape);

		shape.setTexture(noiseBuffer);
		p.debugView.setValue("shape.getVertexCount();", PShapeUtil.vertexCount(shape));
		
		// load shader
		displacementShader = loadShader(
			FileUtil.getFile("haxademic/shaders/vertex/line-frag.glsl"), 
			FileUtil.getFile("haxademic/shaders/vertex/line-vert.glsl")
		);
	}

	public void drawApp() {
		background(0);
		
		// update displacement texture
		noiseTexture.shader().set("zoom", 2.5f + 1.5f * P.sin(p.loop.progressRads()));
		noiseTexture.shader().set("rotation", p.loop.progressRads());
		noiseBuffer.filter(noiseTexture.shader());
		// blur texture for smooothness
		BlurProcessingFilter.instance(p).setBlurSize(5);
		BlurProcessingFilter.instance(p).applyTo(noiseBuffer);
		
		// rotate
		DrawUtil.setCenterScreen(p);
		DrawUtil.basicCameraFromMouse(p.g);
		p.rotateX(P.sin(loop.progressRads()) * 0.2f);
//		CameraUtil.setCameraDistance(p.g, 100, 10000);
		
		// draw mesh
		shape.disableStyle();
		p.stroke(255);
		displacementShader.set("displacementMap", noiseBuffer);
		displacementShader.set("colorMap", DemoAssets.textureNebula());
		displacementShader.set("weight", p.mousePercentX() * 20f);
		displacementShader.set("modelMaxExtent", shapeExtent * 2f);
		if(p.mousePercentX() > 0.5f) {
			displacementShader.set("sheet", 1);
			displacementShader.set("displaceStrength", p.mousePercentY() * pg.height * 0.7f);
		} else {
			displacementShader.set("sheet", 0);
			displacementShader.set("displaceStrength", p.mousePercentY() * pg.height * 0.01f);
		}
		displacementShader.set("colorThickness", (p.mousePercentY() > 0.5f) ? 1 : 0);
		p.shader(displacementShader, P.LINES);  
		p.shape(shape);
		p.resetShader();
		
		// post
		// FXAAFilter.instance(p).applyTo(p.g);
		// BlurBasicFilter.instance(p).applyTo(p.g);
	}
		
}