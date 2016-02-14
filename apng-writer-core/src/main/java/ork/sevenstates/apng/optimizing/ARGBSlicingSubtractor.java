package ork.sevenstates.apng.optimizing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.logging.Logger;

import ork.sevenstates.apng.Tools;

public class ARGBSlicingSubtractor extends ARGBSubtractor {
    private static final Logger LOGGER = Logger.getLogger(Slicer.class.getName());
    public ARGBSlicingSubtractor(Double threshold) {
        super(threshold);
    }

    public ARGBSlicingSubtractor(DeshrapnelingStrategy strategy) {
        super(strategy);
    }

    public ARGBSlicingSubtractor(Double threshold, DeshrapnelingStrategy strategy) {
        super(threshold, strategy);
    }

    @Override
    public Map.Entry<Rectangle, BufferedImage> processImage(BufferedImage from) {
        SupplData data = new SupplData(Tools.dimsFromImage(from));
        Map.Entry<Rectangle,BufferedImage> result = processImageWithData(from, data);

        Rectangle rect = result.getKey();
        if (!data.accessedAtLeastOnce) {
            Dimension oneHellsing = new Dimension(1, 1);
            BufferedImage bi = createImage(oneHellsing);
            bi.setRGB(0, 0, 0);
            return Tools.formatResult(bi, oneHellsing);
        }

        Dimension d = data.getSize();
        LOGGER.fine("Dimm size: " + rect.width + "x" + rect.height);
        LOGGER.fine("Actual size: " + d.width + "x" + d.height + " starting at " + data.topLeft.x + "," + data.topLeft.y);

        if (!d.equals(rect.getSize())) {
            BufferedImage bi = result.getValue().getSubimage(data.topLeft.x, data.topLeft.y, d.width, d.height);
            return Tools.formatResult(bi, new Rectangle(data.topLeft, d));
        } else {
            return result;
        }
    }


}
