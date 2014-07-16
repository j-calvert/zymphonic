package com.recreationallightandmagic.zymphonic.processing;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import com.recreationallightandmagic.zymphonic.processing.Regions.SectionId;

import ddf.minim.AudioSample;
import processing.core.PApplet;
import SimpleOpenNI.SimpleOpenNI;

/**
 * The top-level PApplet class that runs the whole shebang.
 */
public class Wormhole extends PApplet {
	private static final long serialVersionUID = 1L;

	// Whether or not we're running in debug mode (i.e. showing video)
	private static final boolean DEBUG = true;

	private static final int MAX_SIMUL_USERS = 10;

	private LEDs lights; // The LEDs
	private SimpleOpenNI kinect; // The Kinect camera
	// activeHistories indexed by kinect UserID if and only if it is active
	private UserHistory[] activeHistories = new UserHistory[MAX_SIMUL_USERS];
	// pastHistories enqueued upon being nulled out of the activeHistories
	// array. Tail of queue are most recent.
	private Deque<UserHistory> pastHistories = new ArrayDeque<UserHistory>();
	// Regions!
	Regions regions = new Regions(500f, 0f, 500f, 20000f, 20000f);
	// And the sounds they map to
	RegionSoundMap soundMap = new ShellySounds1(2, 2, 8);

	@Override
	public void setup() {
		super.setup();

		// lights = new LEDs();
		// lights.setup(this);

		// Kinect stuff
		kinect = new SimpleOpenNI(this);
		if (kinect.isInit() == false) {
			throw new RuntimeException(
					"Can't init SimpleOpenNI, maybe the camera is not connected!");
		}

		// enable depthMap generation and userblob tracking
		kinect.enableDepth();
		kinect.enableUser();
		kinect.setMirror(true);
		if(DEBUG) {
			size(640, 480);
		}
		// Sound stuff
		soundMap.setup(this);
	}

	@Override
	public InputStream createInput(String fileName) {
		return super.createInput(Constants.SAMPLE_DIRECTORY + fileName);
	}

	@Override
	public void draw() {
		kinect.update();
		updateUserHistories();
		// TODO Write this
		// drawImage(activeHistories);
		playTones();
		if (DEBUG) {
			KinectDebugger.debug(this, kinect);
		}
	}

	private void playTones() {
		for (int kinectUserNum = 0; kinectUserNum < activeHistories.length; kinectUserNum++) {
			if (activeHistories[kinectUserNum] != null) {
				SectionId sectionId = regions
						.getSectionId(activeHistories[kinectUserNum].getHead());
				SectionId prevId = regions
						.getSectionId(activeHistories[kinectUserNum]
								.getHeadOffset(1));
				if (sectionId != null) {
					if (prevId == null || !sectionId.equals(prevId)) {
						if (DEBUG) {
							System.out
									.println("Generating sound for kinectUser: "
											+ kinectUserNum
											+ " in "
											+ sectionId);
						}
						
						AudioSample sample = soundMap.getSample(sectionId.x, sectionId.y,
								sectionId.z);
						if(sample != null) {
							sample.trigger();
						} else if(DEBUG) {
							System.out.println("Nevermind, the sample map gave us a null sample");
						}
					}
				}
			}
		}
	}

	//
	// private void drawLightBall(int kinectUserNum, PVector userCoM) {
	// for (int stripNum = 0; stripNum < NUM_LIGHT_STRIPS; stripNum++) {
	// LightBall.draw(Constants.basicColors[kinectUserNum
	// % Constants.basicColors.length],
	// radiusFromHeight(userCoM.y), depthOffset(userCoM.z),
	// lights.getStrip(stripNum));
	//
	// }
	// }

	private float depthOffset(float z) {
		return 3.4f * z + 0.5f;
	}

	private float radiusFromHeight(float y) {
		return y * 5f;
	}

	private synchronized void updateUserHistories() {
		int[] kinectUserIds = kinect.getUsers();
		for (int i = 0; i < kinectUserIds.length; i++) {
			if (kinectUserIds[i] >= activeHistories.length) {
				// We only track a fixed number of user histories and Kinect
				// recycles its IDs
				continue;
			}
			if(activeHistories[kinectUserIds[i]] != null) {
				// Protecting against what seems to be a slight race condition
				kinect.getCoM(kinectUserIds[i],
						activeHistories[kinectUserIds[i]].getSetHead());
			}
		}
	}

	public synchronized void onNewUser(SimpleOpenNI curContext, int userId) {
		System.out.println("onNewUser - userId: " + userId);
		System.out.println("\tstart tracking skeleton");
		activeHistories[userId] = new UserHistory();
	}

	public synchronized void onLostUser(SimpleOpenNI kinect, int userId) {
		pastHistories.push(activeHistories[userId]);
		activeHistories[userId] = null;
		if (DEBUG) {
			System.out.println("UserId: " + userId + " is no longer active");
		}
	}

	public void onVisibleUser(SimpleOpenNI kinect, int userId) {
		// PVector com = new PVector();
		// kinect.getCoM(userId, com);
		// println(com.z);
	}

}
