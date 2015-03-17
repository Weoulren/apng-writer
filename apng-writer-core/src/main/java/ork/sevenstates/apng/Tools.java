package ork.sevenstates.apng;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.AbstractMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class Tools {

	public static BufferedImage paintImage(Image src, BufferedImage dst) {
		Graphics2D g2d = dst.createGraphics();
		g2d.drawImage(src, 0, 0, null);
		g2d.dispose();
		return dst;
	}

	public static Dimension dimsFromImage(BufferedImage bi) {
		return new Dimension(bi.getWidth(), bi.getHeight());
	}

	public static Map.Entry<Rectangle, BufferedImage> formatResult(BufferedImage source, Dimension d) {
		return new AbstractMap.SimpleImmutableEntry<>(new Rectangle(d), source);
	}

	public static Map.Entry<Rectangle, BufferedImage> formatResult(BufferedImage source, Rectangle r) {
		return new AbstractMap.SimpleImmutableEntry<>(r, source);
	}

	public static ByteBuffer compress(ByteBuffer in, int level) {
		int remaining = in.remaining();
		Deflater deflater = new Deflater(remaining > 42 ? level : 0);

		int size = remaining + 20;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
		DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater, 0x2000, false);
		WritableByteChannel wbc = Channels.newChannel(dos);
		try {
			wbc.write(in);
			dos.finish();
			dos.flush();
			dos.close();
		} catch (IOException e) {
			throw new IllegalStateException("Lolwut?!", e);
		}

		return ByteBuffer.wrap(baos.toByteArray());
	}
}
