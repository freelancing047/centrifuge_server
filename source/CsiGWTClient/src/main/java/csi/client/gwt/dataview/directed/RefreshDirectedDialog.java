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
package csi.client.gwt.dataview.directed;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;

import csi.client.gwt.WebMain;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.events.CloseDataViewEvent;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.DecisionDialog;
import csi.client.gwt.widget.buttons.ButtonDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class RefreshDirectedDialog {

    private DirectedPresenter presenter;
    private DecisionDialog dialog;
    


    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    private static final List<ButtonDef> _buttonList = new ArrayList<ButtonDef>();
    

    //
    // Handle choice being made between templates and dataviews
    //
    private ChoiceMadeEventHandler handleChoiceMadeEvent
    = new ChoiceMadeEventHandler() {
        @Override
        public void onChoiceMade(ChoiceMadeEvent eventIn) {
            
            int _choice = eventIn.getChoice();
            
            switch (_choice) {
                
                case 0:
                    
                    WebMain.injector.getEventBus()
                    .fireEvent(new CloseDataViewEvent(getPresenter().getDataView().getUuid()));

                    break;
                    
                case 1:
                    
                    getPresenter().getRequiredParameters();

                    break;
                    
                case 2:

                    presenter.launchDataSourceEditor();
                    break;
            }
        }
    };


    public RefreshDirectedDialog(DirectedPresenter presenter) {
        this.presenter = presenter;
        if(_buttonList.size() == 0){
	        _buttonList.add( new ButtonDef(i18n.refreshDirectedDialogRefreshButton(), ButtonType.SUCCESS)); //$NON-NLS-1$
	
	        _buttonList.add(new ButtonDef(i18n.refreshDirectedDialogEditButton(), ButtonType.PRIMARY)); //$NON-NLS-1$)
        }
        dialog = new DecisionDialog(i18n.refreshDirectedDialogRefreshTitle(), //$NON-NLS-1$
                i18n.refreshDirectedDialogRefreshMessage(), _buttonList, handleChoiceMadeEvent, 70); //$NON-NLS-1$
        dialog.getCancelButton().setText(i18n.refreshDirectedDialogCloseButton()); //$NON-NLS-1$
    }
    
    public void show() {
        dialog.show();
    }

    public DirectedPresenter getPresenter() {
        return presenter;
    }
}
