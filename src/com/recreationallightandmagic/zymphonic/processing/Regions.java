package com.recreationallightandmagic.zymphonic.processing;

import processing.core.PVector;

/**
 * A 3-dimensional rectangular grid of regions. Is capable: of given a position
 * in space, tell me what region I'm in (and how far in). X and Y are always
 * centered based on the camera's view.
 * 
 * TODO: For an arrangement of LEDs, get region for every LED and display
 * it.
 */
public class Regions {

	public float startingDepth, depthPerSection, widthPerSection, heightPerSection;
	public int numDepthSections, numHeightSections, numWidthSections;

	public SectionId getSectionId(PVector posiition) {
		// TODO: Write and test this
		return new SectionId(0, 0, 2, 0.1f, 0.2f, 2.2f);
	}

	// Simple bean to hold the section ID, along with the distance of the point
	// from the nearest boundary.
	public static class SectionId {
		public int x, y, z;
		public float depthX, depthY, depthZ;

		public SectionId(int x, int y, int z, float depthX, float depthY, float depthZ) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.depthX = depthX;
			this.depthY = depthY;
			this.depthZ = depthZ;
		}

		// Auto-generated hashCode and equal to use as keys in map.
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			result = prime * result + z;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SectionId other = (SectionId) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			if (z != other.z)
				return false;
			return true;
		}

	}
}
