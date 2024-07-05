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
package csi.client.gwt.viz.matrix.settings;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.widget.ui.color.ColorPicker;
import csi.client.gwt.widget.ui.color.ColorPicker.ColorPickerCallback;
import csi.client.gwt.widget.ui.color.ColorPicker.ColorType;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.matrix.MatrixType;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.service.api.ColorActionsServiceProtocol;
import csi.server.common.service.api.ColorActionsServiceProtocol.RangeDirection;
import csi.shared.core.color.ColorModel;
import csi.shared.core.color.ContinuousColorModel;
import csi.shared.core.color.SingleColorModel;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColorsTab extends MatrixSettingsComposite {

    private ColorModel colorModel = new ContinuousColorModel();
    private ColorPicker colorPicker = new ColorPicker();
    private static String DEFAULT_BLUE = "#3498db";

    @UiField
    Button colorModelButton;
    @UiField
    Image colorDisplayImage;

    interface SpecificUiBinder extends UiBinder<Widget, ColorsTab> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public ColorsTab() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
        colorPicker.addColorType(ColorType.SINGLE);
        colorPicker.addColorType(ColorType.DISCRETE);
        colorPicker.addColorType(ColorType.CONTINUOUS);
        colorPicker.bind(colorModelButton);

        colorPicker.setColorPickerCallback(new ColorPickerCallback() {

            @Override
            public void beforeShow() {
                colorPicker.setColorModel(colorModel);
                updateDisplayImage();
            }

            @Override
            public void onSelection(ColorModel model) {
                ColorsTab.this.colorModel = model;
                MatrixViewDef viewDef = getVisualizationSettings().getVisualizationDefinition();
                MatrixSettings settings = viewDef.getMatrixSettings();
                settings.setColorModel(colorModel);
                updateDisplayImage();
            }
        });
    }

    protected void updateDisplayImage() {
        WebMain.injector.getVortex().execute(new Callback<String>() {

            @Override
            public void onSuccess(String result) {
                colorDisplayImage.setUrl(result);
            }
        }, ColorActionsServiceProtocol.class).getColorRangeSample(300, 30, colorModel, RangeDirection.HORIZONTAL);
    }

    @Override
    public void updateViewFromModel() {
        MatrixViewDef viewDef = getVisualizationSettings().getVisualizationDefinition();
        MatrixSettings settings = viewDef.getMatrixSettings();
        if(settings.getColorModel() == null){
            if(MatrixType.HEAT_MAP == settings.getMatrixType()){
                colorModel = new ContinuousColorModel();
            } else {
                colorModel = new SingleColorModel(DEFAULT_BLUE);
            }
            
        } else {
            colorModel = settings.getColorModel();
        }
        updateDisplayImage();
    }

    @Override
    public void updateModelWithView() {
        MatrixViewDef viewDef = getVisualizationSettings().getVisualizationDefinition();
        MatrixSettings settings = viewDef.getMatrixSettings();
        //settings.setColorModel(colorModel);
    }

    
    public ColorModel getColorModel() {
        return colorModel;
    }
    
}
