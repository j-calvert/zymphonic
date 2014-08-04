package com.recreationallightandmagic.zymphonic.processing.lights;

import java.util.Random;

import processing.core.PImage;

public class Rainbow {
	private int width;
	private int offset = 0;
	private int[] rainbowColors = new int[180];

	public Rainbow(int width) {
		this.width = width;
		for (int i = 0; i < 180; i++) {
			int hue = i * 2;
			int saturation = 100;
			int lightness = 10;
			// pre-compute the 180 rainbow colors
			rainbowColors[i] = makeColor(hue, saturation, lightness);
		}
	}

	Random rand = new Random();

	public void rainbow(PImage image) {
		offset = (offset + 5) % 50000;
		for (int x = 0; x < width; x++) {
			int index = (offset + x) % 180;
			// for (int y = 0; y < 8; y++) {
			// image.set(x, y, applet.color(rand.nextInt(255) / 4,
			// rand.nextInt(255) / 4,
			// rand.nextInt(255) / 4));
			// image.set(x, Constants.BOTTOM_LEFT, rainbowColors[index]);
			// image.set(x, Constants.BOTTOM_RIGHT, rainbowColors[index]);
			// }
		}
	}

	int makeColor(int hue, int saturation, int lightness) {
		int red, green, blue;
		int var1, var2;

		if (hue > 359)
			hue = hue % 360;
		if (saturation > 100)
			saturation = 100;
		if (lightness > 100)
			lightness = 100;

		// algorithm from: http://www.easyrgb.com/index.php?X=MATH&H=19#text19
		if (saturation == 0) {
			red = green = blue = lightness * 255 / 100;
		} else {
			if (lightness < 50) {
				var2 = lightness * (100 + saturation);
			} else {
				var2 = ((lightness + saturation) * 100)
						- (saturation * lightness);
			}
			var1 = lightness * 200 - var2;
			red = h2rgb(var1, var2, (hue < 240) ? hue + 120 : hue - 240) * 255 / 600000;
			green = h2rgb(var1, var2, hue) * 255 / 600000;
			blue = h2rgb(var1, var2, (hue >= 120) ? hue - 120 : hue + 240) * 255 / 600000;
		}
		return (red << 16) | (green << 8) | blue;
	}

	int h2rgb(int v1, int v2, int hue) {
		if (hue < 60)
			return v1 * 60 + (v2 - v1) * hue;
		if (hue < 180)
			return v2 * 60;
		if (hue < 240)
			return v1 * 60 + (v2 - v1) * (240 - hue);
		return v1 * 60;
	}

}
