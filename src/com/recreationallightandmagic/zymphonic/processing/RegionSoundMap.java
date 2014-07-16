package com.recreationallightandmagic.zymphonic.processing;

import static com.recreationallightandmagic.zymphonic.processing.Constants.correctNegMod;
import processing.core.PApplet;
import ddf.minim.AudioSample;

public abstract class RegionSoundMap {

	// Make it public so we can poke in new values with bean shell
	public AudioSample[][][] samples;

	public RegionSoundMap(int width, int height, int depth) {
		samples = new AudioSample[width][height][depth];
	}

	// Cycle, for values bigger or smaller than our array indexes
	public AudioSample getSample(int x, int y, int z) {
		return samples[correctNegMod(x, samples.length)][correctNegMod(y,
				samples[0].length)][correctNegMod(z, samples[0][0].length)];
	}


	public void close() {
		for (int x = 0; x < samples.length; x++) {
			for (int y = 0; y < samples[0].length; y++) {
				for (int z = 0; z < samples[0][0].length; z++) {
					samples[x][y][z].close();
				}
			}
		}
	}
	
	public abstract void setup(PApplet applet);
}
