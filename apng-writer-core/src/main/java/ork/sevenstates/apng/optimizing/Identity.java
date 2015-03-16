package ork.sevenstates.apng.optimizing;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Map;

import ork.sevenstates.apng.Tools;

public class Identity implements Optimizer {
    @Override
    public BufferedImage createImage(Dimension d) {
        return new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public Map.Entry<Rectangle, BufferedImage> processImage(BufferedImage from) {
        return Tools.formatResult(from, Tools.dimsFromImage(from));
    }
}
