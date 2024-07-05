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
package csi.client.gwt.viz.matrix.settings;

import csi.client.gwt.viz.shared.settings.AbstractSettingsComposite;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.matrix.MatrixViewDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class MatrixSettingsComposite extends AbstractSettingsComposite<MatrixViewDef> {

    public MatrixSettings getMatrixSettings() {

        if(getVisualizationSettings() == null) {
            return null;
        }
        if(getVisualizationSettings().getVisualizationDefinition() == null) {
            return null;
        }

        MatrixViewDef def = getVisualizationSettings().getVisualizationDefinition();
        return def.getMatrixSettings();
    }

}
