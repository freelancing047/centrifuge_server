package csi.server.business.visualization.mapchart;

public class AMMapMovie {

    public String file; // this is the icon that will be used
    public int width = 10;
    public int height = 10;
    public String lattitude;
    public String longitude;
    public String description;
    public String type;
    public String title;
    public String value;

    public String alpha;
    public String color;

    public boolean fixed_size = false;

    // future attributes that can be set on a movie
    String text_box_x;
    String text_box_y;
    String text_box_width;
    String text_box_height;
}