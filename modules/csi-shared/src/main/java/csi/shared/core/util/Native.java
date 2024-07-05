package csi.shared.core.util;

/**
 * Created by Ivan on 10/16/2017.
 */
public class Native {

    public static native void logXY(int x1, int x2, int y1, int y2)/*-{
        console.log("Start: (" +x1 + ", "+y1+")" + "\t End: (" + x2 + ", " + y2 +")");
    }-*/;

    public static native void logXY(double x1, double x2, double y1, double y2)/*-{
        console.log("Start: (" +x1 + ", "+y1+")" + "\t End: (" + x2 + ", " + y2 +")");
    }-*/;

    public static native void log(String text)/*-{
        console.log(text);
    }-*/;

    public static native void error(String text)/*-{
    console.error(text);
    }-*/;

    public static native void info(String text)/*-{
        console.info(text);
    }-*/;

}
