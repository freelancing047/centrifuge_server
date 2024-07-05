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
package csi.server.common.model.visualization;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.chart.LabelDefinition;
import csi.server.util.sql.api.AggregateFunction;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractMeasureDefinition extends AbstractAttributeDefinition {

    public AbstractMeasureDefinition() {
        super();
    }

    @ManyToOne
    private FieldDef fieldDef;

    private AggregateFunction aggregateFunction = AggregateFunction.COUNT;

    @OneToOne(cascade = CascadeType.ALL)
    private LabelDefinition labelDefinition = new LabelDefinition();

    private boolean allowNulls;
    private int listPosition;

    @Override
    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public LabelDefinition getLabelDefinition() {
        return labelDefinition;
    }

    public void setLabelDefinition(LabelDefinition labelDefinition) {
        this.labelDefinition = labelDefinition;
    }

    public AggregateFunction getAggregateFunction() {
        return aggregateFunction;
    }

    public void setAggregateFunction(AggregateFunction aggregateFunction) {
        this.aggregateFunction = aggregateFunction;
    }

    @Override
    public String getComposedName() {
        String label =  getAggregateFunction().getLabel();
        return getLabelDefinition().getComposedName(
                label + " " + getLabelDefinition().getLabel(getFieldDef()));
    }

    public String intedComposedName() {
        String tokenizedFunction = "___MAGIC__"+getAggregateFunction().toString()+"___DELIM__";

        return getLabelDefinition().getComposedName(
                tokenizedFunction + getLabelDefinition().getLabel(getFieldDef()));
    }

    public boolean isAllowNulls() {
        return allowNulls;
    }

    public void setAllowNulls(boolean allowNulls) {
        this.allowNulls = allowNulls;
    }

    @Override
    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    @Override
    public CsiDataType getDerivedType() {
        return CsiDataType.Number;
    }

    @SuppressWarnings("unchecked")
    protected <T extends ModelObject> void cloneComponents(AbstractMeasureDefinition cloneIn, Map<String, T> fieldMapIn) {
        
        if (null != cloneIn) {
            
            super.cloneComponents(cloneIn);
            
            cloneIn.setFieldDef((FieldDef)cloneFromOrToMap(fieldMapIn, (T)getFieldDef(), fieldMapIn));
            cloneIn.setAggregateFunction(getAggregateFunction());
            cloneIn.setLabelDefinition(getLabelDefinition());
            cloneIn.setAllowNulls(isAllowNulls());
        }
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends ModelObject> void copyComponents(AbstractMeasureDefinition cloneIn, Map<String, T> fieldMapIn) {
        
        if (null != cloneIn) {
            
            super.cloneComponents(cloneIn);
            
            cloneIn.setFieldDef(getFieldDef());
            cloneIn.setAggregateFunction(getAggregateFunction());
            
            if(getLabelDefinition() != null)
            	cloneIn.setLabelDefinition(getLabelDefinition().copy());
            
            cloneIn.setAllowNulls(isAllowNulls());
        }
    }
}
