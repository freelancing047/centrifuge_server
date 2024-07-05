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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.AbstractCategoryDefinition;
import csi.shared.core.visualization.chart.ChartType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CategoryDefinition extends AbstractCategoryDefinition implements Serializable {

    public CategoryDefinition() {
        super();
    }

    private ChartType chartType;

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

    @Override
    public String getDefinitionName() {
        return DefinitionType.CATEGORY.getLabel();
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof CategoryDefinition)) {
            return false;
        } else {
            CategoryDefinition typed = (CategoryDefinition) obj;
            return Objects.equal(this.getUuid(), typed.getUuid());
        }
    }
    
    @Override
    public <T extends ModelObject> CategoryDefinition clone(Map<String, T> fieldMapIn) {
        
        CategoryDefinition myClone = new CategoryDefinition();
        
        super.cloneComponents(myClone, fieldMapIn);
        
        myClone.setChartType(getChartType());
        
        return myClone;
    }
    
    public <T extends ModelObject> CategoryDefinition copy(Map<String, T> fieldMapIn) {
        
    	if(fieldMapIn.containsKey(this.getUuid())){
    		return (CategoryDefinition) fieldMapIn.get(this.getUuid());
    	}
        CategoryDefinition myCopy = new CategoryDefinition();
        
        super.copyComponents(myCopy);
        
        myCopy.setChartType(getChartType());
        fieldMapIn.put(this.getUuid(), (T) myCopy);
        
        return myCopy;
    }
}
