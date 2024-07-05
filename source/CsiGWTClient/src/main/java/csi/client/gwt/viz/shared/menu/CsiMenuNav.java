package csi.client.gwt.viz.shared.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.Divider;
import com.github.gwtbootstrap.client.ui.Nav;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.base.ComplexWidget;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.sencha.gxt.core.client.dom.XDOM;
import csi.client.gwt.widget.boot.DropdownHelper;


public class CsiMenuNav extends ResizeComposite implements ClickHandler {

    private class ResizeNav extends Nav implements RequiresResize{
        @Override
        public void onResize() {

        }
    }

    private Nav menu = new ResizeNav();
    private EnumHashBiMap<MenuKey, Widget> menuToWidgetMapping = EnumHashBiMap.create(MenuKey.class);

    private SetMultimap<String, NavLink> dynamicMenuItems = HashMultimap.create();

    private EventBus eventBus;
    private int headerTotalOffsetWidth;

    public CsiMenuNav() {
        eventBus = new SimpleEventBus();
        initWidget(menu);

        for (MenuKey menuKey : MenuKey.getTopLevelMenus()) {
            CsiDropdown topLevel = new CsiDropdown();
            DropdownHelper.setupZ(topLevel);
            menuToWidgetMapping.put(menuKey, topLevel);
            topLevel.setText(menuKey.getLabel());
            topLevel.setIcon(menuKey.getIcon());
            topLevel.setVisible(false);
            menu.add(topLevel);
            addSubMenu(menuKey, topLevel);
        }
    }

    public void setScrolling(MenuKey menuKeyIn, int heightIn) {

        Widget widget = menuToWidgetMapping.get(menuKeyIn);
        if (widget instanceof CsiDropdown) {
            ((CsiDropdown) widget).setScrolling(heightIn);
        }
    }

    public void cancelScrolling(MenuKey menuKeyIn) {

        Widget widget = menuToWidgetMapping.get(menuKeyIn);
        if (widget instanceof CsiDropdown) {
            ((CsiDropdown) widget).cancelScrolling();
        }
    }

    @Override
    public void onResize() {
        super.onResize();
        for (MenuKey menuKey : MenuKey.values()) {
            Widget widget = menuToWidgetMapping.get(menuKey);
            if (widget instanceof CsiDropdown) {
                    ((CsiDropdown) widget).setText(menuKey.getLabel());
            }
        }
        int widgetCount = menu.getWidgetCount();
        int totalWidth = 0;
        for (int i = 0; i < widgetCount; i++) {
            totalWidth += menu.getWidget(i).getOffsetWidth();
        }

        boolean hideLabels = totalWidth + 30 > headerTotalOffsetWidth;


        for (MenuKey menuKey : MenuKey.values()) {
            Widget widget = menuToWidgetMapping.get(menuKey);
            if (widget instanceof CsiDropdown) {
                if (hideLabels){
                    ((CsiDropdown) widget).setText("");
                    ((CsiDropdown) widget).setTitle(menuKey.getLabel());

                } else {
                    ((CsiDropdown) widget).setText(menuKey.getLabel());
                    ((CsiDropdown) widget).setTitle(null);
                }
            }
        }
    }

    private void addSubMenu(MenuKey parentMenu, ComplexWidget parentMenuWidget) {
        for (MenuKey key : parentMenu.getChildren()) {
            CsiMenuItem child = new CsiMenuItem(key, this);
            menuToWidgetMapping.put(key, child);
            child.setText(key.getLabel());
            parentMenuWidget.add(child);
            addSubMenu(key, child);
        }
    }

    private void addSubMenu(MenuKey parentMenu, CsiDropdown parentMenuWidget) {
        for (MenuKey key : parentMenu.getChildren()) {
            CsiMenuItem child = new CsiMenuItem(key, this);
            menuToWidgetMapping.put(key, child);
            child.setText(key.getLabel());
            parentMenuWidget.add(child);
            addSubMenu(key, child);
    }
    }

    public Widget getMenuWidget(MenuKey x) {
        return menuToWidgetMapping.get(x);
    }

