/*  Butched from OctoWS2811 movie2serial.pde - Transmit video data to 1 or more
Teensy 3.0 boards running OctoWS2811 VideoDisplay.ino
http://www.pjrc.com/teensy/td_libs_OctoWS2811.html
Copyright (c) 2013 Paul Stoffregen, PJRC.COM, LLC
 */

//To configure this program, edit the following sections:
//
//1: change myMovie to open a video file of your choice    ;-)
//
//2: edit the serialConfigure() lines in setup() for your
//   serial device names (Mac, Linux) or COM ports (Windows)
//
//3: if your LED strips have unusual color configuration,
//   edit colorWiring().  Nearly all strips have GRB wiring,
//   so normally you can leave this as-is.
//
//4: if playing 50 or 60 Hz progressive video (or faster),
//   edit framerate in movieEvent().

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PImage;
import processing.serial.Serial;

public class ZymphonicRenderer extends PApplet {
	private static final long serialVersionUID = 1L;
	// Movie myMovie = new Movie(this, "/home/jcalvert/Downloads/bird.avi");
	// Doesn't quite work...

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

	public void setup() {
		String[] list = Serial.list();
		// delay(20);
		System.out.println("Serial Ports List:");
		System.out.println(Arrays.toString(list));
		serialConfigure("/dev/ttyACM0"); // change these to your port names
		// serialConfigure("/dev/ttyACM1");
		if (errorCount > 0)
			exit();
		for (int i = 0; i < 256; i++) {
			gammatable[i] = (int) (Math.pow((float) i / 255.0, gamma) * 255.0 + 0.5);
		}
		size(480, 400); // create the window

		// myMovie.loop(); // start the movie :-)
	}

	// movieEvent runs for each new frame of movie data
	void movieEvent(PImage m) {
		// read the movie's next frame (from if m were declared as a Movie)
		// m.read();

		// if (framerate == 0) framerate = m.getSourceFrameRate();
		framerate = 30.0f; // TODO, how to read the frame rate???

		for (int i = 0; i < numPorts; i++) {
			// copy a portion of the movie's image to the LED image
			int xoffset = percentage(m.width, ledArea[i].x);
			int yoffset = percentage(m.height, ledArea[i].y);
			int xwidth = percentage(m.width, ledArea[i].width);
			int yheight = percentage(m.height, ledArea[i].height);
			ledImage[i].copy(m, xoffset, yoffset, xwidth, yheight, 0, 0,
					ledImage[i].width, ledImage[i].height);
			// convert the LED image to raw data
			byte[] ledData = new byte[(ledImage[i].width * ledImage[i].height * 3) + 3];
			image2data(ledImage[i], ledData, ledLayout[i]);
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
	void serialConfigure(String portName) {
		if (numPorts >= maxPorts) {
			println("too many serial ports, please increase maxPorts");
			errorCount++;
			return;
		}
		try {
			ledSerial[numPorts] = new Serial(this, portName);
			if (ledSerial[numPorts] == null)
				throw new NullPointerException();
			ledSerial[numPorts].write('?');
		} catch (Throwable e) {
			println("Serial port " + portName
					+ " does not exist or is non-functional");
			errorCount++;
			return;
		}
		delay(50);
		String line = ledSerial[numPorts].readStringUntil(10);
		if (line == null) {
			println("Serial port " + portName + " is not responding.");
			println("Is it really a Teensy 3.0 running VideoDisplay?");
			errorCount++;
			return;
		}
		String param[] = line.split(",");
		if (param.length != 12) {
			println("Error: port " + portName
					+ " did not respond to LED config query");
			errorCount++;
			return;
		}
		// only store the info and increase numPorts if Teensy responds properly
		ledImage[numPorts] = new PImage(Integer.parseInt(param[0]),
				Integer.parseInt(param[1]), RGB);
		ledArea[numPorts] = new Rectangle(Integer.parseInt(param[5]),
				Integer.parseInt(param[6]), Integer.parseInt(param[7]),
				Integer.parseInt(param[8]));
		ledLayout[numPorts] = (Integer.parseInt(param[5]) == 0);
		numPorts++;
	}

	// draw runs every time the screen is redrawn - show the movie...
	public void draw() {

		Random rand = new Random();
		PImage movieImage = new PImage(240, 8);
		
		for (int x = 0; x < 240; x++) {
			for (int y = 0; y < 8; y++) {		
				movieImage.set(x, y, rand.nextInt(1));
			}
		}
		// then try to show what was most recently sent to the LEDs
		// by displaying all the images for each port.
		movieEvent(movieImage);
		// Uncomment for seizure inducing strobe effect
//		for (int x = 0; x < 240; x++) {
//			for (int y = 0; y < 8; y++) {		
//				movieImage.set(x, y, 0);
//			}
//		}
//		// then try to show what was most recently sent to the LEDs
//		// by displaying all the images for each port.
//		movieEvent(movieImage);
		for (int i = 0; i < numPorts; i++) {
			// compute the intended size of the entire LED array
			int xsize = percentageInverse(ledImage[i].width, ledArea[i].width);
			int ysize = percentageInverse(ledImage[i].height, ledArea[i].height);
			// computer this image's position within it
			int xloc = percentage(xsize, ledArea[i].x);
			int yloc = percentage(ysize, ledArea[i].y);
			// show what should appear on the LEDs
			image(ledImage[i], 240 - xsize / 2 + xloc, 10 + yloc);
		}
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
