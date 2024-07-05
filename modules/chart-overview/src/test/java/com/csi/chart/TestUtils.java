package com.csi.chart;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;

public class TestUtils
{

    public static Image renderImage( JFreeChart chart, int width, int height )
    {
        Plot plot = chart.getPlot();
    
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Double area = new Rectangle2D.Double(0, 0, width, height);
        
        Graphics2D g2d = image.createGraphics();
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
        // image = chart.createBufferedImage(width, height);
        plot.draw(g2d, area, null, null, null);
        return image;
    
    }

}
