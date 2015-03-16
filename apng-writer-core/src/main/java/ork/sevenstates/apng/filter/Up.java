package ork.sevenstates.apng.filter;

import java.nio.ByteBuffer;

public class Up extends None {
	static final byte INDEX = 2;

	@Override
	protected void encodeRow(ByteBuffer in, int srcOffset, ByteBuffer out, int len, int destOffset) {
		if (srcOffset - len < 0) { //1 line: copy as is 
			super.encodeRow(in, srcOffset, out, len, destOffset);
		} else {
			int bpl = getWidth() * getBpp();
			out.put(destOffset++, INDEX);
			for (int x = 0; x < bpl; x++) {
				int b = (in.get(x + srcOffset) & 0xff) - (in.get(srcOffset + x - len) & 0xff);
				out.put(x + destOffset, (byte) (b & 0xff));
			}
		}

	}
}
