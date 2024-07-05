/** 
 *  Copyright (c) 2013 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.widget.gxt.grid;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;


public class FixedSizeGrid<M> extends Grid<M> {

    public FixedSizeGrid(ListStore<M> store, ColumnModel<M> cm, GridView<M> view) {
        super(store, cm, view);
        init();
    }

    public FixedSizeGrid(ListStore<M> store, ColumnModel<M> cm) {
        super(store, cm);
        init();
    }

    private void init() {
        addStyleName("sencha-gxt-grid"); //$NON-NLS-1$
    }
  
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if(getView() != null && getView().getHeader() != null){
            getView().getHeader().refresh();
        }
    }
    
    @Override
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        if(getView() != null && getView().getHeader() != null){
            getView().getHeader().refresh();
        }
    }
    
}
