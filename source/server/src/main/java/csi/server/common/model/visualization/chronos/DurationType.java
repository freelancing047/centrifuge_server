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
public enum DurationType implements Serializable, HasLabel {

    DAYS("Days", 24 * 3600 * 1000), //
    HOURS("Hours", 3600 * 1000), //
    MINUTES("Minutes", 60 * 1000), //
    SECONDS("Seconds", 1000), //
    MILLISECONDS("Milliseconds", 1), //
    ;

    private String label;
    private int multiplicationFactorForMillis;

    private DurationType(String label, int factor) {
        this.label = label;
        multiplicationFactorForMillis = factor;
    }

    public String getLabel() {
        return label;
    }

    public long toMillis(long duration) {
        return duration * multiplicationFactorForMillis;
    }

}
