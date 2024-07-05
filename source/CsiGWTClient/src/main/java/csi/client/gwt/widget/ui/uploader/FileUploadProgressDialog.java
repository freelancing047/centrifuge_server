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

import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.client.gwt.widget.ui.uploader.UploadWidget.UploadFile;
import csi.server.common.util.Format;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class FileUploadProgressDialog {

	private static final long _updateInterval = 250L; // Update upload progress
														// 4 times a second

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	private Dialog dialog;
	private int totalFiles;

	private long _lastUpdate = (new Date()).getTime();

	@UiField
	Label fileCountText, fileProgressText1, fileProgressText2;
	@UiField
	ProgressBar fileCountProgress, fileProgress;

	interface SpecificUiBinder extends UiBinder<Dialog, FileUploadProgressDialog> {
	}

	private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

	public FileUploadProgressDialog() {
		super();
		dialog = uiBinder.createAndBindUi(this);
		dialog.getActionButton().setVisible(false);
		dialog.hideTitleCloseButton();
		dialog.getCancelButton().setType(ButtonType.DANGER);
	}

	public void show(int fileCountIn) {

		totalFiles = fileCountIn;

		dialog.show();

		updateTotalFilesProgress(1);
		fileProgress.setPercent(0);
	}

	private void updateTotalFilesProgress(int nextItemIn) {
		fileCountText.setText(i18n.fileUploadProgressDialogUpdateMessage( nextItemIn, totalFiles)); //$NON-NLS-1$ //$NON-NLS-2$
		fileCountProgress.setPercent((nextItemIn - 1) * 100 / totalFiles);
	}

	public void hide() {
		dialog.hide();
	}

	public void setFileProgress(UploadFile fileIn, long currentBytesIn) {

		if (null != fileIn) {

			String mySourceName = fileIn.getSourceName();
			String myTargetName = fileIn.getTargetName();
			long mySize = fileIn.getSize();
//			long myTime = (new Date()).getTime();

			//
			// We must throttle the display update requests to avoid
			// overwhelming the system.
			//
//			if (_updateInterval < (myTime - _lastUpdate)) {

				int percent = (int) (currentBytesIn * 100 / mySize);
				if (percent == 100) {
					fileProgressText1.setText(i18n.fileUploadProgressDialogServerProcessMessage()); //$NON-NLS-1$
					fileProgressText2.setText(i18n.fileUploadProgressDialogServerDelimiter() + Format.value(myTargetName)); //$NON-NLS-1$
				} else {
					fileProgressText1.setText(i18n.fileUploadProgressDialogUploadingLabel() + Format.value(mySourceName)); //$NON-NLS-1$
					fileProgressText2.setText(i18n.fileUploadProgressDialogUploadingDelimiter() + Format.value(myTargetName)); //$NON-NLS-1$
				}
				fileProgress.setPercent(percent);

//				_lastUpdate = myTime;
//			}
		}
	}

	public void setLoadComplete(int nextItemIn) {
		updateTotalFilesProgress(nextItemIn);
		if (totalFiles >= nextItemIn) {
			fileProgress.setPercent(0);
		}
	}

	public HandlerRegistration addCancelHandler(final ClickHandler clickHandler) {
		return dialog.getCancelButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				WarningDialog warning = new WarningDialog(i18n.fileUploadProgressDialogCancelLabel(), //$NON-NLS-1$
						i18n.fileUploadProgressDialogCancelWarningMessage()); //$NON-NLS-1$
				warning.addClickHandler(clickHandler);
				warning.show();
			}
		});
	}

}
