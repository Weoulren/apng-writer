package ork.sevenstates.apng;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class APNGMain {

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.err.println("Usage: java -jar apng-writer.jar <folder> <delay> <outputFile>");
		}
		File folder = new File(args[0]);
		
		String[] list = folder.list();
		Arrays.sort(list);
		
		APNGSeqWriter writer = new APNGSeqWriter(args[2], 0);
		int delay = Integer.parseInt(args[1]);
		int gcd = gcd(1000, delay);
		System.out.println(gcd);
		writer.setFpsDen(1000/gcd);
		writer.setFpsNum(delay/gcd);
		
		for (int i = 0; i < list.length; i++) {
			File f = new File(folder, list[i]);
			BufferedImage image = ImageIO.read(f);
			writer.writeImage(image);
		} 
		
		writer.close();
		
	}
	
	private static int gcd(int a, int b) { return b==0 ? a : gcd(b, a%b); }

}
