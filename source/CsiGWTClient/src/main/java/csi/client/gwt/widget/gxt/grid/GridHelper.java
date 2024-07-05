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
package csi.client.gwt.widget.gxt.grid;

import com.sencha.gxt.dnd.core.client.DND.Feedback;
import com.sencha.gxt.dnd.core.client.GridDragSource;
import com.sencha.gxt.dnd.core.client.GridDropTarget;
import com.sencha.gxt.widget.core.client.grid.Grid;

import csi.server.common.model.CsiUUID;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class GridHelper {

    public static <M> void setDefaults(Grid<M> grid) {
        grid.getView().setAutoExpandMax(2000);
        grid.getView().setStripeRows(true);
        grid.getView().setColumnLines(true);
        grid.getView().setForceFit(true);
        grid.getView().setTrackMouseOver(false);
        grid.setBorders(false);
        grid.setLoadMask(true);
    }

    /**
     * Enable dragging elements to another grid
     * @param grid
     */
    public static <M> void setDraggableRowsDefaults(Grid<M> grid) {
        setDefaults(grid);

        new GridDragSource<M>(grid);
        GridDropTarget<M> target = new GridDropTarget<M>(grid);
        
        target.setAllowSelfAsSource(true);
        target.setFeedback(Feedback.INSERT);
    }
   
    /**
     * Enable dragging to reorder elements within grid
     * @param grid
     */
    public static <M> void setReorderableRowsDefaults(Grid<M> grid){
        setDefaults(grid);

        new GridDragSource<M>(grid);
        GridDropTarget<M> target = new GridDropTarget<M>(grid);
        
        target.setAllowSelfAsSource(true);
        target.setFeedback(Feedback.INSERT);
        target.setGroup((new CsiUUID()).toString());
    }
}
