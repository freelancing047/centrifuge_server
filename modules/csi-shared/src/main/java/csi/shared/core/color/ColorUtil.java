/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.shared.core.color;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColorUtil {

    /**
     * @param rgb
     * @return HSL equivalent of rgb.
     */
    public static HSL RGB2HSL(int rgb) {
        int rgbR = (rgb & 0xFF0000) >> 16;
        int rgbG = (rgb & 0xFF00) >> 8;
        int rgbB = (rgb & 0xFF);

        double r = rgbR / 255.0;
        double g = rgbG / 255.0;
        double b = rgbB / 255.0;
        double v;
        double m;
        double vm;
        double r2, g2, b2;

        double h = 0; // default to black
        double s = 0;
        double l = 0;

        v = (r > g) ? r : g;
        v = v > b ? v : b;
        m = r < g ? r : g;
        m = m < b ? m : b;

        l = (m + v) / 2.0;

        if (l <= 0.0) {
            return new HSL(h, s, l);
        }

        vm = v - m;
        s = vm;
        if (s > 0.0) {
            s /= (l <= 0.5) ? (v + m) : (2.0 - v - m);
        } else {
            return new HSL(h, s, l);
        }

        r2 = (v - r) / vm;
        g2 = (v - g) / vm;
        b2 = (v - b) / vm;
        if (r == v) {
            h = (g == m ? 5.0 + b2 : 1.0 - g2);
        }

        else if (g == v) {
            h = (b == m ? 1.0 + r2 : 3.0 - b2);
        }

        else {
            h = (r == m ? 3.0 + g2 : 5.0 - r2);
        }

        h /= 6.0;
        return new HSL(h, s, l);
    }

    /**
     * @param hsl
     * @return RGB equivalent of HSL.
     */
    public static int HSL2RGB(HSL hsl) {

        double h = hsl.h;
        double sl = hsl.s;
        double l = hsl.l;

        double v;
        double r, g, b;

        r = l; // default to gray
        g = l;
        b = l;
        v = (l <= 0.5) ? (l * (1.0 + sl)) : (l + sl - l * sl);

        if (v > 0) {
            double m;
            double sv;
            int sextant;

            double fract, vsf, mid1, mid2;

            m = l + l - v;
            sv = (v - m) / v;
            h *= 6.0;
            sextant = (int) h;
            fract = h - sextant;
            vsf = v * sv * fract;
            mid1 = m + vsf;
            mid2 = v - vsf;
            switch (sextant) {

                case 0:
                    r = v;
                    g = mid1;
                    b = m;
                    break;

                case 1:
                    r = mid2;
                    g = v;
                    b = m;
                    break;

                case 2:
                    r = m;
                    g = v;
                    b = mid1;
                    break;

                case 3:
                    r = m;
                    g = mid2;
                    b = v;
                    break;

                case 4:
                    r = mid1;
                    g = m;
                    b = v;
                    break;

                case 5:
                    r = v;
                    g = m;
                    b = mid2;
                    break;
            }
        }

        int ro = (int) (r * 255.0d);
        int go = (int) (g * 255.0d);
        int bo = (int) (b * 255.0d);

        return 0xFF000000 | ro << 16 | go << 8 | bo;
    }

    public static class HSL {

        private double h;
        private double s;
        private double l;

        public HSL(double h, double s, double l) {
            super();
            this.h = h > 1.0 ? 1.0 : h;
            this.s = s > 1.0 ? 1.0 : s;
            this.l = l > 1.0 ? 1.0 : l;
        }

        public double getH() {
            return h;
        }

        public double getS() {
            return s;
        }

        public double getL() {
            return l;
        }

    }

    public static String toColorString(int rgb) {
        return toColorString((rgb & 0x00FF0000) >> 16, (rgb & 0xFF00) >> 8, rgb & 0xFF);
    }

    public static String toColorString(int r, int g, int b) {
        return "#" + toHexString(r) + toHexString(g) + toHexString(b);
    }

    private static String toHexString(int value) {
        String str = Integer.toHexString(value);
        return str.length() == 1 ? "0" + str : str;
    }

    public static String toColorString(HSL color) {
        return toColorString(HSL2RGB(color));
    }

    /**
     * @param color Hex color string with or without '#'
     * @return
     */
    public static HSL toHSL(String color) {
        return RGB2HSL(toRGB(color));
    }

    public static int toRGB(String color) {
        String str = color.startsWith("#") ? color.substring(1) : color;
        int r = Integer.parseInt(str.substring(0, 2), 16);
        int g = Integer.parseInt(str.substring(2, 4), 16);
        int b = Integer.parseInt(str.substring(4, 6), 16);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static HSL getContrastingGrayScale(HSL hsl) {
        double lum = hsl.getL();
        lum = lum > 0.5 ? 0.0 : 1.0;
        return new HSL(0.0, 0.0, lum);
    }

}