    /**
     * Appends passed in NavLink to the Menu under given top level key.
     *
     * Keeps track of added items by their title, no dupe titles will be added
     *
     * @param menuKey - What top level item to add to
     * @param navLink - item to be added to the menu
     */
    public void addMenuItem(MenuKey menuKey, NavLink navLink){
        //use for dynamic menus, preference is adding to MenuKeys Enum
        if(menuKey==null){
            return;
        }
        if(navLink==null){
            return;
        }

        Widget menuWidget = getMenuWidget(menuKey);
        if(menuWidget instanceof CsiDropdown){
            CsiDropdown dropdown = (CsiDropdown) menuWidget;

            if(!dynamicMenuItems.containsKey(navLink.getText())) {
                dynamicMenuItems.put(navLink.getText().trim(), navLink);
                dropdown.add(navLink);
                dropdown.setVisible(true);
            }
        }
    }

    /**
     * Finds and removes the dynamic(not coming from the enum) NavLink menu item from the menu.
     * @param navText
     */
    public void removeDynamicMenuItem(String navText){
        Set<NavLink> navItems = dynamicMenuItems.get(navText);
        for(NavLink nav : navItems ){
            nav.removeFromParent();
        }
    }

    public void removeAll(MenuKey menuKeyIn){
        if (null != menuKeyIn) {
            Widget myMenuWidget = getMenuWidget(menuKeyIn);
            if(myMenuWidget instanceof CsiDropdown){
                CsiDropdown myDropdown = (CsiDropdown)myMenuWidget;
                myDropdown.clear();
                myDropdown.setVisible(false);
            }
        }
    }

    public HandlerRegistration addClickHandler(MenuKey key, CsiMenuEventHandler handler) {
        handler.addSubType(key);
        Widget menuWidget = getMenuWidget(key);
        if (menuWidget instanceof CsiMenuItem) {
            ((CsiMenuItem) menuWidget).increaseHandlerCount();
            refreshMenuState(key);
        }
        return new HandlerRegistationWrapper(eventBus.addHandler(CsiMenuEvent.getType(), handler), key);
    }

    @Override
    public void onClick(ClickEvent event) {
        Widget sourceParent = ((Widget) event.getSource()).getParent();
        if (sourceParent instanceof CsiMenuItem) {
            MenuKey key = ((CsiMenuItem) sourceParent).getKey();
            if (key != null) {
                CsiMenuEvent menuEvent = new CsiMenuEvent(this, key);
                eventBus.fireEvent(menuEvent);
            }
        }
    }

    /*This method needs more work -  ??*/
    public Divider addSeparatorBeforeKey(MenuKey key) {
        Widget menuWidget = getMenuWidget(key);
        Widget parent = menuWidget.getParent().getParent();
        Divider divider = null;
        if (parent instanceof CsiDropdown) {
            divider = new Divider();
            
            UnorderedList list = ((CsiDropdown) parent).getMenuWiget();
            int index = list.getWidgetIndex(menuWidget);
            if(index != -1){
                list.insert(divider, index);
            }
        }
        return divider;
    }

    private class HandlerRegistationWrapper implements HandlerRegistration {

        HandlerRegistration real;
        private MenuKey key;

        public HandlerRegistationWrapper(HandlerRegistration real, MenuKey key) {
            this.real = real;
            this.key = key;
        }

        @Override
        public void removeHandler() {
            Widget widget = getMenuWidget(key);
            if (widget instanceof CsiMenuItem) {
                CsiMenuItem menuItem = (CsiMenuItem) widget;
                menuItem.decreaseHandlerCount();
            }
            refreshMenuState(key);
            real.removeHandler();
        }
    }

    private void refreshMenuState(MenuKey key) {
        Widget menuWidget = getMenuWidget(key);
        if (menuWidget instanceof CsiMenuItem) {
            CsiMenuItem item = (CsiMenuItem) menuWidget;
            item.getMenuState().applyTo(item);

            Widget parent = menuWidget.getParent().getParent();
            MenuKey parentKey = menuToWidgetMapping.inverse().get(parent);

            if (parentKey != null && parent instanceof CsiDropdown) {
                boolean atLeastOneChildVisible = false;
                for (MenuKey menuKey : parentKey.getChildren()) {
                    Widget childWidget = getMenuWidget(menuKey);
                    if (childWidget.isVisible()) {
                        atLeastOneChildVisible = true;
                        break;
                    }
                }
                parent.setVisible(atLeastOneChildVisible);
            }
        } else if(menuWidget instanceof Divider){

            menuWidget.setVisible(true);
        }
    }

