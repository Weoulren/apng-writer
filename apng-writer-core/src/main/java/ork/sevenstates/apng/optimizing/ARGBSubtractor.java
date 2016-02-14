package ork.sevenstates.apng.optimizing;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ork.sevenstates.apng.Tools;

public class ARGBSubtractor implements Optimizer {
	public static final double DEFAULT_THRESHOLD = 0.6d;
    private static final Logger LOGGER = Logger.getLogger(ARGBSubtractor.class.getName());
    private BufferedImage previous;
    private Double threshold;
    private DeshrapnelingStrategy strategy;

    public ARGBSubtractor() {
        this(DEFAULT_THRESHOLD, DeshrapnelingStrategy.NINE_PIXEL_3PASS);
    }
    
    public ARGBSubtractor(Double threshold) {
        this(threshold, DeshrapnelingStrategy.NINE_PIXEL_3PASS);
    }

    public ARGBSubtractor(DeshrapnelingStrategy strategy) {
        this(DEFAULT_THRESHOLD, strategy);
    }

    public ARGBSubtractor(Double threshold, DeshrapnelingStrategy strategy) {
        this.threshold = threshold;
        setStrategy(strategy);
    }

    @Override
    public BufferedImage createImage(Dimension d) {
        return new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public Map.Entry<Rectangle, BufferedImage> processImage(BufferedImage from) {
        SupplData data = new SupplData(Tools.dimsFromImage(from));
        return processImageWithData(from, data);
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public DeshrapnelingStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(DeshrapnelingStrategy strategy) {
        this.strategy = strategy == null ? DeshrapnelingStrategy.NONE : strategy;
    }

    protected Map.Entry<Rectangle, BufferedImage> processImageWithData(BufferedImage from, SupplData data) {
        Dimension dim = Tools.dimsFromImage(from);
        BufferedImage prevWork = previous;
        previous = from;

        if (prevWork == null) {
            data.accessedAtLeastOnce = true;
            data.apply(0, 0);
            data.apply(dim.width - 1, dim.height - 1);
            return Tools.formatResult(from, dim);
        }

        int[] filtered = getFilteredData(prevWork, from, dim, data);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("all: " + data.all + ", mod: " + data.modified + ", which is " +
                    NumberFormat.getInstance(Locale.US).format(data.fractionModified() * 100d) + "%");
        }

        BufferedImage filteredImage = data2image(filtered, dim);

        if (threshold == null || data.fractionModified() > threshold.doubleValue()) {
            return Tools.formatResult(filteredImage, dim);
        }

        return Tools.formatResult(from, dim);
    }

    private BufferedImage data2image(int[] filtered, Dimension dim) {
		ColorModel rgbDefault = ColorModel.getRGBdefault();
		DataBuffer db = new DataBufferInt(filtered, filtered.length);
		SampleModel sm = rgbDefault.createCompatibleSampleModel(dim.width, dim.height);
		WritableRaster raster = (WritableRaster) Raster.createRaster(sm, db, null);
		return new BufferedImage(rgbDefault, raster, false, null);
	}

    private int[] getFilteredData(BufferedImage prevWork, BufferedImage thisWork, Dimension dOrig, SupplData data) {
        int[] dataThis = (int[]) thisWork.getRaster().getDataElements(0, 0, dOrig.width, dOrig.height, null);
        int[] dataPrev = (int[]) prevWork.getRaster().getDataElements(0, 0, dOrig.width, dOrig.height, null);
        data.all = dataThis.length;
        for (int i = 0; i < dataThis.length; i++) {
            if (dataThis[i] == dataPrev[i]) {
                dataThis[i] = 0;
                data.modified++;
            } else
                data.apply(i % dOrig.width, i / dOrig.width);
        }

        //shrapnelling filter
        int reverted = strategy.process(dataThis, dataPrev, dOrig, 0);
        LOGGER.fine("Deshrapnelling filter reverted " + reverted + " pixels");
        data.modified-=reverted;
        return dataThis;
    }

}
