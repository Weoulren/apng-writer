package ork.sevenstates.apng.optimizing;

import java.awt.Dimension;

/**
 * Deshrapneling strategy. Defines what filter to use to restore torn edges of subtracted images.
 * May affect compressibility hard.
 */
public enum DeshrapnelingStrategy {
    NONE {
        @Override
        int process(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
            return 0;
        }
    },
    BOUNDS_AWARE_9_PIXEL {
        @Override
        int process(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
            return deshrapneller(dataThis, dataPrev, size, counter);
        }
    },
    NINE_PIXEL {
        @Override
        int process(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
            return stripDeshrapneller(dataThis, dataPrev, size, counter);
        }
    },
    BOUNDS_AWARE_9_PIXEL_3PASS {
        @Override
        int process(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
            int reverted = 0;
            for (int j = 0; j < 3; j++)
                reverted = deshrapneller(dataThis, dataPrev, size, reverted);
            return reverted;
        }
    },
    NINE_PIXEL_3PASS {
        @Override
        int process(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
            int reverted = 0;
            for (int j = 0; j < 3; j++)
                reverted = stripDeshrapneller(dataThis, dataPrev, size, reverted);
            return reverted;
        }
    },
    FIVE_MATRIX {
        @Override
        int process(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
            return matrix5filter(dataThis, dataPrev, size, counter);
        }
    },
    BOUNDS_AWARE_9_PIXEL_WITH_5MATRIX {
        @Override
        int process(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
            int reverted = deshrapneller(dataThis, dataPrev, size, counter);
            return matrix5filter(dataThis, dataPrev, size, reverted);
        }
    },
    NINE_PIXEL_WITH_5MATRIX {
        @Override
        int process(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
            int reverted = stripDeshrapneller(dataThis, dataPrev, size, counter);
            return matrix5filter(dataThis, dataPrev, size, reverted);
        }
    },
    BOUNDS_AWARE_9_PIXEL_3PASS_WITH_5MATRIX {
        @Override
        int process(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
            int reverted = BOUNDS_AWARE_9_PIXEL_3PASS.process(dataThis, dataPrev, size, counter);
            return matrix5filter(dataThis, dataPrev, size, reverted);
        }
    },
    NINE_PIXEL_3PASS_WITH_5MATRIX {
        @Override
        int process(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
            int reverted = NINE_PIXEL_3PASS.process(dataThis, dataPrev, size, counter);
            return matrix5filter(dataThis, dataPrev, size, reverted);
        }
    };

    abstract int process(int[] dataThis, int[] dataPrev, Dimension size, int counter);

    static int deshrapneller(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
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

    static int stripDeshrapneller(int[] dataThis, int[] dataPrev, Dimension size, int counter) {
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


    static int matrix5filter(int[] dataThis, int[] dataPrev, Dimension dOrig, int counter) {
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
