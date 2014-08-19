package com.recreationallightandmagic.zymphonic.processing;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PVector;
import SimpleOpenNI.SimpleOpenNI;

import com.recreationallightandmagic.zymphonic.processing.input.DepthRegion;
import com.recreationallightandmagic.zymphonic.processing.input.Kinect;
import com.recreationallightandmagic.zymphonic.processing.lights.LEDs;
import com.recreationallightandmagic.zymphonic.processing.persist.MinimFilesystemHandler;
import com.recreationallightandmagic.zymphonic.processing.persist.SaverLoader;
import com.recreationallightandmagic.zymphonic.processing.persist.WormholeState;
import com.recreationallightandmagic.zymphonic.processing.sound.SoundSamples;

import controlP5.Button;
import controlP5.CheckBox;
import controlP5.ColorPicker;
import controlP5.ControlEvent;
import controlP5.ControlFont;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.Matrix;
import controlP5.Numberbox;
import controlP5.RadioButton;
import controlP5.Slider2D;
import controlP5.Textarea;
import controlP5.Textfield;
import controlP5.Toggle;
import ddf.minim.Minim;

/**
 * Top level applet class that runs the entire application.
 * 
 */
public class WormholeApplication extends PApplet {
	private static final int SAMPLE_MATRIX_WIDTH = 40;
	private static final int BACKGROUND = 64;
	private static final float POSITION_SCALE = 500f; // Scale of position
														// slider
	private static final float RATIO = 640f / 480;
	public static final int ACTIVE_REGION_COLOR = Constants.basicColors[0];
	public static final int ACTIVE_SEGMENT_COLOR = Constants.basicColors[1];
	public static final int ACTIVE_BOTH_COLOR = Constants.basicColors[3];

	private static final long serialVersionUID = 1L;

	protected LEDs lights; // The low level LEDs
	protected Kinect kinect; // The Kinect

	// The region currently selected for editing (e.g. one that was just
	// created)
	private int selectedRegionId1 = -1;
	private int selectedRegionId2 = -1;

	// The segment currently selected within the currently selected region.
	private int selectedSegmentId = -1;

	// A mess of members that comprise the control panel. TODO clean this up if
	// you have time (but be careful, ControlP5 is fickle).
	private ControlP5 cp5;
	private Toggle updateKinect;
	private ColorPicker cp;
	private Numberbox ledIdx;
	// private Numberbox ledSegmentNum;
	private RadioButton regionSelector1;
	private RadioButton regionSelector2;
	private RadioButton segmentSelector;
	private Slider2D position;
	private Slider2D size;
	private Slider2D depth;
	private List<DepthRegion> regions1 = new ArrayList<DepthRegion>();
	private List<DepthRegion> regions2 = new ArrayList<DepthRegion>();
	private Button newRegionButton1;
	private Button newRegionButton2;
	private Textfield newRegionText;
	private Textarea messageText;
	private Button saveButton;
	private Button loadButton;
	private Button resetCam;
	private Matrix soundMatrix;
	private SoundSamples soundSamples;
	private String selectedSoundName;
	private Button stopSound;
	private Button chooseSound;
	private Button playSound;

	// The frame that we view the 3D stuff in.
	private PointCloudFrame viewerFrame;
	private CheckBox ledStripSelector;

	public void setup() {
		lights = new LEDs(this);
		kinect = new Kinect(this);
		// Important that this comes before setupController (since we create the
		// soundMatrix based on this)
		soundSamples = new SoundSamples(
				new Minim(new MinimFilesystemHandler()), SAMPLE_MATRIX_WIDTH);

		setupController();
		viewerFrame = addViewerFrame("Kinect 1", 640, 480, kinect.kinect1, 10,
				true);
		viewerFrame = addViewerFrame("Kinect 2", 640, 480, kinect.kinect2, 500,
				false);

		// *THE* UI !!!
		size(640, 840);
	}

