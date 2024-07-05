package com.csi.chart.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

public class ChartSummary
{
    public List<DimensionSummary> dimensions = new ArrayList<DimensionSummary>();

}
