/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.shared.core.visualization.chart;

import java.io.Serializable;
import java.util.Comparator;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import csi.shared.core.util.HasComparator;
import csi.shared.core.util.HasLabel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@XStreamAlias("ChartType")
public enum ChartType implements HasComparator<ChartType>, HasLabel, Serializable {

    COLUMN("Column"),
    BAR("Bar"),
    AREA("Area"),
    AREA_SPLINE("Area Spline"),
    POLAR("Polar"),
    SPIDER("Spider"),
    LINE("Line"),
    PIE("Pie"),
    DONUT("Donut");

    private String label;

    private ChartType(String label) {
        this.label = label;
    }

    @Override
    public Comparator<ChartType> getComparator() {
        return new Comparator<ChartType>() {
            @Override
            public int compare(ChartType o1, ChartType o2) {
                return o1.name().compareTo(o2.name());
            }
        };
    }

    @Override
    public String getLabel() {
        return label;
    }
}
