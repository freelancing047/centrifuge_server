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
public class LoadMenuHandler<V extends Visualization, M extends AbstractMenuManager<V>> extends
        AbstractMenuEventHandler<V, M> {

    public LoadMenuHandler(V presenter, M menuManager) {
        super(presenter, menuManager);
    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        V presenter = getPresenter();
        presenter.load();
    }
}
