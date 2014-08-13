package com.recreationallightandmagic.zymphonic.processing.input;

import org.codehaus.jackson.annotate.JsonIgnore;

import processing.core.PApplet;
import processing.core.PVector;

import com.recreationallightandmagic.zymphonic.processing.Constants;
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
	private static final int TRIGGER_POINT_COUNT_THRESHOLD = 4;
	private static int[] ZEROS = new int[MAX_SEGMENTS];

	public String name;
	// Position, width, height, depth
	public float x, y, z, w, h, d;

	public String soundNames[] = new String[MAX_SEGMENTS];

	// Everything you need to know to do a derivative of the segment histograms.
	@JsonIgnore
	public int[] pointCount = new int[MAX_SEGMENTS],
			lastPointCount = new int[MAX_SEGMENTS];

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
		arrCp(ZEROS, pointCount);
	}

	// Returns the segment ID that this point is in, if any, -1 otherwise
	// Also do whatever internal processing you want to do for each point.
	public int consider(PApplet applet, PVector pv, boolean isSelected,
			int selectedSegment) {
		if (pv.x > x - w / 2 && pv.x < x + w / 2 && pv.y > y - h / 2
				&& pv.y < y + h / 2 && pv.z > z && pv.z < z + d * MAX_SEGMENTS) {
			// It's in some segment.
			for (int i = 0; i < MAX_SEGMENTS; i++) {
				int pointColor = getPointColor(isSelected, selectedSegment, i);
				if (pv.z < z + d * (i + 1)) {
					// All the internal processing we do for now.
					applet.stroke(pointColor);
					pointCount[i]++;
					return i;
				}
			}
		}
		return -1;
	}

	public void postLoop(WormholeApplication.PointCloudFrame sound3dApplet,
			boolean isSelected, int selectedSegment) {
		for (int i = 0; i < MAX_SEGMENTS; i++) {
			int pointColor = getPointColor(isSelected, selectedSegment, i);
			sound3dApplet.fill(pointColor,
					ratio(pointCount[i], lastPointCount[i]));
			if (pointCount[i] > TRIGGER_POINT_COUNT_THRESHOLD
					* lastPointCount[i]) {
				sound3dApplet.triggerSound(soundNames[i]);
			}
		}
		draw(sound3dApplet, isSelected, selectedSegment);
		arrCp(pointCount, lastPointCount);
	}

	private float ratio(int i, int j) {
		if (i > TRIGGER_POINT_COUNT_THRESHOLD) {
			return 0.5f;
		} else {
			return 0f;
		}
	}

	private void draw(PApplet applet, boolean isSelected, int selectedSegment) {
		applet.pushMatrix();
		applet.translate(x, y, z + d / 2);
		for (int i = 0; i < MAX_SEGMENTS; i++) {
			applet.stroke(getPointColor(isSelected, selectedSegment, i));
			applet.box(w, h, d);
			applet.translate(0, 0, d);
		}
		applet.popMatrix();
	}

	private static void arrCp(int[] from, int[] to) {
		for (int i = 0; i < MAX_SEGMENTS; i++) {
			to[i] = from[i];
		}
	}

	private int getPointColor(boolean isSelected, int selectedSegment, int i) {
		if (i == selectedSegment) {
			if (isSelected) {
				return WormholeApplication.ACTIVE_BOTH_COLOR;
			} else {
				return WormholeApplication.ACTIVE_SEGMENT_COLOR;
			}
		} else if (isSelected) {
			return WormholeApplication.ACTIVE_REGION_COLOR;
		}
		return Constants.WHITE;
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
