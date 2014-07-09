package com.recreationallightandmagic.zymphonic.processing;

import processing.core.PApplet;
import processing.core.PVector;
import SimpleOpenNI.SimpleOpenNI;
import ddf.minim.AudioOutput;
import ddf.minim.AudioPlayer;
import ddf.minim.AudioSample;
import ddf.minim.Minim;
import ddf.minim.signals.SineWave;
import ddf.minim.ugens.ADSR;
import ddf.minim.ugens.Delay;
import ddf.minim.ugens.Instrument;
import ddf.minim.ugens.Oscil;
import ddf.minim.ugens.Waves;

public class UserSounds extends PApplet {
	private static final long serialVersionUID = 1L;

	SimpleOpenNI kinect;
	static int[] userClr;
	PVector com = new PVector();
	PVector com2d = new PVector();

	personBlob[] personBlobs;
	int[] personKtoZ;

	int frame = 0;

	//to be integrated into personblob
	double[] lastdistance = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	double[] lastxdistance = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	AudioPlayer auFear, auPositivity, auHum, auBeat, auSet, auSonic, auComet,
			auThunder, auDrumJack;
	AudioSample auKick, auCello;
	AudioOutput out;
	Oscil wave;
	SineWave sine;
	int userId_z;

	Minim minim;

	public void setup() {
		userClr = new int[] { color(255, 0, 0), color(0, 255, 0),
				color(0, 0, 255), color(255, 255, 0), color(255, 0, 255),
				color(0, 255, 255) };
		size(640, 480);
		minim = new Minim(this);
		auFear = loadMinimFile("Fear.wav");
		auPositivity = loadMinimFile("Positivity.wav");
		auHum = loadMinimFile("hum2.wav");
		auKick = minim.loadSample("/home/shelly/workspace/zymphonic/" + "Kick1.wav");
		auCello = minim.loadSample("/home/shelly/workspace/zymphonic/" + "cello2.wav");
		auBeat = loadMinimFile("beat.wav");
		auSet = loadMinimFile("setunium.wav");
		auSonic = loadMinimFile("sonicboom.wav");
		auComet = loadMinimFile("comet.wav");
		auThunder = loadMinimFile("thunder.wav");
		auDrumJack = loadMinimFile("DrumJack.wav");
		auHum.loop();
		auKick.trigger();
		auBeat.loop();
		out = minim.getLineOut();
		userId_z = 0;
		int maxblobs = 100;
		personBlobs = new personBlob[maxblobs];
		personKtoZ = new int[maxblobs];

		//synthesizer hello world
		out.setTempo(60);

		out.playNote(0, 1, new SineInstrument(30));
		out.playNote(1, 1, new SineInstrument(110));
		out.playNote(2, 1, new SineInstrument(220));
		out.playNote(3, 1, new SineInstrument(440));
		out.playNote(4, 1, new SineInstrument(540));
		wave = new Oscil(440, 0.5f, Waves.SINE);
		wave.patch(out);

		kinect = new SimpleOpenNI(this);
		if (kinect.isInit() == false) {
			println("Can't init SimpleOpenNI, maybe the camera is not connected!");
			exit();
			return;
		}

		// enable depthMap generation
		kinect.enableDepth();

		// enable skeleton generation for all joints
		kinect.enableUser();

		background(200, 0, 0);

		stroke(0, 0, 255);
		strokeWeight(3);
		smooth();
	}

	private AudioPlayer loadMinimFile(String filename) {
		return minim.loadFile("/home/shelly/workspace/zymphonic/" + filename);
	}

