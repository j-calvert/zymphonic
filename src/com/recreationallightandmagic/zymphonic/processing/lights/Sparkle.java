package com.recreationallightandmagic.zymphonic.processing.lights;

import java.util.Random;

import processing.core.PImage;

public class Sparkle {

	Random rand = new Random();

	public void sparkle(PImage image, int ledIdx, int width, int row) {
		for (int i = ledIdx - width / 2; i < ledIdx + width / 2; i++) {
			if(rand.nextInt(6) >= 5) {
				image.set(i, row, -1);
			}
		}
	}

}
