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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;

import csi.client.gwt.widget.boot.Dialog;
import csi.shared.core.color.ColorUtil;
import csi.shared.core.color.ColorUtil.HSL;
import csi.shared.core.color.SingleColorModel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SingleColorPicker extends AbstractColorPicker {

    private static final int BLOCK_WIDTH = 50;
    private static final int BLOCK_HEIGHT = 30;
    private static final int BLOCK_MARGIN = 10;

    private List<String> colorPalette = new ArrayList<String>();

    private String selectedColor;

    @UiField
    AbsolutePanel colorContainer;
    @UiField
    FlowPanel selectedColorDiv;

    interface SpecificUiBinder extends UiBinder<Dialog, SingleColorPicker> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public SingleColorPicker() {
        super();
        for (int i = 0; i < 16; i++) {
            HSL color = new HSL(i / (double)16, 1.0, 0.5);
            colorPalette.add(ColorUtil.toColorString(color));
        }
        for (int i = 0; i < 4; i++) {
            HSL color = new HSL(0.0, 0.0, i / (double)3);
            colorPalette.add(ColorUtil.toColorString(color));
        }
        init(uiBinder.createAndBindUi(this));
    }

    public List<String> getColorPalette() {
        return colorPalette;
    }

    public String getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(String selectedColor) {
        this.selectedColor = selectedColor;
    }

    @Override
    public void show() {
        setupColors();
        super.show();
    }

    private void setupColors() {
        // Initialize to current color.
        selectedColorDiv.getElement().getStyle().setBackgroundColor(this.<SingleColorModel> getColorModel().getColor());

        int row = 0;
        int col = 0;
        int count = 0;
        for (String color : getColorPalette()) {
            row = count / 5;
            col = count % 5;
            count++;

            FlowPanel block = new FlowPanel();
            block.addStyleName("colorPickerColorBlock"); //$NON-NLS-1$
            block.setWidth(BLOCK_WIDTH + "px"); //$NON-NLS-1$
            block.setHeight(BLOCK_HEIGHT + "px"); //$NON-NLS-1$
            final String c = color.startsWith("#") ? color : "#" + color; //$NON-NLS-1$ //$NON-NLS-2$
            block.getElement().getStyle().setBackgroundColor(c);
            colorContainer.add(block, col * (BLOCK_WIDTH + BLOCK_MARGIN), row * (BLOCK_HEIGHT + BLOCK_MARGIN));
            block.addDomHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    selectedColorDiv.getElement().getStyle().setBackgroundColor(c);
                    // Use a new color model so that we don't modify the user supplied instance.
                    SingleColorModel model = new SingleColorModel();
                    model.setColor(c);
                    SingleColorPicker.this.setColorModel(model);
                }
            }, ClickEvent.getType());

        }
    }
}
