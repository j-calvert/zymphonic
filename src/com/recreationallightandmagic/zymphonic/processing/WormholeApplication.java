package com.recreationallightandmagic.zymphonic.processing;

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PVector;

import com.recreationallightandmagic.zymphonic.processing.input.DepthRegion;
import com.recreationallightandmagic.zymphonic.processing.input.Kinect;
import com.recreationallightandmagic.zymphonic.processing.lights.LEDs;
import com.recreationallightandmagic.zymphonic.processing.persist.SaverLoader;
import com.recreationallightandmagic.zymphonic.processing.persist.WormholeState;
import com.recreationallightandmagic.zymphonic.processing.sound.SoundSamples;

import controlP5.Button;
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
 * Top level applet class that runs the entire application. This has only GUI
 * related stuff, everything else is based out of WormholeCore.
 * 
 */
public class WormholeApplication extends PApplet {
	private static final int SAMPLE_MATRIX_WIDTH = 40;
	private static final int BACKGROUND = 64;
	private static final float POSITION_SCALE = 500f; // Scale of position
														// slider
	private static final float RATIO = 640f / 480;
	private static final int ACTIVE_REGION_COLOR = Constants.basicColors[0];

	private static final long serialVersionUID = 1L;

	ControlP5 cp5;
	private Toggle updateKinect;
	private ColorPicker cp;

	private Numberbox ledIdx;
	private Numberbox ledSegmentNum;
	// protected LEDs lights; // The low level LEDs
	protected Kinect kinect; // The Kinect

	private RadioButton regionSelector;

	private Slider2D position;

	private Slider2D size;

	private Slider2D depth;

	private List<DepthRegion> regions = new ArrayList<DepthRegion>();

	// The region currently selected for editing (e.g. one that was just
	// created)
	private int selectedRegionId = -1;

	// The segment currently selected within the currently selected region.
	private int selectedSegmentId = -1;

	private Button newRegionButton;

	private Textfield newRegionText;

	private Textarea messageText;

	private Button saveButton;
	private Button loadButton;

	private Button resetCam;

	private PointCloudFrame viewerFrame;
	private Matrix soundMatrix;
	private SoundSamples soundSamples;

	public void setup() {
		// lights = new LEDs(this);
		kinect = new Kinect(this);
		// Important that this comes before setupController (since we create the
		// soundMatrix based on this)
		soundSamples = new SoundSamples(new Minim(this), SAMPLE_MATRIX_WIDTH);

		setupController();
		viewerFrame = addViewerFrame("Kinect 1", 640, 480);

		// *THE* UI !!!
		size(640, 840);
		background(64);

	}

	/**
	 * @param numSamples
	 *            The number of sound samples (used to size the soundMatrix)
	 */
	private void setupController() {
		if (cp5 != null) {
			if (regionSelector != null) {
				for (Toggle t : regionSelector.getItems()) {
					t.remove();
				}
				regionSelector.remove();
				cp5 = null;
			}
		}
		selectedRegionId = -1;
		selectedSegmentId = -1;
		cp5 = new ControlP5(this);
		cp5.setFont(new ControlFont(createFont("Arial", 12, true), 12));

		// create a toggle
		updateKinect = cp5.addToggle("update\npoint\ncloud")
				.setPosition(10, 10).setSize(20, 20).setMode(ControlP5.SWITCH)
				.setValue(true);
		resetCam = cp5.addButton("reset cam").setPosition(10, 80)
				.setSize(100, 20);

		// create a color picker
		cp = cp5.addColorPicker("picker").setPosition(80, 10)
				.setColorValue(color(255, 128, 0, 128));

		// create a number box (for specifying index within a given strip
		ledIdx = cp5.addNumberbox("ledIndexChooser")
				.setCaptionLabel("LED index").setPosition(380, 10)
				.setSize(100, 14).setRange(0, LEDs.LEDS_PER_STRIP - 1)
				.setValue(6).setDirection(Controller.HORIZONTAL);

		// create a number box (for specifying index within a given strip
		ledSegmentNum = cp5.addNumberbox("ledSegment")
				.setCaptionLabel("Segment Number").setPosition(380, 50)
				.setSize(100, 14).setRange(0, LEDs.NUM_LIGHT_STRIPS - 1)
				.setValue(0).setDirection(Controller.HORIZONTAL);

		regionSelector = cp5.addRadioButton("radioButton").setPosition(20, 160)
				.setSize(20, 20).setColorForeground(color(120))
				.setCaptionLabel("Regions").setColorActive(ACTIVE_REGION_COLOR)
				.setColorLabel(color(255)).setItemsPerRow(5)
				.setSpacingColumn(80);
		int i = 0;
		for (DepthRegion region : regions) {
			addRegionSelector(regionSelector, region.name, i++);
		}

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

		newRegionButton = cp5.addButton("newButton").setPosition(230, 220)
				.setSize(40, 30).setCaptionLabel("New");
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
	}

	public void soundMatrix(int x, int y) {
		System.out.println("Triggered sound matrix at x,y = " + x + ", " + y);
	}