	private void setupController() {
		selectedRegionId1 = -1;
		selectedSegmentId = -1;
		cp5 = new ControlP5(this);
		cp5.setFont(new ControlFont(createFont("Arial", 12, true), 12));

		// create a toggle
		updateKinect = cp5.addToggle("update\npoint\ncloud")
				.setPosition(10, 10).setSize(20, 20).setMode(ControlP5.SWITCH)
				.setValue(true);
		resetCam = cp5.addButton("reset cam").setPosition(10, 80)
				.setSize(100, 20);
		stopSound = cp5.addButton("stop sound").setPosition(120, 80)
				.setSize(100, 20);
		chooseSound = cp5.addButton("choose sound").setPosition(230, 80)
				.setSize(100, 20);
		playSound = cp5.addButton("play sound").setPosition(340, 80)
				.setSize(100, 20);

		// create a color picker
		cp = cp5.addColorPicker("picker").setPosition(80, 10)
				.setColorValue(color(255, 128, 0, 128));

		// create a number box (for specifying index within a given strip
		ledIdx = cp5.addNumberbox("ledIndexChooser")
				.setCaptionLabel("LED index").setPosition(380, 10)
				.setSize(200, 20).setRange(0, LEDs.LEDS_PER_STRIP - 1)
				.setMultiplier(0.1f) // set the sensitifity of the numberbox
				.setValue(6).setDirection(Controller.HORIZONTAL);

		segmentSelector = cp5.addRadioButton("segmentRadioButton")
				.setPosition(20, 120).setSize(15, 15)
				.setColorForeground(color(120))
				.setCaptionLabel("Segments (zones)")
				.setColorActive(ACTIVE_BOTH_COLOR).setColorLabel(color(255))
				.setItemsPerRow(8).setSpacingColumn(32);
		for (int i = 0; i < DepthRegion.MAX_SEGMENTS; i++) {
			addSelectorItem(segmentSelector, "s" + i, i);
		}

		regionSelector1 = cp5.addRadioButton("regionRadioButton1")
				.setPosition(20, 160).setSize(20, 20)
				.setColorForeground(color(120)).setCaptionLabel("Regions1")
				.setColorActive(ACTIVE_REGION_COLOR).setColorLabel(color(255))
				.setItemsPerRow(5).setSpacingColumn(80);

		regionSelector2 = cp5.addRadioButton("regionRadioButton2")
				.setPosition(20, 200).setSize(20, 20)
				.setColorForeground(color(120)).setCaptionLabel("Regions2")
				.setColorActive(ACTIVE_REGION_COLOR).setColorLabel(color(255))
				.setItemsPerRow(5).setSpacingColumn(80);

		position = cp5
				.addSlider2D("\nposition")
				.setPosition(30, 300)
				.setSize((int) (RATIO * 100), 100)
				.setMinX(-POSITION_SCALE * RATIO)
				.setMaxX(POSITION_SCALE * RATIO)
				.setMinY(-POSITION_SCALE)
				.setMaxY(POSITION_SCALE)
				.setArrayValue(
						new float[] { POSITION_SCALE * RATIO, POSITION_SCALE });
		size = cp5.addSlider2D("\nsize").setPosition(190, 300)
				.setSize(100, 100).setMinX(0f).setMaxX(1000f).setMinY(0f)
				.setMaxY(1000f).setArrayValue(new float[] { 100, 100 });
		depth = cp5.addSlider2D("\nz, depth").setPosition(310, 300)
				.setSize(100, 100).setMaxX(1000f).setMaxY(1000f)
				.setArrayValue(new float[] { 500, 500 });

		newRegionButton1 = cp5.addButton("newButton1").setPosition(230, 220)
				.setSize(45, 30).setCaptionLabel("New 1");
		newRegionButton2 = cp5.addButton("newButton2").setPosition(280, 220)
				.setSize(45, 30).setCaptionLabel("New 2");
		saveButton = cp5.addButton("saveButton").setPosition(480, 220)
				.setSize(40, 30).setCaptionLabel("Save");
		loadButton = cp5.addButton("loadButton").setPosition(530, 220)
				.setSize(40, 30).setCaptionLabel("Load");
		newRegionText = cp5.addTextfield("", 30, 220, 200, 30);
		messageText = cp5.addTextarea("message", "", 30, 260, 300, 30);

		int h = soundSamples.getNameGrid()[0].length;
		int w = soundSamples.getNameGrid().length;
		soundMatrix = cp5.addMatrix("soundMatrix").setPosition(30, 440)
				.setSize(10 * w, 10 * h).setGrid(w, h).setGap(1, 1)
				.setInterval(10).setMode(ControlP5.MULTIPLES)
				.setColorBackground(color(120)).setBackground(color(40));

		ledStripSelector = cp5.addCheckBox("ledStripSelector")
				.setPosition(30, 600).setColorForeground(color(120))
				.setColorActive(color(255)).setColorLabel(color(255))
				.setSize(20, 20).setItemsPerRow(4).setSpacingColumn(60)
				.setSpacingRow(20).addItem("Str0", 0).addItem("Str1", 1)
				.addItem("Str2", 2).addItem("Str3", 3).addItem("Str4", 4)
				.addItem("Str5", 5).addItem("Str6", 6).addItem("Str7", 7)
				.addItem("Str8", 8).addItem("Str9", 9).addItem("Str10", 10)
				.addItem("Str11", 11).addItem("Str12", 12).addItem("Str13", 13)
				.addItem("Str14", 14).addItem("Str15", 15);
	}

