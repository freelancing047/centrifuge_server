package com.csi.chart.dto;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

public class TimestampRangeData
    extends RangeData
{
    public Date start;
    public Date end;

}
