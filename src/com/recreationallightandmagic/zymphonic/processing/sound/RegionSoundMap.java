package com.recreationallightandmagic.zymphonic.processing.sound;

import static com.recreationallightandmagic.zymphonic.processing.Constants.correctNegMod;
import processing.core.PApplet;
import ddf.minim.AudioSample;

public abstract class RegionSoundMap {

	// Make it public so we can poke in new values with bean shell
	public AudioSample[][][] samples;
	public AudioSample[] pads;
 	public AudioSample[] ends;
 
	public RegionSoundMap(int width, int height, int depth) {
		samples = new AudioSample[width][height][depth];
		pads = new AudioSample[4];
	 	ends = new AudioSample[4];		
	}
	
	// Cycle, for values bigger or smaller than our array indexes
	public AudioSample getSample(int x, int y, int z) {
		return samples[correctNegMod(x, samples.length)][correctNegMod(y,
				samples[0].length)][correctNegMod(z, samples[0][0].length)];
	}

	public AudioSample getEnd(int userId){
		return ends[userId % ends.length];
	}
	public AudioSample getPad(int userId){
		return pads[userId % pads.length];
	}
	
	public void close() {
		
		for (int p = 0; p < pads.length; p++){
			pads[p].close();
			}
		for (int e = 0; e < ends.length; e++){
			ends[e].close();
			}
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
