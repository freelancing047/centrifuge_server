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
package csi.client.gwt.viz.shared.settings;

import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.visualization.VisualizationDef;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface VisualizationSettings {

    public DataViewDef getDataViewDefinition();

    public <T extends VisualizationDef> T getVisualizationDefinition();

}
