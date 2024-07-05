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
package csi.server.common.model.visualization.matrix;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Doubles;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.shared.core.visualization.matrix.AxisLabel;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;

import csi.server.common.model.ModelObject;
import csi.server.common.model.SortOrder;
import csi.shared.core.color.ColorModel;
import csi.shared.core.color.ContinuousColorModel;
import csi.shared.core.color.SingleColorModel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MatrixSettings extends ModelObject implements Serializable {

    @OneToMany(cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<MatrixCategoryDefinition> axisCategories = new ArrayList<MatrixCategoryDefinition>();

    @OneToMany(cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<MatrixSortDefinition> axisSortDefinitions = new ArrayList<MatrixSortDefinition>();

    @OneToMany(cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "matrixsettings_axisquicksortdefinition")
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<MatrixSortDefinition> axisQuickSortDefinitions = new ArrayList<MatrixSortDefinition>();

    @OneToMany(cascade = CascadeType.ALL)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("listPosition")
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    private List<ChartCriterion> filterCriteria = new ArrayList<ChartCriterion>();

    @Enumerated(value = EnumType.STRING)
    private SortOrder measureQuickSortOrder = SortOrder.NONE;


    @OneToOne(cascade = CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private MatrixMeasureDefinition matrixMeasureDefinition;

    @Enumerated(value = EnumType.STRING)
    private MatrixType matrixType;


    private static String DEFAULT_BLUE = "#3498db";

    private boolean showLabel;
    private boolean useCountForMeasure = true;
    private boolean sortByAxis;

    @Enumerated(value = EnumType.STRING)
    private SortOrder measureSortOrder = SortOrder.DESC;

    @Type(type = "csi.server.dao.jpa.xml.SerializedXMLType")
    @Column(columnDefinition = "TEXT")
    private ColorModel colorModel;
    private boolean bubbleDisplay = true;

    public MatrixSettings() {
        super();
    }

    public boolean isSortByAxis() {
        return sortByAxis;
    }

    public void setSortByAxis(boolean sortByAxis) {
        this.sortByAxis = sortByAxis;
    }

    public List<MatrixSortDefinition> getAxisSortDefinitions() {
        return axisSortDefinitions;
    }

    public void setAxisSortDefinitions(List<MatrixSortDefinition> axisSortDefinitions) {
        this.axisSortDefinitions = axisSortDefinitions;
    }

    public SortOrder getMeasureSortOrder() {
        return measureSortOrder;
    }

    public void setMeasureSortOrder(SortOrder measureSortOrder) {
        this.measureSortOrder = measureSortOrder;
    }

    public MatrixMeasureDefinition getMatrixMeasureDefinition() {
        return matrixMeasureDefinition;
    }

    public void setMatrixMeasureDefinition(MatrixMeasureDefinition matrixMeasureDefinition) {
        this.matrixMeasureDefinition = matrixMeasureDefinition;
    }

    public List<MatrixCategoryDefinition> getAxisCategories() {
        return axisCategories;
    }

    public void setAxisCategories(List<MatrixCategoryDefinition> axisCategories) {
        this.axisCategories = axisCategories;
    }

    public MatrixType getMatrixType() {
        if (matrixType == null) {
            matrixType = MatrixType.BUBBLE;
        }
        return matrixType;
    }

    public void setMatrixType(MatrixType matrixType) {
        this.matrixType = matrixType;
    }

    public boolean getBubbleDisplay() { return bubbleDisplay;}

    public void setBubbleDisplay(boolean bubbleDisplay) { this.bubbleDisplay = bubbleDisplay; }

    public ColorModel getColorModel() {
        ColorModel colorModel = this.colorModel;

        if (colorModel == null) {
            if (MatrixType.HEAT_MAP == getMatrixType()) {
                colorModel = new ContinuousColorModel();
            } else {
                colorModel = new SingleColorModel(DEFAULT_BLUE);
            }
        }

        return colorModel;
    }

    public void setColorModel(ColorModel colorModel) {
        this.colorModel = colorModel;
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    public boolean isUseCountForMeasure() {
        return useCountForMeasure;
    }

    public void setUseCountForMeasure(boolean useCountForMeasure) {
        this.useCountForMeasure = useCountForMeasure;
    }

    public List<MatrixCategoryDefinition> getAxisX() {
        List<MatrixCategoryDefinition> list = new ArrayList<MatrixCategoryDefinition>();
        for (MatrixCategoryDefinition mcd : getAxisCategories()) {
            if (mcd.getAxis() == Axis.X) {
                list.add(mcd);
            }
        }
        return list;
    }

    public List<MatrixCategoryDefinition> getAxisY() {
        List<MatrixCategoryDefinition> list = new ArrayList<MatrixCategoryDefinition>();
        for (MatrixCategoryDefinition mcd : getAxisCategories()) {
            if (mcd.getAxis() == Axis.Y) {
                list.add(mcd);
            }
        }
        return list;
    }

    public List<MatrixSortDefinition> getAxisQuickSortDefinitions() {
        return axisQuickSortDefinitions;
    }

    public void setAxisQuickSortDefinitions(List<MatrixSortDefinition> axisQuickSortDefinitions) {
        this.axisQuickSortDefinitions = axisQuickSortDefinitions;
    }

    public SortOrder getMeasureQuickSortOrder() {
        if(measureQuickSortOrder == null){
            measureQuickSortOrder = SortOrder.NONE;
        }
        return measureQuickSortOrder;
    }

    public void setMeasureQuickSortOrder(SortOrder measureQuickSortOrder) {
        this.measureQuickSortOrder = measureQuickSortOrder;
    }

    @Override
    public <T extends ModelObject> MatrixSettings clone(Map<String, T> fieldMapIn) {
        MatrixSettings myClone = new MatrixSettings();

        super.cloneComponents(myClone);

        if (null != getMatrixMeasureDefinition()) {
            myClone.setMatrixMeasureDefinition(getMatrixMeasureDefinition().clone(fieldMapIn));
        }
        myClone.setMatrixType(getMatrixType());
        myClone.setShowLabel(isShowLabel());
        myClone.setUseCountForMeasure(isUseCountForMeasure());
        myClone.setSortByAxis(isSortByAxis());
        myClone.setMeasureSortOrder(getMeasureSortOrder());
        myClone.setColorModel(getColorModel());
        myClone.setAxisCategories(cloneAxisCategories(fieldMapIn));
        myClone.setAxisSortDefinitions(cloneAxisSortDefinitions(fieldMapIn));

        return myClone;
    }

    private <T extends ModelObject> List<MatrixCategoryDefinition> cloneAxisCategories(Map<String, T> fieldMapIn) {
        if (null != getAxisCategories()) {
            List<MatrixCategoryDefinition> myList = new ArrayList<MatrixCategoryDefinition>();
            for (MatrixCategoryDefinition myItem : getAxisCategories()) {
                myList.add(myItem.clone(fieldMapIn));
            }
            return myList;
        }
        return null;
    }

    private <T extends ModelObject> List<MatrixCategoryDefinition> copyAxisCategories(Map<String, T> fieldMapIn) {
        if (null != getAxisCategories()) {
            List<MatrixCategoryDefinition> myList = new ArrayList<MatrixCategoryDefinition>();
            for (MatrixCategoryDefinition myItem : getAxisCategories()) {
                myList.add(myItem.copy(fieldMapIn));
            }
            return myList;
        }
        return null;
    }

    private <T extends ModelObject> List<MatrixSortDefinition> cloneAxisSortDefinitions(Map<String, T> fieldMapIn) {
        if (null != getAxisSortDefinitions()) {
            List<MatrixSortDefinition> myList = new ArrayList<MatrixSortDefinition>();
            for (MatrixSortDefinition myItem : getAxisSortDefinitions()) {
                myList.add(myItem.clone(fieldMapIn));
            }
            return myList;
        }
        return null;
    }

    private <T extends ModelObject> List<MatrixSortDefinition> copyAxisSortDefinitions(Map<String, T> fieldMapIn) {
        if (null != getAxisSortDefinitions()) {
            List<MatrixSortDefinition> myList = new ArrayList<MatrixSortDefinition>();
            for (MatrixSortDefinition myItem : getAxisSortDefinitions()) {
                myList.add(myItem.copy(fieldMapIn));
            }
            return myList;
        }
        return null;
    }

    public <T extends ModelObject> MatrixSettings copy(Map<String, T> fieldMapIn) {
        MatrixSettings myCopy = new MatrixSettings();

        super.copyComponents(myCopy);

        if (null != getMatrixMeasureDefinition()) {
            myCopy.setMatrixMeasureDefinition(getMatrixMeasureDefinition().copy(fieldMapIn));
        }
        myCopy.setMatrixType(getMatrixType());
        myCopy.setShowLabel(isShowLabel());
        myCopy.setUseCountForMeasure(isUseCountForMeasure());
        myCopy.setSortByAxis(isSortByAxis());
        myCopy.setMeasureSortOrder(getMeasureSortOrder());
        myCopy.setColorModel(getColorModel());

        myCopy.setAxisCategories(copyAxisCategories(fieldMapIn));
        myCopy.setAxisSortDefinitions(copyAxisSortDefinitions(fieldMapIn));

        return myCopy;
    }

    public Comparator<? super AxisLabel> getXAxisComparator() {
        if (!getAxisQuickSortDefinitions().isEmpty()) {
            SortOrder sortOrder = null;

            // there should only be two things in here, x, and y, at most.
            for (MatrixSortDefinition quickSortDef : getAxisQuickSortDefinitions()) {
                if (quickSortDef.getCategoryDefinition().getAxis() == Axis.X) {
                    sortOrder = quickSortDef.getSortOrder();
                }
            }

            // make sure we are not passing in nulls...

            sortOrder = sortOrder == null ? getSortOrderForAxis(Axis.X) : sortOrder;

            return sortAxisByLabel(sortOrder);

        } else if (getMeasureQuickSortOrder() != SortOrder.NONE) {
            return sortByMaxCellValue(getMeasureQuickSortOrder());
        } else {
            if (isSortByAxis()) {
                return sortAxisByLabel(getSortOrderForAxis(Axis.X));
            } else {
                return sortByMaxCellValue(this.getMeasureSortOrder());
            }
        }
    }

    public Comparator<? super AxisLabel> getYAxisComparator() {
        if (!getAxisQuickSortDefinitions().isEmpty()) {
            SortOrder sortOrder = null;

            // there should only be two things in here, x, and y, at most.
            for (MatrixSortDefinition quickSortDef : getAxisQuickSortDefinitions()) {
                if (quickSortDef.getCategoryDefinition().getAxis() == Axis.Y) {
                    sortOrder = quickSortDef.getSortOrder();
                }
            }

            // make sure we are not passing in nulls...

            sortOrder = sortOrder == null ? getSortOrderForAxis(Axis.Y) : sortOrder;

            return sortAxisByLabel(sortOrder);

        } else if (getMeasureQuickSortOrder() != SortOrder.NONE) {
            return sortByMaxCellValue(getMeasureQuickSortOrder());
        } else {
            if (isSortByAxis()) {
                return sortAxisByLabel(getSortOrderForAxis(Axis.Y));
            } else {
                return sortByMaxCellValue(this.getMeasureSortOrder());
            }
        }
    }


    private Comparator<? super AxisLabel> sortAxisByLabel(final SortOrder sortOrder) {
        Comparator comparator = new Comparator<AxisLabel>() {
            @Override
            public int compare(AxisLabel o1, AxisLabel o2) {

                ComparisonChain chain = ComparisonChain.start();
                String value1 = o1.getLabel();
                Double d1 = tryParse(value1);
                String value2 = o2.getLabel();
                Double d2 = tryParse(value2);
                if (d1!=null) {
                    if (d2!=null) {
                        chain = chain.compare(d1, d2);
                        // NOTE: This logic collapses equal numeric value types such a '0' & '00', So we test string as well.
                        // compare = d1 > d2 ? 1 : d1 == d2 ? 0 : -1;
                    } else {
                        chain = chain.compare(d1.doubleValue(), Double.MAX_VALUE);
                    }
                } else if (d2!=null) {
                    chain = chain.compare(Double.MAX_VALUE, d2.doubleValue());
                }
                chain = chain.compare(o1.getLabel(), o2.getLabel());
                int result = chain.compare(value1, value2).result();
                return sortOrder.compared(result);
            }

            private Double tryParse(String value1) {
                try {
                    return Double.parseDouble(value1);
                }catch (Exception e) {
                    return null;
                }
            }
        };
        return comparator;
    }

    /**
     * using this for filter as well.
     * @param measureSortOrder
     * @return
     */
    public Comparator<? super AxisLabel> sortByMaxCellValue(final SortOrder measureSortOrder) {

        Comparator comparator = new Comparator<AxisLabel>() {
            @Override
            public int compare(AxisLabel o1, AxisLabel o2) {
                int result;
                ComparisonChain chain = ComparisonChain.start().compare(o1.getMax(), o2.getMax());
                result = chain.compare(o1, o2, sortAxisByLabel(SortOrder.ASC)).result();
                return measureSortOrder.compared(result);
            }
        };
        return comparator;
    }

    private SortOrder getSortOrderForAxis(Axis axis) {
        SortOrder sortOrder = SortOrder.NONE;
        for (MatrixSortDefinition sd : getAxisSortDefinitions()) {
            if (sd.getCategoryDefinition().getAxis() == axis) {
                sortOrder = sd.getSortOrder();
                break;
            }
        }

        return sortOrder;
    }

    public List<ChartCriterion> getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(List<ChartCriterion> filterCriteria) {
        this.filterCriteria = filterCriteria;
    }
}
