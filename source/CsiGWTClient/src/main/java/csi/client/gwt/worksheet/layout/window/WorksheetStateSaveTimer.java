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
package csi.client.gwt.worksheet.layout.window;

import com.google.gwt.user.client.Timer;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.ExceptionHandler;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.ModelObject;
import csi.server.common.model.worksheet.VisualizationLayoutState;
import csi.server.common.model.worksheet.WorksheetScreenLayout;
import csi.server.common.service.api.ModelActionsServiceProtocol;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class WorksheetStateSaveTimer extends Timer {

    private WorksheetScreenLayout layoutState;
    private boolean invalid = false;
    private boolean locked = false;

    public WorksheetScreenLayout getLayoutState() {
        return layoutState;
    }

    public void setLayoutState(WorksheetScreenLayout layoutState) {
        this.layoutState = layoutState;
    }

    @Override
    public void run() {
        if(invalid){
            layoutState.clear();
            return;
        }
        locked = true;
        if (WebMain.injector.getMainPresenter().canEditDataView()) {

            try {
                WorksheetScreenLayout worksheetScreenLayout = layoutState.getWorksheetDef().getWorksheetScreenLayout();
                for (VisualizationLayoutState layout : worksheetScreenLayout.getLayout().getLayouts()) {
                    if (layout.getHeight() == 0 || layout.getWidth()==0) {
                        layoutState.clear();
                        return;
                    }
                }
                WebMain.injector.getVortex().execute(new ExceptionHandler() {

                    @Override
                    public boolean handle(Throwable t) {
                        locked = false;
                        // Stop default failure handling.
                        return false;
                    }
                }, new Callback<ModelObject>() {

                    @Override
                    public void onSuccess(ModelObject result) {
                        locked = false;
                    }
                }, ModelActionsServiceProtocol.class).save(worksheetScreenLayout);
            } catch (CentrifugeException e) {
                locked = false;
                //e.printStackTrace();
            }
        }
        layoutState.clear();
    } // end run()

    public void invalidate() {
        invalid = true;
    }
    
    public boolean isLocked() {
        return locked ;
    }

}
