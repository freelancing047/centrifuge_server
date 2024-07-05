package csi.server.business.service.widget.processor.core;

/**
 * Graph nodes data
 */
public class GoogleMapsData {

    /**
     * Latitudes
     */
    private String[] lat;

    /**
     * Longitudes
     */
    private String[] lng;

    /**
     * Connector start point index
     */
    private String[] startPoint;

    /**
     * Connector end point index
     */
    private String[] endPoint;

    /**
     * Point image URL
     */
    private String[] imagePath;

    /**
     * Points to image index
     */
    private String[] displayImage;

    /**
     * Tooltip for current node
     */
    private String[] toolTip;

    private String rguuid;

    public String getRguuid() {
        return rguuid;
    }

    public void setRguuid(String rguuid) {
        this.rguuid = rguuid;
    }

    public String[] getToolTip() {
        return toolTip;
    }

    public void setToolTip(String[] toolTip) {
        this.toolTip = toolTip;
    }

    public String[] getDisplayImage() {
        return displayImage;
    }

    public void setDisplayImage(String[] displayImage) {
        this.displayImage = displayImage;
    }

    public String[] getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String[] startPoint) {
        this.startPoint = startPoint;
    }

    public String[] getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String[] endPoint) {
        this.endPoint = endPoint;
    }

    public String[] getLat() {
        return lat;
    }

    public void setLat(String[] lat) {
        this.lat = lat;
    }

    public String[] getLng() {
        return lng;
    }

    public void setLng(String[] lng) {
        this.lng = lng;
    }

    public String[] getImagePath() {
        return imagePath;
    }

    public void setImagePath(String[] imagePath) {
        this.imagePath = imagePath;
    }
}
