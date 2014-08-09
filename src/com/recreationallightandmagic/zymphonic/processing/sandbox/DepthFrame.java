package com.recreationallightandmagic.zymphonic.processing.sandbox;

/**
 * A convenience class for dealing with a frame of depth data.
 */
public class DepthFrame {
	public int[] depthFrame = new int[640 * 480];
	public long timestamp = 0;
	
	public int getDepth(int x, int y) {
		return depthFrame[x + y * 640];
	}
}
