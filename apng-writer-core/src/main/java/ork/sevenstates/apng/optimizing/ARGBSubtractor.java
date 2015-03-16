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

import ork.sevenstates.apng.Tools;

public class ARGBSubtractor implements Optimizer {
	public static final double DEFAULT_THRESHOLD = 0.6d;
    private BufferedImage previous;
    private Double threshold;

    public ARGBSubtractor() {
        this(DEFAULT_THRESHOLD);
    }
    
    public ARGBSubtractor(Double threshold) {
        this.threshold = threshold;
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

        System.err.println("all: " + data.all + ", mod: " + data.modified + ", which is " + 
        NumberFormat.getInstance(Locale.US).format(data.fractionModified()*100d) + "%");

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
        int reverted = 0;
        for (int j = 0; j < 3; j++)
            reverted = stripDeshrapneller(dataThis, dataPrev, dOrig, reverted);
        
//        reverted = matrix5filter(dataThis, dataPrev, dOrig, reverted);
        System.err.println(reverted);
        data.modified-=reverted;
        return dataThis;
    }


    private int deshrapneller(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
        for (int i = 0; i < dataThis.length; i++) { 
            if (dataThis[i] == 0) {
                int bits = 0; int count = 8;
                int x = i % size.width;
                int y = i / size.width;
                if (x > 0 && dataThis[i-1] != 0) {
                	if (dataThis[i-1] != 0) bits++;
                } else count--;
                if (y > 0 && x > 0) {
                    if (dataThis[i - size.width - 1] != 0) bits++;
                } else count--;
                if (y > 0) {
                	if (dataThis[i - size.width] != 0) bits++;
                } else count--;
                if (y > 0 && x < size.width - 1)  {
                	if (dataThis[i - size.width + 1] != 0) bits++;
                } else count--;
                if (x < size.width - 1) {
                    if (dataThis[i + 1] != 0) bits++;
                } else count--;
                if (y < size.height - 1 && x < size.width - 1) {
                	if (dataThis[i + size.width + 1] != 0) bits++;
                } else count--;
                if (y < size.height - 1) {
                	if (dataThis[i + size.width] != 0) bits++;
                } else count--;
                if (y < size.height - 1 && x > 0) {
                	if (dataThis[i + size.width - 1] != 0) bits++;
                } else count--;
                if (bits > count/2) {
                    dataThis[i] = dataPrev[i];
                    counter++;
                }
            }
        }
        return counter;
    }
    
    private int stripDeshrapneller(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
        for (int i = 0; i < dataThis.length; i++) { 
            if (dataThis[i] == 0) {
                int bits = 0; 
                if (i > 0 && dataThis[i-1] != 0)
					bits++;
				if (i > size.width && dataThis[i - size.width - 1] != 0)
					bits++;
				if (i >= size.width && dataThis[i - size.width] != 0)
					bits++;
				if (i - size.width + 1 > 0 && dataThis[i - size.width + 1] != 0)
					bits++;
				if (i + 1 < dataThis.length && dataThis[i + 1] != 0)
					bits++;
				if (i + size.width < dataThis.length && dataThis[i + size.width] != 0)
					bits++;
				if (i + size.width + 1 < dataThis.length && dataThis[i + size.width + 1] != 0)
					bits++;
				if (i + size.width - 1 < dataThis.length && dataThis[i + size.width - 1] != 0)
					bits++;
                if (bits > 4) {
                    dataThis[i] = dataPrev[i];
                    counter++;
                }
            }
        }
        return counter;
    }


    private int matrix5filter(int[] dataThis, int[] dataPrev, Dimension dOrig, int counter) {
        int winLen = 5;
        for (int i = 0; i < dataThis.length; i+=winLen-1) {
            int bits = 0;

            int x = i % dOrig.width;
            int y = i / dOrig.width;

            if (x + winLen > dOrig.width) {
                if (x + winLen == dOrig.width + dOrig.width % winLen){
                    x = dOrig.width - winLen;
                    i-=(winLen - (dOrig.width % winLen) - 1);
                } else {
                    i += (winLen-1) * dOrig.width - x - winLen + 1;
                    continue;
                }
            }

            if (y + winLen > dOrig.height) {
                y = dOrig.height - winLen;
            }

            for (int k = y; k < y + winLen; k++)
                for (int j = x; j < x + winLen; j++)
                    if (dataThis[k * dOrig.width + j] != 0)
                        bits++;

            if (bits > (winLen*winLen)/2 && bits != winLen*winLen) {
                for (int k = y; k < y + winLen; k++)
                    for (int j = x; j < x + winLen; j++)
                        if (dataThis[k * dOrig.width + j] == 0) {
                            counter++;
                            dataThis[k * dOrig.width + j] = dataPrev[k * dOrig.width + j];
                        }
            }
        }
        return counter;
    }
}
