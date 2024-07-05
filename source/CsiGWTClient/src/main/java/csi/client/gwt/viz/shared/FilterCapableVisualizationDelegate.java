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
package csi.client.gwt.viz.shared;

import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.viz.shared.filter.FilterCapableVisualizationPresenter;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.visualization.VisualizationDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class FilterCapableVisualizationDelegate<V extends VisualizationDef, W extends Widget> implements
        FilterCapableVisualizationPresenter {

    private AbstractVisualizationPresenter<V, W> presenter;

    public FilterCapableVisualizationDelegate(AbstractVisualizationPresenter<V, W> presenter) {
        super();
        this.presenter = presenter;
    }

    public VisualizationDef getVisualizationDef() {
        return presenter.getVisualizationDef();
    }

    public String getDataViewUuid() {
        return presenter.getDataViewUuid();
    }

    public void applyFilter(Filter filter) {
        getVisualizationDef().setFilter(filter);
        presenter.saveSettings(true);
    }

}
