package com.haxademic.demo.draw.shapes.shader;

import com.haxademic.core.app.P;
import com.haxademic.core.app.PAppletHax;
import com.haxademic.core.app.config.AppSettings;
import com.haxademic.core.draw.image.ImageUtil;
import com.haxademic.core.draw.shapes.Icosahedron;
import com.haxademic.core.draw.shapes.PShapeUtil;
import com.haxademic.core.draw.shapes.pshader.MeshDeformAndTextureFilter;
import com.haxademic.core.media.DemoAssets;

import processing.core.PImage;
import processing.core.PShape;
import processing.opengl.PShader;

public class Demo_VertexShader_IcosaDeformByBrightness 
extends PAppletHax {
	public static void main(String args[]) { arguments = args; PAppletHax.main(Thread.currentThread().getStackTrace()[1].getClassName()); }

	protected PShape shapeIcos;
	protected PImage texture;
	protected PShader texShader;

	protected void overridePropsFile() {
		p.appConfig.setProperty( AppSettings.LOOP_FRAMES, 360 );
		p.appConfig.setProperty( AppSettings.WIDTH, 1000 );
		p.appConfig.setProperty( AppSettings.HEIGHT, 800 );
	}

	public void setupFirstFrame() {
	

		// load texture
		texture = ImageUtil.imageToGraphics(DemoAssets.textureJupiter());
		
		// create icosahedron
		shapeIcos = Icosahedron.createIcosahedron(p.g, 7, texture);
		PShapeUtil.scaleShapeToExtent(shapeIcos, p.height/4f);
	}

	public void drawApp() {
		background(0);
		
		// draw icosahedron
		p.pushMatrix();
		p.translate(p.width/2f, p.height/2f);
		p.rotateY(loop.progressRads());
		p.rotateZ(0.05f + 0.05f * P.sin(-P.PI/2f + loop.progressRads()));
		
		// apply vertex shader & draw icosahedron
		MeshDeformAndTextureFilter.instance(p).setDisplacementMap(texture);
		MeshDeformAndTextureFilter.instance(p).setDisplaceAmp(0.3f + 0.3f * P.sin(-P.PI/2f + loop.progressRads()));
		MeshDeformAndTextureFilter.instance(p).setSheetMode(false);
		MeshDeformAndTextureFilter.instance(p).applyTo(p);
		// set texture using PShape method
		shapeIcos.setTexture(texture);
		
		p.shape(shapeIcos);
		p.resetShader();
		p.popMatrix();
	}
		
}