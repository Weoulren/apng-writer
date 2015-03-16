package ork.sevenstates.apng.filter;

import java.nio.ByteBuffer;

public class Average extends Filter {
	static final byte INDEX = 3;

	@Override
	protected void encodeRow(ByteBuffer in, int srcOffset, ByteBuffer out, int len, int destOffset) {
		out.put(destOffset++, INDEX);
		
		int bpl = getWidth() * getBpp();
		for (int x = 0; x < bpl; x++) {
			int b = (in.get(x + srcOffset) & 0xff) - 
					((leftPixel(in, srcOffset, x) + abovePixel(in, srcOffset, x)) >>> 1);
			out.put(x + destOffset, (byte) (b & 0xff));
		}
	}

}
