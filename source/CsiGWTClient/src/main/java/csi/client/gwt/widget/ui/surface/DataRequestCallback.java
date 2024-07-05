package csi.client.gwt.widget.ui.surface;

public interface DataRequestCallback {

    /**
     * Callback that requests data for the given coordinates.
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     */
    public void getData(int x1, int x2, int y1, int y2);

    public void requestDataFor(int x1, int x2, int y1, int y2);
}