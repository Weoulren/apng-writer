package ork.sevenstates.apng.filter;

import java.nio.ByteBuffer;

public abstract class Filter {
	private int width;
	private int height;
	private int bpp;
	
	protected Filter() { }

	public int getBpp() {
		return bpp;
	}

	public void setBpp(int newBpp) {
		bpp = newBpp;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int newHeight) {
		height = newHeight;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int newWidth) {
		this.width = newWidth;
	}

	public void close() {}

	public void encode(ByteBuffer in, ByteBuffer out) {
		checkSize(in, out);
		int byteRowLen = width * bpp;
		for (int y = 0; y < height; y++) {
			int yoffset = y * byteRowLen;
			encodeRow(in, yoffset, out, byteRowLen, yoffset + y);
		}
	}

	protected abstract void encodeRow(ByteBuffer in, int srcOffset, ByteBuffer out, int len, int destOffset);

	protected void checkSize(ByteBuffer in, ByteBuffer out) {
		if (out.capacity() != (width * bpp + 1) * height) {
			throw new IllegalArgumentException("Invalid output buffer capacity: capacity != (width*bpp+1)*height, "
																+ out.capacity() + "!=" + (width * bpp + 1) * height);
		}
		if (in.remaining() != width * height * bpp) {
			throw new IllegalArgumentException("Invalid input buffer capacity: capacity != width*bpp*height, "
					+ in.capacity() + "!=" + width * bpp * height);
		}
	}
	
	protected int leftPixel(ByteBuffer in, int srcOffset, int x) {
		return x >= bpp ? in.get(srcOffset + x - bpp) & 0xff : 0;
	}

	protected int abovePixel(ByteBuffer in, int srcOffset, int x) {
		return srcOffset >= width * bpp ? in.get(srcOffset + x - width * bpp) & 0xff : 0;
	}
	
	protected int aboveLeftPixel(ByteBuffer in, int srcOffset, int x) {
		return srcOffset >= width * bpp && x >= bpp ? in.get(srcOffset + x - (width + 1) * bpp) & 0xff : 0;
	}

}
