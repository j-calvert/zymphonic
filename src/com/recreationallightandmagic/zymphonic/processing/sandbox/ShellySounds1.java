package com.recreationallightandmagic.zymphonic.processing.sandbox;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import ddf.minim.AudioSample;
import ddf.minim.Minim;

public class ShellySounds1 extends RegionSoundMap {

	public ShellySounds1(int width, int height, int depth) {
		super(width, height, depth);
	}

	public void setup(PApplet applet) {

		Minim minim = new Minim(applet);

		loadColumn(minim, 0, 0, "Piano1_A1.mp3", "Piano2_C2.mp3",
				"Piano3_E2.mp3", "Piano4_F2.mp3", "Piano5_G2.mp3",
				"Piano6_A2.mp3", "Piano1_A1.mp3", "Piano2_C2.mp3");

		loadColumn(minim, 1, 0, "Bass1_A1.mp3", "Bass2_C2.mp3", "Bass3_E2.mp3",
				"Bass4_F2.mp3", "Bass5_G2.mp3", "Bass6_A2.mp3", "Bass1_A1.mp3",
				"Bass2_C2.mp3");

		/*
		 * loadColumn(minim, 0, 1, "thunder1.mp3", "thunder2.mp3",
		 * "thunder3.mp3", "thunder4.mp3", "thunder5.mp3", "thunder6.mp3",
		 * "thunder7.mp3", "thunder8.mp3");
		 */

		loadColumn(minim, 0, 1, "Fatigue.mp3", "Fear.mp3", "Hostility.mp3",
				"eerie.mp3", "Serenity.mp3", "Positivity.mp3", "Joviality.mp3",
				"Fatigue.mp3");

		loadColumn(minim, 1, 1, "Fatigue.mp3", "Fear.mp3", "Hostility.mp3",
				"eerie.mp3", "Serenity.mp3", "Positivity.mp3", "Joviality.mp3",
				"Fatigue.mp3");

		loadFilesIntoArray(minim, pads, "thunder1.mp3", "thunder2.mp3",
				"thunder3.mp3", "thunder4.mp3");
		loadFilesIntoArray(minim, ends, "thunder1.mp3", "thunder2.mp3",
				"thunder3.mp3", "thunder4.mp3");
//		loadFilesIntoArray(minim, ends, "Serenity.mp3", "Positivity.mp3",
//				"Joviality.mp3", "Fatigue.mp3");

		System.out.println("Done setting up sound");
	}

	private void loadColumn(Minim minim, int x, int y, String... filenames) {
		if (filenames.length != samples[0][0].length) {
			throw new RuntimeException(
					"Must call loadColumn with the correct number of files for the depth of the soundMap");
		}
		loadFilesIntoArray(minim, samples[x][y], filenames);
	}

	private void loadFilesIntoArray(Minim minim, AudioSample[] array,
			String... filenames) {
		for (int z = 0; z < filenames.length; z++) {
			array[z] = getSample(minim, filenames[z]);
		}
	}

	/*
	 * Seems we run out of memory (and program dies with a misleading error
	 * message), so we reuse instances of these things.
	 */
	Map<String, AudioSample> sampleInstances = new HashMap<String, AudioSample>();

	private AudioSample getSample(Minim minim, String filename) {
		if (!sampleInstances.containsKey(filename)) {
			// For the love of god, leave the buffer in.
			sampleInstances.put(filename, minim.loadSample(filename, 9048));
		}
		return sampleInstances.get(filename);
	}

}