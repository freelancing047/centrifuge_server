package csi.client.gwt.viz.timeline.menu; /**
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

import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TimelineSaveMenuHandler<V extends Visualization, M extends AbstractMenuManager<V>> extends AbstractMenuEventHandler<V, M> {

    public TimelineSaveMenuHandler(V presenter, M menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {

        ((TimelinePresenter)getPresenter()).save();
    }

}
