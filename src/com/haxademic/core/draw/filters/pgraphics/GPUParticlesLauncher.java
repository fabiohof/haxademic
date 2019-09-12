package com.haxademic.core.draw.filters.pgraphics;

import java.util.ArrayList;

import com.haxademic.core.app.P;
import com.haxademic.core.data.constants.PBlendModes;
import com.haxademic.core.data.constants.PRenderers;
import com.haxademic.core.draw.context.PG;
import com.haxademic.core.draw.filters.pgraphics.shared.BaseVideoFilter;
import com.haxademic.core.draw.filters.pshader.BlurHFilter;
import com.haxademic.core.draw.filters.pshader.BlurVFilter;
import com.haxademic.core.draw.image.BufferMotionDetectionMap;
import com.haxademic.core.draw.image.ImageUtil;
import com.haxademic.core.draw.particle.ParticleLauncherGPU;
import com.haxademic.core.math.MathUtil;

import processing.core.PGraphics;
import processing.core.PImage;

public class GPUParticlesLauncher
extends BaseVideoFilter {
	
	protected PGraphics motionBuffer;
	protected BufferMotionDetectionMap motionDetectionMap;

	protected PGraphics renderedParticles;
	protected ArrayList<ParticleLauncherGPU> particleLaunchers;

	public GPUParticlesLauncher(int width, int height) {
		super(width, height);

		// build final draw buffer
		renderedParticles = P.p.createGraphics(width, height, PRenderers.P3D);
		motionBuffer = P.p.createGraphics(width / 4, height / 4, PRenderers.P3D);
		motionBuffer.noSmooth();
//		renderedParticles.smooth(8);
		
		// build multiple particles launchers
		particleLaunchers = new ArrayList<ParticleLauncherGPU>();
		int totalVertices = 0;
		for (int i = 0; i < 20; i++) {
			ParticleLauncherGPU particles = new ParticleLauncherGPU(512);
			particleLaunchers.add(particles);
			totalVertices += particles.vertices();
		}
		
		// debug
		P.p.debugView.setTexture("renderedParticles", renderedParticles);
		P.p.debugView.setValue("totalVertices", totalVertices);
		P.p.debugView.setTexture("progressBuffer (1)", particleLaunchers.get(0).positionBuffer());
	}
	
	public void newFrame(PImage frame) {
		// store (and crop fill) frame into `sourceBuffer`
		super.newFrame(frame);
		
		// lazy init and update motion detection buffers/calcs
		if(motionDetectionMap == null) {
			motionDetectionMap = new BufferMotionDetectionMap(sourceBuffer, 0.1f);
			motionDetectionMap.setBlendLerp(0.2f);
			motionDetectionMap.setDiffThresh(0.05f);
			motionDetectionMap.setFalloffBW(0.2f);
			motionDetectionMap.setThresholdCutoff(0.5f);
			motionDetectionMap.setBlur(1f);
			P.p.debugView.setTexture("sourceBuffer", sourceBuffer);
		}
		
		// pre-process motion buffer for smoother launch blobs
		ImageUtil.copyImage(sourceBuffer, motionBuffer);
		BlurHFilter.instance(P.p).setBlurByPercent(0.5f, motionBuffer.width);
		BlurHFilter.instance(P.p).applyTo(motionBuffer);
		BlurVFilter.instance(P.p).setBlurByPercent(0.5f, motionBuffer.height);
		BlurVFilter.instance(P.p).applyTo(motionBuffer);

		// run motion detection
		motionDetectionMap.updateSource(motionBuffer);
	}
	
	public void update() {
		if(motionDetectionMap == null) return;

		// launch particles from random places within the motion detection zones
		motionDetectionMap.loadPixels();

		int particleLauncherIndex = P.p.frameCount % particleLaunchers.size();
		int FRAME_LAUNCH_INTERVAL = 1;
		int MAX_LAUNCHED_PER_FRAME = 200;
		int LAUNCH_ATTEMPTS = 1500;

		particleLaunchers.get(particleLauncherIndex).beginLaunch();
		if(P.p.frameCount % FRAME_LAUNCH_INTERVAL == 0) {
			int numLaunched = 0;
			for (int i = 0; i < LAUNCH_ATTEMPTS; i++) {
				if(numLaunched < MAX_LAUNCHED_PER_FRAME) {
					int checkX = MathUtil.randRange(0, sourceBuffer.width);
					int checkY = MathUtil.randRange(0, sourceBuffer.height);
					if(motionDetectionMap.pixelActive(checkX, checkY)) {
						particleLaunchers.get(particleLauncherIndex).launch(renderedParticles, checkX, checkY);
						numLaunched++;
					}
				} else {
					break;
				}
			}
		}
		particleLaunchers.get(particleLauncherIndex).endLaunch();

		// update particles launcher buffers
		for (int i = 0; i < particleLaunchers.size(); i++) {
			particleLaunchers.get(i).update();
		}

		// render!
		renderedParticles.beginDraw();
		PG.setDrawFlat2d(renderedParticles, true);
		renderedParticles.background(0);
		renderedParticles.fill(255);
		renderedParticles.blendMode(PBlendModes.ADD);
		for (int i = 0; i < particleLaunchers.size(); i++) {
			particleLaunchers.get(i).renderTo(renderedParticles);
		}
		renderedParticles.blendMode(PBlendModes.BLEND);
		renderedParticles.endDraw();

		// draw composite to output buffer
		destBuffer.beginDraw();
		destBuffer.background(0);
		
		destBuffer.blendMode(PBlendModes.BLEND);
		PG.resetPImageAlpha(destBuffer);
		destBuffer.image(sourceBuffer, 0, 0);
//		PG.setPImageAlpha(destBuffer, 0.5f);
//		destBuffer.image(motionDetectionMap.bwBuffer(), 0, 0, destBuffer.width, destBuffer.height);
//		destBuffer.image(motionDetectionMap.differenceBuffer(), 0, 0, destBuffer.width, destBuffer.height);
		PG.setPImageAlpha(destBuffer, 1f);
		destBuffer.blendMode(PBlendModes.ADD);
		destBuffer.image(renderedParticles, 0, 0);
		destBuffer.blendMode(PBlendModes.BLEND);
		
		destBuffer.endDraw();
		// desaturate
//		SaturationFilter.instance(p).setSaturation(0);
//		SaturationFilter.instance(p).applyTo(p);	}
	}
}
