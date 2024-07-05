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

import com.emitrom.lienzo.client.core.shape.Picture;

import csi.client.gwt.dataview.resources.DataSourceClientUtil;
import csi.server.common.model.SqlTableDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TableIcon extends Picture {

    public TableIcon(SqlTableDef tableDef) {
        super(DataSourceClientUtil.get(tableDef, false), false);
    }

}
