package csi.client.gwt.viz.chart.model;

import com.google.gwt.core.client.JavaScriptObject;


public class AxisData extends JavaScriptObject {
    
    protected AxisData() {}
    
    public final native int getWidth() /*-{
        return this.axis.width;
    }-*/;

    public final native void disableOverflow() /*-{
        this.axis.labels.overflow = "false";
    }-*/;

    public final native void enableEndOnTick() /*-{
        this.axis.options.endOnTick = true;
    }-*/;
    
    public final native boolean isLast() /*-{
        return this.isLast;
    }-*/;
    
    public final native int marginRight()  /*-{
        return this.axis.right;
    }-*/;
}
