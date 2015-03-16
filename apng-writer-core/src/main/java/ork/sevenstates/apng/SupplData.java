package ork.sevenstates.apng;

import java.awt.Dimension;
import java.awt.Point;

class SupplData {
	volatile long all;
	volatile long modified;
	Point topLeft;
	Point bottomRight = new Point(0, 0);
	volatile boolean accessedAtLeastOnce;
	SupplData(int width, int height) {
		topLeft = new Point(width, height);
	}
	SupplData(Dimension dim) {
		topLeft = new Point(dim.width, dim.height);
	}
	void apply(int x, int y) {
		accessedAtLeastOnce = true;
		if (topLeft.x > x) topLeft.x = x;
		if (topLeft.y > y) topLeft.y = y;
		if (bottomRight.x < x) bottomRight.x = x;
		if (bottomRight.y < y) bottomRight.y = y;
	}

	Dimension getSize() {
		if (!accessedAtLeastOnce) {
			return new Dimension();
		}
		return new Dimension(bottomRight.x - topLeft.x + 1, bottomRight.y - topLeft.y + 1);
	}
	
	double fractionModified() {
		return (double) modified / (double) all; 
	}
}