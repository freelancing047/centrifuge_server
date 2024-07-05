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
package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.viz.shared.chrome.panel.VizPanelFrameProvider;
import csi.client.gwt.worksheet.layout.window.VisualizationWindow;


/**
 * @author Centrifuge Systems, Inc.
 */
public abstract class FindEventHandler extends BaseCsiEventHandler {

    private AbstractVisualizationPresenter presenter;

    protected FindEventHandler(AbstractVisualizationPresenter presenter) {

        this.presenter = presenter;

    }

    public AbstractVisualizationPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(AbstractVisualizationPresenter presenter) {
        this.presenter = presenter;
    }

    public void find(int x, int y) {
        if (presenter.getChrome() == null || ((VizPanel) presenter.getChrome()).getFrameProvider() == null) {
            return;
        }
        VizPanelFrameProvider frameProvider = ((VizPanel) presenter.getChrome()).getFrameProvider();
        if (frameProvider instanceof VisualizationWindow) {
            VisualizationWindow window = (VisualizationWindow) frameProvider;
            if (window.isVisible()) {
                find();
            }
        }
    }

    protected abstract void find();
}