	@Override
	public InputStream createInput(String fileName) {
		try {
			return new FileInputStream(new File(Constants.SAMPLE_DIRECTORY + fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void addRegionSelector(RadioButton button, String name, int id) {
		RadioButton t = button.addItem(name, id);
		t.getCaptionLabel().getStyle().moveMargin(-7, 0, 0, -3);
		t.getCaptionLabel().getStyle().movePadding(7, 0, 0, 3);
		t.getCaptionLabel().getStyle().backgroundWidth = 45;
		t.getCaptionLabel().getStyle().backgroundHeight = 13;
		regionSelector.activate(name);
	}

	@Override
	public void draw() {
		background(BACKGROUND);
		// updateLights(this.lights);
		kinect.kinect.update();
	}

	@SuppressWarnings("unused")
	private void updateLights(LEDs lights) {
		lights.clear();
		int colorValue = cp.getColorValue();
		for (int i = 0; i < (int) ledIdx.getValue(); i++) {
			for (int j = 0; j < LEDs.NUM_LIGHT_STRIPS; j++) {
				lights.setLedDirect(j, i, colorValue);
			}
		}
		lights.renderLights();
		image(lights.image, 80, 700);
	}

	public void controlEvent(ControlEvent c) {
		message("");
		if (c.isFrom(newRegionButton)) {
			createNewRegion(newRegionText.getText());
		} else if (c.isFrom(saveButton)) {
			saveState(newRegionText.getText());
		} else if (c.isFrom(loadButton)) {
			loadState(newRegionText.getText());
		} else if (c.isFrom(regionSelector)) {
			switchToRegion((int) regionSelector.getValue());
		} else if (c.isFrom(depth)) {
			DepthRegion depthRegion = getSelectedRegion();
			if (depthRegion != null) {
				depthRegion.z = depth.getArrayValue(0);
				depthRegion.d = depth.getArrayValue(1);
			}
		} else if (c.isFrom(position)) {
			DepthRegion depthRegion = getSelectedRegion();
			if (depthRegion != null) {
				depthRegion.x = position.getArrayValue(0);
				depthRegion.y = -position.getArrayValue(1);
			}
		} else if (c.isFrom(size)) {
			DepthRegion depthRegion = getSelectedRegion();
			if (depthRegion != null) {
				depthRegion.w = size.getArrayValue(0);
				depthRegion.h = size.getArrayValue(1);
			}
		} else if (c.isFrom(resetCam)) {
			viewerFrame.reset();
		}
	}

	private void loadState(String text) {
		try {
			WormholeState state = SaverLoader.load(text);
			setupState(state);
			setupController();
		} catch (RuntimeException e) {
			message(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			message(e.getMessage());
		}
	}

	private void setupState(WormholeState state) {
		this.regions = state.regions;
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
		ret.regions = regions;
		return ret;
	}

	private void createNewRegion(String name) {
		if (name == null || name.isEmpty()
				|| regionSelector.getItem(name) != null) {
			message("You have to choose a new, unique name");
		} else {
			DepthRegion dr = new DepthRegion(name, position.getArrayValue(0),
					-position.getArrayValue(1), size.getArrayValue(0),
					size.getArrayValue(1), depth.getArrayValue(0),
					depth.getArrayValue(1));
			addRegionSelector(regionSelector, name, regions.size());
			regions.add(dr);
			selectedRegionId = regions.size() - 1;
		}
	}

	private void switchToRegion(int selected) {
		selectedRegionId = selected;
		DepthRegion depthRegion = getSelectedRegion();
		if (depthRegion != null) {
			// setArrayValue for slider 2D is terrible, be careful here!!!
			position.setArrayValue(new float[] {
					POSITION_SCALE * RATIO + depthRegion.x,
					POSITION_SCALE - depthRegion.y });
			size.setArrayValue(new float[] { depthRegion.w, depthRegion.h });
			depth.setArrayValue(new float[] { depthRegion.z, depthRegion.d });
		}
	}

	private DepthRegion getSelectedRegion() {
		if (selectedRegionId >= 0 && selectedRegionId < regions.size()) {
			return regions.get(selectedRegionId);
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

	PointCloudFrame addViewerFrame(String name, int width, int height) {
		Frame f = new Frame(name);
		PointCloudFrame p = new PointCloudFrame(width, height);
		f.add(p);
		p.init();
		f.setTitle(name);
		f.setSize(p.w, p.h);
		f.setLocation(800, 100);
		f.setResizable(false);
		f.setVisible(true);
		return p;
	}

	public class PointCloudFrame extends PApplet {
		private static final long serialVersionUID = 1L;
		int w, h;
		private PeasyCam cam;

		public void setup() {
			size(w, h, OPENGL);
			cam = new PeasyCam(this, 1000);
			cam.setMinimumDistance(50);
			cam.setMaximumDistance(4000);
		}

		public void reset() {
			cam.reset();
		}

		int profIdx = 0;
		long lastTime = 0;

		public void draw() {
			if (profIdx % 100 == 0) {
				profIdx = 0;
				System.out
						.println("3D FPS: "
								+ (1000 * 100 / (System.currentTimeMillis() - lastTime)));
				lastTime = System.currentTimeMillis();
			}
			profIdx++;

			boolean doDraw = updateKinect.getValue() > 0.5f;
			background(0);
			rotateX(radians(180f));
			stroke(255);
			PVector[] depthPoints = kinect.kinect.depthMapRealWorld();

			for (int i = 0; i < depthPoints.length; i = i + 7) {
				PVector pv = depthPoints[i];
				int[] segments = new int[regions.size()];
				for (int j = 0; j < regions.size(); j++) {
					segments[j] = regions.get(j).consider(pv);
				}
				if (doDraw) {
					getPointColor(regions);
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
				region.draw(this, selectedRegionId == regionId);
			}
		}

		private void getPointColor(List<DepthRegion> regions) {

		}

		public PointCloudFrame(int theWidth, int theHeight) {
			w = theWidth;
			h = theHeight;
		}

	}
}
