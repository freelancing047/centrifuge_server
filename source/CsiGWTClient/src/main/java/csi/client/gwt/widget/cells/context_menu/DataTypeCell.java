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
import com.google.gwt.safehtml.shared.SafeUri;
import com.sencha.gxt.core.client.XTemplates;

import csi.client.gwt.events.GridClickEvent;
import csi.client.gwt.events.GridClickEventHandler;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.widget.DataTypeCallback;
import csi.client.gwt.widget.DataTypeMenu;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.enumerations.CsiDataType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DataTypeCell extends AbstractCell<CsiDataType> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface IconTemplate extends XTemplates {

        @XTemplate("<span title=\"{labelIn}\"><img width=\"16\" height=\"15\" src=\"{uriIn}\" /></span>")
        SafeHtml display(SafeUri uriIn, String labelIn);
    }
    interface LabelTemplate extends XTemplates {

        @XTemplate("<span title=\"{labelIn}\">&nbsp;&nbsp;{labelIn}</span>")
        SafeHtml display(String labelIn);
    }
    interface IconLabelTemplate extends XTemplates {

        @XTemplate("<span title=\"{labelIn}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{uriIn}\" />&nbsp;&nbsp;{labelIn}&nbsp;&nbsp;</span>")
        SafeHtml display(SafeUri uriIn, String labelIn);
    }
    interface MenuIconTemplate extends XTemplates {

        @XTemplate("<span title=\"{labelIn}\"><img width=\"16\" height=\"15\" src=\"{uriIn}\" /><img width=\"16\" height=\"15\" src=\"{chevronIn}\" /></span>")
        SafeHtml display(SafeUri uriIn, String labelIn, SafeUri chevronIn);
    }
    interface MenuLabelTemplate extends XTemplates {

        @XTemplate("<span title=\"{labelIn}\">&nbsp;&nbsp;{labelIn}<img width=\"16\" height=\"15\" src=\"{chevronIn}\" /></span>")
        SafeHtml display(String labelIn, SafeUri chevronIn);
    }
    interface MenuIconLabelTemplate extends XTemplates {

        @XTemplate("<span title=\"{labelIn}\">&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{uriIn}\" />&nbsp;&nbsp;{labelIn}&nbsp;&nbsp;<img width=\"16\" height=\"15\" src=\"{chevronIn}\" /></span>")
        SafeHtml display(SafeUri uriIn, String labelIn, SafeUri chevronIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final IconTemplate _iconTemplate = GWT.create(IconTemplate.class);
    private static final LabelTemplate _labelTemplate = GWT.create(LabelTemplate.class);
    private static final IconLabelTemplate _iconLabelTemplate = GWT.create(IconLabelTemplate.class);
    private static final MenuIconTemplate _menuIconTemplate = GWT.create(MenuIconTemplate.class);
    private static final MenuLabelTemplate _menuLabelTemplate = GWT.create(MenuLabelTemplate.class);
    private static final MenuIconLabelTemplate _menuIconLabelTemplate = GWT.create(MenuIconLabelTemplate.class);

    private boolean _useImage = false;
    private boolean _useText = false;
    private DataTypeCallback _callback = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DataTypeCell(boolean useImageIn, boolean useTextIn, DataTypeCallback callbackIn) {

        super(BrowserEvents.CLICK, BrowserEvents.CONTEXTMENU);

        _useImage = useImageIn;
        _useText = useTextIn;
        _callback = callbackIn;
    }

    public DataTypeCell(boolean useImageIn, boolean useTextIn) {

        super(BrowserEvents.CLICK, BrowserEvents.CONTEXTMENU);

        _useImage = useImageIn;
        _useText = useTextIn;
    }

    public void setCallback(DataTypeCallback callbackIn) {

        _callback = callbackIn;
    }

    public SafeHtml genHtml(Context contextIn, CsiDataType itemIn) {

        SafeHtml myHtml = null;
        SafeUri myChevron = FieldDefUtils.getMenuChevron().getSafeUri();

        if (null != _callback) {

            if (_useImage) {

                SafeUri myUri = FieldDefUtils.getDataTypeImage(itemIn).getSafeUri();
                myHtml = _useText
                        ? _menuIconLabelTemplate.display(myUri, itemIn.getLabel(), myChevron)
                        : _menuIconTemplate.display(myUri, itemIn.getLabel(), myChevron);

            } else if (_useText) {

                myHtml = _menuLabelTemplate.display(itemIn.getLabel(), myChevron);
            }

        } else {

            if (_useImage) {

                SafeUri myUri = FieldDefUtils.getDataTypeImage(itemIn).getSafeUri();
                myHtml = _useText
                        ? _iconLabelTemplate.display(myUri, itemIn.getLabel())
                        : _iconTemplate.display(myUri, itemIn.getLabel());

            } else if (_useText) {

                myHtml = _labelTemplate.display(itemIn.getLabel());
            }
        }
        return myHtml;
    }

    @Override
    public void render(Context contextIn, CsiDataType itemIn, SafeHtmlBuilder bufferIn) {

        SafeHtml myHtml = genHtml(contextIn, itemIn);

        if (null != myHtml) {

            bufferIn.append(myHtml);
        }
    }

    @Override
    public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context contextIn, Element parentIn, CsiDataType valueIn,
                                NativeEvent eventIn, ValueUpdater<CsiDataType> valueUpdaterIn) {
        try {

            String eventType = eventIn.getType();
            if ((null != _callback) && BrowserEvents.CLICK.equals(eventType)) {

                (new DataTypeMenu(_callback, contextIn.getIndex())).showAt(eventIn.getClientX(),
                        eventIn.getClientY());

            } else if ((null != _callback) && BrowserEvents.CONTEXTMENU.equals(eventType)) {

                (new DataTypeMenu(_callback, contextIn.getIndex())).showAt(eventIn.getClientX(),
                                                                                    eventIn.getClientY());
            }

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }
}
