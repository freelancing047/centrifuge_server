package csi.server.business.visualization.mapchart;

public class LatLong {

    protected double latitude;
    protected double longitude;

    public LatLong(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

}
