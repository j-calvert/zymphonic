package com.recreationallightandmagic.zymphonic.processing.lights;

import com.recreationallightandmagic.zymphonic.processing.input.DepthHistoryRegion;

/**
 * Keeps track of the physical mapping of depth as provided by the kinect, and
 * ledIndex[]. For outputting to LED index only. Need a layout program to add
 * points to this map
 */
public class LEDStrip {

	public class Led {
		public int depthIndex;
		public boolean isCalibrationPoint;
		public float physicalX, physicalY;
		public int color;
	}

	// The map. We compute the inverse dynamically. Accumulates image for each
	// frame of rendering
	public Led[] leds = new Led[LEDs.LEDS_PER_STRIP];

	public LEDStrip() {
		for (int i = 0; i < leds.length; i++) {
			leds[i] = new Led();
		}
	}

	public void clear() {
		for (Led led : leds) {
			led.color = 0;
		}
	}

	public void addProjectionBall(int depth, float radius, float physicalX,
			float physicalY) {
		// TODO Write this
	}

	public void addHistogram(DepthHistoryRegion region, float decay,
			int replayOffset) {
		// TODO Write this too
	}

	public void addCalibrationPoint(int depth, int ledIndex, float xCalPoint,
			float yCalPoint, float angleRadians, float distanceInches) {
	}

}