	public void draw() {
		// if (!auHum.isPlaying()){
		// auHum.rewind();
		// }
		// auHum.play();

		// update the cam
		frame = frame + 1;

		kinect.update();

		// draw depthImageMap
		// image(context.depthImage(),0,0);
		image(kinect.userImage(), 0, 0);

		// draw the skeleton if it's available
		int[] userList = kinect.getUsers();

		for (int i = 0; i < userId_z; i++) {
			
			//check for each z user, if an active kinect user 
			if (personBlobs[i].puserId != 99) {

				int currentId = personBlobs[i].puserId;
				
				//skeleton stuff -- good for debugging and fun but we don't need
				if (kinect.isTrackingSkeleton(currentId)) {
					stroke(personBlobs[currentId].personClr);
					// stroke(userClr[ (userList[i] - 1) % userClr.length ] );
					drawSkeleton(currentId);

					PVector rightHand = new PVector();
					float confidence = kinect.getJointPositionSkeleton(
							currentId, SimpleOpenNI.SKEL_LEFT_HAND, rightHand);
					PVector convertedRightHand = new PVector();
					kinect.convertRealWorldToProjective(rightHand,
							convertedRightHand);
					float ellipseSize = map(convertedRightHand.z, 400, 2000,
							100, 10);
					fill(255, 0, 0);
					ellipse(convertedRightHand.x, convertedRightHand.y,
							ellipseSize, ellipseSize);

					PVector leftHand = new PVector();
					float confidence3 = kinect.getJointPositionSkeleton(
							currentId, SimpleOpenNI.SKEL_RIGHT_HAND, leftHand);
					PVector convertedLeftHand = new PVector();
					kinect.convertRealWorldToProjective(leftHand,
							convertedLeftHand);
					float ellipseSize3 = map(convertedLeftHand.z, 400, 2000,
							100, 10);
					fill(255, 0, 0);
					ellipse(convertedLeftHand.x, convertedLeftHand.y,
							ellipseSize3, ellipseSize3);

					PVector torso = new PVector();
					float confidence2 = kinect.getJointPositionSkeleton(
							currentId, SimpleOpenNI.SKEL_TORSO, torso);
					PVector convertedTorso = new PVector();
					kinect.convertRealWorldToProjective(torso, convertedTorso);
					float ellipseSize2 = map(convertedTorso.z, 400, 2000, 100,
							10);
					fill(255, 0, 0);
					ellipse(convertedTorso.x, convertedTorso.y, ellipseSize2,
							ellipseSize2);

				}

				// draw the center of mass
				if (kinect.getCoM(currentId, com)) {

					kinect.convertRealWorldToProjective(com, com2d);
					stroke(100, 255, 0);
					strokeWeight(1);
					beginShape(LINES);
					vertex(com2d.x, com2d.y - 5);
					vertex(com2d.x, com2d.y + 5);

					vertex(com2d.x - 5, com2d.y);
					vertex(com2d.x + 5, com2d.y);
					endShape();

					fill(0, 255, 100);
					text(Integer.toString(currentId), com2d.x, com2d.y);

					//should be right
					double inches = com.z / 25.4;
					
					//probably wrong?
					double xinches = com.x / 25.4;

					// TODO add y
					//TODO add com to frame history of personBlob
					
					println(currentId + " at " + inches + " inches");

					playUserSound(currentId, xinches, inches);
				}
			}// if personBlob is an active user
		}// for each personBLob

		stroke(255);
		strokeWeight(1);

		// draw the waveform of the output
		for (int i = 0; i < out.bufferSize() - 1; i++) {
			line(i, 50 - out.left.get(i) * 50, i + 1,
					50 - out.left.get(i + 1) * 50);
			line(i, 150 - out.right.get(i) * 50, i + 1,
					150 - out.right.get(i + 1) * 50);
		}

		// draw the waveform we are using in the oscillator
		stroke(128, 0, 0);
		strokeWeight(4);
		for (int i = 0; i < width - 1; ++i) {
			point(i,
					(float) (height / 2 - (height * 0.49)
							* wave.getWaveform().value((float) i / width)));
		}

	}

