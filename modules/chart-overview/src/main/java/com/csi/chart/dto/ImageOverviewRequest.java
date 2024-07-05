package com.csi.chart.dto;


import javax.ws.rs.DefaultValue;

public class ImageOverviewRequest extends BaseRequest
{
    @DefaultValue("600")
    public int width;
    
    @DefaultValue("100")
    public int height;
    
    /**
     * render with percentages for stacked charts....Not used right now.
     */
    public boolean absolute;  


}
