/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.server.common.model.visualization.chart;

import csi.server.common.model.ModelObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ChartSettings extends ModelObject implements Serializable {

    @OneToMany(cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("listPosition")
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
    @OneToMany(cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("listPosition")
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<MeasureDefinition> measureDefinitions = new ArrayList<MeasureDefinition>();
    @OneToMany(cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("listPosition")
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<SortDefinition> sortDefinitions = new ArrayList<SortDefinition>();
    @OneToMany(cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "chartsettings_quicksortdef")
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<SortDefinition> quickSortDefs = new ArrayList<SortDefinition>();
    private boolean isShowBreadcrumbs = true;
    @Enumerated(value = EnumType.STRING)
    private DisplayFirst currentView = DisplayFirst.CHART;
    @OneToMany(cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("listPosition")
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<ChartCriterion> filterCriteria = new ArrayList<ChartCriterion>();
    private boolean useCountStarForMeasure = true;
    private boolean pieLabelEnabled = false;
    private boolean pieLabelShowPercentage = false;
    private boolean pieLabelShowValue = false;
    private double pieLabelPercentageThreshold = 0;
    private boolean pieLegendEnabled = true;
    private boolean pieLegendShowValue = false;
    private boolean pieLegendShowPercentage = false;
    private boolean alignAxes = false;
    private boolean chartDisplay = true;
    @Enumerated(value = EnumType.STRING)
    private DisplayFirst defaultView = DisplayFirst.CHART;


    public ChartSettings() {
        super();
    }

    public List<CategoryDefinition> getCategoryDefinitions() {
        return categoryDefinitions;
    }

    public void setCategoryDefinitions(List<CategoryDefinition> categoryDefinitions) {
        this.categoryDefinitions = categoryDefinitions;
    }

    public List<MeasureDefinition> getMeasureDefinitions() {
        return measureDefinitions;
    }

    public void setMeasureDefinitions(List<MeasureDefinition> measureDefinitions) {
        this.measureDefinitions = measureDefinitions;
    }

    public List<SortDefinition> getSortDefinitions() {
        return sortDefinitions;
    }

    public void setSortDefinitions(List<SortDefinition> sortDefinitions) {
        this.sortDefinitions = sortDefinitions;
    }

    public List<ChartCriterion> getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(List<ChartCriterion> filterCriteria) {
        this.filterCriteria = filterCriteria;
    }

    public boolean isUseCountStarForMeasure() {
        return useCountStarForMeasure;
    }

    public void setUseCountStarForMeasure(boolean useCountStarForMeasure) {
        this.useCountStarForMeasure = useCountStarForMeasure;
    }


    public boolean isShowBreadcrumbs() {
        return isShowBreadcrumbs;
    }

    public void setShowBreadcrumbs(boolean showBreadcrumbs) {
        isShowBreadcrumbs = showBreadcrumbs;
    }

    public DisplayFirst getCurrentView() {
        return currentView;
    }

    public void setCurrentView(DisplayFirst currentView) {
        this.currentView = currentView;
    }

    public List<SortDefinition> getQuickSortDef() {
        return quickSortDefs;
    }

    public void setQuickSortDef(List<SortDefinition> quickSortDef) {
        this.quickSortDefs = quickSortDef;
    }

    @Override
    public <T extends ModelObject> ChartSettings clone(Map<String, T> fieldMapIn) {
        ChartSettings myClone = new ChartSettings();

        super.cloneComponents(myClone);
        myClone.setUseCountStarForMeasure(isUseCountStarForMeasure());
        myClone.setCategoryDefinitions(cloneCategoryDefinitions(fieldMapIn));
        myClone.setMeasureDefinitions(cloneMeasureDefinitions(fieldMapIn));
        myClone.setSortDefinitions(cloneSortDefinitions(fieldMapIn));
        myClone.setQuickSortDef(copyQuickSortDefinitions(fieldMapIn));
        myClone.setFilterCriteria(cloneFilterCriteria(fieldMapIn));
        myClone.setCurrentView(getCurrentView());
        myClone.setDefaultView(getDefaultView());
        myClone.setAlignAxes(isAlignAxes());
        myClone.setShowBreadcrumbs(isShowBreadcrumbs());
        cloneAdvancedTab(myClone);

        return myClone;
    }

    public <T extends ModelObject> ChartSettings copy(Map<String, T> fieldMapIn) {
        ChartSettings myCopy = new ChartSettings();

        super.cloneComponents(myCopy);

        myCopy.setUseCountStarForMeasure(isUseCountStarForMeasure());
        myCopy.setCategoryDefinitions(copyCategoryDefinitions(fieldMapIn));
        myCopy.setMeasureDefinitions(copyMeasureDefinitions(fieldMapIn));
        myCopy.setSortDefinitions(copySortDefinitions(fieldMapIn));
        myCopy.setQuickSortDef(copyQuickSortDefinitions(fieldMapIn));
        myCopy.setFilterCriteria(copyFilterCriteria(fieldMapIn));
        myCopy.setCurrentView(getCurrentView());
        myCopy.setDefaultView(getDefaultView());
        myCopy.setAlignAxes(isAlignAxes());
        myCopy.setShowBreadcrumbs(isShowBreadcrumbs());
        cloneAdvancedTab(myCopy);
        return myCopy;
    }

    private void cloneAdvancedTab(ChartSettings newSettings) {
        //labels
        newSettings.setPieLabelEnabled(this.pieLabelEnabled);
        newSettings.setPieLabelShowValue(this.pieLabelShowValue);
        newSettings.setPieLabelShowPercentage(this.pieLabelShowPercentage);
        newSettings.setPieLabelPercentageThreshold(this.pieLabelPercentageThreshold);

        //legends
        newSettings.setPieLegendEnabled(this.pieLegendEnabled);
        newSettings.setPieLegendShowValue(this.pieLegendShowValue);
        newSettings.setPieLegendShowPercentage(this.pieLegendShowPercentage);

    }


    private <T extends ModelObject> List<CategoryDefinition> cloneCategoryDefinitions(Map<String, T> fieldMapIn) {

        if (null != getCategoryDefinitions()) {

            List<CategoryDefinition> myList = new ArrayList<CategoryDefinition>();

            for (CategoryDefinition myItem : getCategoryDefinitions()) {

                myList.add(myItem.clone(fieldMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }

    private <T extends ModelObject> List<CategoryDefinition> copyCategoryDefinitions(Map<String, T> fieldMapIn) {

        if (null != getCategoryDefinitions()) {

            List<CategoryDefinition> myList = new ArrayList<CategoryDefinition>();

            for (CategoryDefinition myItem : getCategoryDefinitions()) {

                myList.add(myItem.copy(fieldMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }

    private <T extends ModelObject> List<MeasureDefinition> cloneMeasureDefinitions(Map<String, T> fieldMapIn) {

        if (null != getMeasureDefinitions()) {

            List<MeasureDefinition> myList = new ArrayList<MeasureDefinition>();

            for (MeasureDefinition myItem : getMeasureDefinitions()) {

                myList.add(myItem.clone(fieldMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }

    private <T extends ModelObject> List<MeasureDefinition> copyMeasureDefinitions(Map<String, T> fieldMapIn) {

        if (null != getMeasureDefinitions()) {

            List<MeasureDefinition> myList = new ArrayList<MeasureDefinition>();

            for (MeasureDefinition myItem : getMeasureDefinitions()) {

                myList.add(myItem.copy(fieldMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }

    private <T extends ModelObject> List<SortDefinition> cloneSortDefinitions(Map<String, T> fieldMapIn) {

        if (null != getSortDefinitions()) {

            List<SortDefinition> myList = new ArrayList<SortDefinition>();

            for (SortDefinition myItem : getSortDefinitions()) {

                myList.add(myItem.clone(fieldMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }

    private <T extends ModelObject> List<SortDefinition> copySortDefinitions(Map<String, T> fieldMapIn) {

        if (null != getSortDefinitions()) {

            List<SortDefinition> myList = new ArrayList<SortDefinition>();

            for (SortDefinition myItem : getSortDefinitions()) {

                myList.add(myItem.copy(fieldMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }

    private <T extends ModelObject> List<SortDefinition> copyQuickSortDefinitions(Map<String, T> fieldMapIn) {

        if (null != getQuickSortDef()) {

            List<SortDefinition> myList = new ArrayList<SortDefinition>();

            for (SortDefinition myItem : getQuickSortDef()) {

                myList.add(myItem.copy(fieldMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }

    private <T extends ModelObject> List<ChartCriterion> cloneFilterCriteria(Map<String, T> fieldMapIn) {
        if (null != getFilterCriteria()) {
            List<ChartCriterion> myList = new ArrayList<ChartCriterion>();

            for (ChartCriterion myItem : getFilterCriteria()) {
                myList.add(myItem.clone(fieldMapIn));
            }

            return myList;
        } else {
            return null;
        }
    }

    private <T extends ModelObject> List<ChartCriterion> copyFilterCriteria(Map<String, T> fieldMapIn) {
        if (null != getFilterCriteria()) {
            List<ChartCriterion> myList = new ArrayList<ChartCriterion>();

            for (ChartCriterion myItem : getFilterCriteria()) {
                myList.add(myItem.copy(fieldMapIn));
            }

            return myList;
        } else {
            return null;
        }
    }

    public boolean isPieLabelEnabled() {
        return pieLabelEnabled;
    }

    public void setPieLabelEnabled(boolean pieLabelEnabled) {
        this.pieLabelEnabled = pieLabelEnabled;
    }

    public boolean isPieLabelShowPercentage() {
        return pieLabelShowPercentage;
    }

    public void setPieLabelShowPercentage(boolean pieLabelShowPercentage) {
        this.pieLabelShowPercentage = pieLabelShowPercentage;
    }

    public boolean isPieLabelShowValue() {
        return pieLabelShowValue;
    }

    public void setPieLabelShowValue(boolean pieLabelShowValue) {
        this.pieLabelShowValue = pieLabelShowValue;
    }

    public double getPieLabelPercentageThreshold() {
        return pieLabelPercentageThreshold;
    }

    public void setPieLabelPercentageThreshold(double pieLabelPercentageThreshold) {
        this.pieLabelPercentageThreshold = pieLabelPercentageThreshold;
    }

    public boolean isPieLegendEnabled() {
        return pieLegendEnabled;
    }

    public void setPieLegendEnabled(boolean pieLegendEnabled) {
        this.pieLegendEnabled = pieLegendEnabled;
    }

    public boolean isPieLegendShowValue() {
        return pieLegendShowValue;
    }

    public void setPieLegendShowValue(boolean pieLegendShowValue) {
        this.pieLegendShowValue = pieLegendShowValue;
    }

    public boolean isPieLegendShowPercentage() {
        return pieLegendShowPercentage;
    }

    public void setPieLegendShowPercentage(boolean pieLegendShowPercentage) {
        this.pieLegendShowPercentage = pieLegendShowPercentage;
    }

    public DisplayFirst getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(DisplayFirst defaultView) {
        this.defaultView = defaultView;
    }

    public boolean isAlignAxes() {
        return alignAxes;
    }

    public void setAlignAxes(boolean alignAxes) {
        this.alignAxes = alignAxes;
    }

    public boolean getChartDisplay() {
        return chartDisplay;
    }

    public void setChartDisplay(boolean chartDisplay) {
        this.chartDisplay = chartDisplay;
    }
}
