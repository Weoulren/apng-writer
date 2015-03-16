package ork.sevenstates.apng.filter;

import java.nio.ByteBuffer;

public class Paeth extends Filter {
	static final byte INDEX = 4;

    /**
     * PaethPredictor: <a href="http://www.w3.org/TR/PNG/#9-figure91">Figure 9.1 @ PNG Spec</a><br>
     * <img src="http://www.w3.org/TR/PNG/figures/fig91.svg"/><br>
     * <code>min { |p - a|, |p - b|, |p - c| }, p =  a + b - c</code>
     */
	private int paethPredictor(int a, int b, int c) {
		int p = a + b - c;			//it can be negative?
		
		int da = Math.abs(p - a);  // p<0 => d >> a ?? 
		int db = Math.abs(p - b);
		int dc = Math.abs(p - c);

		if (da <= db && da <= dc) {
			return a;
		} 
		
		if (db <= dc) {
			return b;
		} 
		
		return c;
	}

	@Override
	protected void encodeRow(ByteBuffer in, int srcOffset, ByteBuffer out, int len, int destOffset) {
		out.put(destOffset++, INDEX);
		
		// data[x, y] = data[x, y] - paethPredictor(data[x - 1, y], data[x, y - 1], data[x - 1, y - 1])
		
		int bpl = getWidth() * getBpp();
		for (int x = 0; x < bpl; x++) {
			int b = (in.get(x + srcOffset) & 0xff) - 
					paethPredictor(
							leftPixel(in, srcOffset, x), 
							abovePixel(in, srcOffset, x),
							aboveLeftPixel(in, srcOffset, x));
			out.put(x + destOffset, (byte) (b & 0xff));
		}
	}
}
