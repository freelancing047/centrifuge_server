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
package csi.client.gwt.widget.gxt.grid.paging;

import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.SortInfo;
import com.sencha.gxt.data.shared.SortInfoBean;
import com.sencha.gxt.widget.core.client.event.SortChangeEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.menu.Menu;

import csi.client.gwt.mapper.menus.GridMenu;

/**
 * Use this with a remote loader that does remote sorting ONLY as Sencha's group-view implementation is completely 
 * broken for remote sorting!
 * @author Centrifuge Systems, Inc.
 *
 */
public class GroupingView<M> extends com.sencha.gxt.widget.core.client.grid.GroupingView<M> {

    private GridMenu<? extends Grid<M>> _menu = null;
    private boolean _hideMenu = false;
    private NodeList<Element> tableRows;

    public GroupingView() {

        super();
    }

    public GroupingView(GridMenu<? extends Grid<M>> menuIn) {

        super();

        _menu = menuIn;
        _hideMenu = (null == _menu);
    }

    @Override
    protected Menu createContextMenu(final int colIndex) {

        return _hideMenu ? null : (null != _menu) ? _menu : super.createContextMenu(colIndex);
    }

    @Override
    protected void doSort(int colIndex, SortDir sortDir) {
        if (groupingColumn != null && isRemoteSort()) {
            // Karthik Abram: Sencha's grouping view completely fails to handle remote sorting loaders. This is a fix.
            List<? extends SortInfo> sortInfos = grid.getLoader().getSortInfo();
            SortInfo previousSortInfo = sortInfos.size() == 2 ? grid.getLoader().getSortInfo().get(1) : null;

            SortDir fixedDir = null;
            if (previousSortInfo == null) {
                fixedDir = SortDir.ASC;
            } else if (previousSortInfo.getSortField().equals(cm.getColumn(colIndex).getPath())) {
                fixedDir = SortDir.toggle(previousSortInfo.getSortDir());
            } else {
                fixedDir = SortDir.ASC;
            }

            grid.getLoader().clearSortInfo();
            grid.getLoader().addSortInfo(0, new SortInfoBean(groupingColumn.getValueProvider(), SortDir.ASC));
            grid.getLoader().addSortInfo(1, new SortInfoBean(cm.getColumn(colIndex).getValueProvider(), fixedDir));
            grid.getLoader().load();
        } else {
            super.doSort(colIndex, sortDir);
        }
    }

    protected void updateHeaderSortState() {
        if (isRemoteSort()) {
            List<? extends SortInfo> infos = grid.getLoader().getSortInfo();
            SortInfo info;
            if (infos.size() == 2) {
                info = infos.get(1);
            } else {
                info = infos.get(0);
            }
            String p = info.getSortField();
            if (p != null && !"".equals(p)) { //$NON-NLS-1$
                ColumnConfig<M, ?> config = cm.findColumnConfig(p);
                if (config != null) {
                    if (sortState == null || (!sortState.getSortField().equals(p))
                            || sortState.getSortDir() != info.getSortDir()) {
                        int index = cm.indexOf(config);
                        if (index != -1) {
                            updateSortIcon(index, info.getSortDir());
                        }
                        grid.fireEvent(new SortChangeEvent(info));
                    }
                }
                sortState = info;
            }
        } else {
            super.updateHeaderSortState();
        }
    }
    
    @Override
    protected NodeList<Element> getRows() {
      if (!enableGrouping || !hasRows()) {
        return super.getRows();
      }
      if(this.tableRows != null){
          return tableRows;
      }
      return dataTable.<XElement> cast().select("." + styles.row());
    }
    
    public void cacheTableRows() {
        this.tableRows = getRows();
    }
    
    public void invalidateTableRows() {
        this.tableRows = null;
    }
}
