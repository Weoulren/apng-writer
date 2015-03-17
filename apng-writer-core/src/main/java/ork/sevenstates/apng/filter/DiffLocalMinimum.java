package ork.sevenstates.apng.filter;

import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Map.Entry;

public class DiffLocalMinimum extends LocalMinimum {
	public static final byte INDEX = -1;
	@Override
	protected Entry<Integer, ByteBuffer> doCall(ByteBuffer src, int srcOffset, int len, Filter f) {
		ByteBuffer dfl = ByteBuffer.allocate(len + 1);

		f.encodeRow(src, srcOffset, dfl, len, 0);
		int e = 0;
		for (int i = 2; i < len + 1; i++) {
			e += Math.abs(dfl.get(i) - dfl.get(i - 1));
		}
		
		dfl.position(0);
		return new AbstractMap.SimpleImmutableEntry<>(e, dfl);
	}

}
