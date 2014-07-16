package com.recreationallightandmagic.zymphonic.processing;

import processing.core.PVector;

/**
 * A 3-dimensional rectangular grid of regions. Is capable: of given a position
 * in space, tell me what region I'm in (and how far in). X and Y are always
 * centered based on the camera's view.
 * 
 * TODO: For an arrangement of LEDs, get region for every LED and display it.
 */
public class Regions {

	public float startingDepth, heightOffset, depthPerSection, widthPerSection,
			heightPerSection;

	public Regions(float startingDepth, float heightOffset,
			float depthPerSection, float widthPerSection, float heightPerSection) {
		this.startingDepth = startingDepth;
		this.heightOffset = heightOffset;
		this.depthPerSection = depthPerSection;
		this.widthPerSection = widthPerSection;
		this.heightPerSection = heightPerSection;
	}

	public SectionId getSectionId(PVector position) {
		if (position == null) {
			return null;
		}
		if (position.z < startingDepth) {
			return null;
		}
		return new SectionId((int) Math.floor(position.x / widthPerSection),
				(int) Math
						.floor((position.y - heightOffset) / heightPerSection),
				(int) Math
						.floor((position.z - startingDepth) / depthPerSection),
				0, 0, 0);
	}


	// Simple bean to hold the section ID, along with the distance of the point
	// from the nearest boundary.
	public static class SectionId {
		public int x, y, z;
		public float depthX, depthY, depthZ;

		public SectionId(int x, int y, int z, float depthX, float depthY,
				float depthZ) {
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

		@Override
		public String toString() {
			return "SectionId [x=" + x + ", y=" + y + ", z=" + z + "]";
		}
		
		

	}
}
