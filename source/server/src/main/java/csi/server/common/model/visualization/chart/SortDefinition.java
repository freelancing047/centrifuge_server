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
package csi.server.common.model.visualization.chart;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import com.google.common.base.MoreObjects;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

import csi.server.common.model.ModelObject;
import csi.server.common.model.SortOrder;
import csi.server.common.model.visualization.AbstractAttributeDefinition;

/**
 *
 * Default sort order is SortOrder.ASC
 *
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SortDefinition extends ModelObject implements Serializable {

    @ManyToOne
    private CategoryDefinition categoryDefinition;
    @ManyToOne
    private MeasureDefinition measureDefinition;

    private int listPosition;

    private boolean countStar;
    @Enumerated(value = EnumType.STRING)
    private SortOrder sortOrder;

    public SortDefinition() {
        super();
        setSortOrder(SortOrder.ASC);
    }

    public SortDefinition(CategoryDefinition cd) {
        this();
        setCategoryDefinition(cd);
    }

    public SortDefinition(MeasureDefinition md) {
        this();
        setMeasureDefinition(md);
    }

    public String getTypeQualifiedDisplayName() {
        if (isCountStar()) {
            return "[measure] Count(*)";
        } else if (isCategory()) {
            return "[category] " + categoryDefinition.getComposedName();
        } else if (measureDefinition != null){
            return "[measure] " + measureDefinition.getComposedName();
        }
        else
            return "false";
    }

    public String getDisplayName() {
        if (isCountStar()) {
            return "Count (*)";
        } else if (isCategory()) {
            return categoryDefinition.getComposedName();
        } else if (measureDefinition != null){
            return measureDefinition.getComposedName();
        }
        else{
            return "false";
        }
    }

    public CategoryDefinition getCategoryDefinition() {
        return categoryDefinition;
    }

    public void setCategoryDefinition(CategoryDefinition categoryDefinition) {
        this.categoryDefinition = categoryDefinition;
    }

    public MeasureDefinition getMeasureDefinition() {
        return measureDefinition;
    }

    public void setMeasureDefinition(MeasureDefinition measureDefinition) {
        this.measureDefinition = measureDefinition;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isCategory() {
        return categoryDefinition != null;
    }

    public AbstractAttributeDefinition getChartAttributeDefinition() {
        return getCategoryDefinition() != null ? getCategoryDefinition() : getMeasureDefinition();
    }

    public boolean isCountStar() {
        return countStar;
    }

    public void setCountStar(boolean countStar) {
        this.countStar = countStar;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(SortDefinition.class.getName(), getUuid());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else {
            SortDefinition typed = (SortDefinition) obj;
            return Objects.equal(this.getUuid(), typed.getUuid());
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("count (*)", isCountStar()) //
                .add("categoryDefinition", getCategoryDefinition()) //
                .add("measureDefinition", getMeasureDefinition()) //
                .add("sort", getSortOrder()) //
                .toString();
    }

    @Override
    public <T extends ModelObject> SortDefinition clone(Map<String, T> fieldMapIn) {
        
        SortDefinition myClone = new SortDefinition();
        
        super.cloneComponents(myClone);
        
        if (null != getCategoryDefinition()) {
            myClone.setCategoryDefinition(getCategoryDefinition().clone(fieldMapIn));
        }
        if (null != getMeasureDefinition()) {
            myClone.setMeasureDefinition(getMeasureDefinition().clone(fieldMapIn));
        }
        myClone.setCountStar(isCountStar());
        myClone.setSortOrder(getSortOrder());
        
        return myClone;
    }
    
    public <T extends ModelObject> SortDefinition copy(Map<String, T> fieldMapIn) {
        
        SortDefinition myCopy = new SortDefinition();
        
        super.copyComponents(myCopy);
        
        if (null != getCategoryDefinition()) {
        	myCopy.setCategoryDefinition(getCategoryDefinition().copy(fieldMapIn));
        }
        if (null != getMeasureDefinition()) {
        	myCopy.setMeasureDefinition(getMeasureDefinition().copy(fieldMapIn));
        }
        myCopy.setCountStar(isCountStar());
        myCopy.setSortOrder(getSortOrder());
        
        return myCopy;
    }
}
