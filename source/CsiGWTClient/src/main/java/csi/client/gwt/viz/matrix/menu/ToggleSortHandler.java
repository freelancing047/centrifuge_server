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

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.MatrixActionsServiceProtocol;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
abstract class ToggleSortHandler extends AbstractMatrixMenuEventHandler {
    public ToggleSortHandler(MatrixPresenter presenter, MatrixMenuManager menuManager) {
        super(presenter, menuManager);
    }

    protected void saveSettings(){
        // update selection on the view def, but don't save into old selection.
        getPresenter().updateRowsSelection();

        VortexFuture<Selection> vortexFuture = WebMain.injector.getVortex().createFuture();
        vortexFuture.execute(MatrixActionsServiceProtocol.class).doQuickSortFilter(getPresenter().getVisualizationDef(), getPresenter().getDataViewUuid());
        vortexFuture.addEventHandler(new VortexEventHandler<Selection>() {
            @Override
            public void onSuccess(Selection result) {
                if(!(result instanceof NullSelection)){
                    getPresenter().saveOldSelection(result);
                }else{
                    getPresenter().popOldSelection();
                }
                getPresenter().reload();
            }

            @Override
            public boolean onError(Throwable t) {
                return false;
            }

            @Override
            public void onUpdate(int taskProgess, String taskMessage) {

            }

            @Override
            public void onCancel() {

            }
        });
//        getPresenter().saveSettings(true);


    }
}
