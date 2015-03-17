package ork.sevenstates.apng;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

import ork.sevenstates.apng.optimizing.Identity;
import ork.sevenstates.apng.optimizing.Optimizer;

public class APNGSeqWriter extends AbstractAPNGWriter {
	private final FileChannel out;
	private long actlBlockOffset = 0;

	public APNGSeqWriter(File f, int alg) throws FileNotFoundException {
		this(f, alg, new Identity());
	}
	
	public APNGSeqWriter(String fName, int alg) throws FileNotFoundException {
		this(new File(fName), alg);
	}
	
	@SuppressWarnings("resource") //Channel will close stream
	public APNGSeqWriter(File f, int alg, Optimizer optimizer) throws FileNotFoundException {
		super(alg, optimizer);
		out = new RandomAccessFile(f, "rw").getChannel();
	}

	public APNGSeqWriter(String fName, int alg, Optimizer optimizer) throws FileNotFoundException {
		this(new File(fName), alg, optimizer);
	}

	public void writeImage(Image img, Dimension size, int fpsNum, int fpsDen) throws IOException {
		ensureOpen();
		if (img == null) {
			throw new IOException("Image is null");
		}

		BufferedImage container = optimizer.createImage(size);
		Tools.paintImage(img, container);

		Map.Entry<Rectangle, BufferedImage> bi = optimizer.processImage(container);
		
//		ImageIO.write(bi.getValue(), "png", new File("debug." + System.currentTimeMillis() + ".png"));

		BufferedImage value = bi.getValue();
		Rectangle key = bi.getKey();
		ByteBuffer buffer = getPixelBytes(value, key.getSize());
		
		
		byte numPlanes = (byte) value.getRaster().getNumBands();
		byte bitsPerPlane = 8;

		if (frameCount == 0) {
			out.write(ByteBuffer.wrap(Consts.getPNGSIGArr()));

			out.write(makeIHDRChunk(key.getSize(), numPlanes, bitsPerPlane));

			actlBlockOffset = out.position();
			out.write(ByteBuffer.wrap(Consts.getacTLArr())); // empty here, filled later
		}

		
		out.write(makeFCTL(key, fpsNum, fpsDen, frameCount != 0));
		out.write(makeDAT(frameCount == 0 ? Consts.IDAT_SIG : Consts.fdAT_SIG, buffer));
		frameCount++;
	}



	public void close() throws IOException {
		//IEND
		out.write(ByteBuffer.wrap(Consts.getIENDArr()));

		long point = out.position();
		
		//frame count
		out.position(actlBlockOffset);
		out.write(make_acTLChunk(frameCount, 0));
		
		super.close();
		out.truncate(point);
		out.close();
	}

}