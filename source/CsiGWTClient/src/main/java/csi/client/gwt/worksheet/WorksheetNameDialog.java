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
package csi.client.gwt.worksheet;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class WorksheetNameDialog {

    private Dialog dialog;

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    @UiField(provided = true)
    String title;
    @UiField
    TextBox nameTextBox;

    interface SpecificUiBinder extends UiBinder<Dialog, WorksheetNameDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public WorksheetNameDialog(String currentName) {
        super();
        title = currentName == null ? i18n.worksheetNameDialogcreateTitle() : i18n.worksheetNameDialogrenameTitle(); //$NON-NLS-1$ //$NON-NLS-2$
        dialog = uiBinder.createAndBindUi(this);
        nameTextBox.setValue(currentName);
        dialog.getActionButton().setText(currentName == null ? i18n.worksheetNameDialogCreateAction() : i18n.worksheetNameDialogrenameTitle()); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.hideOnCancel();
    }

    public String getName() {
        return nameTextBox.getValue().trim().isEmpty() ? i18n.worksheetNameDialogDefaultWorksheetName() : nameTextBox.getValue(); //$NON-NLS-1$
    }

    public void show() { dialog.show(); }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return dialog.getActionButton().addClickHandler(handler);
    }

    public void hide() { dialog.hide(); }
}
