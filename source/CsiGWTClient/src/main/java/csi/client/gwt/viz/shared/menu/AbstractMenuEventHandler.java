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

import csi.client.gwt.viz.Visualization;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractMenuEventHandler<V extends Visualization, M extends AbstractMenuManager<V>> extends
        CsiMenuEventHandler {

    private V presenter;
    private M menuManager;

    public AbstractMenuEventHandler(V presenter, M menuManager) {
        super();
        this.presenter = presenter;
        this.menuManager = menuManager;
    }

    public V getPresenter() {
        return presenter;
    }

    public M getMenuManager() {
        return menuManager;
    }

    /**
     * @return true if the menu handler is applicable for this presenter. false otherwise.
     */
    public boolean isApplicable() {
        return true;
    }
}