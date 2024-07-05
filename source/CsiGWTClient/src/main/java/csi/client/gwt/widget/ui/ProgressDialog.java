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
package csi.client.gwt.widget.ui;

import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.WarningDialog;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ProgressDialog {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface SpecificUiBinder extends UiBinder<Dialog, ProgressDialog> {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private Dialog dialog;

    @UiField
    Label counterLabel;
    @UiField
    Label progressLabel;
    @UiField
    ProgressBar counterBar;
    @UiField
    ProgressBar progressBar;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    ClickHandler _cancelHandler;
    String _cancelLabel;
    String _cancelMessage;

    long _counterLimit;
    long _counterTotal;

    long _progressLimit;
    long _progressTotal;
    
    int _delta;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    ClickHandler cancelHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            WarningDialog warning = new WarningDialog(_cancelLabel, _cancelMessage);
            warning.addClickHandler(_cancelHandler);
            warning.show();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ProgressDialog(String titleIn, long counterLimitIn, String counterTextIn,
                          long progressLimitIn, String progressTextIn, int deltaIn, String cancelLabelIn,
                          String cancelMessageIn, ClickHandler cancelHandlerIn) {

        super();

        _counterLimit = counterLimitIn;
        _progressLimit = progressLimitIn;
        _delta = deltaIn;
        _cancelLabel = cancelLabelIn;
        _cancelMessage = cancelMessageIn;
        _cancelHandler = cancelHandlerIn;

        dialog = uiBinder.createAndBindUi(this);
        dialog.getActionButton().setVisible(false);
        dialog.hideTitleCloseButton();
        dialog.getCancelButton().setType(ButtonType.DANGER);
        dialog.getCancelButton().addClickHandler(cancelHandler);

        resetDisplay(titleIn, counterTextIn, progressTextIn);
    }

    public void show() {

        dialog.show();
    }

    public void hide() {
        dialog.hide();
    }

    public void enableCancelButton() {

        dialog.getCancelButton().setEnabled(true);
    }

    public void disableCancelButton() {

        dialog.getCancelButton().setEnabled(false);
    }

    public void showCancelButton() {

        dialog.getCancelButton().setVisible(true);
        enableCancelButton();
    }

    public void hideCancelButton() {

        disableCancelButton();
        dialog.getCancelButton().setVisible(false);
    }

    public void setCounterLabel(String counterTextIn) {

        counterLabel.setText(counterTextIn);
    }

    public void setProgressLabel(String progressTextIn) {

        progressLabel.setText(progressTextIn);
    }

    public void sumProgress(int valueIn) {

        sumProgress((long)valueIn * (long)(1024 * 1024));
    }

    public void sumProgress(long valueIn) {

        setProgress(_progressTotal + valueIn);
    }

    public void setPercentComplete(int valueIn) {

        updateDisplay(valueIn);
   }

    public void setProgress(int valueIn) {

        setProgress((long)valueIn * (long)(1024 * 1024));
    }

    public void setProgress(long valueIn) {

        _progressTotal = valueIn;
        updateDisplay();
    }

    public void resetDisplay(String titleIn, String counterTextIn, String progressTextIn) {

        dialog.setTitle(titleIn);
        resetDisplay(counterTextIn, progressTextIn);
    }

    public void resetDisplay(String counterTextIn, String progressTextIn) {

        counterLabel.setText(counterTextIn);
        progressLabel.setText(progressTextIn);

        counterBar.setPercent(0);
        progressBar.setPercent(0);
    }

    public void resetProgress(String counterTextIn, String progressTextIn) {

        resetProgress(counterTextIn, progressTextIn, true);
    }

    public void resetProgress(String counterTextIn, String progressTextIn, boolean zeroIn) {

        counterLabel.setText(counterTextIn);
        resetProgress(progressTextIn, zeroIn);
    }

    public void resetProgress(String progressTextIn) {

        resetProgress(progressTextIn, true);
    }

    public void resetProgress(String progressTextIn, boolean zeroIn) {

        progressLabel.setText(progressTextIn);

        resetProgress(zeroIn);
    }

    public void resetProgress() {

        resetProgress(true);
    }

    public void resetProgress(boolean zeroIn) {

        _counterTotal++;
        counterBar.setPercent((int)((100 * Math.min(_counterTotal, _counterLimit)) / _counterLimit));

        if (zeroIn) {

            _progressTotal = 0L;
            progressBar.setPercent(0);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void updateDisplay() {

        int myPercent = (int)((100 * Math.min(_progressTotal, _progressLimit)) / _progressLimit);

        if ((progressBar.getPercent() + _delta) <= myPercent) {

            progressBar.setPercent(myPercent);
        }
    }

    private void updateDisplay(int percentIn) {

        if ((progressBar.getPercent() + _delta) <= percentIn) {

            progressBar.setPercent(percentIn);
        }
    }
}
