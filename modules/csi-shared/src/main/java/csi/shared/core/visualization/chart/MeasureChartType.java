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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import csi.shared.core.util.HasComparator;
import csi.shared.core.util.HasLabel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public enum MeasureChartType implements HasComparator<MeasureChartType>, HasLabel, Serializable {

    DEFAULT("Default"),
    COLUMN("Column"),
    AREA("Area"),
    AREA_SPLINE("Area Spline"),
    LINE("Line"), 
    PIE("Pie"),
    DONUT("Donut");

    private String label;

    private MeasureChartType(String label) {
        this.label = label;
    }

    @Override
    public Comparator<MeasureChartType> getComparator() {
        return Comparator.comparing(Enum::name);
    }

    @Override
    public String getLabel() {
        return label;
    }

    public List<MeasureChartType> getAllowedChartTypes(List<ChartType> chartTypes){



        return new ArrayList<>();
    }

}
