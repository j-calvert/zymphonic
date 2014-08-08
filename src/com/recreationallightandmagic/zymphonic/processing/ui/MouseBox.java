package com.recreationallightandmagic.zymphonic.processing.ui;

import processing.core.PApplet;

/**
 * A box that's used in the UI to create, represent, and modify a DepthRegion.
 */
public class MouseBox implements MouseInputHandler {

	// Upper left corner, and height and width, same as in DepthRegion
	public int x;
	public int y;
	public int w;
	public int h;
	public int color;
	private boolean locked;
	private int yOffset;
	private int xOffset;

	// Test if the cursor is over the box
	boolean overbox(int mouseX, int mouseY) {
		return mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h;
	}

	public void draw(PApplet applet, int mouseX, int mouseY) {// Draw the box
		if (overbox(mouseX, mouseY)) {
			applet.fill(color, 0.4f);
			applet.stroke(color);
			if (locked) {
				applet.strokeWeight(2.0f);
			}
		} else {
			applet.noFill();
		}
		applet.rect(x, y, w, h);
		applet.strokeWeight(1.0f);
	}

	@Override
	public void mousePressed(int mouseX, int mouseY) {
		if (overbox(mouseX, mouseY)) {
			locked = true;
		} else {
			locked = false;
		}
		xOffset = mouseX - x;
		yOffset = mouseY - y;

	}

	@Override
	public void mouseDragged(int mouseX, int mouseY) {
		if (locked) {
			x = mouseX - xOffset;
			y = mouseY - yOffset;
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		locked = false;
	}

}
