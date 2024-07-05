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

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.shared.core.color.ColorModel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractColorPicker {

    public interface ColorPickerModelSelection {

        public void onSelection(ColorModel model);
    }

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    private Dialog dialog;
    private ColorModel colorModel;
    private ColorPickerModelSelection colorPickerModelSelection;

    interface ImageBundle extends ClientBundle {

        ImageResource colorBox();
    }

    protected static ImageBundle imageBundle = GWT.create(ImageBundle.class);

    protected void init(Dialog dialogInstance) {
        this.dialog = dialogInstance;
        dialog.hideOnCancel();
        dialog.getActionButton().setText(i18n.abstractColorPickerSelectLabel()); //$NON-NLS-1$
        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                dialog.hide();
                getColorPickerModelSelection().onSelection(getColorModel());
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected <T extends ColorModel> T getColorModel() {
        return (T) colorModel;
    }

    public void setColorModel(ColorModel colorModel) {
        this.colorModel = colorModel;
    }

    protected ColorPickerModelSelection getColorPickerModelSelection() {
        return colorPickerModelSelection;
    }

    public void setColorPickerModelSelection(ColorPickerModelSelection colorPickerModelSelection) {
        this.colorPickerModelSelection = colorPickerModelSelection;
    }

    /**
     * Shows the color picker dialog
     */
    public void show() {
        dialog.show();
    }

}
