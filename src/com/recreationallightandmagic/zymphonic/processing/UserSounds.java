package com.recreationallightandmagic.zymphonic.processing;

import processing.core.PApplet;	

import processing.core.PVector;
import SimpleOpenNI.SimpleOpenNI;
import ddf.minim.AudioOutput;
import ddf.minim.AudioPlayer;
import ddf.minim.AudioSample;
import ddf.minim.Minim;
import ddf.minim.signals.SineWave;
	 
	 public class UserSounds extends PApplet {
		 	private static final long serialVersionUID = 1L;
					
		 	SimpleOpenNI kinect;

		 	static int[] userClr;
		 	PVector com = new PVector();
		 	PVector com2d = new PVector();

		 	personBlob[] personBlobs;
		 	int[] personKtoZ;
		 	
		 	int frame = 0;
		 	int userId_z;
	 		int maxblobs = 100;

	 		//inches above which, assume on zipline
	 		float ziplineyinches = 25;
	 		
	 		//zinches at which trigger zipline sound effect *starts*:
	 		float ziplinetrigger = 20;
	 		//how many triggers in sound space...(notreally needed):
	 		int ztriggers = 20;
	 		//inches where the end sound happens:
	 		int thundertrigger = 70;
	 		//spacing in between each sound pad in inches:
	 		float ztriggersinches = 12;
	 		
		 	Minim minim;
	 		
		 	AudioSample auKick, auCello;
		 	AudioSample[] piano = new AudioSample[6];
		 	AudioSample[] bass = new AudioSample[6];
		 	AudioPlayer[] pads = new AudioPlayer[4];
		 	AudioPlayer[] ends = new AudioPlayer[4];
		 	AudioPlayer[] zips = new AudioPlayer[8];
		 	AudioPlayer auThunder;
		 	 		
		 	public void setup() {
		 		
		 		println("starting...");
		 		userClr = new int[] { color(255, 0, 0), color(0, 255, 0),
		 				color(0, 0, 255), color(255, 255, 0), color(255, 0, 255),
		 				color(0, 255, 255) };
		 		size(640, 480);
	
		 		userId_z = 0;

		 		personBlobs = new personBlob[maxblobs];
		 		personKtoZ = new int[maxblobs];
		 		
		 		kinect = new SimpleOpenNI(this);
		 		if (kinect.isInit() == false) {
		 			println("Can't init SimpleOpenNI, maybe the camera is not connected!");
		 			exit();
		 			return;
		 		}

		 		kinect.setMirror(true);
		 		
		 		// enable depthMap generation
		 		kinect.enableDepth();

		 		// enable skeleton generation for all joints
		 		kinect.enableUser();

		 		background(200, 0, 0);

		 		stroke(0, 0, 255);
		 		strokeWeight(3);
		 		smooth();
		 		
		 		minim = new Minim(this);
		 		
		 		auKick = minim.loadSample("/home/shelly/workspace/zymphonic/" + "Kick1.wav", 6048);
		 		auCello = minim.loadSample("/home/shelly/workspace/zymphonic/" + "cello2.wav", 8048);

		 		piano[0] = minim.loadSample("/home/shelly/workspace/zymphonic/" + "Piano1_A1.wav", 6048);
		 		piano[1] = minim.loadSample("/home/shelly/workspace/zymphonic/" + "Piano2_C2.wav", 6048);
		 		piano[2] = minim.loadSample("/home/shelly/workspace/zymphonic/" + "Piano3_E2.wav", 6048);
		 		piano[3] = minim.loadSample("/home/shelly/workspace/zymphonic/" + "Piano4_F2.wav", 6048);
		 		piano[4] = minim.loadSample("/home/shelly/workspace/zymphonic/" + "Piano5_G2.wav", 6048);
		 		piano[5] = minim.loadSample("/home/shelly/workspace/zymphonic/" + "Piano6_A2.wav", 6048);

		 		bass[0] =  minim.loadSample("/home/shelly/workspace/zymphonic/" + "Bass1_A1.wav", 6048);
		 		bass[1] =  minim.loadSample("/home/shelly/workspace/zymphonic/" + "Bass2_C2.wav", 6048);
		 		bass[2] =  minim.loadSample("/home/shelly/workspace/zymphonic/" + "Bass3_E2.wav", 6048);
		 		bass[3] =  minim.loadSample("/home/shelly/workspace/zymphonic/" + "Bass4_F2.wav", 6048);
		 		bass[4] =  minim.loadSample("/home/shelly/workspace/zymphonic/" + "Bass5_G2.wav", 6048);
		 		bass[5] =  minim.loadSample("/home/shelly/workspace/zymphonic/" + "Bass6_A2.wav", 6048);
		 
		 		pads[0] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "Pad1_A1.wav", 66048);
		 		pads[1] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "Pad2_C2.wav", 66048);
		 		pads[2] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "Pad3_E2.wav", 66048);
		 		pads[3] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "Pad4_G2.wav", 66048);

		 		
		 		ends[0] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "End_A.wav", 66048);
		 		ends[1] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "End_C.wav", 66048);
		 		ends[2] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "End_E.wav", 66048);
		 		ends[3] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "End_G.wav", 66048);

		 		
		 		/*
		 		zips[0] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "Zipline1_A_1.mp3", 2048);
		 	*/
		 		
		 		//zips[1] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "Zipline1_C.wav");
		 		/*
		 		zips[2] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "Zipline1_E.wav");
		 		zips[3] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "Zipline1_G.wav");
		 		/*
		 		zips[4] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "Zipline2_A.wav");
		 		zips[5] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "Zipline2_C.wav");
		 		zips[6] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "Zipline2_E.wav");
		 		zips[7] =  minim.loadFile("/home/shelly/workspace/zymphonic/" + "Zipline2_G.wav");
*/
		 		
		 		auThunder = minim.loadFile("/home/shelly/workspace/zymphonic/" + "thunder.wav", 2048);
		 		
		 		auCello.trigger();
        		auKick.trigger();
		 		pads[0].play();
		 		
		 		println("end setup");
		 	}

		 	public void draw() {	 		
		 		
		 		kinect.update();

		 		image(kinect.userImage(), 0, 0);

		 		// draw the skeleton if it's available
		 		int[] userList = kinect.getUsers();

		 		//println("user count " + userList.length);
		 		
		 		for (int i = 0; i < userId_z; i++) {	 			
		 		
		 			//check for each z user, if an active kinect user 
		 			if (personBlobs[i].puserId != 99) {

		 				int currentId = personBlobs[i].puserId;
		 				boolean stillactive = false;
		 				
		 			    for (int h = 0; h < userList.length; h++)
		 			    {
		 			    	if (userList[h] == currentId) {
		 			    		stillactive = true;
		 			    		}
		 			    }
		 				
		 				if (stillactive == true) {
		 				/*
		 				if (kinect.isTrackingSkeleton(currentId)) {
		 					stroke(personBlobs[currentId].personClr);
		 				}*/

		 				// draw the center of mass
		 				if (kinect.getCoM(currentId, com)) {
		 					
		 					println(currentId + " at " + com.x);

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
		 					double zinches = com.z / 25.4;
		 					
		 					double xinches = com.x / 25.4;
		 					
		 					double yinches = com.y / 25.4;

		 					if (xinches != 0)
		 					{
		 						playUserSound(personBlobs[i], xinches, yinches, zinches);
		 					}
		 				  }
		 				}//if still active in userlist
		 			}// if personBlob is still an active user
		 		}// for each personBLob

		 		frame = frame + 1;

		 	}

		 	void playUserSound(personBlob thisPerson, double xinches, double yinches, double zinches) {
		 //		 println("in play user sound");

		 		float movementz = abs( (float) (zinches - thisPerson.lastzinches));
		 		float movementx = abs((float) (xinches - thisPerson.lastxinches));		 	
		 		float movementy = abs((float) (yinches - thisPerson.lastyinches));		 	
		 		
		 		double currentztrigger = Math.floor(zinches / ztriggersinches);
		 				 		
		 		if (currentztrigger != thisPerson.lastztrigger)
		 		   {
			 		println ("yinches " + yinches + " zinches: " + zinches + " currentztrigger: " + currentztrigger + " lastztrigger: " + thisPerson.lastztrigger);

		 		   //auKick.trigger();
		 			
		 			if (yinches > ziplineyinches) 
		 			   {
		 				if (thisPerson.lastztrigger < ziplinetrigger & zinches > ziplinetrigger)
		 				   {
		 			 	//	int zip = (int)Math.floor((currentztrigger + thisPerson.puserId) % 8);
		 			 		
		 			 	//	if (!zips[zip].isPlaying()) {
		 		 		//		zips[zip].rewind();
		 		 		//	}	
		 				//	zips[zip].play();
		 			       }
		 				
		 				if (thisPerson.lastztrigger < thundertrigger & zinches > thundertrigger)
		 				   {

		 		 		   int end = (int)Math.floor((currentztrigger + thisPerson.puserId) % 5);
		 		 		 	 	
		 		 		   println("end: " + end);
		 		 		   if (end < 4)
		 		 		      {
			 					if (!ends[end].isPlaying())
			 					   {
			 					   ends[end].rewind();
			 					   }
			 					ends[end].play();	 						 		 		   
		 		 		      }
		 		 		   if (end == 4)
		 		 		      {
		 		 			   /*
  		 					  if (!auThunder.isPlaying())
		 					     {
		 						 auThunder.rewind();
		 					     }
		 					     auThunder.play();	 
		 					     */
		 		 		      }		
		 			       }//occasional thunder	
		 		 		   
		 				}//y above zipline line
		 			
		 			if (yinches < ziplineyinches) 
		 			   {
			 		
			 		   int key = (int)Math.floor((currentztrigger + thisPerson.puserId) % 6);
	
			 		   if (xinches < 0) 
			 		      {
			 			   println("piano key: " + key);
			 			   piano[key].trigger();
			 		      }
			 		   if (xinches > 0) 
			 		      {
			 			   println("bass key: " + key);
			 			   bass[key].trigger();
			 		      }
			 		   
		 		      }
			 		   thisPerson.lastztrigger = currentztrigger;
		 		   }//this person is in a different trigger

		 		if (movementz > 6) 
		 		   {

		 		    // auKick.trigger();
		 	//		thisPerson.lastzinches = zinches;
		 		   }		 		

		 		if (movementx > 6) 
		 		   {
		 		    // auCello.trigger();
		 			println("xinches " + xinches);		 			
		 			thisPerson.lastxinches = xinches;
		 		   }
		 		
		 	}//end playersound

		 	public void mouseMoved() {
		 		
		 	}

		 	public void onNewUser(SimpleOpenNI curContext, int userId) {
		 		
		 		println("onNewUser - userId: " + userId + " frame: " + frame);
		 	
		 		curContext.startTrackingSkeleton(userId);
 		
		 		personBlob tempBlob = new personBlob(userId, userId_z);

		 		personBlobs[userId_z] = tempBlob;

		 		personKtoZ[userId] = userId_z;

		 		userId_z = userId_z + 1;
		 		
		 		if (userId_z > (maxblobs - 2)) userId_z = 0;
		 		
		 		int currentpad = (int)Math.floor(userId % 4);

		 		if (!pads[currentpad].isPlaying()) {
	 				pads[currentpad].rewind();
	 			}		 		
		 		pads[currentpad].play();

		 	}

		 	public void onLostUser(SimpleOpenNI curContext, int userId) {

		 		println("onLostUser - userId: " + userId);
		 	
		 		int pblobz = personKtoZ[userId];
		 		personBlobs[pblobz].puserId = 99;

		 	}

		 	public void onVisibleUser(SimpleOpenNI curContext, int userId) {
		 		// println("onVisibleUser - userId: " + userId);
		 	}

		 	//mostly here for testing stuff; irrelevant
		 	public void keyPressed() {
		 		switch (key) {
		 		case ' ':
		 		case 'p':
		 		case '1':
		 		}
		 	}

		 	//need for closing sound
		 	public void stop() {
		 		
		 		auKick.close();
		 		auCello.close();
		 		
		 		piano[0].close();
		 		piano[1].close();
		 		piano[2].close();
		 		piano[3].close();
		 		piano[4].close();
		 		piano[5].close();

		 		bass[0].close();
		 		bass[1].close();
		 		bass[2].close();
		 		bass[3].close();
		 		bass[4].close();
		 		bass[5].close();

		 		pads[0].close();
		 		pads[1].close();
		 		pads[2].close();
		 		pads[3].close();

		 		
		 		ends[0].close();
		 		ends[1].close();
		 		ends[2].close();
		 		ends[3].close();
		 		
		 		
		/* 		
		 		zips[0].close();
		 */		
		 		
//		 		zips[1].close();
		 		/*
		 		zips[2].close();
		 		zips[3].close();
		 		*/
		 		/*
		 		zips[4].close();
		 		zips[5].close();
		 		zips[6].close();
		 		zips[7].close();
*/		
		 		
		 		auThunder.close();
		 		
		 		minim.stop();

		 		super.stop();

		 	}

		 	static class personBlob {

		 		int puserId = 99;
		 		int puserId_z;
		 		PVector[] frameLocs = new PVector[1000];
		 		int personClr;
		 		long timestamp = System.currentTimeMillis();
		 		double lastzinches = 0;
		 		double lastxinches = 0;
		 		double lastyinches = 0;
		 		double lastztrigger = 0;
		 		

		 		personBlob(int tempuserId, int tempuserId_z) {
		 			
		 			System.out.println("testing");
		 			
		 			println("creating new person");
		 			
		 			puserId = tempuserId;
		 			puserId_z = tempuserId_z;

		 			println("tempuserId " + tempuserId + "tempuserId_z " + tempuserId_z);
		 			personClr = userClr[(tempuserId - 1) % userClr.length];
		 			println("in new person blob constructor " + puserId + " userId_z: "
		 					+ puserId_z);
		 		}
		 	}
	 }