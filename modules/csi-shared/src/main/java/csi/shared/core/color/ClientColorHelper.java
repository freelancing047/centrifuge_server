package csi.shared.core.color;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;
import com.google.common.math.IntMath;
import com.google.gwt.core.client.GwtScriptOnly;
import com.google.gwt.user.client.rpc.IsSerializable;

public class ClientColorHelper {

    public static class Color implements IsSerializable {

         private float chroma;
         private float hue;
         private float intensity;
         private float value;
         private float lightness;
         private float luma;
         private float HSVsaturation;
         private float HSLsaturation;
         private int intColor;

        private  float red;

        private  float blue;

        private  float green;
        private  float alpha;
        


        public Color(int intColor) {
            this.intColor = intColor;
            red = int_to_red(intColor) / 255F;
            green = int_to_green(intColor) / 255F;
            blue = int_to_blue(intColor) / 255F;
            alpha = int_to_alpha(intColor) / 255F;
            float M = Math.max(Math.max(red, green), blue);
            float m = Math.min(Math.min(red, green), blue);
            chroma = M - m;
            float _hue = 0F;
            if (M == red) {
                _hue = ((green - blue) / chroma) % 6;
            } else if (M == green) {
                _hue = (((blue - red) / chroma) + 2) % 6;
            } else if (M == blue) {
                _hue = (((red - green) / chroma) + 4) % 6;
            }
            hue = IntMath.mod((int) (60 * _hue), 360);
            intensity = (red + green + blue) / 3F;
            value = M;
            lightness = (M + m) / 2L;
            luma = (.3F * red) + (.59F * green) + (.11F * blue);

            if (chroma == 0) {
                HSVsaturation = 0F;
                HSLsaturation = 0F;
            } else {
                HSVsaturation = chroma / value;
                HSLsaturation = chroma / (1 - Math.abs(((2 * lightness) - 1)));
            }
        }
        public Color(){

        }

        public int getBlue() {
            return Math.round(blue * 255)& 0xFF;
        }

        public float getChroma() {
            return chroma;
        }

        public int getGreen() {
            return Math.round(green * 255) & 0xFF;
        }

        public float getHSLsaturation() {
            return HSLsaturation;
        }

        public float getHSVsaturation() {
            return HSVsaturation;
        }

        public float getHue() {
            return hue;
        }

        public int getIntColor() {
            return intColor;
        }

        public float getIntensity() {
            return intensity;
        }

        public float getLightness() {
            return lightness;
        }

        public float getLuma() {
            return luma;
        }

        public int getRed() {
            return Math.round(red * 255) & 0xFF;
        }

        public float getValue() {
            return value;
        }

        @Override
        public String toString() {
            String r = Integer.toHexString(getRed());
            while (r.length() < 2) {
                r = "0" + r;
            }
            String g = Integer.toHexString(getGreen());
            while (g.length() < 2) {
                g = "0" + g;
            }
            String b = Integer.toHexString(getBlue());
            while (b.length() < 2) {
                b = "0" + b;
            }
            return "#" + r + g + b;
        }
    }

    private static ClientColorHelper instance;

    public static ClientColorHelper get() {
        if (instance == null) {
            instance = new ClientColorHelper();
        }
        return instance;
    }

    public static int int_to_alpha(int color) {
        return (color >> 24) % 256;
    }

    public static int hsv_to_int(float hue, float saturation, float value) {
        float chroma = value * saturation;
        float _hue = hue / 60;
        float x = chroma * (1 - Math.abs((_hue % 2) - 1));
        float m = value - chroma;
        if (_hue < 1) {
            return rgb_to_int(Math.round((chroma + m) * 255), Math.round((x + m) * 255), Math.round(m * 255));
        }
        if (_hue < 2) {
            return rgb_to_int(Math.round((x + m) * 255), Math.round((chroma + m) * 255), Math.round(m * 255));
        }
        if (_hue < 3) {
            return rgb_to_int(Math.round((m) * 255), Math.round((chroma + m) * 255), Math.round((x + m) * 255));
        }
        if (_hue < 4) {
            return rgb_to_int(Math.round((m) * 255), Math.round((x + m) * 255), Math.round((chroma + m) * 255));
        }
        if (_hue < 5) {
            return rgb_to_int(Math.round((x + m) * 255), Math.round((m) * 255), Math.round((chroma + m) * 255));
        }
        if (_hue < 6) {
            return rgb_to_int(Math.round((chroma + m) * 255), Math.round((m) * 255), Math.round((x + m) * 255));
        }
        return 0;
    }

    public static int int_to_blue(int color) {
        return ((color >> 0) % 256) & 0xFF;
    }

    public static int int_to_green(int color) {
        return ((color >> 8) % 256) & 0xFF;
    }

    public static int int_to_red(int color) {
        return ((color >> 16) % 256) & 0xFF;
    }

    public static int rgb_to_int(int red, int green, int blue) {
        int color = red;
        color = (color << 8) + green;
        color = (color << 8) + blue;
        return color;
    }

     private float golden_ratio_conjugate = 0.618033988749895F * 5;

    private Map<Integer, Color> colors;

    private double randomHue;
    private Random r = new Random();

    private ClientColorHelper() {
        colors = Maps.newTreeMap();
    }

    public Color make(int intColor) {
        Color color = colors.get(intColor);
        if (color == null) {
            color = new Color(intColor);
        }
        return color;
    }

    public Color makeFromRGB(int red, int green, int blue) {
        return make(rgb_to_int(red, green, blue));
    }

    public Color makeFromString(String color) {
        return make(Integer.parseInt(color));
    }

    public Color makeFromHex(String color) {
        return make(Integer.parseInt(color, 16));
    }

    public Color randomHueWheel(float saturation, float value) {
        if(randomHue == 0){
            randomHue = r.nextDouble();
        }
        randomHue += golden_ratio_conjugate * 5;
        randomHue -= (int) Math.floor(randomHue);
        float hue = IntMath.mod((int) (randomHue * 360), 360);
        return make(hsv_to_int(hue, saturation, value));
    }

    public Color randomHueWheel() {
        return randomHueWheel((float) ((r.nextDouble() / 5F) + .7F), (float) ((r.nextDouble() / 4F) + .75F));
    }
}
