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

import csi.server.common.model.visualization.VisualizationDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface SettingsActionCallback<T extends VisualizationDef> {

    /**
     * Called when the settings have been saved.
     * @param vizDef A new visualization if one has been created.
     * @param suppressLoadAfterSave 
     */
    public void onSaveComplete(T vizDef, boolean suppressLoadAfterSave);

    public void onCancel();
}
