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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.widget.boot.WarningDialog;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DeleteMenuHandler<V extends Visualization, M extends AbstractMenuManager<V>> extends
        AbstractMenuEventHandler<V, M> {

    public DeleteMenuHandler(V presenter, M menuManager) {
        super(presenter, menuManager);
        
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        WarningDialog dialog = new WarningDialog(CentrifugeConstantsLocator.get().deleteMenuHanlder_title(),
        		CentrifugeConstantsLocator.get().deleteMenuHandler_message());
        dialog.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                getPresenter().delete();
                
            }
        });
        dialog.show();
    }

}
