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
package csi.server.business.service.chart;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TableResult {

    private int rowCount;

    private List<String> categories = new ArrayList<String>();
    private Set<Integer> nullCategory = new HashSet<Integer>();
    private List<List<Number>> dimensionValues = new ArrayList<List<Number>>();
    private List<Number> totalValues = new ArrayList<Number>();

    private boolean isTableLimitExceeded = false;
    private boolean isChartLimitExceeded = false;


    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<Number> getTotalValues() {
        return totalValues;
    }

    public void setTotalValues(List<Number> totalValuesIn) {
        this.totalValues = totalValuesIn;
    }

    public List<List<Number>> getDimensionValues() {
        return dimensionValues;
    }

    public void setDimensionValues(List<List<Number>> dimensionValues) {
        this.dimensionValues = dimensionValues;
    }

    public int getColumnCount() {
        return dimensionValues.size() + 1;
    }

    public Set<Integer> getNullCategory() {
        return nullCategory;
    }

    public boolean isTableLimitExceeded() {
        return isTableLimitExceeded;
    }

    public void setTableLimitExceeded(boolean tableLimitExceeded) {
        isTableLimitExceeded = tableLimitExceeded;
    }

    public boolean isChartLimitExceeded() {
        return isChartLimitExceeded;
    }

    public void setChartLimitExceeded(boolean chartLimitExceeded) {
        isChartLimitExceeded = chartLimitExceeded;
    }

    public void removeCategoriesOnRight(Integer threshold, Integer integer) {
        
    }
    
    public void removeCategoriesOnLeft(Integer threshold) {
        
        if(categories.size() - 1 - threshold > 0){
            return;
        }
        
        int toIndex = categories.size() - threshold;
        if(toIndex < 0){
            toIndex = 0;
        }
        categories.subList(0, toIndex).clear();
        
        for(List<Number> measure: dimensionValues){
            measure.subList(0, toIndex).clear();
        }
        
        rowCount = categories.size() - 1;
    }
}
