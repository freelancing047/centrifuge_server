package csi.client.gwt.mapper.grid_model;

import com.google.gwt.cell.client.Cell;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;

import csi.client.gwt.WebMain;
import csi.client.gwt.mapper.data_model.BasicDragItem;
import csi.client.gwt.widget.gxt.grid.paging.GridComponentManager;

import java.util.List;

/**
 * Created by centrifuge on 3/27/2016.
 */
public class ModelBuilder<T extends BasicDragItem> {

    ModelKeyProvider<T> _keyProvider;
    GridComponentManager<T> _manager;

    public ModelBuilder() {

        _keyProvider = new KeyProvider<T>();
        _manager = WebMain.injector.getGridFactory().create(_keyProvider);
    }

    public ColumnModel<T> genModel() {

        return new ColumnModel<T>(_manager.getColumnConfigList());
    }

    public ListStore<T> genStore() {

        return _manager.getStore();
    }

    public <S> ModelBuilder<T> addColumn(BuilderInfo<T, S> columnIn) {

        ColumnConfig<T, S> myConfig = _manager.create(columnIn.valueProvider, columnIn.width,
                columnIn.label, columnIn.sortable, columnIn.menuDisabled);

        if (null != columnIn.displayCell) {

            myConfig.setCell(columnIn.displayCell);
        }
        return this;
    }

    public Cell getDisplayCel(int columnIn) {

        Cell myCell = null;
        ColumnConfig<T, ?> myConfig = getColumnConfig(columnIn);

        if (null != myConfig) {

            myCell = myConfig.getCell();
        }
        return myCell;
    }

    public ColumnConfig<T, ?> getColumnConfig(int columnIn) {

        ColumnConfig<T, ?> myConfig = null;
        List<ColumnConfig<T, ?>> myList = _manager.getColumnConfigList();

        if ((0 <= columnIn) && (myList.size() > columnIn)) {

            myConfig = myList.get(columnIn);
        }
        return myConfig;
    }
}
