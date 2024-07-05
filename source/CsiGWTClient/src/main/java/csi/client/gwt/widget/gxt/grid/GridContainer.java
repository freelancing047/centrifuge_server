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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class GridContainer extends LayoutPanel {

    private boolean pagerAttached;
    private SimpleLayoutPanel gridPanel = new SimpleLayoutPanel();
    private SimpleLayoutPanel pagingPanel = new SimpleLayoutPanel();
    private static final int HEIGHT_PAGER = 30;
    private int _top = 0;
    private int _bottom = 10;

    public GridContainer(int topIn, int bottomIn) {
        super();

        _top = topIn;
        _bottom = bottomIn;

        // Critical - otherwise we get a relative positioned div with no height.
        setHeight("100%"); //$NON-NLS-1$
        setWidth("100%"); //$NON-NLS-1$
        add(gridPanel);
        setWidgetTopBottom(gridPanel, _top, Unit.PX, (HEIGHT_PAGER), Unit.PX);
        add(pagingPanel);
        setWidgetBottomHeight(pagingPanel, 0, Unit.PX, HEIGHT_PAGER, Unit.PX);
        // Suppresses boot input style on the toolbar to keep the toolbar height compact.
        getWidgetContainerElement(pagingPanel).addClassName("compact"); //$NON-NLS-1$
    }


    public GridContainer(int topIn) {

        this(topIn, 10);
    }

    public GridContainer(){
        this(0, 10);

    }
    @UiChild(tagname = "grid", limit = 1)
    public <M> void setGrid(Grid<M> grid) {
        gridPanel.setWidget(grid);
    }

    @UiChild(tagname = "pager", limit = 1)
    public void setPager(ToolBar toolBar) {
        pagingPanel.setWidget(toolBar);
        pagerAttached = true;
    }

    @Override
    protected void onAttach() {
        if (!pagerAttached) {
            setWidgetTopBottom(gridPanel, _top, Unit.PX, _bottom, Unit.PX);
            setWidgetVisible(pagingPanel, false);
        }
        super.onAttach();
    }
}