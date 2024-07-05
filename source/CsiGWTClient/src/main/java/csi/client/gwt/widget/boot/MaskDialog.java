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

import com.github.gwtbootstrap.client.ui.Paragraph;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.widget.buttons.SimpleButton;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class MaskDialog extends SizeProvidingModal {

    private enum State {
        INIT, HIDDEN, SHOWN
    };

    private State state = State.INIT;
    private boolean killTask = false;
    private boolean isProcessing = false;
    private HandlerRegistration handlerRegistration = null;
    private ClickHandler buttonHandler = null;
    @UiField
    Paragraph messageParagraph;
//    @UiField
//    HorizontalPanel buttonPanel;
    @UiField
    SimpleButton cancelButton;

    interface SpecificUiBinder extends UiBinder<Widget, MaskDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public MaskDialog(CanBeShownParent parentIn) {
        super(parentIn);
        identifyAsAlert();
        setBodyWidth("400px");
        setBodyHeight("110px");
        add(uiBinder.createAndBindUi(this));
        cancelButton.setVisible(false);
    }

    public MaskDialog(String titleIn, String messageIn, CanBeShownParent parentIn) {
        this(parentIn);
        setMessage(titleIn, messageIn);
    }

    public MaskDialog(String messageIn, CanBeShownParent parentIn) {
        this(parentIn);
        setMessage(messageIn);
    }

    public MaskDialog(String titleIn, String messageIn) {
        this((CanBeShownParent)null);
        setMessage(titleIn, messageIn);
    }

    public MaskDialog(String messageIn) {
        this((CanBeShownParent)null);
        setMessage(messageIn);
    }

    public void setMessage(String titleIn, String messageIn) {

        setTitle(titleIn);
        setMessage(messageIn);
    }

    public void setMessage(String messageIn) {

        if (null != messageIn) {

            messageParagraph.setText(messageIn);
        }
    }

    @Override
    public void show() {
        isProcessing = true;
        Scheduler.get().scheduleEntry(() -> {
            if(killTask){
                killTask = false;
                isProcessing = killTask;
            } else {
                MaskDialog.super.show();
                state = State.SHOWN;
                isProcessing = false;
            }
            return isProcessing;
        });
    }

    @Override
    public void hide() {
        hideCancelButton();
        //if hide gets called before the show() has been finished.
        if (isProcessing){
            killTask=true;
        }
        if (state == State.SHOWN) {
            super.hide();
        }
        state = State.HIDDEN;
    }

    public void hideCancelButton() {

        if (null != handlerRegistration) {

            handlerRegistration.removeHandler();
        }
        buttonHandler = null;
        cancelButton.setVisible(false);
    }

    public void showCancelButton(ClickHandler handlerIn) {

        if ((null == buttonHandler) || (buttonHandler != handlerIn)) {

            if (null != handlerRegistration) {

                handlerRegistration.removeHandler();
                buttonHandler = null;
            }
            if (null != handlerIn) {

                handlerRegistration = cancelButton.addClickHandler(handlerIn);
                buttonHandler = handlerIn;
            }
        }
        cancelButton.setVisible(true);
    }
}
