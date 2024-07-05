package csi.config;

@Deprecated
public class TimePlayerConfig
    extends AbstractConfigurationSettings
{

    private static final float DEFAULT_ALPHA        = 0.3f;

    private static final int   DEFAULT_SLOW_SPEED   = 10000;

    private static final int   DEFAULT_MEDIUM_SPEED = 5000;

    private static final int   DEFAULT_FAST_SPEED   = 1000;

    private int                slowSpeed;
    private int                mediumSpeed;
    private int                fastSpeed;

    private Float              alphaLevel;

    public int getSlowSpeed() {
        if (slowSpeed <= 0) {
            slowSpeed = DEFAULT_SLOW_SPEED;
        }
        return slowSpeed;
    }

    public void setSlowSpeed(int slowSpeed) {
        this.slowSpeed = slowSpeed;
    }

    public int getMediumSpeed() {
        if (mediumSpeed <= 0) {
            mediumSpeed = DEFAULT_MEDIUM_SPEED;
        }
        return mediumSpeed;
    }

    public void setMediumSpeed(int mediumSpeed) {
        this.mediumSpeed = mediumSpeed;
    }

    public int getFastSpeed() {
        if (fastSpeed <= 0) {
            fastSpeed = DEFAULT_FAST_SPEED;
        }
        return fastSpeed;
    }

    public void setFastSpeed(int fastSpeed) {
        this.fastSpeed = fastSpeed;
    }

    public float getAlphaLevel() {
        if (alphaLevel == null) {
            return DEFAULT_ALPHA;
        }

        return this.alphaLevel.floatValue();
    }

    public void setAlphaLevel(float value) {
        alphaLevel = Float.valueOf(value);
    }
}
