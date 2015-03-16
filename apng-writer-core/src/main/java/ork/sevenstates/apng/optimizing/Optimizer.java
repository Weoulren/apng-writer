package ork.sevenstates.apng.optimizing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public interface Optimizer {

    BufferedImage createImage(Dimension d);

    Map.Entry<Rectangle,BufferedImage> processImage(BufferedImage from);
}
