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

import com.google.common.collect.ComparisonChain;
import csi.shared.core.util.HasLabel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class AxisLabel implements HasLabel, Comparable<AxisLabel>, Serializable {

    private int ordinalPosition;
    private String label;
    private double min, max, sum, avg;
    private int count;
    private List<String> subCategories;


    public AxisLabel() {
        super();
        subCategories = new ArrayList<>();
    }

    public AxisLabel(String label) {
        this.label = label;
    }

    public List<String> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<String> subCategories) {
        this.subCategories = subCategories;
    }

    public void addSubCategory(String cat){
        if(subCategories==null){
            subCategories = new ArrayList<>();
        }
        if(!cat.isEmpty()) {
            subCategories.add(cat);
        }
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Number getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getAvg() {
        if (getCount() == 0) {
            return 0;
        } else {
            return Math.round(getSum() / (double) getCount() * 100.0) / 100.0;
        }
    }

    public double getSum() {
        return sum;
    }

    public void setAvg(double avg) { this.avg = avg;}

    public void setSum(double sum) {
        this.sum = sum;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void accumulate(Cell cell) {
        double d = cell.getValue().doubleValue();
        sum += d;
        if (count == 0) {
            min = d;
            max = d;
        } else {
            if (min > d) {
                min = d;
            }
            if (max < d) {
                max = d;
            }
        }
        count++;
    }

    public void adjust(Cell dup, Cell cell) {
        sum += cell.getValue().doubleValue();
        dup.setValue(dup.getValue().doubleValue() + cell.getValue().doubleValue());
/*        int[] ids = CsiArrayUtils.deDupe(CsiArrayUtils.merge(cell.getIds(), dup.getIds()));
        
        cell.setIds(ids);*/

        if (max < dup.getValue().doubleValue()) {
            max = dup.getValue().doubleValue();
        }
    }
    
    public static Integer[] mergeArrays(Integer[] a, Integer[] b){
        int size = a.length + b.length;
        Integer[] total = new Integer[size];
        for(int ii=0; ii<a.length; ii++){
            total[ii] = a[ii];
        }
        
        for(int ii=0; ii<b.length; ii++){
            total[ii+a.length] = b[ii];
        }
        
        return total;
    }

    @Override
    public int compareTo(AxisLabel that) {
        return ComparisonChain.start().compare(this.getLabel(), that.getLabel()).result();
    }



    @Override
    // THIS IS BAD ACCORDING TO PATRICK.
    public int hashCode() {
        return getLabel().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if ((obj instanceof AxisLabel)) {
            AxisLabel typed = (AxisLabel) obj;
            return compareTo(typed) == 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return label;
    }

    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }
}
