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

import csi.shared.core.util.HasLabel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public enum Axis implements Serializable, HasLabel {

    X("X-axis"), //
    Y("Y-axis");

    private String label;

    public Axis flipAxis(){
        if(this == Axis.X){
            return Axis.Y;
        }else{
            return Axis.X;
        }
    }

    private Axis(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

}
