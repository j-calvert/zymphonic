package com.recreationallightandmagic.zymphonic.processing;

import static com.recreationallightandmagic.zymphonic.processing.Constants.NUM_LIGHT_STRIPS;
import static com.recreationallightandmagic.zymphonic.processing.Constants.WORKSPACE;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import processing.core.PApplet;
import processing.core.PVector;
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
	private Deque<UserHistory> pastHistories = new ArrayDeque<UserHistory>();

	@Override
	public void setup() {
		super.setup();
		for (int i = 0; i < activeHistories.length; i++) {
			activeHistories[i] = new UserHistory();
		}

		lights = new LEDs();
		lights.setup(this);

		kinect = new SimpleOpenNI(this);
		if (kinect.isInit() == false) {
			throw new RuntimeException(
					"Can't init SimpleOpenNI, maybe the camera is not connected!");
		}

		// enable depthMap generation and userblob tracking
		kinect.enableDepth();
		kinect.enableUser();

		size(640, 480);
	}

	@Override
	public InputStream createInput(String fileName) {
		return super.createInput(WORKSPACE + fileName);
	}

	@Override
	public void draw() {
		kinect.update();
		updateUserHistories();
		// TODO  Write this
//		drawImage(activeHistories);
//		playTones(activeHistories);
		if (DEBUG) {
			KinectDebugger.debug(this, kinect);
		}
	}

	private void drawLightBall(int kinectUserNum, PVector userCoM) {
		for (int stripNum = 0; stripNum < NUM_LIGHT_STRIPS; stripNum++) {
			LightBall.draw(Constants.basicColors[kinectUserNum
					% Constants.basicColors.length],
					radiusFromHeight(userCoM.y), depthOffset(userCoM.z),
					lights.getStrip(stripNum));

		}
	}

	private float depthOffset(float z) {
		return 3.4f * z + 0.5f;
	}

	private float radiusFromHeight(float y) {
		return y * 5f;
	}

	private void updateUserHistories() {
		int[] kinectUserIds = kinect.getUsers();
		for (int i = 0; i < kinectUserIds.length; i++) {
			if (kinectUserIds[i] >= activeHistories.length) {
				// We only track a fixed number of user histories.
				continue;
			}
			kinect.getCoM(kinectUserIds[i],
					activeHistories[kinectUserIds[i] - 1].getSetHead());
		}
	}

	public void onNewUser(SimpleOpenNI curContext, int userId) {
		System.out.println("onNewUser - userId: " + userId);
		System.out.println("\tstart tracking skeleton");

		// curContext.startTrackingSkeleton(userId);
	}

	public void onLostUser(SimpleOpenNI kinect, int userId) {
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
