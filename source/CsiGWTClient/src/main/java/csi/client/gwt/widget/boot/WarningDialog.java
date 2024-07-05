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
public class WarningDialog {

    private Dialog dialog;

    @UiField
    Paragraph message;

    interface SpecificUiBinder extends UiBinder<Dialog, WarningDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public WarningDialog(String title, String messageText) {
        super();

        dialog = uiBinder.createAndBindUi(this);

        Button myYesButton = dialog.getActionButton();
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
        myYesButton.setText(Dialog.txtYesButton);
        myYesButton.setWidth("40px");
        myNoButton.setText(Dialog.txtNoButton);
        myNoButton.setWidth("40px");
        message.setText(messageText);
    }

    public void show() {
        dialog.show();
    }

    public void addClickHandler(ClickHandler handler) {
        dialog.getActionButton().addClickHandler(handler);
    }
}
