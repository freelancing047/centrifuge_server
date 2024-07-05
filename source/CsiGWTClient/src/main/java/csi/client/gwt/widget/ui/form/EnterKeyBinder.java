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
package csi.client.gwt.widget.ui.form;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.sencha.gxt.widget.core.client.form.Field;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class EnterKeyBinder {

    private HasKeyPressHandlers widget;

    public static EnterKeyBinder bind(HasKeyPressHandlers widget) {
        EnterKeyBinder binder = new EnterKeyBinder();
        binder.widget = widget;
        return binder;
    }

    public void to(final HasClickHandlers widgetToClick) {
        this.widget.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    NativeEvent clickEvent = Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false);
                    DomEvent.fireNativeEvent(clickEvent, widgetToClick);
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        @SuppressWarnings("rawtypes")
                        @Override
                        public void execute() {
                            if (widget instanceof Field) {
                                ((Field) widget).focus();
                            } else {
                                NativeEvent clickEvent = Document.get().createClickEvent(0, 0, 0, 0, 0, false, false,
                                        false, false);
                                DomEvent.fireNativeEvent(clickEvent, widget);
                            }
                        }
                    });
                }
            }
        });
    }
}
