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
package csi.client.gwt.widget.cells.context_menu;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.XTemplates;

import csi.client.gwt.events.GridClickEvent;
import csi.client.gwt.events.GridClickEventHandler;
import csi.client.gwt.widget.DataCopyCallback;
import csi.client.gwt.widget.DataCopyMenu;
import csi.client.gwt.widget.boot.Dialog;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DataCopyCell<T> extends AbstractCell<T> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface LabelTemplate extends XTemplates {

        @XTemplate("<span title=\"{labelIn}\">&nbsp;&nbsp;{labelIn}</span>")
        SafeHtml display(String labelIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final LabelTemplate _labelTemplate = GWT.create(LabelTemplate.class);

    private DataCopyCallback<T> _callback = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DataCopyCell(DataCopyCallback callbackIn) {

        super(BrowserEvents.CLICK, BrowserEvents.CONTEXTMENU);

        _callback = callbackIn;
    }

    public void setCallback(DataCopyCallback callbackIn) {

        _callback = callbackIn;
    }

    @Override
    public void render(Context contextIn, T itemIn, SafeHtmlBuilder bufferIn) {

        bufferIn.append(_labelTemplate.display(itemIn.toString()));
    }

    @Override
    public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context contextIn, Element parentIn, T valueIn,
                               NativeEvent eventIn, ValueUpdater<T> valueUpdaterIn) {
        try {

            String eventType = eventIn.getType();
            if ((null != _callback) && BrowserEvents.CONTEXTMENU.equals(eventType)) {

                (new DataCopyMenu<T>(_callback, contextIn.getIndex(), valueIn)).showAt(eventIn.getClientX(),
                        eventIn.getClientY());
            }

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }
}
