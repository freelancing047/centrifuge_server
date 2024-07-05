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

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.AbstractCategoryDefinition;
import csi.shared.core.util.TypedClone;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MatrixCategoryDefinition extends AbstractCategoryDefinition implements
        TypedClone<MatrixCategoryDefinition> {

    @Enumerated(EnumType.ORDINAL)
    private Axis axis;

    public MatrixCategoryDefinition() {
        super();
    }

    public Axis getAxis() {
        return axis;
    }

    public void setAxis(Axis axis) {
        this.axis = axis;
    }

    @Override
    public String getDefinitionName() {
        return "Category";
    }

    @Override
    public MatrixCategoryDefinition getClone() {
        MatrixCategoryDefinition mcd = new MatrixCategoryDefinition();
        mcd.setAllowNulls(this.isAllowNulls());
        mcd.setAxis(this.getAxis());
        mcd.setBundleFunction(this.getBundleFunction());
        mcd.setBundleFunctionParameters(this.getBundleFunctionParameters());
        mcd.setFieldDef(this.getFieldDef());
        mcd.setLabelDefinition(this.getLabelDefinition());
        return mcd;
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof MatrixCategoryDefinition)) {
            return false;
        } else {
            MatrixCategoryDefinition typed = (MatrixCategoryDefinition) obj;
            return Objects.equal(this.getUuid(), typed.getUuid());
        }
    }

    @Override
    public <T extends ModelObject> MatrixCategoryDefinition clone(Map<String, T> fieldMapIn) {
        MatrixCategoryDefinition myClone = new MatrixCategoryDefinition();
        super.cloneComponents(myClone, fieldMapIn);
        
        myClone.setAxis(getAxis());
        return myClone;
    }
    
    public <T extends ModelObject> MatrixCategoryDefinition copy(Map<String, T> fieldMapIn) {
        if(fieldMapIn.get(this.getUuid()) != null){
            return (MatrixCategoryDefinition) fieldMapIn.get(this.getUuid());
        }
        MatrixCategoryDefinition myCopy = new MatrixCategoryDefinition();
        super.copyComponents(myCopy);
        
        myCopy.setAxis(getAxis());
        fieldMapIn.put(this.getUuid(), (T) myCopy);
        return myCopy;
    }
}
