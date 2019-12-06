package com.haxademic.sketch.test;

import com.haxademic.core.app.PAppletHax;
import com.haxademic.core.app.config.AppSettings;
import com.haxademic.core.draw.image.ImageSequenceMovieClip;
import com.haxademic.core.file.FileUtil;

import processing.video.Movie;

public class VideoAndImageSequenceAdvancedControls
extends PAppletHax {
	public static void main(String args[]) { arguments = args; PAppletHax.main(Thread.currentThread().getStackTrace()[1].getClassName()); }

	protected Movie movie;
	protected ImageSequenceMovieClip imageSequence;

	protected void overridePropsFile() {
		p.appConfig.setProperty( AppSettings.WIDTH, 1280 );
		p.appConfig.setProperty( AppSettings.HEIGHT, 640 );
		p.appConfig.setProperty( AppSettings.SMOOTHING, AppSettings.SMOOTH_HIGH );
	}

	public void setupFirstFrame() {
		
		movie = new Movie( p, FileUtil.getFile("video/cacheflowe/render-2015-04-23-13-38-17-export.mp4"));
		imageSequence = new ImageSequenceMovieClip(FileUtil.getFile("images/floaty-blob.anim/"), "png", 18);
	}

	public void drawApp() {
		p.background(255);
		
		if(p.frameCount == 100) {
			moviePlay();
			imageSequence.play();
		}
		if(p.frameCount == 130) {
			moviePauseOnFirstFrame();
			imageSequencePauseOnFirstFrame();
		}
		if(p.frameCount == 400) {
			movieLoop();
			imageSequence.loop();
		}
		if(p.frameCount == 500) {
			moviePause();
			imageSequence.pause();
		}
		if(p.frameCount == 600) {
			moviePauseOnLastFrame();
			imageSequencePauseOnLastFrame();
		}
		if(p.frameCount == 700) {
			movieStop();
			imageSequence.stop();
		}
		if(p.frameCount == 800) {
			moviePauseOnFirstFrame();
			imageSequencePauseOnFirstFrame();
		}
		if(p.frameCount == 900) {
			movieLoop();
			imageSequence.loop();
		}
		
		// draw movie
		if(movieStopped == false) p.image(movie, 0, 0, 640, 640);
		
		// draw image sequence
		imageSequence.update();
		imageSequence.preCacheImages();
		p.image(imageSequence.image(), 640, 0, 640, 640);
	}
	
	// Movie controls helpers
	
	protected boolean moviePaused = false;
	protected boolean movieStopped = true;

	protected void moviePlay() {
		movie.play();
		movieStopped = false;
	}
	
	protected void moviePauseOnFirstFrame() {
		movie.jump(0);
		movie.play();
		movie.pause();
		moviePaused = true;
		movieStopped = false;
	}
	
	protected void moviePauseOnLastFrame() {
		movie.play();
		movie.jump(movie.duration());
		movie.pause();
		moviePaused = true;
		movieStopped = false;
	}
	
	protected void movieStop() {
		movie.stop();
		movieStopped = true;	
	}
	
	protected void moviePause() {
		if(moviePaused == true) return;
		movie.pause();
		moviePaused = true;
		movieStopped = false;
	}
	
	protected void movieUnpause() {
		if(moviePaused == false) return;
		movie.pause();
		moviePaused = false;
		movieStopped = false;
	}
	
	protected void movieLoop() {
		movieUnpause();
		movie.loop();
		movieStopped = false;
	}
	
	// ImageSequence controls helpers

	protected void imageSequencePauseOnFirstFrame() {
		imageSequence.seek(0);
		imageSequence.play();
		imageSequence.pause();
	}

	protected void imageSequencePauseOnLastFrame() {
		imageSequence.seek(1);
		imageSequence.pause();
	}
		
}
