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
package csi.client.gwt.widget.ui.color;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.service.api.ColorActionsServiceProtocol;
import csi.server.common.service.api.ColorActionsServiceProtocol.RangeDirection;
import csi.shared.core.color.ContinuousColorModel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ContinuousColorPicker extends AbstractColorPicker {

    private int startX, startY, endX, endY;
    private boolean dragging;

    @UiField
    Image colorBox, selectedRange;
    @UiField(provided = true)
    Canvas canvas;

    interface SpecificUiBinder extends UiBinder<Dialog, ContinuousColorPicker> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public ContinuousColorPicker() {
        super();
        canvas = Canvas.createIfSupported();
        canvas.setCoordinateSpaceWidth(ContinuousColorModel.BOX_MODEL_WIDTH);
        canvas.setCoordinateSpaceHeight(ContinuousColorModel.BOX_MODEL_HEIGHT);

        init(uiBinder.createAndBindUi(this));
        colorBox.setResource(imageBundle.colorBox());
    }

    @Override
    public void show() {
        displaySelection(this.<ContinuousColorModel> getColorModel());
        super.show();
    }

    @UiHandler("canvas")
    public void handleMouseDown(MouseDownEvent event) {
        startX = event.getRelativeX(canvas.getElement());
        startY = event.getRelativeY(canvas.getElement());
        dragging = true;
    }

    @UiHandler("canvas")
    public void handleMouseMove(MouseMoveEvent event) {
        if (dragging) {
            endX = event.getRelativeX(canvas.getElement());
            endY = event.getRelativeY(canvas.getElement());
            displaySelectionLine();
        }
    }

    private void displaySelectionLine() {
        Context2d ctx = canvas.getContext2d();
        ctx.clearRect(0, 0, ContinuousColorModel.BOX_MODEL_WIDTH, ContinuousColorModel.BOX_MODEL_HEIGHT);
        ctx.beginPath();
        ctx.setStrokeStyle(CssColor.make(10, 10, 10));
        ctx.setLineWidth(1.0);
        ctx.moveTo(startX, startY);
        ctx.lineTo(endX, endY);
        ctx.stroke();
    }

    @UiHandler("canvas")
    public void handleMouseUp(MouseUpEvent event) {
        dragging = false;
        updateSelection();
    }

    @UiHandler("canvas")
    public void handleMouseOut(MouseOutEvent event) {
        dragging = false;
    }

    private void updateSelection() {
        ContinuousColorModel model = new ContinuousColorModel();
        model.setStartX(startX);
        model.setStartY(startY);
        model.setEndX(endX);
        model.setEndY(endY);
        setColorModel(model);

        displaySelection(model);
    }

    private void displaySelection(ContinuousColorModel model) {
        WebMain.injector.getVortex().execute(new Callback<String>() {

            @Override
            public void onSuccess(String result) {
                selectedRange.setUrl(result);
            }
        }, ColorActionsServiceProtocol.class)
                .getColorRangeSample(ContinuousColorModel.BOX_MODEL_WIDTH, 40, model, RangeDirection.HORIZONTAL);
    }
}
