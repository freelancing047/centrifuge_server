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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

/**
 * Allows various 
 * @author Centrifuge Systems, Inc.
 *
 */
public class FormChangeManager {

    private List<HandlerRegistration> registrations = new ArrayList<HandlerRegistration>();
    private List<HasEnabled> enabledOnChange = new ArrayList<HasEnabled>();
    private boolean formChanged = false;

    public void bind(Widget w) {
        WidgetWalker walker = new WidgetWalker() {

            @Override
            public void actOn(Widget widget) {
                if (widget instanceof HasChangeHandlers) {
                    bind((HasChangeHandlers) widget);
                }
            }
        };
        walker.startingAt(w).includingFirst().walk();
    }

    private void bind(HasChangeHandlers w) {
        HandlerRegistration registration = w.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                enable();
            }
        });
        registrations.add(registration);
    }

    protected void enable() {
        for (HasEnabled enable : enabledOnChange) {
            enable.setEnabled(true);
        }
    }

    public void unbind() {
        for (HandlerRegistration registration : registrations) {
            registration.removeHandler();
        }
    }

    public void enableOnChange(HasEnabled widget, HasEnabled... others) {
        enabledOnChange.add(widget);
        if (others != null && others.length > 0) {
            for (HasEnabled hasEnabled : others) {
                enabledOnChange.add(hasEnabled);
            }
        }
    }

    public boolean isFormChanged() {
        return formChanged;
    }

}
