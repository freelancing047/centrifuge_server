package csi.server.business.visualization.graph;

import java.awt.image.BufferedImage;

public interface ImageProvider {

    BufferedImage create(int w, int h);
}
