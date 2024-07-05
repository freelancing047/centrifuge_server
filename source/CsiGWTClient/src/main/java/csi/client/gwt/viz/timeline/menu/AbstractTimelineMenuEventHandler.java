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
package csi.client.gwt.viz.timeline.menu;

import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.timeline.presenters.TimelinePresenter;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractTimelineMenuEventHandler extends
        AbstractMenuEventHandler<TimelinePresenter, TimelineMenuManager> {

    public AbstractTimelineMenuEventHandler(TimelinePresenter presenter, TimelineMenuManager menuManager) {
        super(presenter, menuManager);
    }

}
