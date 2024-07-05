package csi.client.gwt.viz.table.grid;

import java.util.List;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortInfoBean;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;

import csi.client.gwt.viz.table.TableView;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;

public class TableVizGrid<M> extends ResizeableGrid<M> {

    private TableView view;
    
    private PagingLoader<FilterPagingLoadConfig, PagingLoadResult<List<?>>> sortLoader;

    public TableVizGrid(ListStore<M> store, ColumnModel<M> cm, TableView tableView) {
        super(store, cm, new TableVizGridView<M>());
        view = tableView;
      }

    public void sort(SortInfoBean bean) {

        sortLoader.clearSortInfo();
        sortLoader.addSortInfo(bean);
        view.trackHorizontalPosition();
        sortLoader.load();
    }

    public PagingLoader<FilterPagingLoadConfig, PagingLoadResult<List<?>>> getSortLoader() {
        return sortLoader;
    }

    public void setSortLoader(PagingLoader<FilterPagingLoadConfig, PagingLoadResult<List<?>>> loader) {
        this.sortLoader = loader;
    }
    
    

    
}
