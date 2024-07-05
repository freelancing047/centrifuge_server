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
package csi.client.gwt.viz.timeline.settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import csi.client.gwt.viz.shared.settings.VisualizationSettings;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.timeline.TimelineCachedState;
import csi.server.common.model.visualization.timeline.TimelineLegendDefinition;
import csi.server.common.model.visualization.timeline.TimelineSettings;
import csi.server.common.model.visualization.timeline.TimelineTrackState;
import csi.server.common.model.visualization.timeline.TimelineViewDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TimelineGeneralTab extends TimelineSettingsComposite {

    @UiField
    TextBox name;

    @UiField
    FieldDefComboBox groupByField;
    
    @UiField
    FieldDefComboBox colorByField;

    @UiField
    FieldDefComboBox dotSizeField;
    
    
    FieldDefProperties props = GWT.create(FieldDefProperties.class);
    
    private TimelineSettingsPresenter presenter;

    interface SpecificUiBinder extends UiBinder<Widget, TimelineGeneralTab> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public TimelineGeneralTab() {
        super();
                
        initWidget(uiBinder.createAndBindUi(this));
        
    }

    @Override
    public void updateViewFromModel() {
    	TimelineViewDef timelineViewDef = getVisualizationSettings().getVisualizationDefinition();
    	name.setValue(timelineViewDef.getName());

        List<FieldDef> fieldDefs = getVisualizationSettings().getDataViewDefinition().getModelDef().getFieldDefs();

        
        //Apply existing settings	
        TimelineSettings settings = timelineViewDef.getTimelineSettings();
        if(settings != null){

        	FieldDef groupBy = settings.getGroupByField();
        	FieldDef colorBy = settings.getColorByField();
            FieldDef dotSize = settings.getDotSize();
            
            
            for(FieldDef field: fieldDefs){
                
                colorByField.getStore().add(field);
                groupByField.getStore().add(field);
                if(field.getValueType() == CsiDataType.Integer || field.getValueType() == CsiDataType.Number){
                    dotSizeField.getStore().add(field);
                }
            }

            if(dotSize != null){
                dotSizeField.setValue(dotSize);
            }
            
            if(groupBy != null){
                groupByField.setValue(groupBy);
            }
            
            if(colorBy != null){
                colorByField.setValue(colorBy);
            }
            
        }
    }
    
    @Override
    public void setVisualizationSettings(VisualizationSettings visualizationSettings) {
        super.setVisualizationSettings(visualizationSettings);
        createEditor();
    }
    
    @Override
    public void updateModelWithView() {
    	
        TimelineViewDef timelineViewDef = getVisualizationSettings().getVisualizationDefinition();
        timelineViewDef.setName(name.getValue().trim());
        
        TimelineSettings settings = timelineViewDef.getTimelineSettings();
        if(settings == null){
        	settings = new TimelineSettings();
        }

        TimelineCachedState cachedState = new TimelineCachedState();
        cachedState.setTrackStates(new HashSet<TimelineTrackState>());
        timelineViewDef.setState(cachedState);
        
        settings.setGroupByField(groupByField.getCurrentValue());
        settings.setColorByField(colorByField.getCurrentValue());
        settings.setDotSize(dotSizeField.getCurrentValue());
        
        List<TimelineLegendDefinition> legendItems = settings.getLegendItems();
        if(legendItems == null){
            legendItems = new ArrayList<TimelineLegendDefinition>();

            settings.setLegendItems(legendItems);
        }
    	
    }
    
    @UiHandler("clearGroup")
    public void clearGroup(ClickEvent e){
        groupByField.setValue(null);
    }
    
    @UiHandler("clearColor")
    public void clearColor(ClickEvent e){
        colorByField.setValue(null);
    }
    
    @UiHandler("clearDotSize")
    public void clearDotSize(ClickEvent e){
        dotSizeField.setValue(null);
    }
    
    
    private void createEditor() {
        

    }

    public TimelineSettingsPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(TimelineSettingsPresenter presenter) {
        this.presenter = presenter;
    }
    
    
    interface FieldDefProperties extends PropertyAccess<FieldDef> {
        ModelKeyProvider<FieldDef> uuid();

        LabelProvider<FieldDef> fieldName();
    }

}
