package ork.sevenstates.apng;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.RGBImageFilter;

class SubstractionFilter extends RGBImageFilter {
	private final BufferedImage prevWork;
	private final SupplData data;

	SubstractionFilter(BufferedImage prevWork, Dimension dOrig) {
		data = new SupplData(dOrig.width, dOrig.height);
		this.prevWork = prevWork;
	}

	@Override
	public int filterRGB(int x, int y, int rgb) {
		data.all++;
		int prev = prevWork.getRGB(x, y);
		if (rgb == prev) {
			data.modified++;
			return 0;
		}
		
		data.apply(x, y);
		
		return rgb;
	}

	public SupplData getData() {
		return data;
	}
}