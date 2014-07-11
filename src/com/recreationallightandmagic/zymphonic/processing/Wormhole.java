package com.recreationallightandmagic.zymphonic.processing;

import processing.core.PApplet;
import processing.core.PVector;
import SimpleOpenNI.SimpleOpenNI;

/**
 * The top-level PApplet class that runs the whole shebang.
 */
public class Wormhole extends PApplet {
	private static final long serialVersionUID = 1L;

	// Whether or not we're running in debug mode (hence showing video)
	private static final boolean DEBUG = true;

	private static final int MAX_USERS = 10;

	private Lights lights; // The LEDs
	private SimpleOpenNI kinect; // The Kinect camera
	private UserHistory[] userHistories = new UserHistory[MAX_USERS];

	private static final int NUM_LIGHT_STRIPS = 8;

	@Override
	public void setup() {
		super.setup();
		for (int i = 0; i < userHistories.length; i++) {
			userHistories[i] = new UserHistory();
		}

		lights = new Lights(this, NUM_LIGHT_STRIPS, 4);

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
	public void draw() {
		kinect.update();
		updateUserHistories();
		lightUpUserHistories();
		lights.display();
		if (DEBUG) {
			KinectDebugger.debug(this, kinect);
		}
	}

	private void lightUpUserHistories() {
		for (int userNum = 0; userNum < userHistories.length; userNum++) {
			PVector userCoM = userHistories[userNum].getHead();
			if (userCoM == null) {
				continue;
			}

			for (int stripNum = 0; stripNum < NUM_LIGHT_STRIPS; stripNum++) {
				LightBall.draw(Constants.basicColors[userNum
						% Constants.basicColors.length],
						radiusFromHeight(userCoM.y), depthOffset(userCoM.z),
						lights.getStrip(stripNum));

			}
		}
	}

	private float depthOffset(float z) {
		return 3.4f * z + 0.5f;
	}

	private float radiusFromHeight(float y) {
		return y * 5f;
	}

	private void updateUserHistories() {
		int[] userList = kinect.getUsers();
		for (int i = 0; i < userList.length; i++) {
			if (userList[i] >= userHistories.length) {
				// We only track a fixed number of user histories.
				continue;
			}
			kinect.getCoM(userList[i], userHistories[userList[i] - 1].getSetHead());
		}
	}

	public void onNewUser(SimpleOpenNI curContext, int userId) {
		System.out.println("onNewUser - userId: " + userId);
		System.out.println("\tstart tracking skeleton");

		// curContext.startTrackingSkeleton(userId);
	}

	public void onLostUser(SimpleOpenNI curContext, int userId) {
		System.out.println("onLostUser - userId: " + userId);
	}

	public void onVisibleUser(SimpleOpenNI kinect, int userId) {
		// PVector com = new PVector();
		// kinect.getCoM(userId, com);
		// println(com.z);
	}

}
