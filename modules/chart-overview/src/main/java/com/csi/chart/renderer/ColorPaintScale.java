package com.csi.chart.renderer;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.renderer.GrayPaintScale;

public class ColorPaintScale
    extends GrayPaintScale
{
    
    protected int color;

    public ColorPaintScale(int rgb) {
        super();
    }

    public ColorPaintScale(double lowerBound, double upperBound) {
        super(lowerBound, upperBound);
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Paint getPaint( double value )
    {
        Paint paint = super.getPaint(value);
        if( paint instanceof Color ) {
            Color c = (Color) paint;
            int i = c.getBlue();
            
            int alpha = i;
            int updated = ((alpha & 0xFF) <<24) | color;
            paint = new Color(updated, true);
        }
        
        return paint;
    }
    
    public int getColor()
    {
        return color;
    }

    public void setColor( int color )
    {
        this.color = (0x00FFFFFF & color);
    }


    

}
