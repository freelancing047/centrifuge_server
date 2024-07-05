package csi.client.gwt.viz.timeline.menu;
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

import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;
import csi.client.gwt.viz.timeline.settings.TimelineSettingsPresenter;
import csi.server.common.model.visualization.timeline.TimelineViewDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TimelineSettingsHandler extends AbstractTimelineMenuEventHandler implements
        SettingsActionCallback<TimelineViewDef> {

    public TimelineSettingsHandler(TimelinePresenter presenter, TimelineMenuManager menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        TimelineSettingsPresenter presenter = new TimelineSettingsPresenter(this);
        presenter.setDataView(getPresenter().getDataView());
        presenter.setVisualizationDef(getPresenter().getVisualizationDef());
        presenter.setVisualization(getPresenter());
        presenter.show();
    }

    @Override
    public void onSaveComplete(TimelineViewDef visualizationDef, boolean suppressLoadAfterSave) {
        if (!suppressLoadAfterSave) {
            
//            //FIXME: Have to defer here because something in view is not yet rendered properly, not sure what
//            Scheduler.get().scheduleDeferred(new ScheduledCommand(){
//
//                @Override
//                public void execute() {
//                    getPresenter().loadVisualization();
//                }});
        }
    }

    @Override
    public void onCancel() {
        // Noop
    }
}
