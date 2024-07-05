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
package csi.client.gwt.etc;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.Event;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class HierarchicalEventBus extends SimpleEventBus {

    private EventBus parent;

    public HierarchicalEventBus(EventBus parent) {
        super();
        this.parent = parent;
    };

    @Override
    public void fireEvent(Event<?> event) {
        super.fireEvent(event);
        parent.fireEvent(event);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        super.fireEvent(event);
        parent.fireEvent(event);
    }

    @Override
    public void fireEventFromSource(Event<?> event, Object source) {
        super.fireEventFromSource(event, source);
        parent.fireEventFromSource(event, source);
    }

    @Override
    public void fireEventFromSource(GwtEvent<?> event, Object source) {
        super.fireEventFromSource(event, source);
        parent.fireEventFromSource(event, source);
    }
}
