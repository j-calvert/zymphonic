package com.recreationallightandmagic.zymphonic.processing;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import SimpleOpenNI.SimpleOpenNI;

import com.recreationallightandmagic.zymphonic.processing.input.DepthHistoryRegion;
import com.recreationallightandmagic.zymphonic.processing.input.Kinect;
import com.recreationallightandmagic.zymphonic.processing.lights.LEDs;

/**
 * A mid-level PApplet class that runs everything not having to do with admin
 * (GUI) user input
 */
public class WormholeCore extends PApplet {

	private static final long serialVersionUID = 1L;

	public static final boolean TRACK_USERS = false;
	public static final int MAX_SIMUL_USERS = 10;

	protected LEDs lights; // The low level LEDs
	protected Kinect kinect; // The Kinect
	// activeHistories indexed by kinect UserID if and only if it is active
	private DepthHistoryRegion[] activeHistories = new DepthHistoryRegion[MAX_SIMUL_USERS];
	// pastHistories enqueued upon being nulled out of the activeHistories
	// array. Tail of queue are most recent.
	private Deque<DepthHistoryRegion> pastHistories = new ArrayDeque<DepthHistoryRegion>();
	// Regions!
	/*
	 * public float startingDepth, heightOffset, depthPerSection,
	 * widthPerSection, heightPerSection;
	 */

	// with split screen at 0, bass vs. piano:
	float xinches = 1;
	// with split screen at 0, zipline cut off:
	float yinches = 1;
	// inches per depthzone:
	float zinches = 24;

	float pixperinch = (float) 25.4;
	float xpixels = xinches * pixperinch;
	float ypixels = yinches * pixperinch;
	float zpixels = zinches * pixperinch;

	// I think we're going with a whole new regions model
	// Regions regions = new Regions(xpixels, ypixels, zpixels);
	// // And the sounds they map to
	// RegionSoundMap soundMap = new ShellySounds1(2, 2, 8);

	// private Rainbow rainbow = new Rainbow(960);
	// private Sparkle sparkle = new Sparkle();

	// CP5ToggleExample cp5Panel = new CP5ToggleExample();

	@Override
	public void setup() {
		super.setup();

		// Initialize our core components: lights, kinect, and sound
		lights = new LEDs(this);
		kinect = new Kinect(this);

		// Sound stuff
		// soundMap.setup(this);

	}

	@Override
	public InputStream createInput(String fileName) {
		return super.createInput(Constants.SAMPLE_DIRECTORY + fileName);
	}

	@Override
	public void draw() {
		kinect.draw(this);
		lights.renderLights();

		updateUserHistories();

		// testFlashLights();
		playTones();

	}

	private void playTones() {
		// baselineImage(image);
		// for (int kinectUserNum = 0; kinectUserNum < activeHistories.length;
		// kinectUserNum++) {
		// if (activeHistories[kinectUserNum] != null) {
		// PVector head = activeHistories[kinectUserNum].getHead();
		// // SectionId sectionId = regions.getSectionId(head);
		// int leftOrRight = sectionId.x % 2 == 0 ? Constants.LEFT :
		// Constants.RIGHT;
		// SectionId prevId = regions
		// .getSectionId(activeHistories[kinectUserNum]
		// .getHeadOffset(1));
		//
		// if (sectionId != null) {
		// if (prevId == null || !sectionId.equals(prevId)) {
		// if (DEBUG) {
		// System.out
		// .println("Generating sound for kinectUser: "
		// + kinectUserNum
		// + " in "
		// + sectionId);
		// }
		//
		// AudioSample sample = soundMap.getSample(sectionId.x,
		// sectionId.y, sectionId.z);
		// if (sample != null) {
		// sample.trigger();
		// // some flare
		// addToImage(image, head, 10, kinectUserNum + 5, leftOrRight);
		//
		// } else if (DEBUG) {
		// System.out
		// .println("Nevermind, the sample map gave us a null sample");
		// }
		// }
		// }
		// addToImage(image, head, 5, kinectUserNum, leftOrRight);
		// }
		// }
		// // lights.renderLights(image);

	}

	private void addToImage(PImage image, PVector head, int width, int userId,
			int leftOrRight) {
		int d = translateToDepth(head.z);
		for (int i = d - width; i < d + width; i++) {
			int color = Constants.basicColors[(userId - 1)
					% Constants.basicColors.length];
			image.set(i, leftOrRight, color);
		}
		// sparkle.sparkle(image, d, width, leftOrRight);
	}

	// 60 LEDs per meter
	private int translateToDepth(float z) {
		return (int) (((z / 1000f) + 2f) * 60f);
	}

	private synchronized void updateUserHistories() {
		// int[] kinectUserIds = kinect.kinect.getUsers();
		// for (int i = 0; i < kinectUserIds.length; i++) {
		// if (kinectUserIds[i] >= activeHistories.length) {
		// // We only track a fixed number of user histories and Kinect
		// // recycles its IDs
		// continue;
		// }
		// if (activeHistories[kinectUserIds[i]] != null) {
		// // Protecting against what seems to be a slight race condition
		// kinect.kinect.getCoM(kinectUserIds[i],
		// activeHistories[kinectUserIds[i]].getSetHead());
		// }
		// }
	}

	// Start Kinect callbacks
	public synchronized void onNewUser(SimpleOpenNI curContext, int userId) {
		System.out.println("onNewUser - userId: " + userId);
		System.out.println("\tstart tracking skeleton");
		// AudioSample pad = soundMap.getEnd(userId);
		// if (pad != null) {
		// pad.trigger();
		// }
	}

	public synchronized void onLostUser(SimpleOpenNI kinect, int userId) {
		pastHistories.push(activeHistories[userId]);
		activeHistories[userId] = null;
	}

	public void onVisibleUser(SimpleOpenNI kinect, int userId) {
		// PVector com = new PVector();
		// kinect.getCoM(userId, com);
		// println(com.z);
	}
	// End Kinect callbacks

}
