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
package csi.client.gwt.dataview;

import java.util.Arrays;
import java.util.List;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.fieldlist.FieldList;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.ApplicationToolbar;
import csi.client.gwt.mainapp.ApplicationToolbarLocator;
import csi.server.common.util.ConnectorSupport;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.DecisionDialog;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.ButtonDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class RefreshDataViewDialog {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _closeButton = Dialog.txtCloseButton;
    private static final String _refreshButton = Dialog.txtRefreshButton;
    private static final String _sourcesButton = _constants.refreshDataViewDialog_SourcesButton();
    private static final String _fieldListButton = _constants.refreshDataViewDialog_FieldListButton();
    private static final String _title = _constants.refreshDataViewDialog_Title();
    private static final String _displayMessage
                                = _constants.refreshDataViewDialog_DisplayMessage(_refreshButton, _fieldListButton,
            _sourcesButton, _closeButton);

    private AbstractDataViewPresenter presenter;
    private DecisionDialog dialog;
    
    private List<ButtonDef> _buttonList;
    

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

                    ApplicationToolbarLocator.getInstance().abortDataView();
                    break;
                    
                case 1:
                    
                    getPresenter().getRequiredParameters();

                    if(dialog != null)
                        dialog.hide();
                    break;

                case 2:

                    presenter.launchDataSourceEditor();
                    break;

                case 3:

                    FieldList fieldList = new FieldList(presenter, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {

                            (new RefreshDataViewDialog(presenter)).show();
                        }
                    } );
                    fieldList.show();

                    break;
            }
        }
    };


    public RefreshDataViewDialog(AbstractDataViewPresenter abstractDataViewPresenter) {
        this.presenter = abstractDataViewPresenter;

        boolean myEnableFlag = ConnectorSupport.getInstance().canEnterSourceEditor(presenter.getDataSources());
        _buttonList = Arrays.asList(

                new ButtonDef(_refreshButton, ButtonType.SUCCESS),
                new ButtonDef(_sourcesButton, ButtonType.PRIMARY, myEnableFlag),
                new ButtonDef(_fieldListButton, ButtonType.PRIMARY)
        );
        dialog = new DecisionDialog(_title, _displayMessage, _buttonList, handleChoiceMadeEvent, 80);
        dialog.getCancelButton().setText(_closeButton);
    }
    
    public boolean isHidden(){
        return dialog == null || dialog.isVisible();
    }
    
    public void show() {

        if (WebMain.injector.getMainPresenter().isDataViewReadOnly()) {

            Display.error(_constants.refreshErrorDialogTitle(), _constants.serverMessage_DataViewEditError());

        } else {

            dialog.show();
        }
    }

    public AbstractDataViewPresenter getPresenter() {
        return presenter;
    }
}
