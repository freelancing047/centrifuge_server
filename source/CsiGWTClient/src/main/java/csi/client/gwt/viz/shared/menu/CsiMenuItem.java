package csi.client.gwt.viz.shared.menu;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickHandler;

public class CsiMenuItem extends NavLink {

    private MenuKey key;
    private int handlerCount;
    private MenuState runtimeState = null;
    private MenuState menuState = MenuState.HIDDEN;

    // tie to master menu item
    public CsiMenuItem(MenuKey key, ClickHandler handler) {
        setText(key.getLabel());
        addClickHandler(handler);
        this.key = key;
        IconType icon = key.getIcon();
        if (icon != null) {
            setIcon(icon);
        }
        setVisible(false);
        setDisabled(true);
    }

    public MenuState getMenuState() {
        if (runtimeState != null && handlerCount > 0) {
            return runtimeState;
        } else {
            return menuState;
        }
    }

    public void setMenuState(MenuState menuState) {
        this.runtimeState = menuState;
    }

    public boolean isEnabled() {
        return getMenuState() == MenuState.ENABLED;
    }

    public boolean isDisabled() {
        return getMenuState() == MenuState.DISABLED;
    }

    public boolean isHidden() {
        return getMenuState() == MenuState.HIDDEN;
    }

    public MenuKey getKey() {
        return key;
    }

    public void increaseHandlerCount() {
        handlerCount += 1;
        menuState = MenuState.ENABLED;
    }

    public void decreaseHandlerCount() {
        handlerCount -= 1;
        if (handlerCount == 0) {
            runtimeState = null;
            menuState = MenuState.HIDDEN;
        }
    }

}
