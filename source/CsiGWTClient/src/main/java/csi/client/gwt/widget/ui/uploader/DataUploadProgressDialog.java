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
package csi.client.gwt.widget.ui.uploader;

import java.util.Date;

import csi.server.common.util.Format;
import org.vectomatic.file.File;
import org.vectomatic.file.FileUploadExt;

import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.WarningDialog;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DataUploadProgressDialog {

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
	private Dialog dialog;

	private long _lastUpdate = (new Date()).getTime();

    @UiField
    FlowPanel progressPanel;
	@UiField
	Label fileProgressText1, fileProgressText2;
	@UiField
	ProgressBar fileProgress;

	interface SpecificUiBinder extends UiBinder<Dialog, DataUploadProgressDialog> {
	}

	private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

	public DataUploadProgressDialog(FileUploadExt dummyButton) {
		super();
		dialog = uiBinder.createAndBindUi(this);
		dialog.getActionButton().setVisible(false);
		dialog.hideTitleCloseButton();
		dialog.getCancelButton().setType(ButtonType.DANGER);
        dialog.addLeftControl(dummyButton);
        dummyButton.setVisible(false);
	}

	public void show() {

		dialog.show();

		updateTotalFilesProgress(1);
		fileProgress.setPercent(0);
	}

	private void updateTotalFilesProgress(int nextItemIn) {
	}

	public void hide() {
		dialog.hide();
	}

	public void setFileProgress(File fileIn, long currentBytesIn) {

		if (null != fileIn) {

			String mySourceName = fileIn.getName();
			String myTargetName = fileIn.getName();
			long mySize = fileIn.getSize();

            int percent = (int) (currentBytesIn * 100 / mySize);
            if (percent == 100) {
                fileProgressText1.setText(_constants.fileUploadProgressDialogServerProcessMessage()); //$NON-NLS-1$
                fileProgressText2.setText(_constants.fileUploadProgressDialogServerDelimiter() + Format.value(myTargetName)); //$NON-NLS-1$
            } else {
                fileProgressText1.setText(_constants.fileUploadProgressDialogUploadingLabel() + Format.value(mySourceName)); //$NON-NLS-1$
                fileProgressText2.setText(_constants.fileUploadProgressDialogUploadingDelimiter() + Format.value(myTargetName)); //$NON-NLS-1$
            }
            fileProgress.setPercent(percent);
		}
	}

	public void setLoadComplete(int nextItemIn) {
	}

	public HandlerRegistration addCancelHandler(final ClickHandler clickHandler) {
		return dialog.getCancelButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				WarningDialog warning = new WarningDialog(_constants.fileUploadProgressDialogCancelLabel(), //$NON-NLS-1$
                        _constants.fileUploadProgressDialogCancelWarningMessage()); //$NON-NLS-1$
				warning.addClickHandler(clickHandler);
				warning.show();
			}
		});
	}

}
