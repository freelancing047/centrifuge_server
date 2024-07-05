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
package csi.shared.core.publish;

import java.io.Serializable;

import csi.shared.core.util.HasLabel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public enum SnapshotType implements HasLabel, Serializable {

    VISUALIZATION("Visualizaton"), //
    WORKSHEET("Worksheet"), //
    DATAVIEW("Dataview"), //
    ;

    private String label;

    private SnapshotType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