	// Kinda a generic name, but is the only way I've figured out how to 'read'
	// a matrix button push. Would prefer to call it 'triggerSound'.
	public void soundMatrix(int x, int y) {
		try {
			if (soundMatrix.get(x, y)) {
				String soundName = soundSamples.getNameGrid()[x][y];
				this.viewerFrame.triggerSound(soundName);
				soundMatrix.set(x, y, false);
			}
		} catch (Exception e) {
			message("Something went wrong getting sound for element in soundMatrix");
			e.printStackTrace();
		}
		System.out.println("Triggered sound matrix at x,y = " + x + ", " + y);
	}

	public void addSelectorItem(RadioButton button, String name, int id) {
		RadioButton t = button.addItem(name, id);
		t.getCaptionLabel().getStyle().moveMargin(-7, 0, 0, -3);
		t.getCaptionLabel().getStyle().movePadding(7, 0, 0, 3);
		t.getCaptionLabel().getStyle().backgroundWidth = 45;
		t.getCaptionLabel().getStyle().backgroundHeight = 13;
	}

	@Override
	public void draw() {
		background(BACKGROUND);
		updateLights(this.lights);
		kinect.kinect1.update();
		kinect.kinect2.update();
	}

	float[] isSelected = new float[LEDs.NUM_LIGHT_STRIPS];

	private void updateLights(LEDs lights) {
		lights.clear();
		int colorValue = cp.getColorValue();
		// int ledStripNum = (int) ledSegmentNum.getValue();
		Constants.arrCp(ledStripSelector.getArrayValue(), isSelected);
		for (int ledStripNum = 0; ledStripNum < LEDs.NUM_LIGHT_STRIPS; ledStripNum++) {
			if (isSelected[ledStripNum] > 0.5f) { // is selected
				lights.drawCursor(ledStripNum, (int) ledIdx.getValue(),
						Constants.basicColors[5], Constants.basicColors[1]);
			}
		}
		lights.renderLights();
		image(lights.image, 80, 800);
	}

	public void controlEvent(ControlEvent c) {
		message("");
		if (c.isFrom(newRegionButton1)) {
			createNewRegion(regionSelector1, regions1, newRegionText.getText());
			selectedRegionId1 = regions1.size() - 1;
		} else if (c.isFrom(newRegionButton2)) {
			createNewRegion(regionSelector2, regions2, newRegionText.getText());
			selectedRegionId2 = regions2.size() - 1;
		} else if (c.isFrom(saveButton)) {
			saveState(newRegionText.getText());
		} else if (c.isFrom(loadButton)) {
			loadState(newRegionText.getText());
		} else if (c.isFrom(segmentSelector)) {
			selectedSegmentId = (int) segmentSelector.getValue();
		} else if (c.isFrom(regionSelector1)) {
			int regionId = (int) regionSelector1.getValue();
			selectedRegionId1 = regionId;
			switchToRegion(regions1, regionId);
		} else if (c.isFrom(regionSelector2)) {
			int regionId = (int) regionSelector2.getValue();
			selectedRegionId2 = regionId;
			switchToRegion(regions2, regionId);
		} else if (c.isFrom(resetCam)) {
			viewerFrame.reset();
		} else if (c.isFrom(stopSound)) {
			soundSamples.stopAll();

		} else {
			DepthRegion depthRegion = getSelectedRegion(regions1,
					selectedRegionId1);
			if (depthRegion != null) {
				depthRegionEvent(c, depthRegion);
			}
			depthRegion = getSelectedRegion(regions2, selectedRegionId2);
			if (depthRegion != null) {
				depthRegionEvent(c, depthRegion);
			}
		}
	}

