package com.recreationallightandmagic.zymphonic.processing;

import processing.core.PApplet;

/**
 * Some basic, handy constants, and some global variables (that might need to be
 * configured per machine and or physical installation.
 */
public class Constants {

	public static final String PROJECT_DIRECTORY = "/home/"
			+ System.getProperty("user.name") + "/workspace/zymphonic/";
	public static final String SAMPLE_DIRECTORY = PROJECT_DIRECTORY
			+ "soundSamples/";
	public static final String WORMHOLE_STATE_DIRECTORY = PROJECT_DIRECTORY
			+ "savedStates/";

	// new int[] { applet.color(255, 0, 0),
	// applet.color(0, 255, 0), applet.color(0, 0, 255),
	// applet.color(255, 255, 0), applet.color(255, 0, 255),
	// applet.color(0, 255, 255) };
	public static int[] basicColors = new int[] { -65536, -16711936, -16776961,
			-256, -65281, -16711681 };
	
	// new PApplet().color(255, 255, 255) = new PApplet().color(255)
	public static int WHITE = -1;

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
		System.out.println(new PApplet().color(255, 255, 255));
		// System.out.println(Constants.correctNegMod(-3, 10));
		//
		// for (float i = -50; i <= 50; i += 5) {
		// int floor = (int) Math.floor(i / 10f);
		// System.out.println(i + " " + floor + " " + floor % 4 + " "
		// + Constants.correctNegMod(floor, 4));
		//
		// }

	}

}
