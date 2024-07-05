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
package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

import csi.client.gwt.widget.buttons.Button;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DataviewConflictDialog {

    private Dialog dialog;

    @UiField
    Paragraph message;

    interface SpecificUiBinder extends UiBinder<Dialog, DataviewConflictDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    Button myYesButton;
    public DataviewConflictDialog(String title, String messageText) {
        super();

        dialog = uiBinder.createAndBindUi(this);

        myYesButton = dialog.getActionButton();
        Button myNoButton = dialog.getCancelButton();

        Icon icon = new Icon(IconType.WARNING_SIGN);
        icon.getElement().getStyle().setFontSize(20, Unit.PX);
        icon.getElement().getStyle().setColor(Dialog.txtWarningColor);
        icon.getElement().getStyle().setPaddingRight(10, Unit.PX);

        dialog.identifyAsAlert();
        dialog.hideTitleCloseButton();
        dialog.addToHeader(icon);

        CsiHeading heading = Dialog.createHeading(title);
        heading.getElement().getStyle().setDisplay(Display.INLINE);
        dialog.addToHeader(heading);
        dialog.hideOnCancel();
        dialog.hideOnAction();
        myYesButton.setType(ButtonType.WARNING);
        myYesButton.setText("Reopen");
        myYesButton.setWidth("40px");
        myNoButton.setText("Close");
        myNoButton.setWidth("40px");
        message.setText(messageText);
    }

    public void setActionText(String text) {
        myYesButton.setText(text);
    }

    public void show() {
        dialog.show();
    }
    public void hide() {
        dialog.hide();
    }

    public void addClickHandler(ClickHandler handler) {
        dialog.getActionButton().addClickHandler(handler);
    }

    public void removeActionButton() {
        dialog.getActionButton().setVisible(false);
    }
    
}
