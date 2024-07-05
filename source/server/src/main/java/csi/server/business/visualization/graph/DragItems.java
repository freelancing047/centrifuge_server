package csi.server.business.visualization.graph;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import prefuse.visual.VisualItem;

public class DragItems {

    public List<VisualItem> items = new ArrayList<VisualItem>();
    public int imageX;
    public int imageY;
    public int startX;
    public int startY;
    public Rectangle2D bounds;
    public int imageW;
    public int imageH;
    public Rectangle2D clipArea;

}
