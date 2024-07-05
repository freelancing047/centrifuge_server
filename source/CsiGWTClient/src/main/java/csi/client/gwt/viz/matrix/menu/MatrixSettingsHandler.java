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
package csi.client.gwt.viz.matrix.menu;

import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.matrix.settings.MatrixSettingsPresenter;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.server.common.model.visualization.matrix.MatrixViewDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class MatrixSettingsHandler extends AbstractMatrixMenuEventHandler implements
        SettingsActionCallback<MatrixViewDef> {

    public MatrixSettingsHandler(MatrixPresenter presenter, MatrixMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        MatrixSettingsPresenter presenter = new MatrixSettingsPresenter(this);
        presenter.setDataView(getPresenter().getDataView());
        presenter.setVisualizationDef(getPresenter().getVisualizationDef());
        presenter.setVisualization(getPresenter());
        presenter.show();
    }

    @Override
    public void onSaveComplete(MatrixViewDef visualizationDef, boolean suppressLoadAfterSave) {
        getPresenter().getModel().clearSelectedCells();
        getPresenter().saveViewStateToVisualizationDef();
        getPresenter().invalidateCache();
        getPresenter().getView().resetSearchDialog();

        if (!suppressLoadAfterSave) {
            getPresenter().loadVisualization();
        }
    }

    @Override
    public void onCancel() {
        // Noop
    }
}
