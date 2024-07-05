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
package csi.client.gwt.viz.shared.menu;

import java.util.HashMap;
import java.util.Map;

import csi.client.gwt.viz.Visualization;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractMenuManager<V extends Visualization> {

    private V presenter;
    private Map<String, AbstractMenuManager> menuManagers = new HashMap<String, AbstractMenuManager>();

    public AbstractMenuManager(V presenter) {
        this.presenter = presenter;
    }

    public V getPresenter() {
        return presenter;
    }

    @SuppressWarnings("rawtypes")
    public void register(MenuKey key, AbstractMenuEventHandler handler) {
        if (handler.isApplicable()) {
            getPresenter().getChrome().getMenu().addClickHandler(key, handler);
        }
    }
    
    public void addSeperatorAboveKey(MenuKey key){
        getPresenter().getChrome().getMenu().addSeparatorBeforeKey(key);
    }
    
    public void unCheckItem(MenuKey key){
    	getPresenter().getChrome().getMenu().unCheckedMenuItem(key);
    }
    
    public void checkItem(MenuKey key){
    	getPresenter().getChrome().getMenu().checkedMenuItem(key);
    }

    public void enable(MenuKey key) {
        getPresenter().getChrome().getMenu().enable(key);
    }

    public void disable(MenuKey key) {
        getPresenter().getChrome().getMenu().disable(key);
    }

    public void hide(MenuKey key) {
        getPresenter().getChrome().getMenu().hide(key);
    }
    
    public void registerMenuManager(String key, AbstractMenuManager mgr) {
    	menuManagers.put(key, mgr);
    }

    public void unregisterMenuManager(String key) {
    	menuManagers.remove(key);
    }
    
    abstract public void registerPreloadMenus(boolean limitedMenu);
    
    /**
     * Subclasses are expected to register handlers against menu keys by calling register()
     */
    public void registerMenus(boolean limitedMenu) {
    	registerCommonMenus(limitedMenu);
    }
    
    private void registerCommonMenus(boolean limitedMenu) {
    	if (menuManagers.size() > 0) {
    		for (AbstractMenuManager mgr : menuManagers.values()) {
    			mgr.registerMenus(limitedMenu);
    		}
    	}
    }
    
    public void hideMenus(boolean limitedMenu) {}
    
    public void showMenus(boolean limitedMenu) {}

    public void updateDynamicMenus(){};
}
