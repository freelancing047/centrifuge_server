package csi.client.gwt.viz.shared.menu;

import java.util.EnumSet;
import java.util.Set;

import csi.client.gwt.etc.BaseCsiEventHandler;

public abstract class CsiMenuEventHandler extends BaseCsiEventHandler {

    private Set<MenuKey> subtypes = EnumSet.noneOf(MenuKey.class);

    public abstract void onMenuEvent(CsiMenuEvent event);

    public void subTypeCheck(CsiMenuEvent event) {
        if (subtypes.contains(event.getMenuKey())) {
            onMenuEvent(event);
        }
    }

    public void addSubType(MenuKey key) {
        subtypes.add(key);
    }

    public void removeSubType(MenuKey key) {
        subtypes.remove(key);
    }
}