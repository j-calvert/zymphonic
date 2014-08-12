package com.recreationallightandmagic.zymphonic.processing.sound;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.recreationallightandmagic.zymphonic.processing.Constants;

import ddf.minim.AudioSample;
import ddf.minim.Minim;

/**
 * Loads samples from files and provides access to them.
 * 
 * Went hunting all variations of red herrings until I figured out that there's
 * a hard limit of 32 open samples: http://www.jsresources.org/faq_audio.html
 */
public class SoundSamples {

	Map<String, AudioSample> sampleInstances = Collections
			.synchronizedMap(new LruCache());
	String[][] sampleNameGrid;
	private Minim minim;

	private static File directory = new File(Constants.SAMPLE_DIRECTORY);

	public SoundSamples(Minim minim, int width) {
		this.minim = minim;
		String[] list = directory.list();
		Arrays.sort(list);
		sampleNameGrid = new String[width][list.length / width + 1];
		int i = 0;
		for (String file : list) {
			sampleInstances.put(file, getSample(file));
			sampleNameGrid[i % width][i / width] = file;
			i++;
			System.out.println("Loaded sample " + i + " " + file);
		}
	}

	public AudioSample getSample(String filename) {
		if (!sampleInstances.containsKey(filename)) {
			sampleInstances.put(filename, minim.loadSample(filename));
		}
		return sampleInstances.get(filename);
	}

	public String[][] getNameGrid() {
		return sampleNameGrid;
	}

	// Courtesy of
	// http://stackoverflow.com/questions/221525/how-would-you-implement-an-lru-cache-in-java-6
	private class LruCache extends LinkedHashMap<String, AudioSample> {
		private static final long serialVersionUID = 1L;
		private static final int SIZE = 30;

		public LruCache() {
			super(SIZE + 1, 1.0f, true);
		}

		@Override
		protected boolean removeEldestEntry(
				final Map.Entry<String, AudioSample> eldest) {
			if (super.size() > SIZE) {
				eldest.getValue().close();
				return true;
			}
			return false;
		}
	}

	public void stopAll() {
		for (AudioSample sample : sampleInstances.values()) {
			sample.stop();
		}
	}
}
