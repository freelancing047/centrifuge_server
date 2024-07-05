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

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.form.DualListField.Mode;
import com.sencha.gxt.widget.core.client.form.validator.EmptyValidator;

import csi.client.gwt.viz.shared.CsiDualListField;
import csi.client.gwt.viz.shared.settings.VisualizationSettings;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.timeline.TimelineCachedState;
import csi.server.common.model.visualization.timeline.TimelineField;
import csi.server.common.model.visualization.timeline.TimelineSettings;
import csi.server.common.model.visualization.timeline.TimelineTrackState;
import csi.server.common.model.visualization.timeline.TimelineViewDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TimelineTooltipsTab extends TimelineSettingsComposite {

    @UiField(provided = true)
    CsiDualListField<FieldDef, FieldDef> dualListField;
    
    private final ListStore<FieldDef> availColumns;
    private final ListStore<FieldDef> selectedColumns;
    FieldDefProperties props = GWT.create(FieldDefProperties.class);
    
    private TimelineSettingsPresenter presenter;

    interface SpecificUiBinder extends UiBinder<Widget, TimelineTooltipsTab> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public TimelineTooltipsTab() {
        super();
        availColumns = new ListStore<FieldDef>(props.uuid());
        selectedColumns = new ListStore<FieldDef>(props.uuid());
        
        dualListField = new CsiDualListField<FieldDef, FieldDef>(availColumns, selectedColumns, new IdentityValueProvider<FieldDef>(), new FieldDefNameCell());

        dualListField.addValidator(new EmptyValidator<List<FieldDef>>());
        dualListField.setEnableDnd(true);
        dualListField.setMode(Mode.INSERT);

        dualListField.setWidth("100%");
        ListView<FieldDef, FieldDef> fromView = dualListField.getFromView();
        ListView<FieldDef, FieldDef> toView = dualListField.getToView();

        fromView.setWidth(215);
        toView.setWidth(215);

        dualListField.setHeight(205);
        
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void updateViewFromModel() {
    	TimelineViewDef timelineViewDef = getVisualizationSettings().getVisualizationDefinition();

        List<FieldDef> fieldDefs = getVisualizationSettings().getDataViewDefinition().getModelDef().getFieldDefs();

        
        
        //Apply existing settings	
        TimelineSettings settings = timelineViewDef.getTimelineSettings();
        if(settings != null){
        	
        	if(settings.getFieldList() != null){
        		for(TimelineField timelineField: settings.getFieldList()){
        			selectedColumns.add(timelineField.getFieldDef());
        		}
        	}
        	
        	for(FieldDef field: fieldDefs){
        		if(!selectedColumns.getAll().contains(field)){
        			availColumns.add(field);
        		}
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
        TimelineSettings settings = timelineViewDef.getTimelineSettings();
        if(settings == null){
        	settings = new TimelineSettings();
        }
        
        if(timelineViewDef.getState() == null){
            TimelineCachedState cachedState = new TimelineCachedState();
            cachedState.setTrackStates(new HashSet<TimelineTrackState>());
            timelineViewDef.setState(cachedState);
        }

        List<TimelineField> selectedFields = new ArrayList<TimelineField>();
        for(FieldDef fieldDef: selectedColumns.getAll()){
        	TimelineField field = new TimelineField();
        	field.setFieldDef(fieldDef);
        	selectedFields.add(field);
        }
        
        settings.setFieldList(selectedFields);
        
    	
    	
    }
    
    
    private void createEditor() {
        
    }

    public TimelineSettingsPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(TimelineSettingsPresenter presenter) {
        this.presenter = presenter;
    }
    
    public void addAvailableField(FieldDef fieldDef) {
        availColumns.add(fieldDef);
    }

    public void addSelectedField(FieldDef fieldDef) {
        selectedColumns.add(fieldDef);
    }
    
    interface FieldDefProperties extends PropertyAccess<FieldDef> {
        ModelKeyProvider<FieldDef> uuid();

        LabelProvider<FieldDef> fieldName();
    }

}
