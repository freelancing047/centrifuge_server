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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.validation.feedback.StringValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.NotBlankValidator;
import csi.client.gwt.validation.validator.Validator;
import csi.client.gwt.validation.validator.VisualizationUniqueNameValidator;
import csi.client.gwt.viz.shared.settings.AbstractSettingsPresenter;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.client.gwt.viz.shared.settings.VisualizationSettingsModal;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.timeline.TimelineCachedState;
import csi.server.common.model.visualization.timeline.TimelineEventDefinition;
import csi.server.common.model.visualization.timeline.TimelineSettings;
import csi.server.common.model.visualization.timeline.TimelineTimeSetting;
import csi.server.common.model.visualization.timeline.TimelineTrackState;
import csi.server.common.model.visualization.timeline.TimelineViewDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TimelineSettingsPresenter extends AbstractSettingsPresenter<TimelineViewDef> {


    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    @UiField
    TimelineGeneralTab generalTab;

    @UiField
    TimelineEventsTab eventsTab;
    
    @UiField
    TimelineTooltipsTab tooltipsTab;
//    
//    @UiField
//    TimelineAdvancedTab advancedTab;
    

    private EventDialog eventDialog = null;
    
    @UiTemplate("TimelineSettingsView.ui.xml")
    interface SpecificUiBinder extends UiBinder<VisualizationSettingsModal, TimelineSettingsPresenter> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public TimelineSettingsPresenter(SettingsActionCallback<TimelineViewDef> settingsActionCallback) {
        super(settingsActionCallback);
    }

    @Override
    protected void bindUI() {
        uiBinder.createAndBindUi(this);
//        tabSort.setCategoriesTab(tabCategories);
//        tabSort.setMeasuresTab(tabMeasure);
        generalTab.setPresenter(this);
        eventsTab.setPresenter(this);
        
    }

    @Override
    public void show() {
        // Important: This needs to happen before the ui-bind so that visualizationModal picks up a non-null
        // visualization setting.
        if (getVisualizationDef() == null) {
            // Create scenario.
            TimelineViewDef def = createNewVisualizationDef();
            setVisualizationDef(def);
            createMode = true;
        }

        bindUI();

        setupHandlers();
        initiateValidator();

        vizSettings.show();
        vizSettings.updateViewFromModel();
        
    }
    
    public void resetViewport() {
        if(getVisualizationDef().getState() == null){
            TimelineCachedState cachedState = new TimelineCachedState();
            cachedState.setTrackStates(new HashSet<TimelineTrackState>());
            getVisualizationDef().setState(cachedState);
        }
        getVisualizationDef().getState().setScrollPosition(0);
        getVisualizationDef().getState().setStartPosition(Long.MIN_VALUE);
        getVisualizationDef().getState().setEndPosition(Long.MAX_VALUE);
        
    }

    @Override
    protected TimelineViewDef createNewVisualizationDef() {
    	TimelineViewDef timelineViewDef = new TimelineViewDef();
        timelineViewDef.setBroadcastListener(WebMain.getClientStartupInfo().isListeningByDefault());

        TimelineSettings settings = timelineViewDef.getTimelineSettings();
        if(settings == null){
            settings = new TimelineSettings();
            timelineViewDef.setTimelineSettings(settings);
        }
        
        if(timelineViewDef.getState() == null){
            TimelineCachedState cachedState = new TimelineCachedState();
            cachedState.setTrackStates(new HashSet<TimelineTrackState>());
            timelineViewDef.setState(cachedState);
        }

        String name = UniqueNameUtil.getDistinctName(UniqueNameUtil.getVisualizationNames(dataViewPresenter), i18n.timelineTitle()); //$NON-NLS-1$
        timelineViewDef.setName(name);
        return timelineViewDef;
    }

    @Override
    protected void saveVisualizationToServer() {
        resetViewport();

        ((TimelinePresenter)getVisualization()).resetSort();
        VortexFuture<Void> future = getVisualization().saveSettings(true);
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {
                getVisualizationDef().getSelection().clearSelection();
                vizSettings.hide();
                settingsActionCallback.onSaveComplete(getVisualizationDef(), vizSettings.isSuppressLoadOnSave());
            }
        });
    }

    @Override
    protected void initiateValidator() {
        NotBlankValidator notBlankValidator = new NotBlankValidator(generalTab.name);
        VisualizationUniqueNameValidator visualizationUniqueNameValidator = new VisualizationUniqueNameValidator(
                getDataViewDef().getModelDef().getVisualizations(), generalTab.name, getVisualizationDef().getUuid());
        
      //Determines at least one field is a Date
        Validator eventValidator = new Validator() {
            @Override
            public boolean isValid() {
                
                List<TimelineEventDefinition> events = getVisualizationDef().getTimelineSettings().getEvents();
                
                return events != null && events.size() > 0;
            }
        };

        ValidationFeedback eventsFeedback = new StringValidationFeedback(i18n.timelineValidationEvents()); //$NON-NLS-1$
        
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(eventValidator, eventsFeedback));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(notBlankValidator, StringValidationFeedback.getEmptyVisualizationFeedback()));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(visualizationUniqueNameValidator, StringValidationFeedback.getDuplicateVisualizationFeedback()));
    }

    public void newDefinition() {
        if(eventDialog == null){
            eventDialog = new EventDialog(this);
        }
        eventDialog.show();
    }

    public void cancelEventDefinition() {
        eventDialog.hide();
    }

    public void saveEventDefinition(FieldDef start, FieldDef end, FieldDef label) {
        
        TimelineEventDefinition event = new TimelineEventDefinition();
        
        saveEventDefinition(start, end, label, event);
    }

    public void saveEventDefinition(FieldDef start, FieldDef end, FieldDef label, TimelineEventDefinition event) {
        TimelineTimeSetting startTime = null;
        
        if(start != null){
            startTime = new TimelineTimeSetting();
            startTime.setFieldDef(start);
        }
        
        TimelineTimeSetting endTime = null;
        if(end != null){
            endTime = new TimelineTimeSetting();
            endTime.setFieldDef(end);
        }
        
        event.setStartField(startTime);
        event.setEndField(endTime);
        event.setLabelField(label);
        
        List<TimelineEventDefinition> events = getVisualizationDef().getTimelineSettings().getEvents();
        if(events == null || events.size() == 0){
            events = new ArrayList<TimelineEventDefinition>();
            getVisualizationDef().getTimelineSettings().setEvents(events);
        }
        if(!events.contains(event)){
            events.add(event);
        }
        eventsTab.updateViewFromModel();
        eventDialog.hide();
    }

    public void deleteEvent(TimelineEventDefinition eventDefinition) {
        List<TimelineEventDefinition> events = getVisualizationDef().getTimelineSettings().getEvents();
        events.remove(eventDefinition);
        eventsTab.updateViewFromModel();
    }

    public void editEvent(TimelineEventDefinition eventDefinition) {
        if(eventDialog == null){
            eventDialog = new EventDialog(this);
        }
        List<TimelineEventDefinition> events = getVisualizationDef().getTimelineSettings().getEvents();
        if(events == null){
            events = new ArrayList<TimelineEventDefinition>();
        }
        
        getVisualizationDef().getTimelineSettings().setEvents(events);
        eventDialog.setSelection(eventDefinition);
        eventDialog.show();
    }
    
}
