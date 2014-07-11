package com.recreationallightandmagic.zymphonic.processing;

import processing.core.PVector;

/**
 * A UserEvent is an element of UserHistory. It's a simple, reusable POJO that tracks
 * everything we might care to know about a user at a given point in time.
 * 
 * For now, that's just the centerOfMass position and the timestamp
 * corresponding to the event, but TODO we may add information about the user
 * skeleton in the future.
 */
public class UserEvent {

	public PVector centerOfMass = new PVector();
	public long timestamp = 0;

}
