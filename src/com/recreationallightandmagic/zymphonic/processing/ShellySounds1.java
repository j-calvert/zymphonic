package com.recreationallightandmagic.zymphonic.processing;

import processing.core.PApplet;
import ddf.minim.Minim;

public class ShellySounds1 {

	RegionSoundMap soundMap = new RegionSoundMap(2, 2, 8);

	public void setup(PApplet applet) {

		Minim minim = new Minim(applet);

		loadColumn(minim, 0, 0, "Piano1_A1.wav", "Piano2_C2.wav",
				"Piano3_E2.wav", "Piano4_F2.wav", "Piano5_G2.wav",
				"Piano6_A2.wav", "Piano1_A1.wav", "Piano2_C2.wav");

		loadColumn(minim, 1, 0, "Bass1_A1.wav", "Bass2_C2.wav", "Bass3_E2.wav",
				"Bass4_F2.wav", "Bass5_G2.wav", "Bass6_A2.wav", "Bass1_A1.wav",
				"Bass2_C2.wav");

		loadColumn(minim, 1, 1, "thunder1.mp3", "thunder2.mp3", "thunder3.mp3",
				"thunder4.mp3", "thunder5.mp3", "thunder6.mp3", "thunder7.mp3",
				"thunder8.mp3");

		loadColumn(minim, 1, 1, "Fatigue.mp3", "Fear.mp3", "Hostility.mp3",
				"eerie.mp3", "Serenity.mp3", "Positivity.mp3", "Joviality.mp3",
				"End_C.mp3");

		System.out.println("Done setting up sound");
	}

	private void loadColumn(Minim minim, int x, int y, String... filenames) {
		if (filenames.length != soundMap.samples[0][0].length) {
			throw new RuntimeException(
					"Must call loadColumn with the correct number of files for the depth of the soundMap");
		}
		for (int z = 0; z < filenames.length; z++) {
			soundMap.samples[x][y][z] = minim.loadSample(filenames[z]);
		}
	}

}