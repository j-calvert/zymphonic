package com.recreationallightandmagic.zymphonic.processing.lights;

/**  Butchered from OctoWS2811 movie2serial.pde 
 * 
 * Transmit video data to 1 or more
 Teensy 3.0 boards running OctoWS2811 VideoDisplay.ino
 http://www.pjrc.com/teensy/td_libs_OctoWS2811.html
 Copyright (c) 2013 Paul Stoffregen, PJRC.COM, LLC

 To configure this program, edit the following sections:

 1: change myMovie to open a video file of your choice    ;-)

 2: edit the serialConfigure() lines in setup() for your
 serial device names (Mac, Linux) or COM ports (Windows)

 3: if your LED strips have unusual color configuration,
 edit colorWiring().  Nearly all strips have GRB wiring,
 so normally you can leave this as-is.

 4: if playing 50 or 60 Hz progressive video (or faster),
 edit framerate in movieEvent().
 */

import java.awt.Rectangle;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PImage;
import processing.serial.Serial;

public class LEDs {
	float gamma = 1.7f;

	int numPorts = 0; // the number of serial ports in use
	int maxPorts = 24; // maximum number of serial ports

	Serial[] ledSerial = new Serial[maxPorts]; // each port's actual Serial port
	Rectangle[] ledArea = new Rectangle[maxPorts]; // the area of the movie each
													// port gets, in % (0-100)
	boolean[] ledLayout = new boolean[maxPorts]; // layout of rows, true = even
													// is left->right
	PImage[] ledImage = new PImage[maxPorts]; // image sent to each port
	int[] gammatable = new int[256];
	int errorCount = 0;
	float framerate = 0;

	// A segment is a physical, contiguous strip of LEDs
	public static final int LEDS_PER_STRIP = 240;

	// How many do we chain from one cat5 pair from the teensy
	public static final int STRIPS_PER_SEGMENT = 2;

	// Physical mapping to teensy port. Needs to be a multiple of 8 to optimize
	// use of teensy board with shield.
	public static final int NUM_LIGHT_SEGMENTS = 8;

	public static final int NUM_LIGHT_STRIPS = NUM_LIGHT_SEGMENTS
			* STRIPS_PER_SEGMENT;

	// The thing we (re-)use in the low-level render method
	public final PImage image = new PImage(LEDS_PER_STRIP * STRIPS_PER_SEGMENT,
			NUM_LIGHT_SEGMENTS);

	public final LEDStrip[] ledStrips = new LEDStrip[NUM_LIGHT_STRIPS];

	public void clear() {
		for (LEDStrip led : ledStrips) {
			led.clear();
		}
	}

	public void drawCursor(int ledStripNum, int ledIndex, int centerColor,
			int outerColor) {
		setLedDirect(ledStripNum, ledIndex, centerColor);
		setLedDirect(ledStripNum, ledIndex - 1, outerColor);
		setLedDirect(ledStripNum, ledIndex + 1, outerColor);
		setLedDirect(ledStripNum, ledIndex - 2, outerColor);
		setLedDirect(ledStripNum, ledIndex + 2, outerColor);
	}

	public void setLedDirect(int ledStripNum, int ledIndex, int color) {
		if (ledIndex <= 0 || ledIndex >= LEDS_PER_STRIP) {
			return;
		}
		ledStrips[ledStripNum].leds[ledIndex].color = color;
	}

	public LEDs(PApplet applet) {

		for (int i = 0; i < ledStrips.length; i++) {
			ledStrips[i] = new LEDStrip();
		}

		String[] list = Serial.list();
		// delay(20);
		System.out.println("Serial Ports List:");
		System.out.println(Arrays.toString(list));
		serialConfigure(applet, "/dev/ttyACM1"); // change these to your port
													// names
		// serialConfigure("/dev/ttyACM1");
		if (errorCount > 0)
			throw new RuntimeException(
					"Got error setting up Lights.  Check that port is right for teensy.");
		for (int i = 0; i < 256; i++) {
			gammatable[i] = (int) (Math.pow((float) i / 255.0, gamma) * 255.0 + 0.5);
		}
	}

	public void renderLights() {

		// This part pretty much precludes any zig zagging, but that's OK for
		// this application
		for (int stripNum = 0; stripNum < ledStrips.length; stripNum++) {
			int row = stripNum / STRIPS_PER_SEGMENT;
			int colOffset = (stripNum % STRIPS_PER_SEGMENT) * LEDS_PER_STRIP;
			for (int j = 0; j < LEDS_PER_STRIP; j++) {
				int thisColor = ledStrips[stripNum].leds[j].color;
				image.set(colOffset + j, row, thisColor);
			}
		}

		framerate = 30.0f; // TODO, how to read the frame rate???

		for (int i = 0; i < numPorts; i++) {
			// // copy a portion of the movie's image to the LED image
			// int xoffset = percentage(image.width, ledArea[i].x);
			// int yoffset = percentage(image.height, ledArea[i].y);
			// int xwidth = percentage(image.width, ledArea[i].width);
			// int yheight = percentage(image.height, ledArea[i].height);
			// ledImage[i].copy(image, xoffset, yoffset, xwidth, yheight, 0, 0,
			// ledImage[i].width, ledImage[i].height);
			// convert the LED image to raw data
			byte[] ledData = new byte[(image.width * image.height * 3) + 3];
			image2data(image, ledData, true);
			if (i == 0) {
				ledData[0] = '*'; // first Teensy is the frame sync master
				int usec = (int) ((1000000.0 / framerate) * 0.75);
				ledData[1] = (byte) (usec); // request the frame sync pulse
				ledData[2] = (byte) (usec >> 8); // at 75% of the frame time
			} else {
				ledData[0] = '%'; // others sync to the master board
				ledData[1] = 0;
				ledData[2] = 0;
			}
			// send the raw data to the LEDs :-)
			ledSerial[i].write(ledData);
		}
	}

