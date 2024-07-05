package csi.client.gwt.viz.shared.menu;

import csi.client.gwt.etc.BaseCsiEvent;



public class CsiMenuEvent extends BaseCsiEvent<CsiMenuEventHandler>{
    
    public static final Type<CsiMenuEventHandler> TYPE = new Type<CsiMenuEventHandler>();
    private final MenuKey menuKey;
    private final CsiMenuNav menu;

    
    public CsiMenuEvent(CsiMenuNav menu, MenuKey key) {
        this.menu = menu;
        menuKey = key;
    }
    
    public static Type<CsiMenuEventHandler> getType(){
        return TYPE;
    }

    @Override
    public Type<CsiMenuEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CsiMenuEventHandler handler) {
        handler.subTypeCheck(this);
    }

    public MenuKey getMenuKey() {
        return menuKey;
    }
    
    public CsiMenuNav getMenu() {
        return menu;
    }
}