package com.recreationallightandmagic.zymphonic.processing;

/**
 * A thing that gets rendered by the Lights. Has a color and a diameter.
 */
public class LightBall {

	public static void draw(int color, float radius, float position, int[] leds) {
		int startIndex = (int) ((position - radius) / Constants.LEDS_PER_METER);
		int endIndex = (int) ((position + radius) / Constants.LEDS_PER_METER);
		for (int i = startIndex; i <= endIndex; i++) {
			if (i <= 0) {
				continue;
			}
			if (i >= leds.length) {
				break;
			}
			leds[i] = color;
		}
	}

}
