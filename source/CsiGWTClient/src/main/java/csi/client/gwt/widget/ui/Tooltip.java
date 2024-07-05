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
package csi.client.gwt.widget.ui;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sencha.gxt.core.client.dom.XDOM;

import csi.shared.core.Constants;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class Tooltip extends AbsolutePanel {

    private static final int OFF_SCREEN_OFFSET = -1000;
    private static final int TOOLTIP_OFFSET = 15;
    private FlowPanel tooltipDiv;
    private Icon icon;

    /**
     * Causes a tooltip to be displayed at a location related to the "highlight" point. The highlight point is the 
     * point of interest for which the tooltip needs to be displayed.
     * @param innerHTML The tooltip contents to display
     * @param x Location within container of "highlight" point.
     * @param y Location within container of "highlight" point.
     */
    public void display(SafeHtml innerHTML, final int x, final int y) {
        tooltipDiv = new FlowPanel();
        tooltipDiv.addStyleName(Constants.UIConstants.Styles.CSI_TOOLTIP_CLASS);
        tooltipDiv.getElement().setInnerSafeHtml(innerHTML);
        tooltipDiv.getElement().getStyle().setDisplay(Display.INLINE);
        tooltipDiv.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
        add(tooltipDiv, OFF_SCREEN_OFFSET, OFF_SCREEN_OFFSET);

        // Use a deferred command since tooltipDiv's offset is not available until browser renders.
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                // The null check here is required because this is a deferred command. A fast mouse over & out can
                // cause hide to be called before this executes.
                if (tooltipDiv != null) {
                    // Prefer top-left. Drop to bottom or right if overflow.
                    int tooltipX = x - TOOLTIP_OFFSET - tooltipDiv.getOffsetWidth();
                    if (tooltipX < 0) {
                        tooltipX = x + TOOLTIP_OFFSET;
                    }
                    int tooltipY = y - TOOLTIP_OFFSET - tooltipDiv.getOffsetHeight();
                    if (tooltipY < 0) {
                        tooltipY = y + TOOLTIP_OFFSET;
                    }
                    
                    icon = new Icon(IconType.REMOVE);


                    icon.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
                    icon.addDomHandler(new ClickHandler(){

						@Override
						public void onClick(ClickEvent event) {
							Tooltip.this.hide();
						}}, ClickEvent.getType());
                    
                    Tooltip.this.add(tooltipDiv, tooltipX, tooltipY);
                    Tooltip.this.add(icon, tooltipX + tooltipDiv.getOffsetWidth() - 12, tooltipY + 2);
                    //Tooltip.this.add
                }
            }
        });
    }

    public void hide() {
        if (tooltipDiv != null) {
        	tooltipDiv.setVisible(false);
        	tooltipDiv.setHeight("0px"); //$NON-NLS-1$
        	tooltipDiv.setWidth("0px"); //$NON-NLS-1$
        	
        	icon.setVisible(false);

        	icon.setHeight("0px"); //$NON-NLS-1$
        	icon.setWidth("0px"); //$NON-NLS-1$
            remove(tooltipDiv);
            remove(icon);
        }
        tooltipDiv = null;
    }
    

}
