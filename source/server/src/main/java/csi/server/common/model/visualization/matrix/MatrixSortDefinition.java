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
package csi.server.common.model.visualization.matrix;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

import csi.server.common.model.ModelObject;
import csi.server.common.model.SortOrder;
import com.google.common.base.MoreObjects;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MatrixSortDefinition extends ModelObject implements Serializable {

    public MatrixSortDefinition() {
        super();
        setSortOrder(SortOrder.ASC);
    }

    @ManyToOne
    private MatrixCategoryDefinition categoryDefinition;

    private SortOrder sortOrder;

    public String getId() {
        return categoryDefinition.getUuid();
    }

    public MatrixCategoryDefinition getCategoryDefinition() {
        return categoryDefinition;
    }

    public void setCategoryDefinition(MatrixCategoryDefinition categoryDefinition) {
        this.categoryDefinition = categoryDefinition;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getAxisName() {
        return getCategoryDefinition().getAxis().getLabel();
    }

    public String getDisplayName() {
        return categoryDefinition.getComposedName();
    }

    public void toggleSortOrder(){
        if(getSortOrder() == SortOrder.ASC){
            setSortOrder(SortOrder.DESC);
        }else{
            setSortOrder(SortOrder.ASC);

        }
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(MatrixSortDefinition.class.getName(), getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else {
            MatrixSortDefinition typed = (MatrixSortDefinition) obj;
            return Objects.equal(this.getId(), typed.getId());
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("categoryDefinition", getCategoryDefinition()) //
                .add("sort", getSortOrder()) //
                .toString();
    }
 
    @Override
    public <T extends ModelObject> MatrixSortDefinition clone(Map<String, T> fieldMapIn) {
        
        MatrixSortDefinition myClone = new MatrixSortDefinition();
        
        super.cloneComponents(myClone);
        if (null != getCategoryDefinition()) {
            myClone.setCategoryDefinition(getCategoryDefinition().clone(fieldMapIn));
        }
        myClone.setSortOrder(getSortOrder());
        
        return myClone;
    }
    
    public <T extends ModelObject> MatrixSortDefinition copy(Map<String, T> fieldMapIn) {
        
        MatrixSortDefinition myCopy = new MatrixSortDefinition();
        
        super.cloneComponents(myCopy);
        if (null != getCategoryDefinition()) {
        	myCopy.setCategoryDefinition(getCategoryDefinition().copy(fieldMapIn));
        }
        myCopy.setSortOrder(getSortOrder());
        
        return myCopy;
    }
}
