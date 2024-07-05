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

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.widget.core.client.menu.ColorMenu;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ColorPickerCell extends SelectionHandlingCell<String> {

    private ColorMenu colorMenu = new ColorMenu();
    private boolean includeHashMark = true;
    private ValueUpdater<String> valueUpdater;

    interface CellTemplate extends SafeHtmlTemplates {

        @Template("<div style=\"{0};padding-top: 2px; text-align:center;\"><i class='icon-tint' style='font-size: 1.5em;'/></div>")
        SafeHtml template(SafeStyles style);

    }

    private static final CellTemplate cellTemplate = GWT.create(CellTemplate.class);

    public ColorPickerCell() {
        super(BrowserEvents.CLICK);
        colorMenu.getPalette().addSelectionHandler(new SelectionHandler<String>() {

            @Override
            public void onSelection(SelectionEvent<String> event) {
                String color = isIncludeHashMark() ? colorWithHash(event.getSelectedItem()) : colorWithoutHash(event
                        .getSelectedItem());
                valueUpdater.update(color);
                colorMenu.hide();
            }
        });
    }

    public boolean isIncludeHashMark() {
        return includeHashMark;
    }

    public void setIncludeHashMark(boolean includeHashMark) {
        this.includeHashMark = includeHashMark;
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
        sb.append(cellTemplate.template(SafeStylesUtils.forTrustedColor(colorWithHash(value))));
    }

    @Override
    public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, String value,
            NativeEvent event, ValueUpdater<String> valueUpdater) {
        if (BrowserEvents.CLICK.equals(event.getType())) {
            this.valueUpdater = valueUpdater;
            colorMenu.setColor(colorWithoutHash(value));
            colorMenu.show(parent, new AnchorAlignment(Anchor.TOP_LEFT, Anchor.BOTTOM_RIGHT, true));
        } else {
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
        }
    }

    private String colorWithHash(String color) {
        return color.startsWith("#") ? color : "#" + color; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String colorWithoutHash(String color) {
        return color.startsWith("#") ? color.substring(1) : color; //$NON-NLS-1$
    }

}
