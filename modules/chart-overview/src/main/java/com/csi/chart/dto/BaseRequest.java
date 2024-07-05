package com.csi.chart.dto;

import java.util.List;

public class BaseRequest
{

    public Integer dimensionCount;
    
    public List<SortInfo> sorting;
    public List<String> drill;

    public int metric;

    public BaseRequest() {
        super();
    }

}
