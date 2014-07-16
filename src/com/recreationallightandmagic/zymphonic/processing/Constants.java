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

	// Only needed (at this time) for absolute path to sound files via
	// Wormhole.createInput
	public static final String SAMPLE_DIRECTORY = "/home/" + System.getProperty("user.name") +"/workspace/zymphonic/soundSamples/";

	// new int[] { applet.color(255, 0, 0),
	// applet.color(0, 255, 0), applet.color(0, 0, 255),
	// applet.color(255, 255, 0), applet.color(255, 0, 255),
	// applet.color(0, 255, 255) };
	public static int[] basicColors = new int[] { -65536, -16711936, -16776961,
			-256, -65281, -16711681 };

	// Not a constant, but widely used
	public static int correctNegMod(int val, int mod) {
		val = val % mod;
		if (val < 0) {
			return mod + val;
		} else {
			return val;
		}
	}

	// Ad-hoc tests that it does what it should:
	public static void main(String[] args) {
		System.out.println(Constants.correctNegMod(-3, 10));

		for (float i = -50; i <= 50; i += 5) {
			int floor = (int) Math.floor(i / 10f);
			System.out.println(i + " " + floor + " " + floor % 4 + " "
					+ Constants.correctNegMod(floor, 4));

		}

	}

}
