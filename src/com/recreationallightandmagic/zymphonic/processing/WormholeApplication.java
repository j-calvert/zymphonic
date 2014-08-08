package com.recreationallightandmagic.zymphonic.processing;

import java.awt.Frame;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PVector;

import com.recreationallightandmagic.zymphonic.processing.input.DepthRegion;
import com.recreationallightandmagic.zymphonic.processing.input.Kinect;
import com.recreationallightandmagic.zymphonic.processing.lights.LEDs;
import com.recreationallightandmagic.zymphonic.processing.ui.MouseBox;

import controlP5.ColorPicker;
import controlP5.ControlEvent;
import controlP5.ControlFont;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.Numberbox;
import controlP5.RadioButton;
import controlP5.Slider2D;
import controlP5.Toggle;

/**
 * Top level applet class that runs the entire application. This has only GUI
 * related stuff, everything else is based out of WormholeCore.
 *
 */
public class WormholeApplication extends WormholeCore {
	private static final long serialVersionUID = 1L;

	ControlP5 cp5;
	private Toggle updateKinect;
	private ColorPicker cp;

	private static int YORG = 0;

	private Numberbox ledIdx;
	private Numberbox ledSegmentNum;
	protected LEDs lights; // The low level LEDs

	private RadioButton mouseModeChooser;

	private MouseBox mouseInputHandler;

	private Slider2D corner;

	private Slider2D size;

	private Slider2D depth;

	public void setup() {
		lights = new LEDs(this);
		kinect = new Kinect(this);

		// // // Control Panel // // //
		cp5 = new ControlP5(this);
		cp5.setFont(new ControlFont(createFont("Arial", 12, true), 12));

		// create a toggle
		updateKinect = cp5.addToggle("update\nKinect")
				.setPosition(10, YORG + 10).setSize(20, 20);

		// create a color picker
		cp = cp5.addColorPicker("picker").setPosition(80, YORG + 10)
				.setColorValue(color(255, 128, 0, 128));

		// create a number box (for specifying index within a given strip
		ledIdx = cp5.addNumberbox("ledIndexChooser")
				.setCaptionLabel("LED index").setPosition(380, YORG + 10)
				.setSize(100, 14).setRange(0, LEDs.LEDS_PER_STRIP - 1)
				.setValue(6).setDirection(Controller.HORIZONTAL);

		// create a number box (for specifying index within a given strip
		ledSegmentNum = cp5.addNumberbox("ledSegment")
				.setCaptionLabel("Segment Number").setPosition(380, YORG + 50)
				.setSize(100, 14).setRange(0, LEDs.NUM_LIGHT_STRIPS - 1)
				.setValue(0).setDirection(Controller.HORIZONTAL);

		// Radio buttons that determine the mode of mouse interaction
		mouseModeChooser = cp5.addRadioButton("radioButton")
				.setPosition(20, YORG + 160).setSize(20, 20)
				.setColorForeground(color(120)).setColorActive(color(255))
				.setColorLabel(color(255)).setItemsPerRow(5)
				.setSpacingColumn(50).addItem("50", 1).addItem("100", 2)
				.addItem("150", 3).addItem("200", 4).addItem("250", 5);

		for (Toggle t : mouseModeChooser.getItems()) {
			t.getCaptionLabel().getStyle().moveMargin(-7, 0, 0, -3);
			t.getCaptionLabel().getStyle().movePadding(7, 0, 0, 3);
			t.getCaptionLabel().getStyle().backgroundWidth = 45;
			t.getCaptionLabel().getStyle().backgroundHeight = 13;
		}

		// MouseInputHandler one-off
		DepthRegion mouseInputHandler= new DepthRegion();
		mouseInputHandler.x = 20;
		mouseInputHandler.y = 60;
		mouseInputHandler.w = 120;
		mouseInputHandler.h = 160;
		mouseInputHandler.color = Constants.basicColors[2];

		corner = cp5.addSlider2D("corner").setPosition(30, YORG + 300)
				.setSize(100, 100).setArrayValue(new float[] { 50, 50 });
		size = cp5.addSlider2D("size").setPosition(150, YORG + 300)
				.setSize(100, 100).setArrayValue(new float[] { 50, 50 });
		depth = cp5.addSlider2D("depth").setPosition(270, YORG + 300)
				.setSize(100, 100).setArrayValue(new float[] { 50, 50 });
		addViewerFrame("Kinect 1", 640, 480);

		//
		// LedUI

		// *THE* UI !!!
		this.size(640, 640);
		this.background(64);

	}

	@Override
	public void draw() {
		lights.clear();
		int colorValue = cp.getColorValue();
		for (int i = 0; i < (int) ledIdx.getValue(); i++) {
			for (int j = 0; j < LEDs.NUM_LIGHT_STRIPS; j++) {
				lights.setLedDirect(j, i, colorValue);
			}
		}
		// kinect.draw(this);
		kinect.kinect.update();
		mouseInputHandler.draw(this, mouseX, mouseY);
		lights.renderLights();
		image(lights.image, 80, 700);
	}

	@Override
	public void mouseDragged() {
		super.mouseDragged();
		mouseInputHandler.mouseDragged(mouseX, mouseY);
	}

	@Override
	public void mousePressed() {
		super.mousePressed();
		mouseInputHandler.mousePressed(mouseX, mouseY);
	}

	@Override
	public void mouseReleased() {
		mouseInputHandler.mouseReleased(mouseX, mouseY);
		super.mouseReleased();
	}

	public void controlEvent(ControlEvent c) {
		System.out.println("Event from Controller " + c.getName()
				+ " with value " + c.getValue());
		if (c.isFrom(updateKinect)) {
			kinect.redraw = c.getValue() > .5;
		}
	}

	ViewerFrame addViewerFrame(String name, int width, int height) {
		Frame f = new Frame(name);
		ViewerFrame p = new ViewerFrame(width, height);
		f.add(p);
		p.init();
		f.setTitle(name);
		f.setSize(p.w, p.h);
		f.setLocation(800, 100);
		f.setResizable(false);
		f.setVisible(true);
		return p;
	}

	public class ViewerFrame extends PApplet {
		private static final long serialVersionUID = 1L;
		int w, h;
		private PeasyCam cam;

		public void setup() {
			size(w, h, OPENGL);
			cam = new PeasyCam(this, 1000);
			cam.setMinimumDistance(50);
			cam.setMaximumDistance(1500);
		}

		public void draw() {
			background(0);
			rotateX(radians(180f));
			stroke(255);
			PVector[] depthPoints = kinect.kinect.depthMapRealWorld();
			for (int i = 0; i < depthPoints.length; i = i + 10) {
				PVector pv = depthPoints[i];
				point(pv.x, pv.y, pv.z);
			}

		}

		public ViewerFrame(int theWidth, int theHeight) {
			w = theWidth;
			h = theHeight;
		}

	}
}