	private void depthRegionEvent(ControlEvent c, DepthRegion depthRegion) {
		if (c.isFrom(depth)) {
			depthRegion.z = depth.getArrayValue(0);
			depthRegion.d = depth.getArrayValue(1);
		} else if (c.isFrom(position)) {
			depthRegion.x = position.getArrayValue(0);
			depthRegion.y = -position.getArrayValue(1);
		} else if (c.isFrom(size)) {
			depthRegion.w = size.getArrayValue(0);
			depthRegion.h = size.getArrayValue(1);
		} else if (c.isFrom(playSound)) {
			String soundName = depthRegion.getSoundName(selectedSegmentId);
			if (soundName != null) {
				System.out.println("Triggering " + soundName);
				this.viewerFrame.triggerSound(soundName);
			}
		} else if (c.isFrom(chooseSound)) {
			depthRegion.setSoundName(selectedSegmentId, selectedSoundName);
		}
	}

	private void loadState(String text) {
		try {
			WormholeState state = SaverLoader.load(text);
			setupState(state);
			if (regionSelector1 != null) {
				for (Toggle t : regionSelector1.getItems()) {
					t.remove();
				}
				regionSelector1.remove();
			}

			if (regionSelector2 != null) {
				for (Toggle t : regionSelector2.getItems()) {
					t.remove();
				}
				regionSelector2.remove();
			}

			regionSelector1 = cp5.addRadioButton("regionRadioButton1")
					.setPosition(20, 160).setSize(20, 20)
					.setColorForeground(color(120)).setCaptionLabel("Regions1")
					.setColorActive(ACTIVE_REGION_COLOR)
					.setColorLabel(color(255)).setItemsPerRow(5)
					.setSpacingColumn(80);

			regionSelector2 = cp5.addRadioButton("regionRadioButton2")
					.setPosition(20, 200).setSize(20, 20)
					.setColorForeground(color(120)).setCaptionLabel("Regions2")
					.setColorActive(ACTIVE_REGION_COLOR)
					.setColorLabel(color(255)).setItemsPerRow(5)
					.setSpacingColumn(80);

			int i = 0;
			for (DepthRegion region : regions1) {
				addSelectorItem(regionSelector1, region.name, i++);
			}
			i = 0;
			for (DepthRegion region : regions2) {
				addSelectorItem(regionSelector2, region.name, i++);
			}

		} catch (RuntimeException e) {
			message(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			message(e.getMessage());
		}
	}

	private void setupState(WormholeState state) {
		this.regions1 = state.regions1;
		this.regions2 = state.regions2;
	}

	private void saveState(String filename) {
		try {
			SaverLoader.save(filename, getState());
		} catch (RuntimeException e) {
			message(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			message(e.getMessage());
		}
	}

	private WormholeState getState() {
		WormholeState ret = new WormholeState();
		ret.regions1 = regions1;
		ret.regions2 = regions2;
		return ret;
	}

	private void createNewRegion(RadioButton regionSelector,
			List<DepthRegion> regions, String name) {
		if (name == null || name.isEmpty()
				|| regionSelector.getItem(name) != null) {
			message("You have to choose a new, unique name");
		} else {
			DepthRegion dr = new DepthRegion(name, position.getArrayValue(0),
					-position.getArrayValue(1), size.getArrayValue(0),
					size.getArrayValue(1), depth.getArrayValue(0),
					depth.getArrayValue(1));
			addSelectorItem(regionSelector, name, regions.size());
			regionSelector.activate(name);
			regions.add(dr);
		}
	}

	private void switchToRegion(List<DepthRegion> regions, int regionId) {
		DepthRegion depthRegion = getSelectedRegion(regions, regionId);
		if (depthRegion != null) {
			// setArrayValue for slider 2D is terrible, be careful here!!!
			position.setArrayValue(new float[] {
					POSITION_SCALE * RATIO + depthRegion.x,
					POSITION_SCALE - depthRegion.y });
			size.setArrayValue(new float[] { depthRegion.w, depthRegion.h });
			depth.setArrayValue(new float[] { depthRegion.z, depthRegion.d });
		}
	}

	private DepthRegion getSelectedRegion(List<DepthRegion> regions,
			int regionId) {
		if (regionId >= 0 && regionId < regions.size()) {
			return regions.get(regionId);
		} else {
			return null;
		}
	}

	private void message(String string) {
		if (messageText != null && string != null) {
			messageText.setText(string);
		}
	}

	// Point cloud view frame stuff from here on

	PointCloudFrame addViewerFrame(String name, int width, int height,
			SimpleOpenNI kinect, int screenPos, boolean isKinectOne) {
		Frame f = new Frame(name);
		PointCloudFrame p = new PointCloudFrame(width, height, kinect,
				isKinectOne);
		f.add(p);
		p.init();
		f.setTitle(name);
		f.setSize(p.w, p.h);
		f.setLocation(800, screenPos);
		f.setResizable(false);
		f.setVisible(true);
		return p;
	}

	public class PointCloudFrame extends PApplet {
		private static final int SACRIFICED_PV_RESOLUTION = 7;
		private static final long serialVersionUID = 1L;
		int w, h;
		private PeasyCam cam;

		private SimpleOpenNI thisKinect;

		public void setup() {
			size(w, h, OPENGL);
			cam = new PeasyCam(this, 1000);
			cam.setMinimumDistance(50);
			cam.setMaximumDistance(4000);
		}

		public void reset() {
			cam.reset();
		}

		public void triggerSound(String soundName) {
			if (soundName != null) {
				selectedSoundName = soundName;
				soundSamples.getSample(soundName).trigger();
				message("Selected Sound: " + selectedSoundName);
			} else {
				message("Computer says no (sound there).  Selected sound still "
						+ selectedSoundName);
			}
		}

		int profIdx = 0;
		long lastTime = 0;
		private boolean isKinectOne;

		public void draw() {
			if (isKinectOne) {
				if (profIdx % 100 == 0) {
					profIdx = 0;
					System.out
							.println("3D FPS: "
									+ (1000 * 100 / (System.currentTimeMillis() - lastTime)));
					lastTime = System.currentTimeMillis();
				}
				profIdx++;
			}
			boolean doDraw = updateKinect.getValue() > 0.5f;
			background(0);
			rotateX(radians(180f));
			stroke(255);
			// PVector[] depthPoints = new PVector[0];
			PVector[] depthPoints = thisKinect.depthMapRealWorld();

			List<DepthRegion> regions;
			if (isKinectOne) {
				regions = regions1;
			} else {
				regions = regions2;
			}
			for (int regionId = 0; regionId < regions.size(); regionId++) {
				DepthRegion region = regions.get(regionId);
				region.preLoop();
			}

			for (int i = 0; i < depthPoints.length; i = i
					+ SACRIFICED_PV_RESOLUTION) {
				PVector pv = depthPoints[i];
				int[] segments = new int[DepthRegion.MAX_SEGMENTS];
				stroke(Constants.WHITE);
				for (int regionId = 0; regionId < regions.size(); regionId++) {
					if (doDraw) {
						// AND consider entails, accumulate color
						segments[regionId] = regions.get(regionId).consider(
								this,
								pv,
								isKinectOne ? selectedRegionId1 == regionId
										: selectedRegionId2 == regionId,
								selectedSegmentId);
					}
				}
				if (doDraw) {
					point(pv.x, pv.y, pv.z);
				}
			}
			// Represent the kinect
			noFill();
			box(40f, 10f, 20f);
			translate(0, -10, 0);
			box(20f, 10f, 20f);
			// Draw the regions
			translate(0, 10, 0);
			for (int regionId = 0; regionId < regions.size(); regionId++) {
				DepthRegion region = regions.get(regionId);
				region.postLoop(this,
						isKinectOne ? selectedRegionId1 == regionId
								: selectedRegionId2 == regionId,
						selectedSegmentId);
			}
		}

		// kinectId should be 1 or 2, indicates which regions correspond to this
		// kinect.
		public PointCloudFrame(int theWidth, int theHeight,
				SimpleOpenNI kinect, boolean isKinectOne) {
			w = theWidth;
			h = theHeight;
			thisKinect = kinect;
			this.isKinectOne = isKinectOne;
		}

	}
}
