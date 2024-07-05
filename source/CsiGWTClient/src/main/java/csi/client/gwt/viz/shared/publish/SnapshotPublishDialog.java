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
package csi.client.gwt.viz.shared.publish;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.client.gwt.widget.ui.TagInput;
import csi.server.common.service.api.PublishingActionsServiceProtocol;
import csi.shared.core.publish.SnapshotImagingResponse;
import csi.shared.core.publish.SnapshotPublishRequest;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SnapshotPublishDialog {

    private Dialog dialog;
    private SnapshotImagingResponse imagingResponse;
    private SnapshotPublishRequest publishRequest;

    @UiField
    Form form;
    @UiField
    FullSizeLayoutPanel mainContainer;
    @UiField
    FlowPanel imageContainer;
    @UiField
    ControlGroup cgName, cgDescription;
    @UiField
    TextBox fieldName;
    @UiField
    TextArea fieldDescription;
    @UiField
    TagInput fieldTags;

    interface SpecificUiBinder extends UiBinder<Dialog, SnapshotPublishDialog> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public SnapshotPublishDialog() {
        super();
        dialog = uiBinder.createAndBindUi(this);
        dialog.hideOnCancel();
        addHandlers();

        mainContainer.getWidgetContainerElement(form).getStyle().setOverflowY(Overflow.AUTO);
    }

    public void setImagingResponse(SnapshotImagingResponse imagingResponse) {
        this.imagingResponse = imagingResponse;
    }

    public void setPublishRequest(SnapshotPublishRequest publishRequest) {
        this.publishRequest = publishRequest;
    }

    private void addHandlers() {
        dialog.getActionButton().setText(CentrifugeConstantsLocator.get().snapshotPublishDialog_publish());
        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                publishCheck();
            }
        });
    }

    public void show() {
        for (String imageData : imagingResponse.getImageData()) {
            Image image = new Image();
            image.setWidth("245px");
            image.setHeight("245px");
            image.setUrl(imageData);
            imageContainer.add(image);
        }

        dialog.show();
    }

    protected void publishCheck() {
        boolean nameEmpty = Strings.isNullOrEmpty(fieldName.getValue());
        boolean descriptionEmpty = Strings.isNullOrEmpty(fieldDescription.getValue());
        cgName.setType(nameEmpty ? ControlGroupType.ERROR : ControlGroupType.NONE);
        cgDescription.setType(descriptionEmpty ? ControlGroupType.ERROR : ControlGroupType.NONE);
        if (!(nameEmpty || descriptionEmpty)) {
            publish();
        }
    }

    private void publish() {
        publishRequest.setName(fieldName.getValue());
        publishRequest.setDescription(fieldDescription.getValue());
        publishRequest.setTags(fieldTags.getTags());
        publishRequest.getImageData().addAll(imagingResponse.getImageData());

        WebMain.injector.getVortex().execute(new Callback<Void>() {

            @Override
            public void onSuccess(Void result) {
                dialog.hide();
            }
        }, PublishingActionsServiceProtocol.class).publish(publishRequest);
    }
}
