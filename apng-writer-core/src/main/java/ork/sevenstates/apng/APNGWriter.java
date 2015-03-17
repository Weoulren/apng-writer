package ork.sevenstates.apng;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

import ork.sevenstates.apng.optimizing.Identity;
import ork.sevenstates.apng.optimizing.Optimizer;

public class APNGWriter extends AbstractAPNGWriter {
	private final WritableByteChannel out;
	private int frameCount = 0;

	private boolean initialized;
	private int frameCountEnd;

	public APNGWriter(File f, int alg) throws FileNotFoundException {
		this(f, alg, new Identity());
	}
	
	public APNGWriter(String fName, int alg) throws FileNotFoundException {
		this(new File(fName), alg);
	}
	
	@SuppressWarnings("resource") //Channel will close stream
	public APNGWriter(File f, int alg, Optimizer optimizer) throws FileNotFoundException {
		this(new FileOutputStream(f).getChannel(), alg, optimizer);
	}
	
	public APNGWriter(OutputStream os, int alg, Optimizer optimizer) {
		this(Channels.newChannel(os), alg, optimizer);
	}
	
	public APNGWriter(WritableByteChannel out, int alg, Optimizer optimizer) {
		super(alg, optimizer);
		this.out = out;
	}

	public APNGWriter(String fName, int alg, Optimizer optimizer) throws FileNotFoundException {
		this(new File(fName), alg, optimizer);
	}
	
	public APNGWriter(OutputStream os, int alg) {
		this(Channels.newChannel(os), alg, new Identity());
	}
	
	public APNGWriter(WritableByteChannel out, int alg) {
		this(out, alg, new Identity());
	}
	
	private void ensureInitialized() throws IOException {
		if (!initialized) {
			throw new IOException("Stream is uninitialized");
		}
	}
	
	public void writeImage(BufferedImage img) throws IOException {
		ensureOpen();
		ensureInitialized();
		super.writeImage(img);
	}
	
	public void writeHeader(Dimension size, int frameCount, int loopCount) throws IOException {
		ensureOpen();
		if (initialized) 
			throw new IOException("Already initialized");
		
		BufferedImage container = optimizer.createImage(size);
		byte numPlanes = (byte) container.getRaster().getNumBands();
		byte bitsPerPlane = 8;
		
		out.write(ByteBuffer.wrap(Consts.getPNGSIGArr()));
		out.write(makeIHDRChunk(size, numPlanes, bitsPerPlane));
		out.write(make_acTLChunk(frameCount, loopCount));
		frameCountEnd = frameCount;
		initialized = true;
	}

	public void writeImage(Image img, Dimension size, int fpsNum, int fpsDen) throws IOException {
		ensureOpen();
		ensureInitialized();
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
		
		out.write(makeFCTL(key, fpsNum, fpsDen, frameCount != 0));
		out.write(makeDAT(frameCount == 0 ? Consts.IDAT_SIG : Consts.fdAT_SIG, buffer));
		frameCount++;
	}

	public void close() throws IOException {
		if (closed)
			return;
		
		ensureInitialized();
		
		if (frameCount != frameCountEnd)
			throw new IOException("expected and real image count mismatch: " + frameCount + "!=" + frameCountEnd);
		
		super.close(); // we don't need any resources of parent to finish 
		
		//IEND
		out.write(ByteBuffer.wrap(Consts.getIENDArr()));
		out.close();
	}
	
}