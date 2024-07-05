package com.csi.chart.spi;

import java.awt.image.BufferedImage;

import com.csi.chart.ImageFactory;

/**
 * Provides an instance of an image.  This class constructs
 * {@link BufferedImage} instances with an alpha channel {@link BufferedImage#TYPE_INT_ARGB}.
 * 
 * @author Centrifuge Systems, Inc.
 *
 */
public class DefaultImageFactory
    implements ImageFactory
{

    @Override
    public BufferedImage createImage( int width, int height )
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return image;
    }

}
