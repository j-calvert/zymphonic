package com.recreationallightandmagic.zymphonic.processing.input;

import org.codehaus.jackson.annotate.JsonIgnore;

import processing.core.PApplet;
import processing.core.PVector;

import com.recreationallightandmagic.zymphonic.processing.WormholeApplication;

/**
 * A history of depth measurements for a region. We keep a few running averages
 * handy for background subtraction. Kinda like a circular buffer, but we don't
 * track the tail (we just skip elements where timestamp is 0)
 * 
 * Capable of getting a depthHistogram from a DepthFrame.
 */
public class DepthRegion {

	public static final int MAX_SEGMENTS = 16;
	private static boolean[] ZEROS = new boolean[MAX_SEGMENTS];

	public String name;
	// Position, width, height, depth
	public float x, y, z, w, h, d;

	public String soundNames[] = new String[MAX_SEGMENTS];

	@JsonIgnore
	public boolean isSelected;
	@JsonIgnore
	public int selectedSegment;

	@JsonIgnore
	// This thing changes point by point (by call to consider).
	public int pointSegment;

	@JsonIgnore
	public boolean[] isHit = new boolean[MAX_SEGMENTS],
			wasHit = new boolean[MAX_SEGMENTS];

	public DepthRegion() {
	}// For Jackson

	public DepthRegion(String name, float x, float y, float w, float h,
			float z, float d) {
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
				&& pv.y < y + h / 2 && pv.z > z && pv.z < z + d * MAX_SEGMENTS) {
			// It's in some segment.
			for (int i = 0; i < MAX_SEGMENTS; i++) {
				if (pv.z < z + d * (i + 1)) {
					// All the internal processing we do for now.
					isHit[i] = true;
					pointSegment = i;
					return pointSegment;
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
		for (int i = 0; i < MAX_SEGMENTS; i++) {
			to[i] = from[i];
		}
	}

	public void draw(PApplet applet, boolean isSelected, int selectedSegment) {
		applet.pushMatrix();
		applet.translate(x, y, z + d / 2);
		for (int i = 0; i < MAX_SEGMENTS; i++) {
			applet.stroke(255);
			getPointColor(applet, isSelected, selectedSegment, i);
			applet.box(w, h, d);
			applet.translate(0, 0, d);
		}
		applet.popMatrix();
	}

	private void getPointColor(PApplet applet, boolean isSelected,
			int selectedSegment, int i) {
		if (i == selectedSegment) {
			if (isSelected) {
				applet.stroke(WormholeApplication.ACTIVE_BOTH_COLOR);
			} else {
				applet.stroke(WormholeApplication.ACTIVE_SEGMENT_COLOR);
			}
		} else if (isSelected) {
			applet.stroke(WormholeApplication.ACTIVE_REGION_COLOR);
		}
	}

	public String getSoundName(int segmentId) {
		if (segmentId >= 0 && segmentId < MAX_SEGMENTS
				&& soundNames[segmentId] != null
				&& !soundNames[segmentId].isEmpty()) {
			return soundNames[segmentId];
		}
		return null;
	}

	public void setSoundName(int segmentId, String soundName) {
		if (segmentId >= 0 && segmentId < MAX_SEGMENTS) {
			soundNames[segmentId] = soundName;
		}
	}
}
