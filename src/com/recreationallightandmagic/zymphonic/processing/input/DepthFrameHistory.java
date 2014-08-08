package com.recreationallightandmagic.zymphonic.processing.input;

import processing.core.PApplet;

import com.recreationallightandmagic.zymphonic.processing.Constants;

/**
 * A history of depth measurements. We keep a few running averages handy for
 * background subtraction. Kinda like a circular buffer, but we don't track the
 * tail (we just skip elements where timestamp is 0)
 *
 */
public class DepthFrameHistory {

		private final DepthFrame[] depths;

		/**
		 * The main working method. Gets the histogram for this region from the
		 * depthMap.
		 */
		public DepthFrame getDepthFrame(int[] depthMap) {
			// TODO implement me.
			return null;
		}

		/**
		 * Draws itself in the UI (may play into UI that's used to define one).
		 */
		public void draw(PApplet applet) {
			// TODO implement me.
		}

		private int headIndex = -1;

		public DepthFrameHistory(int capacity) {
			depths = new DepthFrame[capacity];
			for (int i = 0; i < capacity; i++) {
				depths[i] = new DepthFrame();
			}
		}

		/**
		 * Get the PVector at the head of the list for the purpose of setting its
		 * components. This takes care of setting the corresponding timestamp and
		 * incrementing the headIndex accordingly.
		 */
		public synchronized DepthFrame getSetHead() {
			headIndex++;
			if (headIndex >= depths.length) {
				headIndex = 0;
			}
			depths[headIndex].timestamp = System.currentTimeMillis();
			return depths[headIndex];
		}

		public synchronized DepthFrame getHead() {
			return getHeadOffset(0);
		}

		public synchronized DepthFrame getHeadOffset(int offset) {
			int index = Constants.correctNegMod(headIndex - offset, depths.length);
			if (headIndex >= 0 && depths[index] != null) {
				return depths[index];
			} else {
				return null;
			}
		}
}
