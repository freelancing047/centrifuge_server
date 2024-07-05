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
package csi.client.gwt.widget.boot;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TabLink;
import com.github.gwtbootstrap.client.ui.TabPane;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.resources.Bootstrap.Tabs;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.widget.misc.ReactsToVisibilityChange;
import csi.client.gwt.widget.misc.WidgetWalker;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class CsiTabPanel extends TabPanel implements RequiresResize, ProvidesResize {

    private static long _radioTag = 0;

    private int defaultSelectedTab = 1;
    private List<TabLink> links = new ArrayList<TabLink>();
    private String tabPosition;
    private AbstractCsiTab activeTab = null;
    private TabLink activeLink = null;

    public static void fixRadioButtons(com.github.gwtbootstrap.client.ui.RadioButton[] buttonArrayIn) {

        if ((null != buttonArrayIn) && (0 < buttonArrayIn.length)) {

            String myRadioGroup = Long.toString(_radioTag++);

            for (int i = 0; buttonArrayIn.length > i; i++) {

                com.github.gwtbootstrap.client.ui.RadioButton myButton = buttonArrayIn[i];
                myButton.setName(myRadioGroup);
            }
        }
    }

    public static void fixRadioButtons(com.google.gwt.user.client.ui.RadioButton[] buttonArrayIn) {

        if ((null != buttonArrayIn) && (0 < buttonArrayIn.length)) {

            String myRadioGroup = Long.toString(_radioTag++);

            for (int i = 0; buttonArrayIn.length > i; i++) {

                com.google.gwt.user.client.ui.RadioButton myButton = buttonArrayIn[i];
                myButton.setName(myRadioGroup);
            }
        }
    }

    public static void fixRadioButtons(List<? extends ButtonBase> buttonListIn) {

        if ((null != buttonListIn) && (0 < buttonListIn.size())) {

            String myRadioGroup = Long.toString(_radioTag++);

            if (buttonListIn.get(0) instanceof com.github.gwtbootstrap.client.ui.RadioButton) {

                for (ButtonBase myButton : buttonListIn) {

                    ((com.github.gwtbootstrap.client.ui.RadioButton)myButton).setName(myRadioGroup);
                }

            } else if (buttonListIn.get(0) instanceof com.google.gwt.user.client.ui.RadioButton) {

                for (ButtonBase myButton : buttonListIn) {

                    ((com.google.gwt.user.client.ui.RadioButton)myButton).setName(myRadioGroup);
                }
            }
        }
    }

    public CsiTabPanel() {
        super();
        init();
    }

    /**
     * @param position
     */
    public CsiTabPanel(Tabs position) {
        super(position);
        init();
    }

    public void disableTabs() {

        for (TabLink myLink : links) {

            myLink.setDisabled(true);
        }
    }

    public void enableTabs() {

        for (TabLink myLink : links) {

            myLink.setDisabled(false);
        }
    }

    public void enableTabs(boolean enableIn) {

        for (TabLink myLink : links) {

            myLink.setDisabled(!enableIn);
        }
    }

    private void init() {
        addShownHandler(new ShownEvent.Handler() {

            @Override
            public void onShow(ShownEvent shownEvent) {
                onResize();

                activeLink = links.get(getSelectedTab());
                TabPane pane = activeLink.getTabPane();
                if (pane instanceof TabReferencingTabPane) {
                    AbstractCsiTab csitab = ((TabReferencingTabPane) pane).getTab();
                    csitab.onShow();
                    activeTab = csitab;
                }
                new WidgetWalker() {

                    @Override
                    public void actOn(Widget widget) {
                        if (widget instanceof ReactsToVisibilityChange) {
                            ((ReactsToVisibilityChange) widget).onShow();
                        }
                    }
                }.startingAt(pane).walk();
            }
        });

        addShowHandler(new ShowEvent.Handler() {

            @Override
            public void onShow(ShowEvent showEvent) {
                if (getSelectedTab() != -1) {
                    TabPane pane = links.get(getSelectedTab()).getTabPane();
                    if (pane instanceof TabReferencingTabPane) {
                        AbstractCsiTab csitab = ((TabReferencingTabPane) pane).getTab();
                        csitab.onHide();
                    }
                    new WidgetWalker() {

                        @Override
                        public void actOn(Widget widget) {
                            if (widget instanceof ReactsToVisibilityChange) {
                                ((ReactsToVisibilityChange) widget).onHide();
                            }
                        }
                    }.startingAt(pane).walk();
                }
            }
        });
    }

    @Override
    public void onResize() {
        if (isAttached()) {
            for (TabLink link : links) {
                TabPane pane = link.getTabPane();
                int height = this.getOffsetHeight();
                if (!("left".equals(this.tabPosition) || "right".equals(this.tabPosition))) {
                    height -= link.getOffsetHeight();
                }
                if (height > 0) {
                    pane.setHeight(height + "px");
                    for (int i = 0; i < pane.getWidgetCount(); i++) {
                        Widget widget = pane.getWidget(i);
                        if (widget instanceof RequiresResize) {
                            ((RequiresResize) widget).onResize();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setTabPosition(String position) {
        this.tabPosition = position;
        super.setTabPosition(position);
    }

    @Override
    public void add(Widget child) {
        if (child instanceof TabLink) {
            links.add((TabLink) child);
        }
        super.add(child);
    }

    @Override
    public boolean remove(int index) {
        links.remove(index);
        return super.remove(index);
    }

    @Override
    public boolean remove(Widget w) {
        if (w instanceof TabLink) {
            links.remove((TabLink) w);
        }
        return super.remove(w);
    }

    @Override
    public boolean remove(IsWidget child) {
        if (child instanceof AbstractCsiTab) {
            links.remove(((AbstractCsiTab) child).asTabLink());
        }
        return super.remove(child);
    }

    @Override
    public final void clear() {
        // Don't call parent. Clears structures without removing widgets!

        for (TabLink myLink : links) {

            super.remove(myLink);
//            myLink.removeFromParent();
        }
        links.clear();
    }

    public void selectTab(Tab tab) {
        int newTabIndex = links.indexOf(tab.asTabLink());
        int oldTabIndex = getSelectedTab();
        if (oldTabIndex != newTabIndex) {
            selectTab(newTabIndex);
        }
    }

    public int getDefaultSelectedTab() {
        return defaultSelectedTab;
    }

    /**
     * @param defaultSelectedTab 1 based index.
     */
    public void setDefaultSelectedTab(int defaultSelectedTab) {
        this.defaultSelectedTab = defaultSelectedTab;
        selectDefault();
    }

    @Override
    protected void onAttach() {
        super.onAttach();
            selectDefault();
    }

    public void selectDefault() {
        if (links.size() >= getDefaultSelectedTab()) {
            selectTab(getDefaultSelectedTab() - 1);
        }
    }
    
    public void hideLinks() {
        for (TabLink link : links) {
            link.setVisible(false);
            link.getParent().setVisible(false);
        }

    }
    
    public void showLinks() {
        for (TabLink link : links) {
            link.setVisible(true);
        }
    }

    public List<TabLink> getLinks() {
        return links;
    }

    public AbstractCsiTab getActiveTab() {

        return activeTab;
    }

    public TabLink getActiveLink() {

        return activeLink;
    }
}
