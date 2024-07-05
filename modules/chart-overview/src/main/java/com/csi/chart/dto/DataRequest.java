package com.csi.chart.dto;

import java.util.List;
import java.util.Map;

public class DataRequest
    extends BaseRequest
{
    public int offset;
    public int size;
    
    public boolean support2D;
    public int yOffset;
    public int ySize;
}
