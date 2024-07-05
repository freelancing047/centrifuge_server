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
package csi.client.gwt.viz.shared.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.DataViewRegistry;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.publish.SnapshotPublishDialog;
import csi.client.gwt.vortex.Callback;
import csi.server.common.service.api.PublishingActionsServiceProtocol;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.publish.SnapshotImagingRequest;
import csi.shared.core.publish.SnapshotImagingResponse;
import csi.shared.core.publish.SnapshotPublishRequest;
import csi.shared.core.publish.SnapshotType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class PublishMenuHandler<V extends Visualization, M extends AbstractMenuManager<V>> extends
        AbstractMenuEventHandler<V, M> {

    public PublishMenuHandler(V presenter, M menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public boolean isApplicable() {
        return getPresenter().isImagingCapable();
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        ImagingRequest ir = getPresenter().getImagingRequest();
        SnapshotImagingRequest request = new SnapshotImagingRequest();
        request.getImagingRequests().add(ir);
        WebMain.injector.getVortex().execute(new Callback<SnapshotImagingResponse>() {

            @Override
            public void onSuccess(SnapshotImagingResponse result) {
                SnapshotPublishRequest request = new SnapshotPublishRequest();
                request.setDataViewUuid(DataViewRegistry.getInstance()
                        .dataViewPresenterForVisualization(getPresenter().getUuid()).getUuid());
                request.setMetaDescription("Snapshot of visualization " + getPresenter().getName());
                request.setSnapshotType(SnapshotType.VISUALIZATION);

                SnapshotPublishDialog dialog = new SnapshotPublishDialog();
                dialog.setImagingResponse(result);
                dialog.setPublishRequest(request);
                dialog.show();
            }
        }, PublishingActionsServiceProtocol.class).getImages(request);
    }
}