	void playUserSound(int userId, double xinches, double inches) {
		// println("in play user sound");

		// println("playing user " + userId + " sound,  " + distance + " inches"
		// + " last distance " + lastdistance[userId]);

		if (!auFear.isPlaying()) {
			auFear.rewind();
		}
		auFear.play();

		float freq = map((float) inches, 30, 200, 220, 480);

		//todo convert lastdistance into person class
		float movement = abs( (float) (inches - lastdistance[userId]));
		float movementx = abs((float) (xinches - lastxdistance[userId]));
	
		if (lastdistance[userId] < 40 &&  inches > 40) {
			if (!auDrumJack.isPlaying()) {
				auDrumJack.rewind();
			}
			auDrumJack.play();
		}
		if (lastdistance[userId] < 50 &&  inches > 50) {
			if (!auComet.isPlaying()) {
				auComet.rewind();
			}
			auComet.play();
		}
		if (lastdistance[userId] < 100 &&  inches > 100) {
			if (!auThunder.isPlaying()) {
				auThunder.rewind();
			}
			auThunder.play();
		}

		if (movement > 6 || movementx > 6) {
			// println("playing user " + userId + " sound  " + distance +
			// " inches" + " last distance " + lastdistance[userId]);
			// auKick.trigger();
			// auCello.trigger();
			out.playNote(0, 2, new SineInstrument(freq));

			lastdistance[userId] =  inches;
			lastxdistance[userId] = xinches;
		}

		// auKick.trigger();

		// map(distance, 20, 140, 0, 1);
		// wave.setAmplitude( amp );

		// println(freq);
		// wave.setFrequency( freq );

	}

	void stopUserSound(int userId) {
		auFear.pause();
	}

	public void mouseMoved() {
		// usually when setting the amplitude and frequency of an Oscil
		// you will want to patch something to the amplitude and frequency
		// inputs
		// but this is a quick and easy way to turn the screen into
		// an x-y control for them.

		
		///from the sample we can remove  for testing (oscillator) without having to move around
		float amp = map(mouseY, 0, height, 1, 0);
		wave.setAmplitude(amp);

		float freq = map(mouseX, 0, width, 110, 880);
		wave.setFrequency(freq);
	}

