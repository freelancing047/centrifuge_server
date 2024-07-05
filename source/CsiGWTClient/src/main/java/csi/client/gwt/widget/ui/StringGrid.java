package csi.client.gwt.widget.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

/**
 * Created by centrifuge on 11/3/2015.
 */
public class StringGrid implements IsWidget {

    private class ColumnInfo implements ValueProvider<String[], String> {

        int _columnIndex;

        public ColumnInfo(int columnIndexIn) {

            _columnIndex = columnIndexIn;
        }

        @Override
        public String getValue(String[] arrayIn) {

            return (arrayIn.length > _columnIndex) ? arrayIn[_columnIndex] : "";
        }

        @Override
        public void setValue(String[] arrayIn, String valueIn) {

            if (arrayIn.length > _columnIndex) {

                arrayIn[_columnIndex] = valueIn;
            }
        }

        @Override
        public String getPath() {
            return null;
        }
    }

    private class RowKeyProvider implements ModelKeyProvider<String[]> {


        @Override
        public String getKey(String[] itemIn) {

            return itemIn[0];
        }
    }

    private List<ColumnConfig<String[], ?>> _columnConfig;
    private ColumnModel<String[]> _columnModel;
    private ListStore<String[]> _dataStore;
    private Grid<String[]> _grid;

    public StringGrid(String[] columnHeadersIn, int[] widthIn) {

        int myMinWidth = 0;

        _columnConfig = new ArrayList<ColumnConfig<String[], ?>>(columnHeadersIn.length);
        _dataStore = new ListStore<String[]>(new RowKeyProvider());

        for (int i = 0; columnHeadersIn.length > i; i++) {

            int myColumnWidth = widthIn[Math.min((widthIn.length - 1), i)];

            _columnConfig.add(new ColumnConfig<String[], String>(new ColumnInfo(i), myColumnWidth, columnHeadersIn[i]));
            myMinWidth += myColumnWidth;
        }
        _columnModel = new ColumnModel<String[]>(_columnConfig);
//        _grid = new ResizeableGrid<String[]>(_dataStore, _columnModel, myMinWidth);
        _grid = new Grid<String[]>(_dataStore, _columnModel);
        _grid.getView().setAutoFill(false);
        _grid.getView().setForceFit(false);
        _grid.getView().setSortingEnabled(false);
        _grid.getView().setAdjustForHScroll(true);
        _grid.setWidth(myMinWidth);
    }

    public StringGrid(List<String[]> dataIn, String[] columnHeadersIn, int[] widthIn) {

        this(columnHeadersIn, widthIn);

        _grid.getStore().addAll(dataIn);
    }

    public StringGrid(List<String[]> dataIn, String[] columnHeadersIn, int[] widthIn, int heightIn) {

        this(columnHeadersIn, widthIn);

        _grid.getStore().addAll(dataIn);
        _grid.setHeight(heightIn);
    }

    public ListStore<String[]> getStore() {

        return _grid.getStore();
    }

    @Override
    public Widget asWidget() {

        return _grid;
    }
}
