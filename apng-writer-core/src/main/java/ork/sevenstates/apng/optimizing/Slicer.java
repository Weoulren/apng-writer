package ork.sevenstates.apng.optimizing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.logging.Logger;

import ork.sevenstates.apng.Tools;

public class Slicer extends Identity {
    private static final Logger LOGGER = Logger.getLogger(Slicer.class.getName());
    private BufferedImage previous;

    @Override
    public Map.Entry<Rectangle, BufferedImage> processImage(BufferedImage from) {
        BufferedImage prevWork = previous;
        previous = from;

        Dimension dOrig = Tools.dimsFromImage(from);

        if (prevWork == null) {
            return Tools.formatResult(from, dOrig);
        }

        SupplData data = new SupplData(dOrig.width, dOrig.height);

        if (calculateBounds(from, prevWork, dOrig, data)) {
            return Tools.formatResult(from, dOrig);
        }

        if (!data.accessedAtLeastOnce) {
            Dimension oneHellsing = new Dimension(1, 1);
            BufferedImage bi = createImage(oneHellsing);
            bi.setRGB(0, 0, from.getRGB(0, 0));
            return Tools.formatResult(bi, oneHellsing);
        }

        Dimension d = data.getSize();
        LOGGER.fine("Dimm size: " + dOrig.width + "x" + dOrig.height);
        LOGGER.fine("Actual size: " + d.width + "x" + d.height + " starting at " + data.topLeft.x + "," + data.topLeft.y);

        if (!d.equals(dOrig)) {
            BufferedImage bi = from.getSubimage(data.topLeft.x, data.topLeft.y, d.width, d.height);
            return Tools.formatResult(bi, new Rectangle(data.topLeft, d));
        }

        return Tools.formatResult(from, d);
    }

    private boolean calculateBounds(BufferedImage from, BufferedImage prevWork, Dimension dOrig, SupplData data) {
        int[] dataThis = (int[]) from.getRaster().getDataElements(0, 0, dOrig.width, dOrig.height, null);
        int[] dataPrev = (int[]) prevWork.getRaster().getDataElements(0, 0, dOrig.width, dOrig.height, null);

        data.all = dataThis.length;
        System.err.println(dataThis.length);
        int indexfw = 0;
        for (;indexfw < dataThis.length && dataThis[indexfw] == dataPrev[indexfw]; indexfw++);

        if (dataThis.length == indexfw) { //no data
            LOGGER.fine("no data");
            return false;
        }

        data.accessedAtLeastOnce = true;
        int indexbw = dataThis.length - 1;
        for (;dataThis[indexbw] == dataPrev[indexbw] && indexbw >= indexfw; indexbw--);

        int ys = indexfw / dOrig.width;
        int ye = indexbw / dOrig.width;

        int xs = indexfw % dOrig.width;
        int xe = indexbw % dOrig.width;

        if (xs == 0 && ys == 0 && xe + 1 == dOrig.width && ye + 1 == dOrig.height){
            return true;
        }

        indexfw = (ys + 1) * dOrig.width;//start of next row
        for (; indexfw < dataThis.length; indexfw+=dOrig.width) {
            for (int t = xs - 1; t >= 0; t--) {
                if (dataThis[indexfw + t] != dataPrev[indexfw + t]) {
                    xs = t;
                }
            }
            if (xs == 0)
                break;
        }
        indexbw = (ye - 1) * dOrig.width;//start of prev row
        for (; 0 <= indexbw; indexbw-=dOrig.width) {
            for (int t = xe + 1; t < dOrig.width; t++) {
                if (dataThis[indexbw + t] != dataPrev[indexbw + t]) {
                    xe = t;
                }
            }
            if (xe == dOrig.width - 1)
                break;
        }
        data.topLeft=new Point(xs, ys);
        data.bottomRight = new Point(xe, ye);

        if (data.getSize().equals(dOrig)) { //sad that to understand this we had to make all the way
            return true;
        }

        LOGGER.fine("Math for this one: xs,ys=" + xs + ","+ys + "; wxh=" + (xe - xs + 1) + "x" + (ye - ys + 1));
        return false;
    }
}
