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

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Strings;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LabelDefinition extends ModelObject implements Serializable {

    public LabelDefinition() {
        super();
    }
    
    private String staticLabel;

    public String getLabel(FieldDef fieldDef) {
        if (Strings.isNullOrEmpty(staticLabel)) {
            return fieldDef.getFieldName();
        } else {
            return staticLabel;
        }
    }

            public String getLabelDescription() {
                if (staticLabel == null) {
                    return DefinitionType.FIELD_NAME.getLabel();
                } else {
            return staticLabel;
        }
    }

    public String getStaticLabel() {
        return staticLabel;
    }

    public LabelDefinition setStaticLabel(String value) {
        staticLabel = value;
        return this;
    }

    public void setFieldName() {
        staticLabel = null;
    }

    public boolean isStatic() {
        return staticLabel != null && staticLabel.trim().length() > 0;
    }

    public String getComposedName(String name) {
        return isStatic() && !Strings.isNullOrEmpty(getStaticLabel()) ? getStaticLabel() : name;
    }
    
    @Override
    public LabelDefinition clone() {
        
        LabelDefinition myClone = new LabelDefinition();
        
        super.cloneComponents(myClone);

        myClone.setStaticLabel(getStaticLabel());
        
        return myClone;
    }
    
    public LabelDefinition copy() {
        
        LabelDefinition myClone = new LabelDefinition();
        
        super.copyComponents(myClone);

        if(getStaticLabel() != null){
        	myClone.setStaticLabel(getStaticLabel());
        }
        return myClone;
    }
    
   
}
