package com.recreationallightandmagic.zymphonic.processing;

import processing.core.PVector;

/**
 * A history of UserEvents. Kinda like a circular buffer, but we don't track the
 * tail (we just skip elements where timestamp is 0)
 */
public class UserHistory {

	private static int CAPACITY = 1000;

	private UserEvent[] events = new UserEvent[CAPACITY];

	private int headIndex = -1;

	public UserHistory() {
		for (int i = 0; i < CAPACITY; i++) {
			events[i] = new UserEvent();
		}
	}

	/**
	 * Get the PVector at the head of the list for the purpose of setting its
	 * components. This takes care of setting the corresponding timestamp and
	 * incrementing the headIndex accordingly.
	 */
	public synchronized PVector getSetHead() {
		headIndex++;
		if(headIndex >= CAPACITY) {
			headIndex = 0;
		}
		events[headIndex].timestamp = System.currentTimeMillis();
		return events[headIndex].centerOfMass;
	}
	
	public synchronized PVector getHead() {
		if(headIndex >= 0) {			
			return events[headIndex].centerOfMass;
		} else {
			return null;
		}
	}

}
