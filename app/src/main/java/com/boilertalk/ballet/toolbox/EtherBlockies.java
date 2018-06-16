package com.boilertalk.ballet.toolbox;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.graphics.ColorUtils;
import android.util.Log;

import java.util.Arrays;


public class EtherBlockies {
    private long[] randseed = new long[4];
    int color, bgcolor, spotcolor, size;
    double[] imageData;


    public EtherBlockies(char[] seed, int size, int scale) {
        Log.d("SDEFTREE", "SEED: " + Arrays.toString(seed));
        seedrand(seed);

        this.color = createColor();
        this.bgcolor = createColor();
        this.spotcolor = createColor();

        this.size = size;

        this.imageData = createImageData(size);
    }

    private void seedrand(char[] seed) {
        for (int i = 0; i < randseed.length; i++) {
           randseed[i] = 0;
        }

        for(int i = 0; i < seed.length; i++) {
            randseed[i%4] = 0xffffffffL & (((randseed[i%4] << 5) - randseed[i%4]) + seed[i]);
        }
    }

    private double rand() {
        int t = (int) (randseed[0] ^ (0xffffffffL & (randseed[0] << 11)));

        randseed[0] = randseed[1];
        randseed[1] = randseed[2];
        randseed[2] = randseed[3];
        int t3 = (int) (randseed[3] & 0xffffffffL);
        randseed[3] = t3 ^ (t3 >> 19) ^ t ^ (t >> 8);

        double retV = ((double)randseed[3])/ (double)(1L << 31);
        return retV;
    }

    private int createColor() {
        float[] hsl = new float[3];

        hsl[0] = (float) Math.floor(rand() * 360);
        hsl[1] = (float) ((rand() * 60) + 40)/100;
        hsl[2] = (float) ((rand()+rand()+rand()+rand()) * 25)/100;

        Log.d("SSDDSEE", "H: " + Float.toString(hsl[0]) + ", S: " + Float.toString(hsl[1]) +
                ", L: " + Float.toString(hsl[2]));

        return ColorUtils.HSLToColor(hsl);
    }

    private double[] createImageData(int size) {
        int dataWidth = (int) Math.ceil(size / 2);
        int mirrorWidth = size - dataWidth;

        double[] data = new double[size*size];
        for(int i = 0; i < size; i++) {
            double[] row = new double[size];
            for(int j = 0; j < dataWidth; j++) {
                row[j] = Math.floor(rand()*2.3);
            }
            double[] subrow = Arrays.copyOfRange(row, 0, mirrorWidth);
            for(int k = 0; k < mirrorWidth; k++) {
                row[k+dataWidth] = subrow[subrow.length - 1 - k];
            }
            for(int l = 0; l < size; l++) {
                data[l + (i*size)] = row[l];
            }
        }
        return data;
    }

    public int[] getColors() {
        int[] colors = new int[imageData.length];
        for(int i = 0; i < imageData.length; i++) {
            int colonum = (int) imageData[i];
            switch(colonum) {
                case 1:
                    colors[i] = color;
                    break;
                case 2:
                    colors[i] = spotcolor;
                    break;
                default:
                    colors[i] = bgcolor;
            }
        }
        return colors;
    }

    public Bitmap getBitmap() {
        return Bitmap.createBitmap(getColors(), size, size, Bitmap.Config.ARGB_8888);
    }

    public void printinfo() {
        Log.d("DHST", "COLOR: " + color + ",BG: " + bgcolor + ",SPOT: " + spotcolor);
    }
}