	// image2data converts an image to OctoWS2811's raw data format.
	// The number of vertical pixels in the image must be a multiple
	// of 8. The data array must be the proper size for the image.
	void image2data(PImage image, byte[] data, boolean layout) {
		int offset = 3;
		int x, y, xbegin, xend, xinc, mask;
		int linesPerPin = image.height / 8;
		int pixel[] = new int[8];

		for (y = 0; y < linesPerPin; y++) {
			if ((y & 1) == (layout ? 0 : 1)) {
				// even numbered rows are left to right
				xbegin = 0;
				xend = image.width;
				xinc = 1;
			} else {
				// odd numbered rows are right to left
				xbegin = image.width - 1;
				xend = -1;
				xinc = -1;
			}
			for (x = xbegin; x != xend; x += xinc) {
				for (int i = 0; i < 8; i++) {
					// fetch 8 pixels from the image, 1 for each pin
					pixel[i] = image.pixels[x + (y + linesPerPin * i)
							* image.width];
					pixel[i] = colorWiring(pixel[i]);
				}
				// convert 8 pixels to 24 bytes
				for (mask = 0x800000; mask != 0; mask >>= 1) {
					byte b = 0;
					for (int i = 0; i < 8; i++) {
						if ((pixel[i] & mask) != 0)
							b |= (1 << i);
					}
					data[offset++] = b;
				}
			}
		}
	}

	// translate the 24 bit color from RGB to the actual
	// order used by the LED wiring. GRB is the most common.
	int colorWiring(int c) {
		int red = (c & 0xFF0000) >> 16;
		int green = (c & 0x00FF00) >> 8;
		int blue = (c & 0x0000FF);
		red = gammatable[red];
		green = gammatable[green];
		blue = gammatable[blue];
		return (green << 16) | (red << 8) | (blue); // GRB - most common wiring
	}

	// ask a Teensy board for its LED configuration, and set up the info for it.
	void serialConfigure(PApplet applet, String portName) {
		if (numPorts >= maxPorts) {
			System.out
					.println("too many serial ports, please increase maxPorts");
			errorCount++;
			return;
		}
		try {
			ledSerial[numPorts] = new Serial(applet, portName);
			if (ledSerial[numPorts] == null)
				throw new NullPointerException();
			ledSerial[numPorts].write('?');
		} catch (Throwable e) {
			System.out.println("Serial port " + portName
					+ " does not exist or is non-functional");
			errorCount++;
			return;
		}
		applet.delay(50);
		String line = ledSerial[numPorts].readStringUntil(10);
		if (line == null) {
			System.out.println("Serial port " + portName
					+ " is not responding.");
			System.out
					.println("Is it really a Teensy 3.0 running VideoDisplay?");
			errorCount++;
			return;
		}
		String param[] = line.split(",");
		if (param.length != 12) {
			System.out.println("Error: port " + portName
					+ " did not respond to LED config query");
			errorCount++;
			return;
		}
		// only store the info and increase numPorts if Teensy responds properly
		ledImage[numPorts] = new PImage(Integer.parseInt(param[0]),
				Integer.parseInt(param[1]), PApplet.RGB);
		ledArea[numPorts] = new Rectangle(Integer.parseInt(param[5]),
				Integer.parseInt(param[6]), Integer.parseInt(param[7]),
				Integer.parseInt(param[8]));
		ledLayout[numPorts] = (Integer.parseInt(param[5]) == 0);
		numPorts++;
	}

	// scale a number by a percentage, from 0 to 100
	int percentage(int num, int percent) {
		double mult = percentageFloat(percent);
		double output = num * mult;
		return (int) output;
	}

	// scale a number by the inverse of a percentage, from 0 to 100
	int percentageInverse(int num, int percent) {
		double div = percentageFloat(percent);
		double output = num / div;
		return (int) output;
	}

	// convert an integer from 0 to 100 to a float percentage
	// from 0.0 to 1.0. Special cases for 1/3, 1/6, 1/7, etc
	// are handled automatically to fix integer rounding.
	double percentageFloat(int percent) {
		if (percent == 33)
			return 1.0 / 3.0;
		if (percent == 17)
			return 1.0 / 6.0;
		if (percent == 14)
			return 1.0 / 7.0;
		if (percent == 13)
			return 1.0 / 8.0;
		if (percent == 11)
			return 1.0 / 9.0;
		if (percent == 9)
			return 1.0 / 11.0;
		if (percent == 8)
			return 1.0 / 12.0;
		return (double) percent / 100.0;
	}

}
