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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class MatrixCategoryResponse implements Serializable {

    private List<AxisLabel> categoryX, categoryY;
    private String axisXLabel;
    private String axisYLabel;
    private String measureLabel;
    private String mainTitle;

    private boolean isSummaryX, isSummaryY;

    private String overviewImage;
    private String rangeImage;

    private boolean limitExceeded;
    private int count;

    public MatrixCategoryResponse() {
    }

    public boolean isSummaryX() {
        return isSummaryX;
    }

    public void setSummaryX(boolean summaryX) {
        isSummaryX = summaryX;
    }

    public boolean isSummaryY() {
        return isSummaryY;
    }

    public void setSummaryY(boolean summaryY) {
        isSummaryY = summaryY;
    }

    /*These two methods need to go.*/
    public boolean isLimitExceeded() {
        return limitExceeded;
    }

    public void setLimitExceeded(boolean limitExceeded) {
        this.limitExceeded = limitExceeded;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<AxisLabel> getCategoryX() {
        return categoryX;
    }

    public void setCategoryX(List<AxisLabel> categoryX) {
        this.categoryX = categoryX;
    }

    public List<AxisLabel> getCategoryY() {
        return categoryY;
    }

    public void setCategoryY(List<AxisLabel> categoryY) {
        this.categoryY = categoryY;
    }

    public String getAxisXLabel() {
        return axisXLabel;
    }

    //TODO this get called too many times, wrap it somewhere lower.
    public void setAxisXLabel(String axisXLabel) {
        this.axisXLabel = axisXLabel;
    }

    public String getAxisYLabel() {
        return axisYLabel;
    }

    public void setAxisYLabel(String axisYLabel) {
        this.axisYLabel = axisYLabel;
    }

    public String getMeasureLabel() {
        return measureLabel;
    }

    public void setMeasureLabel(String measureLabel) {
        this.measureLabel = measureLabel;
    }

    public String getMainTitle() {
        return mainTitle;
    }

    public void setMainTitle(String chartLabel) {
        this.mainTitle = chartLabel;
    }

    public String getOverviewImage() {
        return overviewImage;
    }

    public void setOverviewImage(String overviewImage) {
        this.overviewImage = overviewImage;
    }

    public String getRangeImage() {
        return rangeImage;
    }

    public void setRangeImage(String rangeImage) {
        this.rangeImage = rangeImage;
    }

    public void updateCategories(MatrixCategoryResponse r){
        categoryX = mergeLists(categoryX, r.getCategoryX());
        categoryY = mergeLists(categoryY,r.getCategoryY());
    }

    private List<AxisLabel> mergeLists(List<AxisLabel> a, List<AxisLabel> b){
        Set<AxisLabel> set = new HashSet<>();
        set.addAll(a);
        set.addAll(b);
        return new ArrayList<AxisLabel>(set);
    }

}
