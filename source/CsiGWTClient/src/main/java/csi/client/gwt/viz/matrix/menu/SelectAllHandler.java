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
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.service.api.MatrixActionsServiceProtocol;
import csi.shared.core.visualization.matrix.MatrixDataRequest;
import csi.shared.core.visualization.matrix.MatrixWrapper;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SelectAllHandler extends AbstractMatrixMenuEventHandler {

    public SelectAllHandler(MatrixPresenter presenter, MatrixMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        // Select all the elements.
        getPresenter().getModel().getSelectedCells().clear();

        if(getPresenter().getModel().isSummary()){

            MatrixDataRequest dr = new MatrixDataRequest();
            dr.setVizUuid(getPresenter().getVisualizationDef().getUuid());
            dr.setDvUuid(getPresenter().getDataViewUuid());
            dr.setSummarizationPolicy(MatrixDataRequest.REQUEST_SUMMARIZATION_POLICY.DISALLOW_SUMMARY);
            dr.setExtent(0, getPresenter().getModel().getMetrics().getAxisXCount(),0, getPresenter().getModel().getMetrics().getAxisYCount());
            Vortex vortex = getPresenter().getVortex();
            VortexFuture<MatrixWrapper> vortexFuture = vortex.createFuture();

            vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
                @Override
                public void onSuccess(MatrixWrapper result) {
                    result.getData().getCells().forEach(cell -> {
                            getPresenter().getModel().selectCell(cell);
                    });

                    getPresenter().getView().setLoadingIndicator(false);
                    getPresenter().getView().refresh();
                }
            });

            vortexFuture.execute(MatrixActionsServiceProtocol.class).getCellsInRegionForSelection(dr);
            getPresenter().getView().setLoadingIndicator(true);

        }else {
            getPresenter().getModel().getSelectedCells().addAll(getPresenter().getModel().getMatrixDataResponse().getCells());
        }
        // Redisplay.
        getPresenter().getView().refresh();
    }

}
