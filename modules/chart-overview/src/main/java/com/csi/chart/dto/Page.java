package com.csi.chart.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.Vector;

public class Page
{
    protected int page;
    protected int size;
    protected List<Map> points;
    
    public Page(int page, int preferredSize) {
        this.page = page;
        this.size = 0;
        this.points = new ArrayList<Map>();
    }

    public int getPage()
    {
        return page;
    }


    public int getSize()
    {
        return size;
    }

    public List<Map> getPoints()
    {
        return points;
    }

    public void setPoints( List<Map> points )
    {
        this.points = points;
        this.size = points.size();
    }
    
    public void addPoint(Map point ) {
        this.points.add(point);
        this.size++;
    }
    

}
