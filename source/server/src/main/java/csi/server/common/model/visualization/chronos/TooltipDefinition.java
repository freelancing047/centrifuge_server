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
package csi.server.common.model.visualization.chronos;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.LabeledFieldReference;
import csi.server.common.model.ModelObject;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TooltipDefinition extends ModelObject implements Serializable {


    public TooltipDefinition() {
        super();
    }

    @OneToOne
    private LabeledFieldReference field;
    private boolean hyperLink;

    public LabeledFieldReference getField() {
        return field;
    }

    public void setField(LabeledFieldReference field) {
        this.field = field;
    }

    public boolean isHyperLink() {
        return hyperLink;
    }

    public void setHyperLink(boolean hyperLink) {
        this.hyperLink = hyperLink;
    }

}
