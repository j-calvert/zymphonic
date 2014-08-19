package com.recreationallightandmagic.zymphonic.processing.input;

import processing.core.PApplet;
import SimpleOpenNI.SimpleOpenNI;

import com.recreationallightandmagic.zymphonic.processing.sandbox.WormholeCore;

/**
 * Stuff encapsulated around Kinect interaction for the purpose of basic code
 * orginaztion. Deliberately break law of demeter and expect/encourage things
 * that have one of these to access the inner kinect member directly (e.g. via
 * this.kinect.kinect)
 */
public class Kinect {

	public SimpleOpenNI kinect1;
	public SimpleOpenNI kinect2;

	public Kinect(PApplet applet) {
		SimpleOpenNI.start();
		int deviceCount = SimpleOpenNI.deviceCount();
		kinect2 = new SimpleOpenNI(1, applet);
		kinect1 = new SimpleOpenNI(0, applet);
		init(kinect1);
		init(kinect2);
	}

	private void init(SimpleOpenNI kinect) {
		if (kinect.isInit() == false) {
			throw new RuntimeException(
					"Can't init SimpleOpenNI, maybe the camera is not connected!");
		}
		// enable depthMap generation
		kinect.enableDepth();
		if (WormholeCore.TRACK_USERS) {
			kinect.enableUser();
		}
		kinect.setMirror(true);
	}

	/**
	 * A mapping from the real-world coordinate system of a Kinect, to LED strip
	 * indexes
	 */
	// TODO Implement me
	int getLedIdx(int ledStripId, float depth) {
		return -1;
	}

}