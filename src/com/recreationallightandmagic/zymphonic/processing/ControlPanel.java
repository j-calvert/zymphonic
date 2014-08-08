package com.recreationallightandmagic.zymphonic.processing;

import processing.core.PApplet;

import com.recreationallightandmagic.zymphonic.processing.input.Kinect;
import com.recreationallightandmagic.zymphonic.processing.lights.LEDs;

import controlP5.ColorPicker;
import controlP5.ControlEvent;
import controlP5.ControlFont;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.Numberbox;
import controlP5.RadioButton;
import controlP5.Slider2D;
import controlP5.Toggle;

public class ControlPanel extends PApplet {
	private static final long serialVersionUID = 1L;

	private Numberbox ledIdx;
	private Numberbox ledSegmentNum;
	ControlP5 cp5;
	private int YORG = 480; // TODO Use translate instead

	private RadioButton mouseModeChooser;

	private Slider2D s;

	private Toggle updateKinect;

	private ColorPicker cp;

	public void setup() {

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

		s = cp5.addSlider2D("wave").setPosition(30, YORG + 300)
				.setSize(100, 100).setArrayValue(new float[] { 50, 50 });

	}

	public void controlEvent(ControlEvent c) {
		System.out.println("Event from Controller " + c.getName()
				+ " with value " + c.getValue());
		if (c.isFrom(updateKinect)) {
			// kinect.redraw = c.getValue() > .5;
		}
	}

}