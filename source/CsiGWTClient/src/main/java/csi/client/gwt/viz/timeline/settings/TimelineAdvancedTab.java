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

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.viz.shared.settings.VisualizationSettings;
import csi.server.common.model.visualization.timeline.TimelineViewDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TimelineAdvancedTab extends TimelineSettingsComposite {

    private TimelineSettingsPresenter presenter;

    @UiField
    CheckBox summaryCheckbox;
    @UiField
    CheckBox groupSpaceCheckbox;
    @UiField
    RadioButton sortAscending;
    @UiField
    RadioButton sortDescending;

    interface SpecificUiBinder extends UiBinder<Widget, TimelineAdvancedTab> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public TimelineAdvancedTab() {
        super();
        
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void updateViewFromModel() {
    	TimelineViewDef timelineViewDef = getVisualizationSettings().getVisualizationDefinition();
    	if(timelineViewDef.getTimelineSettings() != null){
    	    
    	    if(timelineViewDef.getTimelineSettings().getShowSummary() == null){
    	        timelineViewDef.getTimelineSettings().setShowSummary(true);
            }
            
            if(timelineViewDef.getTimelineSettings().getGroupNameSpace() == null){
                timelineViewDef.getTimelineSettings().setGroupNameSpace(true);
            }
            
            summaryCheckbox.setValue(timelineViewDef.getTimelineSettings().getShowSummary());
            groupSpaceCheckbox.setValue(timelineViewDef.getTimelineSettings().getGroupNameSpace());
            
            if(timelineViewDef.getTimelineSettings().getSortAscending() == null || timelineViewDef.getTimelineSettings().getSortAscending()){
                sortAscending.setValue(true, true);
                sortDescending.setValue(false, true);
            } else {
                sortDescending.setValue(true, true);
                sortAscending.setValue(false, true);
            }
    	}

    }
    
    @Override
    public void setVisualizationSettings(VisualizationSettings visualizationSettings) {
        super.setVisualizationSettings(visualizationSettings);
    }
    
    @Override
    public void updateModelWithView() {
    	
        TimelineViewDef timelineViewDef = getVisualizationSettings().getVisualizationDefinition();
        if(timelineViewDef.getTimelineSettings() != null){
            timelineViewDef.getTimelineSettings().setShowSummary(summaryCheckbox.getValue());
            timelineViewDef.getTimelineSettings().setGroupNameSpace(groupSpaceCheckbox.getValue());
        }
        
        if(sortAscending.getValue()){
            timelineViewDef.getTimelineSettings().setSortAscending(true);
        } else {
            timelineViewDef.getTimelineSettings().setSortAscending(false);
        }
    	
    }
    
    public TimelineSettingsPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(TimelineSettingsPresenter presenter) {
        this.presenter = presenter;
    }
    

}
