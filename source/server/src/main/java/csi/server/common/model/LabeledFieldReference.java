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
package csi.server.common.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.visualization.chart.LabelDefinition;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LabeledFieldReference extends BundledFieldReference {

    public LabeledFieldReference() {
        super();
    }

    @ManyToOne
    private LabelDefinition labelDefinition = new LabelDefinition();

    public LabelDefinition getLabelDefinition() {
        return labelDefinition;
    }

    public void setLabelDefinition(LabelDefinition labelDefinition) {
        this.labelDefinition = labelDefinition;
    }

}
