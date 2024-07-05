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

import csi.shared.core.util.HasLabel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public enum EventBarStyle implements Serializable, HasLabel {

    SOLID("Solid"), //
    DASHED_EVEN("Dashed - even"), //
    DASHED_SHORT("Dashed - short"), //
    DASHED_LONG("Dashed - long"), //
    ;

    private String label;

    private EventBarStyle(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

}
