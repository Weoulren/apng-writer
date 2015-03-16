package ork.sevenstates.apng.filter;

import java.nio.ByteBuffer;

public class Sub extends Filter {
	static final byte INDEX = 1;

	@Override
	protected void encodeRow(ByteBuffer in, int srcOffset, ByteBuffer out, int len, int destOffset) {
		out.put(destOffset++, INDEX);
		
		int bpp = getBpp();
		int bpl = getWidth() * bpp;
		
		int x = 0;
		for (; x < bpp; x++) { //1 pixel: copy as is
			out.put(x + destOffset, in.get(x + srcOffset));
		}
		for (; x < bpl; x++) {
			int b = (in.get(x + srcOffset) & 0xff) - (in.get(x + srcOffset - bpp) & 0xff);
			out.put(x + destOffset, (byte) (b & 0xff));
		}

	}
}
