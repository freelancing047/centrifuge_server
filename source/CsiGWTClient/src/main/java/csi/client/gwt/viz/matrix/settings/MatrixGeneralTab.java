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

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.matrix.MatrixType;
import csi.server.common.model.visualization.matrix.MatrixViewDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class MatrixGeneralTab extends MatrixSettingsComposite {

    @UiField
    TextBox chartName;
    @UiField
    CheckBox labelField;
    @UiField
    RadioButton bubbleDisplay;
    @UiField
    RadioButton heatMapDisplay;
    
    private MatrixSettingsPresenter presenter;

    interface SpecificUiBinder extends UiBinder<Widget, MatrixGeneralTab> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public MatrixGeneralTab() {
        super();
        initWidget(uiBinder.createAndBindUi(this));

        List<String> list = new ArrayList<String>();
        MatrixTypeLabelProvider labelProvider = new MatrixTypeLabelProvider();
        for (MatrixType mtype : MatrixType.values()) {
            if(!mtype.getLabel().equals(MatrixType.CO_OCCURRENCE.getLabel()) && !mtype.getLabel().equals(MatrixType.CO_OCCURRENCE_DIR.getLabel())) {
                list.add(labelProvider.getLabel(mtype));
            }
        }

        Collections.sort(list);



        bubbleDisplay.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                MatrixViewDef matrixViewDef = getVisualizationSettings().getVisualizationDefinition();
                MatrixSettings settings = matrixViewDef.getMatrixSettings();
                settings.setBubbleDisplay(true);
                settings.setMatrixType(MatrixType.BUBBLE);
                presenter.updateColor();
            }
        });

        heatMapDisplay.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                MatrixViewDef matrixViewDef = getVisualizationSettings().getVisualizationDefinition();
                MatrixSettings settings = matrixViewDef.getMatrixSettings();
                settings.setBubbleDisplay(false);
                settings.setMatrixType(MatrixType.HEAT_MAP);
                presenter.updateColor();
            }
        });
    }

    @Override
    public void updateViewFromModel() {
        chartName.setValue(getVisualizationSettings().getVisualizationDefinition().getName());
        MatrixViewDef viewDef = getVisualizationSettings().getVisualizationDefinition();
        MatrixSettings settings = viewDef.getMatrixSettings();

        if(settings.getBubbleDisplay()) {
            bubbleDisplay.setValue(true, true);
            heatMapDisplay.setValue(false,true);
        } else {
            heatMapDisplay.setValue(true, true);
            bubbleDisplay.setValue(false,true);
        }
        labelField.setValue(settings.isShowLabel());
    }

    @Override
    public void updateModelWithView() {
        getVisualizationSettings().getVisualizationDefinition().setName(chartName.getValue().trim());
        MatrixViewDef viewDef = getVisualizationSettings().getVisualizationDefinition();
        MatrixSettings settings = viewDef.getMatrixSettings();
        if (bubbleDisplay.getValue())
            settings.setBubbleDisplay(true);
        else
            settings.setBubbleDisplay(false);
        settings.setShowLabel(labelField.getValue());
    }

    public MatrixSettingsPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(MatrixSettingsPresenter presenter) {
        this.presenter = presenter;
    }

}