    public void checkedMenuItem(MenuKey key) {
        Widget menuWidget = getMenuWidget(key);
        if (menuWidget instanceof CsiMenuItem) {
        	((CsiMenuItem) menuWidget).setIcon(IconType.CHECK);
            refreshMenuState(key);
        }
    }

    public void unCheckedMenuItem(MenuKey key) {
        Widget menuWidget = getMenuWidget(key);
        if (menuWidget instanceof CsiMenuItem) {
        	((CsiMenuItem) menuWidget).setIcon(IconType.CHECK_EMPTY);
            refreshMenuState(key);
        }
    }

    public void disable(MenuKey key) {
        Widget menuWidget = getMenuWidget(key);
        if (menuWidget instanceof CsiMenuItem) {
            CsiMenuItem item = (CsiMenuItem) menuWidget;
            item.setMenuState(MenuState.DISABLED);
            refreshMenuState(key);
        }
    }

    public void enable(MenuKey key) {
        Widget menuWidget = getMenuWidget(key);
        if (menuWidget instanceof CsiMenuItem) {
            CsiMenuItem item = (CsiMenuItem) menuWidget;
            item.setMenuState(MenuState.ENABLED);
            refreshMenuState(key);
        }
    }

    public void hide(MenuKey key) {
        Widget menuWidget = getMenuWidget(key);
        if (menuWidget instanceof CsiMenuItem) {
            CsiMenuItem item = (CsiMenuItem) menuWidget;
            item.setMenuState(MenuState.HIDDEN);
            refreshMenuState(key);
        }
    }

    /**
     * Sets the visibility of the NavLink to the visibility param
     * @param navText - Title of the menu item which to hide
     * @param visibility true - visible
     */
    public void setDynamicItemVisible(String navText, boolean visibility){
        Set<NavLink> navItems = dynamicMenuItems.get(navText);
        for(NavLink nav : navItems ){
            nav.setVisible(visibility);
        }
    }

    /**
     * Removes all items from the menu that have been added dynamically, using the
     * addMenuItem(MenuKey menuKey, NavLink navLink).
     *
     */
    public void removeAllDynamicValues(){
        for(NavLink nav : dynamicMenuItems.values()){
            nav.removeFromParent();
        }
        dynamicMenuItems.clear();
    }


    /**
     * Returns a map of visible items in the menu, with their nav text being the key
     * @return Map<String, Boolean> - < NavLink.getText(), visiblity of navlink> for every item that was added.
     */
    public Map<String, Boolean> getVisibleDynamicItems() {
        Map<String, Boolean> visMap = new HashMap<String, Boolean>();

        for (NavLink nav : dynamicMenuItems.values()) {
            visMap.put(nav.getText().trim(), nav.isVisible());
        }
        return visMap;
    }

    public void hideDropdown() {
    	for (Widget widget : menuToWidgetMapping.values()) {
    		if (widget instanceof CsiDropdown) {
    			CsiDropdown topLevel = (CsiDropdown) widget;
    			topLevel.getMenuWiget().getElement().getStyle().setZIndex(-1);
    		}
        }
    }

    public void showDropdown() {
        for (Widget widget : menuToWidgetMapping.values()) {
            if (widget instanceof CsiDropdown) {
                CsiDropdown topLevel = (CsiDropdown) widget;
    			topLevel.getMenuWiget().getElement().getStyle().setZIndex(XDOM.getTopZIndex());
            }
        }
    }

    public boolean isDropdownVisible() {
        boolean isAnyVisible = false;
        for (Widget widget: menuToWidgetMapping.values()) {
            if (widget instanceof CsiDropdown) {
                CsiDropdown dropdown = (CsiDropdown) widget;
                if (dropdown.getElement().getClassName() == "dropdown open") {
                    isAnyVisible = true;
                }
            }
        }
        return isAnyVisible;
    }

    public void setHeaderTotalOffsetWidth(int offsetWidth) {
        headerTotalOffsetWidth = offsetWidth;
    }
}
