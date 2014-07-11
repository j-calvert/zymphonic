package com.recreationallightandmagic.zymphonic.processing;

import java.util.Arrays;

import processing.core.PApplet;
import processing.serial.Serial;

/**
 * LED driver
 */
public class Lights {

	private static final float GAMMA = 1.7f;

	private final Serial ledSerial;
	private final int[] gammatable = new int[256];
	private final int[][] leds;
	
	public Lights(PApplet applet, int numStrips, float totalLength) {
		String[] list = Serial.list();
		// delay(20);
		System.out.println("Serial Ports List:");
		System.out.println(Arrays.toString(list));
		// change this to your port name
		ledSerial = new Serial(applet,  "/dev/ttyACM0");
		for (int i = 0; i < 256; i++) {
			gammatable[i] = (int) (Math.pow((float) i / 255.0, GAMMA) * 255.0 + 0.5);
		}
		leds = new int[numStrips][(int) Math.floor(totalLength * Constants.LEDS_PER_METER)];
	}

	private static float FRAMERATE = 30;
	public void display() {
		byte[] ledData = image2data(leds);
		ledData[0] = '*'; // first Teensy is the frame sync master
		int usec = (int) ((1000000.0 / FRAMERATE) * 0.75);
		ledData[1] = (byte) (usec); // request the frame sync pulse
		ledData[2] = (byte) (usec >> 8); // at 75% of the frame time
		ledSerial.write(ledData);
		for(int i = 0; i < leds.length; i++) {
			for(int j = 0; j < leds[0].length; j++) {
				leds[i][j] = 0;
			}
		}
	}
	
	public int[] getStrip(int stripNum) {
		return leds[stripNum];
	}
	

	// Convert to byte array that we write to all 8 pins, regardless of how many
	// are actually hooked up to LEDs and getting non-zero colors
	// TODO Figure out how to not write to unused pins
	private byte[] image2data(int[][] colors) {
		int offset = 3;
		int pixel[] = new int[8];
		byte[] ret = new byte[(colors.length * colors[0].length * 3) + 3];

		for (int z = 0; z < colors[0].length; z++) {
			for (int i = 0; i < 8; i++) {
				// fetch 8 pixels from the image, 1 for each pin
				if (colors.length > i) {
					pixel[i] = colors[i][z];
				}
				pixel[i] = colorWiring(pixel[i]);
			}
			// convert 8 pixels to 24 bytes
			for (int mask = 0x800000; mask != 0; mask >>= 1) {
				byte b = 0;
				for (int i = 0; i < 8; i++) {
					if ((pixel[i] & mask) != 0)
						b |= (1 << i);
				}
				ret[offset++] = b;
			}
		}
		return ret;
	}

	// translate the 24 bit color from RGB to the actual
	// order used by the LED wiring. GRB is the most common.
	private int colorWiring(int c) {
		int red = (c & 0xFF0000) >> 16;
		int green = (c & 0x00FF00) >> 8;
		int blue = (c & 0x0000FF);
		red = gammatable[red];
		green = gammatable[green];
		blue = gammatable[blue];
		return (green << 16) | (red << 8) | (blue); // GRB - most common wiring
	}

}
