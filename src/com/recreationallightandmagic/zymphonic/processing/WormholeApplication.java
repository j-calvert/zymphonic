package com.recreationallightandmagic.zymphonic.processing;

import com.recreationallightandmagic.zymphonic.processing.lights.LEDs;

import controlP5.ColorPicker;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.Numberbox;
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

	private static int YORG = 480;

	private Numberbox ledIdx;
	private Numberbox ledSegmentNum;

	public void setup() {
		super.setup();

		cp5 = new ControlP5(this);

		// create a toggle
		updateKinect = cp5.addToggle("updateKinect").setPosition(10, YORG + 10)
				.setSize(50, 20);

		// create a color picker
		cp = cp5.addColorPicker("picker").setPosition(80, YORG + 10)
				.setColorValue(color(255, 128, 0, 128));

		// create a number box (for specifying index within a given strip
		ledIdx = cp5.addNumberbox("ledIndexChooser")
				.setCaptionLabel("LED index").setPosition(380, YORG + 10)
				.setSize(100, 14).setRange(0, LEDs.LEDS_PER_STRIP - 1)
				.setValue(60).setDirection(Controller.HORIZONTAL);

		// create a number box (for specifying index within a given strip
		ledSegmentNum = cp5.addNumberbox("ledSegment")
				.setCaptionLabel("Segment Number").setPosition(380, YORG + 50)
				.setSize(100, 14).setRange(0, LEDs.NUM_LIGHT_STRIPS - 1)
				.setValue(0).setDirection(Controller.HORIZONTAL);

		// *THE* UI !!!
		this.size(640, 960);

	}

	public void draw() {
		lights.clear();
		for (int i = 0; i < (int) ledIdx.getValue(); i++) {
			lights.setLedDirect((int) ledSegmentNum.getValue(), i,
					cp.getColorValue());
		}
		super.draw();
	}

	public void controlEvent(ControlEvent c) {
		System.out.println("Event from Controller " + c.getName()
				+ " with value " + c.getValue());
		if (c.isFrom(updateKinect)) {
			kinect.redraw = c.getValue() > .5;
		} else if (c.isFrom(cp)) {
			// color = color((int) (c.getArrayValue(0)),
			// (int) (c.getArrayValue(1)), (int) (c.getArrayValue(2)),
			// (int) (c.getArrayValue(3)));
		} else if (c.isFrom(ledIdx)) {

		}
	}
}
