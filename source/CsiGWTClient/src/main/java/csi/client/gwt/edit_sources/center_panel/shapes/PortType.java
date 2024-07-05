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
package csi.client.gwt.edit_sources.center_panel.shapes;

import csi.server.common.model.operator.OpMapType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public enum PortType {

    JOIN_LEFT(OpMapType.JOIN), //
    JOIN_RIGHT(OpMapType.JOIN), //
    APPEND_TOP(OpMapType.APPEND), //
    APPEND_BOTTOM(OpMapType.APPEND), //
    ;

    private OpMapType mapType;

    private PortType(OpMapType mapType) {
        this.mapType = mapType;
    }

    public OpMapType getMapType() {
        return mapType;
    }
}
