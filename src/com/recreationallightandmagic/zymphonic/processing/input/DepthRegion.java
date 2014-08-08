package com.recreationallightandmagic.zymphonic.processing.input;

import processing.core.PApplet;

/**
 * A history of depth measurements for a region. We keep a few running averages
 * handy for background subtraction. Kinda like a circular buffer, but we don't
 * track the tail (we just skip elements where timestamp is 0)
 * 
 * Capable of getting a depthHistogram from a DepthFrame.
 */
public class DepthRegion {

	// Upper left corner, nearest, and height, width and depth array
	public float x, y, z, w, h, d, n;

	/**
	 * The main working method. Records data of interest re: depths for this
	 * region from the depthFrame.
	 */
	public void procesDepthFrame(int[] depthMap) {
		// TODO implement me.
	}

	/**
	 * Draws itself in the UI (may play into UI that's used to define one).
	 */
	public void draw(PApplet applet) {
		applet.pushMatrix();
		applet.translate(x, y, z);
		drawShit(applet);
		applet.popMatrix();
	}

	private void drawShit(PApplet applet) {
		applet.line(x, y, z, x + w, y, z);
		applet.line(x, y, z, x, y + h, z);
		applet.line(x, y, z, x + w, y, z);
		applet.line(x, y, z, x, y, z + d);
	}
}
