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

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.widget.misc.ModelAwareView;
import csi.server.common.model.visualization.VisualizationDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractSettingsComposite<V extends VisualizationDef> 
	extends ResizeComposite 
	implements VisualizationSettingsAware, ModelAwareView, HasOneWidget {

    private VisualizationSettings visualizationSettings;

    public VisualizationSettings getVisualizationSettings() {
        return visualizationSettings;
    }

    @Override
    public void setVisualizationSettings(VisualizationSettings visualizationSettings) {
        this.visualizationSettings = visualizationSettings;
    }

    @Override
    public Widget getWidget() {
        return super.getWidget();
    }
    
    @Override
    @Deprecated
    public void setWidget(Widget widget) {
        throw new RuntimeException("Method not supported");
    }

    @Override
    public void setWidget(IsWidget w) {
        throw new RuntimeException("Method not supported");
    }

}
