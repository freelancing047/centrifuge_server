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
package csi.client.gwt.worksheet.layout.window;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.XElement;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.worksheet.layout.window.appearance.VisualizationBarAppearance;
import csi.client.gwt.worksheet.layout.window.events.WindowCascadeEvent;
import csi.client.gwt.worksheet.layout.window.events.WindowTileEvent;
import csi.server.common.model.visualization.VisualizationType;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class VisualizationBarIconContainer extends FlowPanel {

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private String CASCADE_TOOLTIP = i18n.visualizationBarIconContainerCascadeTooltip(); //$NON-NLS-1$
	private String WINDOW_TOOLTIP = i18n.visualizationBarIconContainerEqualTooltip(); //$NON-NLS-1$
	private EventBus eventBus;
    private List<VisualizationBarIcon> icons = new ArrayList<VisualizationBarIcon>();
    private static final VisualizationBarAppearance appearance = GWT.create(VisualizationBarAppearance.class);

    private Button pin;
    private Image tile;
    private Image cascade;

    static {
        appearance.style().ensureInjected();
    }

    public VisualizationBarIconContainer() {
        super();

        pin = new Button();
        pin.setSize(ButtonSize.LARGE);
        pin.setToggle(true);
        pin.setIcon(IconType.PUSHPIN);
        pin.setType(ButtonType.LINK);
        pin.getElement().getStyle().setColor("#939393");
        pin.getElement().getStyle().setPaddingRight(2, Style.Unit.PX);
        pin.getElement().getStyle().setPaddingLeft(3, Style.Unit.PX);
        pin.getElement().getStyle().setPaddingBottom(10, Style.Unit.PX);
        pin.getElement().getStyle().setPaddingTop(0, Style.Unit.PX);
        pin.getElement().getStyle().setWidth(11, Style.Unit.PX);
        pin.getElement().getStyle().setProperty("userSelect", "none");
        add(pin);

        tile = new Image(appearance.windowTile());
        tile.addStyleName(appearance.style().iconStyle());
        tile.getElement().getStyle().setPaddingRight(2, Style.Unit.PX);
        tile.getElement().getStyle().setPaddingBottom(10, Style.Unit.PX);
        tile.getElement().getStyle().setWidth(15, Style.Unit.PX);
        tile.getElement().getStyle().setProperty("userSelect", "none");
        tile.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new WindowTileEvent());
            }
        });
        tile.setTitle(WINDOW_TOOLTIP);
        
        cascade = new Image(appearance.windowCascade());
        cascade.addStyleName(appearance.style().iconStyle());
        cascade.getElement().getStyle().setPaddingBottom(10, Style.Unit.PX);
        cascade.getElement().getStyle().setWidth(15, Style.Unit.PX);
        cascade.getElement().getStyle().setProperty("userSelect", "none");
        cascade.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new WindowCascadeEvent());
            }
        });
        cascade.setTitle(CASCADE_TOOLTIP);
        
        add(tile);
        add(cascade);
    }

    public Button getPin() {
        return pin;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void add(Widget w) {
        if (w instanceof VisualizationBarIcon) {
            VisualizationBarIcon icon = (VisualizationBarIcon) w;
            icons.add(icon);
        }
        super.add(w);
    }

    public int getVizBarYPosition(VisualizationType type) {
        for (int i = 0; i < getWidgetCount(); i++) {
            Widget w = getWidget(i);
            if (w instanceof VisualizationBarIcon) {
                VisualizationBarIcon vbw = (VisualizationBarIcon) w;
                if (vbw.getVisualizationType() == type) {
                    return vbw.getElement().<XElement> cast().getPosition(false).getY()
                            - this.getElement().<XElement> cast().getPosition(false).getY();
                }
            }
        }
        return 0;
    }

    public void update() {
        for (VisualizationBarIcon icon : icons) {
            icon.update();
        }
    }
}
