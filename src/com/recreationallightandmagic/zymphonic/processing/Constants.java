package com.recreationallightandmagic.zymphonic.processing;

/**
 * Some basic, handy constants, and some global variables (that might need to be
 * configured per machine and or physical installation.
 */
public class Constants {

	// To convert from physical meters to LED-array index
	public static final float LEDS_PER_METER = 60;

	// Physical mapping
	public static final int NUM_LIGHT_STRIPS = 8;


	// Only needed (at this time) for absolute path to sound files via Wormhole.createInput
	public static final String SAMPLE_DIRECTORY = "/home/shelly/workspace/zymphonic/samples";

	// new int[] { applet.color(255, 0, 0),
	// applet.color(0, 255, 0), applet.color(0, 0, 255),
	// applet.color(255, 255, 0), applet.color(255, 0, 255),
	// applet.color(0, 255, 255) };
	public static int[] basicColors = new int[] { -65536, -16711936, -16776961,
			-256, -65281, -16711681 };

}
