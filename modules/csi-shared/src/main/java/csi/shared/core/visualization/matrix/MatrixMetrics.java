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
package csi.shared.core.visualization.matrix;

import java.io.Serializable;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")

public class MatrixMetrics implements Serializable {

    private int axisXCount;
    private int axisYCount;
    private int totalCells = 0;
    private double minValue = Double.MAX_VALUE;
    private double maxValue = Double.MIN_VALUE;
    private double sum;

    public void setTotalCells(int totalCells) {
        this.totalCells = totalCells;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public int getTotalCells() {
        return totalCells;
    }

    public double getMeanValue() {
        return meanValue;
    }

    public void setMeanValue(double meanValue) {
        this.meanValue = meanValue;
    }

    private double meanValue;

    public int getAxisXCount() {
        return axisXCount;
    }

    public void setAxisXCount(int axisXCount) {
        this.axisXCount = axisXCount;
    }

    public int getAxisYCount() {
        return axisYCount;
    }

    public void setAxisYCount(int axisYCount) {
        this.axisYCount = axisYCount;
    }

    public int getCells() {
        return totalCells;
    }

    public double getMinValue() {
        return  minValue == Double.MAX_VALUE ? Double.MAX_VALUE : minValue;
    }

    public double getMaxValue() {

        return maxValue == Double.MIN_VALUE ? 0 : maxValue;
    }

    public double getSum() {
        return sum;
    }

    public void process(List<Cell> cells) {
        for (Cell cell : cells) {
            processCell(cell);
        }

        meanValue = sum/totalCells;
    }

    public void processCell(Cell c){
        double v = c.getValue().doubleValue();
        totalCells++;
        sum += v;
        minValue = Math.min(minValue, v);
        maxValue = Math.max(maxValue, v);
    }

}
