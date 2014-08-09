package com.recreationallightandmagic.zymphonic.processing.input;

import com.recreationallightandmagic.zymphonic.processing.Constants;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * A history of depth measurements for a region. We keep a few running averages
 * handy for background subtraction. Kinda like a circular buffer, but we don't
 * track the tail (we just skip elements where timestamp is 0)
 * 
 * Capable of getting a depthHistogram from a DepthFrame.
 */
public class DepthRegion {

	private static int MAX_SECTIONS = 20;
	private static boolean[] ZEROS = new boolean[MAX_SECTIONS];

	public String name;
	// Upper left corner, nearest, and height, width and depth array
	public float x, y, z, w, h, d;
	public boolean[] isHit = new boolean[MAX_SECTIONS],
			wasHit = new boolean[MAX_SECTIONS];


	public DepthRegion(String name, float x, float y, float w,
			float h, float z, float d) {
		super();
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		this.h = h;
		this.d = d;
	}

	public void preLoop() {
		arrCp(ZEROS, isHit);
	}

	// Returns the segment ID that this point is in, if any, -1 otherwise
	// Also do whatever internal processing you want to do for each point.
	public int consider(PVector pv) {
		if (pv.x > x - w / 2 && pv.x < x + w / 2 && pv.y > y - h / 2
				&& pv.y < y + h / 2 && pv.z > z && pv.z < z + d * MAX_SECTIONS) {
			// It's in some segment.
			for (int i = 0; i < MAX_SECTIONS; i++) {
				if (pv.z < z + d * (i + 1)) {
					// All the internal processing we do for now.
					isHit[i] = true;
					return i;
				}
			}
		}
		return -1;
	}

	public void postLoop() {
		// TODO trigger sounds, draw, etc.
		arrCp(isHit, wasHit);
	}

	private static void arrCp(boolean[] from, boolean[] to) {
		for (int i = 0; i < MAX_SECTIONS; i++) {
			to[i] = from[i];
		}
	}

	public void draw(PApplet applet, boolean isSelected) {
		applet.pushMatrix();
		if (isSelected) {
			applet.stroke(Constants.basicColors[4]);
		} else {
			applet.stroke(Constants.basicColors[0]);
		}
		applet.translate(x, y, z + d / 2);
		for (int i = 0; i < MAX_SECTIONS; i++) {
			applet.box(w, h, d);
			applet.translate(0, 0, d);
		}
		applet.popMatrix();
	}

}
