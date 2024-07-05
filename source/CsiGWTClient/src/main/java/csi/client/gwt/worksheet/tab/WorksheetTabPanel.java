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
package csi.client.gwt.worksheet.tab;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.fx.client.FxElement;
import com.sencha.gxt.fx.client.animation.AfterAnimateEvent;
import com.sencha.gxt.fx.client.animation.Fx;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.DataViewPresenter;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.worksheet.tab.events.*;
import csi.shared.core.color.ClientColorHelper;

import java.util.List;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class WorksheetTabPanel extends TabPanel implements RequiresResize, HasMouseDownHandlers, HasMouseUpHandlers{

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final String TOOLTIP_TEXT = "<span title=\"" + i18n.worksheetTabPanelAddTooltip() + "\">+</span>"; //$NON-NLS-1$
    private MainPresenter _mainPresenter = null;
    private AddTabWidget addTab = null;
    private MenuItem renameMenuItem = null;
    private MenuItem deleteMenuItem = null;
    private MenuItem colorMenuItem = null;

    private class AddTabWidget extends Label {

        public AddTabWidget(){
            super();
        }

    }


    private Menu menu = null;
    private Widget contextItem;

    private Widget currentItem;
    private Widget dragItem;
    private int scrollIncrement = 100;
    private static int TAB_HEIGHT = 25;


    public WorksheetTabPanel() {
        super();
        init();
    }

    private void init() {
        setAnimScroll(true);
        setTabScroll(true);
        addStyleName("senchaTab"); //$NON-NLS-1$
//        addStyleName("tabStyle");

        // Check for write capability
        /*
        if (!getMainPresenter().getDataViewPresenter(true).isReadOnly()) {

            TabItemConfig tabItemConfig =  new TabItemConfig("+"); //$NON-NLS-1$
            tabItemConfig.setHTML(TOOLTIP_TEXT);

            AddTabWidget addTab = new AddTabWidget();

            add(addTab, tabItemConfig);
        }
        */

        addMouseDownHandler(createMouseDownHandler());
        addMouseUpHandler(createMouseUpHandler());

        addBeforeSelectionHandler(new BeforeSelectionHandler<Widget>() {

            @Override
            public void onBeforeSelection(BeforeSelectionEvent<Widget> event) {
                if (event.getItem() instanceof AddTabWidget) {
                    event.cancel();
                }
            }
        });

        addFocusHandler(new TabFocusEventHandler() {

            @Override
            public void onFocus(Widget tabContentWidget) {
                if (tabContentWidget instanceof AddTabWidget) {
                    TabCreateEvent e = new TabCreateEvent();
                    fireEvent(e);
                }
            }
        });

        setCloseContextMenu(true);
        supportUpdates();
    }

    public void setReadOnly() {

        if (null != addTab) {

            remove(addTab);
            addTab = null;
        }
        menu = null;
    }

    public void supportUpdates() {

        if (null == menu) {

            TabItemConfig tabItemConfig =  new TabItemConfig("+"); //$NON-NLS-1$

            tabItemConfig.setHTML("+");


            addTab = new AddTabWidget();

            // I tried to add this tooltip, but it wouldn't trigger correctly. TODO.
/*            ToolTipConfig cfg = new ToolTipConfig();
            cfg.setBody(i18n.worksheetTabPanelAddTooltip());
            cfg.setShowDelay(100);
            cfg.setHideDelay(1000);
            cfg.setAutoHide(true);
            cfg.setEnabled(true);
            QuickTip t = new QuickTip(addTab);
            t.setAllowTextSelection(false);
            t.update(cfg);*/

            add(addTab, tabItemConfig);

            menu = new Menu();
            {
                renameMenuItem = new MenuItem(i18n.worksheetTabPanelRenameItem()); //$NON-NLS-1$
                renameMenuItem.addSelectionHandler(new SelectionHandler<Item>() {

                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        TabRenameEvent re = new TabRenameEvent();
                        re.setTabContentWidget(contextItem);
                        fireEvent(re);
                    }
                });
                menu.add(renameMenuItem);
            }
            {
                deleteMenuItem = new MenuItem(i18n.worksheetTabPanelDeleteItem()); //$NON-NLS-1$
                deleteMenuItem.addSelectionHandler(new SelectionHandler<Item>() {

                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        TabDeleteEvent re = new TabDeleteEvent();
                        re.setTabContentWidget(contextItem);
                        fireEvent(re);
                    }
                });
                menu.add(deleteMenuItem);
            }
            {
                colorMenuItem = new MenuItem(i18n.worksheetTabPanelColorItem()); //$NON-NLS-1$
                colorMenuItem.addSelectionHandler(new SelectionHandler<Item>() {

                    @Override
                    public void onSelection(SelectionEvent<Item> event) {
                        TabColorEvent re = new TabColorEvent();
                        re.setTabContentWidget(contextItem);
                        fireEvent(re);
                    }
                });


                menu.add(colorMenuItem);
            }
        }
    }

    protected void setDragItem(Widget dragItem) {
        this.dragItem = dragItem;
    }


    private MouseDownHandler createMouseDownHandler() {

        return new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                    List<Visualization> visualizations = getMainPresenter().getDataViewPresenter(true).getVisualizations();
                    if(visualizations.size() > 0) {
                        for (Visualization viz : visualizations) {
                            if (viz !=null) {
                                VizPanel chrome = (VizPanel) viz.getChrome();
                                if (chrome != null) {
                                    chrome.hideMenu();
                                    chrome.showNameRow();
                                }
                            }
                        }
                    }

                Widget item = findSelectedItem(event);
                if (item == null) {
                    WorksheetTabPanel.this.setCurrentItem(null);
                } else {
                    WorksheetTabPanel.this.setCurrentItem(item);
                }

                if(findTabRange(event)){
                    dragItem = currentItem;
                    if(dragItem != null){
                        try{
                            XElement tabItem = findItem(getWidgetIndex(dragItem));
                            Element childElement = tabItem.getFirstChildElement().getNextSiblingElement();
                            //childElement.getStyle().setBackgroundColor("#4dc3ff");
                            childElement.setPropertyString("style", "border-bottom-style:dashed;");
                            childElement.getStyle().setBorderColor("#ff4d4d");
                            childElement.getStyle().setBorderWidth(1, Unit.PX);
                            childElement.getStyle().setCursor(Cursor.COL_RESIZE);
                            NodeList<Element> spanElements = childElement.getElementsByTagName("span");
                            Element backgroundSpan = spanElements.getItem(1);
                            if(backgroundSpan.hasClassName("colored-worksheet")) {
                                childElement.getStyle().setPadding(0, Unit.PX);
                            }

                        } catch(Exception e){
                            //no-op and not writing 5 ifs
                        }
                    }
                }
            }

        };
    }

    private MouseUpHandler createMouseUpHandler() {

        return new MouseUpHandler() {

            @Override
            public void onMouseUp(MouseUpEvent event) {

                if(dragItem != null){
                    try{
                        XElement tabItem = findItem(getWidgetIndex(dragItem));
                        Element childElement = tabItem.getFirstChildElement().getNextSiblingElement();
                        //childElement.getStyle().setBackgroundColor("#4dc3ff");
                        childElement.setPropertyString("style", "");
                        NodeList<Element> spanElements = childElement.getElementsByTagName("span");
                        Element backgroundSpan = spanElements.getItem(1);
                        if(backgroundSpan.hasClassName("colored-worksheet")) {
                            childElement.getStyle().setPadding(0, Unit.PX);
                        }
                    } catch(Exception e){
                        //no-op and not writing 5 ifs
                    }
                }

                Widget item = findSelectedItem(event);
                if (item == null) {
                    WorksheetTabPanel.this.setCurrentItem(null);
                } else {
                    WorksheetTabPanel.this.setCurrentItem(item);
                }

                if(findTabRange(event)){
                    final int index = WorksheetTabPanel.this.getWidgetIndex(currentItem);
                    final int currentIndex = WorksheetTabPanel.this.getWidgetIndex(dragItem);

                    //Don't want to swap with AddTabWidget
                    if(currentItem instanceof AddTabWidget || dragItem instanceof AddTabWidget){
                        return;
                    }

                    final TabItemConfig tabConfig = WorksheetTabPanel.super.getConfig(dragItem);

                    if (index != currentIndex) {
                        // Check for write capability and display error if not allowed
                        if (getMainPresenter().getDataViewPresenter(true).canUpdate()) {
                            Integer backgroundColor = getTabColor(dragItem);
                            // Find out the current focused tab so we can switch back to it after re-establishing color on the drag tab.
                            Widget currentActiveWidget = getActiveWidget();

                            WorksheetTabPanel.super.remove(dragItem);
                            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                                @Override
                                public void execute() {

                                    TabDragEvent event = new TabDragEvent();
                                    event.setNewIndex(index);
                                    event.setOldIndex(currentIndex);
                                    fireEvent(event);

                                    DataViewPresenter dvp = (DataViewPresenter) getMainPresenter().getDataViewPresenter(true);
                                    WorksheetTabPanel.super.insert(dragItem, index, tabConfig);
                                    WorksheetTabPanel.this.setActiveWidget(dragItem);
                                    dvp.colorWorksheet(dvp.getActiveWorksheet(), backgroundColor);
                                    WorksheetTabPanel.this.setActiveWidget(currentActiveWidget);
                                    dragItem = null;
                                }
                            });
                        }
                    }
                }
            }

        };
    }

    private Integer getTabColor(Widget tab) {
        XElement tabItem = getCurrentTab(tab);
        NodeList<Element> emElements = tabItem.getElementsByTagName("em");
        Element em = emElements.getItem(0);
        NodeList<Element> spanElements = em.getElementsByTagName("span");
        Element backgroundSpan = spanElements.getItem(1);
        String backgroundSpanColor = backgroundSpan.getStyle().getBackgroundColor();
        if (!backgroundSpanColor.isEmpty()) {
            String[] rgbColor = backgroundSpanColor.substring(4, backgroundSpanColor.length() - 1).split(",");
            return ClientColorHelper.get().makeFromRGB(Integer.parseInt(rgbColor[0]), Integer.parseInt(rgbColor[1].substring(1)), Integer.parseInt(rgbColor[2].substring(1))).getIntColor();
        } else {
            return ClientColorHelper.get().makeFromHex("FFFFFF").getIntColor();
        }
    }


    protected void setCurrentItem(Widget object) {
        this.currentItem = object;
    }

    private boolean findTabRange(MouseEvent<?> event){
        boolean withinRange = false;

        int y = event.getClientY();

        if(currentItem != null){
            if(this.getActiveWidget() != null){
                int worksheetTop = this.getActiveWidget().getAbsoluteTop();
                if(y > worksheetTop - TAB_HEIGHT && y < worksheetTop){
                    withinRange = true;
                }
            }
        }

        return withinRange;
    }

    private Widget findSelectedItem(MouseEvent<?> event) {

        XElement target = event.getNativeEvent().getEventTarget().cast();
        Element item = findItem(target);
        Widget w = null;
        if (item != null) {
            event.stopPropagation();
            int index = itemIndex(item);
            if(index != -1){
                w = getWidget(index);
            }
        }

        return w;
    }


    public HandlerRegistration addTabDragHandler(TabDragEventHandler handler) {
        return addHandler(handler, TabDragEvent.type);
    }

    public HandlerRegistration addRenameHandler(TabRenameEventHandler handler) {
        return addHandler(handler, TabRenameEvent.type);
    }

    public HandlerRegistration addColorHandler(TabColorEventHandler handler) {
        return addHandler(handler, TabColorEvent.type);
    }

    public HandlerRegistration addCreateHandler(TabCreateEventHandler handler) {
        return addHandler(handler, TabCreateEvent.type);
    }

    public HandlerRegistration addDeleteHandler(TabDeleteEventHandler handler) {
        return addHandler(handler, TabDeleteEvent.type);
    }

    public HandlerRegistration addPublishHandler(TabPublishEventHandler handler) {
        return addHandler(handler, TabPublishEvent.type);
    }

    @Override
    protected void onItemContextMenu(Widget item, int x, int y) {
        contextItem = item;
        if ((null != menu) && (item instanceof AddTabWidget == false)) {
            menu.showAt(x, y);
        }
    }

    /**
     * This is fully duplicated from TabPanel because focusTab was marked private and we need to override for the
     * case where we have no tabs and the + tab is the only tab (to detect click on +).
     * @param event
     */
    protected void onClick(Event event) {
        XElement target = event.getEventTarget().cast();
        Element item = findItem(target);
        if (item != null) {
            event.stopPropagation();
            Widget w = getWidget(itemIndex(item));
            boolean close = getAppearance().isClose(target);
            if (close) {
                close(w);
            } else if (w != getActiveWidget()) {
                setActiveWidget(w);
                focusTab(w);
            } else if (w == getActiveWidget()) {
                focusTab(w);
            }
        }

        if (this.getAppearance().getScrollLeft(this.getElement()) != null && target.isOrHasChild(this.getAppearance().getScrollLeft(this.getElement()))) {
            event.stopPropagation();
            this.onScrollLeft();
        }

        if (this.getAppearance().getScrollRight(this.getElement()) != null && target.isOrHasChild(this.getAppearance().getScrollRight(this.getElement()))) {
            event.stopPropagation();
            this.onScrollRight(this.getElement());
        }
    }

    private void onScrollLeft() {
        int pos = this.getScrollPos();
        int s = Math.max(0, pos - this.getScrollIncrement());
        if (s != pos) {
            this.scrollTo(s, this.getAnimScroll());
        }

    }

    private void onScrollRight(Element element) {
        int sw = this.getScrollWidth() - this.getScrollArea();
        int pos = this.getScrollPos();
        int s = Math.min(sw, pos + this.getScrollIncrement());
        if (s != pos) {
            this.scrollTo(s, this.getAnimScroll());
        }
    }

    private int getScrollIncrement() {
        return this.scrollIncrement;
    }

    @Override
    public void setScrollIncrement(int scrollIncrement) {
        super.setScrollIncrement(scrollIncrement);
        this.scrollIncrement = scrollIncrement;
    }

    private int getScrollPos() {
        return this.getAppearance().getStripWrap(this.getElement()).getScrollLeft();
    }

    private int getScrollArea() {
        return Math.max(0, this.getAppearance().getStripWrap(this.getElement()).getClientWidth());
    }

    private int getScrollWidth() {
        return this.getAppearance().getStripEdge(this.getElement()).getOffsetsTo(this.getStripWrap()).getX() + this.getScrollPos();
    }

    private void scrollTo(int pos, boolean animate) {
        XElement stripWrap = this.getStripWrap();
        if (animate) {
            Fx fx = new Fx();
            fx.addAfterAnimateHandler(new AfterAnimateEvent.AfterAnimateHandler() {
                public void onAfterAnimate(AfterAnimateEvent event) {
                    WorksheetTabPanel.this.getAppearance().updateScrollButtons(WorksheetTabPanel.this.getElement());
                }
            });
            ((FxElement)stripWrap.cast()).scrollTo(Style.ScrollDirection.LEFT, pos, true, fx);
        } else {
            stripWrap.setScrollLeft(pos);
            this.getAppearance().updateScrollButtons(this.getElement());
        }

    }

    public HandlerRegistration addFocusHandler(TabFocusEventHandler handler) {
        return addHandler(handler, TabFocusEvent.type);
    }

    public XElement getCurrentTab(Widget widget) {
        XElement tabItem = findItem(getWidgetIndex(widget));
        return tabItem;
    }

    private void focusTab(Widget w) {
        TabFocusEvent event = new TabFocusEvent();
        event.setTabContentWidget(w);
        fireEvent(event);
    }

    @Override
    public void onResize() {
        // Setting the height explicitly causes the onResize to properly account for the tab-bar height and
        // adjust the body height accordingly.
        setHeight(getOffsetHeight() + "px"); //$NON-NLS-1$
        onResize(getOffsetWidth(), getOffsetHeight());
    }

    @Override
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
        return addDomHandler(handler, MouseUpEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return addDomHandler(handler, MouseDownEvent.getType());
    }

    private MainPresenter getMainPresenter() {

        if (null == _mainPresenter) {

            _mainPresenter = WebMain.injector.getMainPresenter();
        }
        return _mainPresenter;
    }

    public Widget getAddTabWidget() {
        return addTab;
    }
}
