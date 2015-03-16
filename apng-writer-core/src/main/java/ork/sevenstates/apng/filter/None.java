package ork.sevenstates.apng.filter;

import java.nio.ByteBuffer;

public class None extends Filter {
	static final byte INDEX = 0;

	@Override
	protected void encodeRow(ByteBuffer in, int srcOffset, ByteBuffer out, int len, int destOffset) {
		out.put(destOffset++, INDEX);
		int bpl = getWidth() * getBpp();
		ByteBuffer tmp = in.duplicate();
		tmp.position(srcOffset).limit(srcOffset + bpl);
		out.position(destOffset);
		out.put(tmp);
	}

}
