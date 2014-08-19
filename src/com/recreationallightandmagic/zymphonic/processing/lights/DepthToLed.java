package com.recreationallightandmagic.zymphonic.processing.lights;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mapping from Kinect depth to LED index
 */
public class DepthToLed {

	public List<DepthLed> calibrations = new ArrayList<DepthLed>();

	public void addCalibration(float depth, int ledIdx) {
		DepthLed dl = new DepthLed();
		dl.depth = depth;
		dl.ledIdx = ledIdx;
		calibrations.add(dl);
		Collections.sort(calibrations);
	}

	public int getLedIdx(float depth) {
		if (calibrations.size() < 2) {
			System.err.println("Don't call getLedIdx before you calibrate");
			return -1;
		}
		if (calibrations.get(0).depth > depth) {
			return -1;
		} else {
			DepthLed lastDl = calibrations.get(0);
			for (DepthLed dl : calibrations) {
				if (dl.depth < depth) {
					return interpolate(lastDl, dl, depth);
				}
				lastDl = dl;
			}
		}
		return -1;
	}

	private int interpolate(DepthLed lastDl, DepthLed dl, float depth) {
//		return (int) ((depth - lastDl.depth) * dl.ledIdx + (dl.depth - lastDl.depth) *lastDl.ledIdx) /(dl.depth - lastDl.depth));
		return -1;
	}

	private static class DepthLed implements Comparable<DepthLed> {

		public float depth;
		public int ledIdx;

		@Override
		public int compareTo(DepthLed that) {
			if (this.depth < that.depth) {
				return -1;
			} else if (this.depth > that.depth) {
				return 1;
			}
			return 0;
		}

	}

}
