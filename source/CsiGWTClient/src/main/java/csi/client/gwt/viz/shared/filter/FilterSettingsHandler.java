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
package csi.client.gwt.viz.shared.filter;

import com.github.gwtbootstrap.client.ui.event.HideEvent;
import com.github.gwtbootstrap.client.ui.event.HideHandler;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.server.common.model.filter.Filter;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class FilterSettingsHandler<T extends Visualization> extends
        AbstractMenuEventHandler<T, AbstractMenuManager<T>> {

    public FilterSettingsHandler(T presenter, AbstractMenuManager<T> menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        //check for marker interface
        if (getPresenter() instanceof FilterCapableVisualizationPresenter) {
            openFilterDialog();
        } else {
            new ErrorDialog(CentrifugeConstantsLocator.get().filterSettingsHandler_ErrorTitle(), CentrifugeConstantsLocator.get().filterSettingsHandler_ErrorMessage()).show();
        }
    }

    private void openFilterDialog() {
        Filter filter = getPresenter().getVisualizationDef().getFilter();
        String dataViewUuid = getPresenter().getDataViewUuid();
        final ManageFilterDialog dialog = new ManageFilterDialog(filter, dataViewUuid);
        dialog.addHideHandler(new HideHandler() {
            @Override
            public void onHide(HideEvent hideEvent) {
                getPresenter().getVisualizationDef().setFilter(dialog.getSelectedFilter());

                if(getPresenter() instanceof Graph) {//FIXME: Make separate handler for graph
                    getPresenter().reload();
                } else {
                    //FIXME: only load if filter has changed?
                    VortexFuture<Void> future = getPresenter().saveSettings(true);
                }
                
            }
        });
        dialog.show();
    }
}
