package ork.sevenstates.apng;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import ork.sevenstates.apng.optimizing.ARGBSlicingSubtractor;

public class APNGZipMain {

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.err.println("Usage: java -jar apng-writer.jar <zipFile> <delay> <outputFile>");
		}
		File zip = new File(args[0]);
		ZipFile zipFile = new ZipFile(zip);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		Set<ZipEntry> zes = new TreeSet<ZipEntry>(new ZipEntryNameComparator());
		while (entries.hasMoreElements()) {
			zes.add(entries.nextElement());
			
		}
		long time = System.currentTimeMillis();
		APNGWriter writer = new APNGWriter(args[2], -2, new ARGBSlicingSubtractor(null));
		int delay = Integer.parseInt(args[1]);
		int gcd = gcd(1000, delay);
		System.out.println(gcd);
		writer.setFpsDen(1000/gcd);
		writer.setFpsNum(delay/gcd);
		
		boolean init = false;

		for (ZipEntry zipEntry : zes) {
			System.out.println(zipEntry.getName());
			InputStream inputStream = zipFile.getInputStream(zipEntry);
			ImageInputStream iis = ImageIO.createImageInputStream(inputStream);
			
			BufferedImage image = ImageIO.read(iis);

			inputStream.close();
			
			if (!init) {
				writer.writeHeader(new Dimension(image.getWidth(), image.getHeight()), zes.size(), 0);
				init = true;
			}
			
			writer.writeImage(image);
		}
		
		writer.close();
		zipFile.close();
		System.out.println(System.currentTimeMillis() - time);
	}
	
	private static int gcd(int a, int b) { return b==0 ? a : gcd(b, a%b); }
	
	private static class ZipEntryNameComparator implements Comparator<ZipEntry> {

		@Override
		public int compare(ZipEntry o1, ZipEntry o2) {
			return o1.getName().compareTo(o2.getName());
		}
		
	}

}