	// draw the skeleton with the selected joints
	void drawSkeleton(int userId) {
		// to get the 3d joint data
		/*
		 * PVector jointPos = new PVector();
		 * context.getJointPositionSkeleton(userId
		 * ,SimpleOpenNI.SKEL_NECK,jointPos); println(jointPos);
		 */

		kinect.drawLimb(userId, SimpleOpenNI.SKEL_HEAD, SimpleOpenNI.SKEL_NECK);

		kinect.drawLimb(userId, SimpleOpenNI.SKEL_NECK,
				SimpleOpenNI.SKEL_LEFT_SHOULDER);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER,
				SimpleOpenNI.SKEL_LEFT_ELBOW);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_ELBOW,
				SimpleOpenNI.SKEL_LEFT_HAND);

		kinect.drawLimb(userId, SimpleOpenNI.SKEL_NECK,
				SimpleOpenNI.SKEL_RIGHT_SHOULDER);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER,
				SimpleOpenNI.SKEL_RIGHT_ELBOW);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_ELBOW,
				SimpleOpenNI.SKEL_RIGHT_HAND);

		kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER,
				SimpleOpenNI.SKEL_TORSO);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER,
				SimpleOpenNI.SKEL_TORSO);

		kinect.drawLimb(userId, SimpleOpenNI.SKEL_TORSO,
				SimpleOpenNI.SKEL_LEFT_HIP);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_HIP,
				SimpleOpenNI.SKEL_LEFT_KNEE);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_KNEE,
				SimpleOpenNI.SKEL_LEFT_FOOT);

		kinect.drawLimb(userId, SimpleOpenNI.SKEL_TORSO,
				SimpleOpenNI.SKEL_RIGHT_HIP);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_HIP,
				SimpleOpenNI.SKEL_RIGHT_KNEE);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_KNEE,
				SimpleOpenNI.SKEL_RIGHT_FOOT);
	}

	// -----------------------------------------------------------------
	// SimpleOpenNI events

	void onNewUser(SimpleOpenNI curContext, int userId) {
		println("onNewUser - userId: " + userId);
		println("\tstart tracking skeleton");

		if (!auSet.isPlaying()) {
			auSet.rewind();
		}
		auSet.play();

		curContext.startTrackingSkeleton(userId);

		personBlob tempBlob = new personBlob(userId, userId_z);

		personBlobs[userId_z] = tempBlob;

		// println("array is " + personBlobs.length);

		personKtoZ[userId] = userId_z;

		userId_z = userId_z + 1;

		// println("new person in array: " + personBlobs[userId_z].userId);

		/* println("new ktoz in array: " + userId); */

		/* println("new person: " + personKtoZ[userId]); */
		// userId_z = userId_z + 1;
		println("userId_z" + userId_z);

	}

	void onLostUser(SimpleOpenNI curContext, int userId) {
		if (!auSonic.isPlaying()) {
			auSonic.rewind();
		}
		auSonic.play();

		println("onLostUser - userId: " + userId);
		stopUserSound(userId);

		int pblobz = personKtoZ[userId];
		personBlobs[pblobz].puserId = 99;

	}

	void onVisibleUser(SimpleOpenNI curContext, int userId) {
		// println("onVisibleUser - userId: " + userId);
	}

	//mostly here for testing stuff; irrelevant
	public void keyPressed() {
		switch (key) {
		case ' ':
			kinect.setMirror(!kinect.mirror());
			break;
		case 'f':
			if (!auFear.isPlaying()) {
				auFear.rewind();
			}
			auFear.play();
			println("pressed t");
			break;
		case 'p':
			if (!auPositivity.isPlaying()) {
				auPositivity.rewind();
			}
			auPositivity.play();
			println("pressed y");
			break;
		case '1':
			wave.setWaveform(Waves.SINE);
			break;
		case '2':
			wave.setWaveform(Waves.TRIANGLE);
			break;
		case '3':
			wave.setWaveform(Waves.SAW);
			break;
		case '4':
			wave.setWaveform(Waves.SQUARE);
			break;
		case '5':
			wave.setWaveform(Waves.QUARTERPULSE);
			break;
		}
	}

	//need for closing sound
	public void stop() {
		auFear.close();
		auPositivity.close();
		auHum.close();
		auKick.close();
		minim.stop();

		super.stop();

	}

	static class personBlob {

		int puserId = 99;
		int puserId_z;
		PVector[] frameLocs = new PVector[1000];
		int personClr;
		long timestamp = System.currentTimeMillis();

		personBlob(int tempuserId, int tempuserId_z) {
			puserId = tempuserId;
			puserId_z = tempuserId_z;
			// stroke(userClr[ (userList[i] - 1) % userClr.length ] );

			println("tempuserId " + tempuserId + "tempuserId_z " + tempuserId_z);
			personClr = userClr[(tempuserId - 1) % userClr.length];
			// println(personClr);
			println("in new person blob constructor " + puserId + " userId_z: "
					+ puserId_z);
		}
	}

	//from thre sample code for the synthesizer
	class SineInstrument implements Instrument {
		Oscil sineWave;
		ADSR envelope;
		Delay delay;

		SineInstrument(float frequency) {
			sineWave = new Oscil(frequency, 0.5f, Waves.SINE);
			envelope = new ADSR(10.0f, 0.001f); // max amplitude (unknown unit),
												// attack speed in seconds
			sineWave.patch(envelope);
		}

		public void noteOn(float duration) {
			envelope.noteOn();
			envelope.patch(out);
		}

		public void noteOff() {
			envelope.unpatchAfterRelease(out);
			envelope.noteOff();
		}
	}

}