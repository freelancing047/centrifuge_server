package csi.config;

public class PopupConfig
        extends AbstractConfigurationSettings {

    private static int satisfiedCountMin = 1;
    private static int millisecondWaitMin = 100;

    protected int satisfiedCount = 10;
    protected int millisecondWait = 2000;

    public int getSatisfiedCount() {
        return satisfiedCount;
    }

    public void setSatisfiedCount(Integer satisfiedCountIn) {

        if (null != satisfiedCountIn) {

            satisfiedCount = Math.max(satisfiedCountIn, satisfiedCountMin);
        }
    }

    public int getMillisecondWait() {
        return millisecondWait;
    }

    public void setMillisecondWait(Integer millisecondWaitIn) {

        if (null != millisecondWaitIn) {

            millisecondWait = Math.max(millisecondWaitIn, millisecondWaitMin);
        }
    }
}
