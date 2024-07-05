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
package csi.client.gwt.viz.shared.settings;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ModalFooter;
import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.widget.boot.SizeProvidingModal;
import csi.client.gwt.widget.misc.FormChangeManager;
import csi.client.gwt.widget.misc.ModelAwareView;
import csi.client.gwt.widget.misc.WidgetWalker;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;

/**
 * The base widget for visualizations.
 * @author Centrifuge Systems, Inc.
 *
 */
public class VisualizationSettingsModal extends SizeProvidingModal implements ModelAwareView, VisualizationSettings {

    private EventBus eventBus = new SimpleEventBus();
    private VisualizationDef visualizationDef;
    private DataViewDef dataViewDef;
    private FormChangeManager formChangeManager = new FormChangeManager();

    @UiField
    CheckBox suppressLoadAtStartup;
    @UiField
    CheckBox suppressLoadAfterSave;
    @UiField
    CheckBox hideOverviewCheckBox;
    @UiField
    Button buttonSave;
    @UiField
    Button buttonCancel;
    @UiField
    DivWidget leftControlContainer;

    interface SpecificUiBinder extends UiBinder<ModalFooter, VisualizationSettingsModal> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    @UiConstructor
    public VisualizationSettingsModal(String title, VisualizationDef visualizationDef, DataViewDef dataViewDef) {
        super();
        this.visualizationDef = visualizationDef;
        this.dataViewDef = dataViewDef;
        this.add(uiBinder.createAndBindUi(this));
        this.setTitle(title);
        this.setHideOthers(false);
    }

    @Override
    public DataViewDef getDataViewDefinition() {
        return dataViewDef;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends VisualizationDef> T getVisualizationDefinition() {
        return (T) visualizationDef;
    }

    @Override
    public void show() {
        // Propagate the data view and visualization definitions to child widgets.
        WidgetWalker walker = new WidgetWalker() {

            @Override
            public void actOn(Widget widget) {
                if (widget instanceof VisualizationSettingsAware) {
                    VisualizationSettingsAware vsa = (VisualizationSettingsAware) widget;
                    vsa.setVisualizationSettings(VisualizationSettingsModal.this);
                }
            }
        };
        walker.startingAt(this).walk();
        super.show();
        enable();
        if (visualizationDef.getType() != VisualizationType.DRILL_CHART) {
            hideOverviewCheckBox.setVisible(false);
        }

    }

    @Override
    public void updateViewFromModel() {
        WidgetWalker walker = new WidgetWalker() {

            @Override
            public void actOn(Widget widget) {
                if (widget instanceof ModelAwareView) {
                    ((ModelAwareView) widget).updateViewFromModel();
                }
            }
        };
        walker.startingAt(this).walk();
        suppressLoadAtStartup.setValue(visualizationDef.isSuppressLoadAtStartup());
        hideOverviewCheckBox.setValue(visualizationDef.getHideOverview());
        formChangeManager.bind(this);
    }

    @Override
    public void updateModelWithView() {
        WidgetWalker walker = new WidgetWalker() {

            @Override
            public void actOn(Widget widget) {
                if (widget instanceof ModelAwareView) {
                    ((ModelAwareView) widget).updateModelWithView();
                }
            }
        };
        walker.startingAt(this).walk();
    	visualizationDef.setSuppressLoadAtStartup(suppressLoadAtStartup.getValue());
    	visualizationDef.setHideOverview(hideOverviewCheckBox.getValue());
    }

    @UiHandler("buttonSave")
    public void saveClicked(ClickEvent event) {
        eventBus.fireEventFromSource(event, event.getSource());
    }

    @UiHandler("buttonCancel")
    public void cancelClicked(ClickEvent event) {
        eventBus.fireEventFromSource(event, event.getSource());
    }

    public HandlerRegistration registerSaveClickHandler(ClickHandler clickHandler) {
        return eventBus.addHandlerToSource(ClickEvent.getType(), getButtonSave(), clickHandler);
    }

    public HandlerRegistration registerCancelClickHandler(ClickHandler clickHandler) {
        return eventBus.addHandlerToSource(ClickEvent.getType(), buttonCancel, clickHandler);
    }

    public Boolean isSuppressLoadOnSave() {
        return suppressLoadAfterSave.getValue();
    }

    @UiChild(tagname = "leftControl")
    public void addLeftControl(Widget widget) {
        leftControlContainer.add(widget);
    }

    public void enable() {
        if(getButtonSave() != null)
            getButtonSave().setEnabled(true);
    }

    public void disable() {
        if(getButtonSave() != null)
            getButtonSave().setEnabled(false);
    }

	public Button getButtonSave() {
		return buttonSave;
	}

}
