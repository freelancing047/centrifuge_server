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
package csi.client.gwt.widget.misc;

import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class WidgetWalker {

    private Widget firstElement;
    private boolean includeFirst;
    
    /**
     * @param widget Widget to act on.
     */
    public abstract void actOn(Widget widget);

    public WidgetWalker startingAt(Widget w) {
        firstElement = w;
        return this;
    }

    public WidgetWalker includingFirst() {
        includeFirst = true;
        return this;
    }

    public void walk() {
        if (includeFirst) {
            actOn(firstElement);
        }
        walk(firstElement);
    }

    private void walk(Widget widget) {
        if (widget instanceof HasWidgets) {
            HasWidgets hw = (HasWidgets) widget;
            for (Widget w : hw) {
                actOn(w);
                walk(w);
            }
        } else if (widget instanceof HasOneWidget) {
            Widget w = ((HasOneWidget) widget).getWidget();
            actOn(w);
            walk(w);
        }
    }
}
