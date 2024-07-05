/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.worksheet.layout.window;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.worksheet.WorksheetView;
import csi.client.gwt.worksheet.layout.icons.VisualizationIconManager;
import csi.client.gwt.worksheet.layout.icons.VisualizationIconManager.IconSize;
import csi.client.gwt.worksheet.layout.window.events.NewVisualizationEvent;
import csi.client.gwt.worksheet.layout.window.events.VisualizationBarSelectionEvent;
import csi.server.common.model.visualization.VisualizationType;

/**
 * @author Centrifuge Systems, Inc.
 */
public class VisualizationBarIcon extends Composite {

    private static final int IMAGE_HEIGHT = 46;
    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private Menu menu = new Menu();
    private VisualizationType visualizationType;
    private WorksheetView worksheetView;
    private FlowPanel imagePanel;
    private Image image;
    private VisualizationBarIconBadge countBadge;


    VisualizationBarIcon(VisualizationType type, WorksheetView view, int counter) {
        super();
        worksheetView = view;
        visualizationType = type;

        imagePanel = new FlowPanel();
        image = new Image();
        imagePanel.add(image);

        image.setResource(VisualizationIconManager.getIcon(type, IconSize.SMALL, false));

        Style imagePanelStyle = imagePanel.getElement().getStyle();
        imagePanelStyle.setProperty("margin", "0px auto");
        imagePanelStyle.setTextAlign(Style.TextAlign.CENTER);
        imagePanelStyle.setMarginBottom(10, Style.Unit.PX);
        imagePanelStyle.setBackgroundColor("none");
        imagePanelStyle.setWidth(41, Style.Unit.PX);
        imagePanelStyle.setHeight(IMAGE_HEIGHT, Style.Unit.PX);
        imagePanelStyle.setProperty("userSelect", "none");

        image.getElement().getStyle().setProperty("margin", "0px auto");
        image.getElement().getStyle().setProperty("userSelect", "none");

        initWidget(imagePanel);
        image.setAltText(VisualizationIconManager.getAltText(type));
        this.setTitle(VisualizationIconManager.getAltText(type));

        int vizSize = worksheetView.getVisualizations(visualizationType).size();
        countBadge = new VisualizationBarIconBadge(String.valueOf(vizSize));
        if (vizSize > 0) {
            imagePanel.add(countBadge);
        }
        countBadge.getElement().getStyle().setTop(23 + ((IMAGE_HEIGHT + 10) * counter), Style.Unit.PX);
        countBadge.getElement().getStyle().setProperty("userSelect", "none");

        this.getElement().getStyle().setCursor(Cursor.POINTER);
        addDomHandler(event -> {
            menu = new Menu();
            setupMenu();
            menu.show(VisualizationBarIcon.this.getElement(),
                    new AnchorAlignment(Anchor.TOP_RIGHT, Anchor.TOP_LEFT));
        }, ClickEvent.getType());

        addDomHandler(event -> {
            //4 cases: None, None + Hover, Exists, Exists + Hover
            image.setResource(VisualizationIconManager.getIcon(visualizationType, IconSize.SMALL, true));
        }, MouseOverEvent.getType());

        addDomHandler(event -> image.setResource(VisualizationIconManager.getIcon(visualizationType, IconSize.SMALL, false)), MouseOutEvent.getType());

    }

    private void setupMenu() {
        menu.clear();

        if (!worksheetView.isReadOnly()) {

            MenuItem newItem;

//        if(visualizationType == VisualizationType.CHRONOS){
//        	newItem = new MenuItem(i18n.visualizationBarIconBetaText()); //$NON-NLS-1$
//        } else if(visualizationType == VisualizationType.GEOSPATIAL_V2){
//            newItem = new MenuItem(i18n.visualizationBarIconBetaText()); //$NON-NLS-1$
//        } else {
            newItem = new MenuItem(i18n.visualizationBarIconDefaultText()); //$NON-NLS-1$
            //}
            newItem.addSelectionHandler(new SelectionHandler<Item>() {

                @Override
                public void onSelection(SelectionEvent<Item> event) {
                    worksheetView.getEventBus().fireEvent(new NewVisualizationEvent(visualizationType));
                }
            });
            menu.add(newItem);
            newItem.getElement().getFirstChildElement().getStyle().setLeft(-13, Style.Unit.PX);
        }
        menu.add(new SeparatorMenuItem());
        for (final Visualization visualization : worksheetView.getVisualizations(visualizationType)) {
            MenuItem windowItem = new MenuItem(visualization.getName());
            menu.add(windowItem);

            VizPanel v = ((VizPanel) visualization.getChrome());
            if (v.getFrameProvider() instanceof VisualizationWindow) {
                VisualizationWindow frameProvider = (VisualizationWindow) v.getFrameProvider();
                if (!frameProvider.isMinimized()) {
                    windowItem.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
                } else {
                    windowItem.getElement().getStyle().setFontWeight(Style.FontWeight.LIGHTER);
                }
            }
            windowItem.addSelectionHandler(event -> worksheetView.getEventBus().fireEvent(new VisualizationBarSelectionEvent(visualization)));
            windowItem.getElement().getFirstChildElement().getStyle().setLeft(-13, Style.Unit.PX);
        }
//        menu.getElement().getStyle().setWidth(90, Style.Unit.PX);
        menu.getElement().getStyle().setProperty("borderRadius", "6px");
        menu.getElement().getFirstChildElement().getStyle().setBackgroundColor("white");

    }

    VisualizationType getVisualizationType() {
        return visualizationType;
    }

    public void update() {
        setupMenu();
        if (worksheetView.getVisualizations(visualizationType).size() > 0) {
            imagePanel.add(countBadge);
        } else {
            imagePanel.remove(countBadge);
        }
        countBadge.setCount(String.valueOf(worksheetView.getVisualizations(visualizationType).size()));
    }
}
