package ork.sevenstates.apng.filter;

import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.zip.Deflater;

public class DeflaterLocalMinimum extends LocalMinimum {
	public static final byte INDEX = -2;
	@Override
	protected Entry<Integer, ByteBuffer> doCall(ByteBuffer src, int srcOffset, int len, Filter f) {
		Deflater d = new Deflater();
		ByteBuffer dfl = ByteBuffer.allocate(len + 1);

		f.encodeRow(src, srcOffset, dfl, len, 0);
		d.setInput(dfl.array());
		d.finish();
		byte[] tmpbuf = new byte[len + 20];
		int writen = d.deflate(tmpbuf);
		if (!d.needsInput())
			writen += d.deflate(tmpbuf);
		d.end();
		dfl.position(0);
		return new AbstractMap.SimpleImmutableEntry<>(writen, dfl);
	}

}
