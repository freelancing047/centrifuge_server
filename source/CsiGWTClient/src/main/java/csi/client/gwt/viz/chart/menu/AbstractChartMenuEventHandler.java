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
package csi.client.gwt.viz.chart.menu;

import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public abstract class AbstractChartMenuEventHandler extends AbstractMenuEventHandler<ChartPresenter, ChartMenuManager> {

    public AbstractChartMenuEventHandler(ChartPresenter presenter, ChartMenuManager menuManager) {
        super(presenter, menuManager);
    }

}
