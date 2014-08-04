package com.recreationallightandmagic.zymphonic.processing.ui;

/**
 * Something that can implement (a subset of) the mouse event handling API, with
 * access to only the members of the global "Wormhole" container that it needs
 * to do its job.
 * 
 * @author iamlegit
 *
 */
public interface MouseInputHandler {

	void mousePressed(int mouseX, int mouseY);
	
	void mouseReleased(int mouseX, int mouseY);
	
	void mouseDragged(int mouseX, int mouseY);
}
