package csi.server.business.visualization.graph;

import java.awt.image.BufferedImage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultImageProvider implements ImageProvider {
   private static final Logger LOG = LogManager.getLogger(DefaultImageProvider.class);

    private boolean allowAlpha;

    public DefaultImageProvider(boolean alphaSupport) {
        allowAlpha = alphaSupport;
    }

    @Override
    public BufferedImage create(int w, int h) {
        int type = allowAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        if (LOG.isTraceEnabled()) {
            String msg = String.format("Allocating rendering image.  Dimensions: (%1$d, %2$d), alpha? %3$b", w, h, allowAlpha);
            LOG.trace(msg);
        }

        BufferedImage bi = new BufferedImage(w, h, type);

        return bi;
    }

}
