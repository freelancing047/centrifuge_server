package csi.client.gwt.etc;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.GridView;

import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.input_boxes.ValidityCheckCapable;

/**
 * Created by centrifuge on 8/17/2015.
 */
public class ValidatingGrid<T> extends ResizeableGrid<T> implements ValidityCheckCapable {

    boolean _isRequired = true;

    public ValidatingGrid(ListStore<T> listStoreIn, ColumnModel<T> columnModelIn, GridView<T> viewIn) {

        super(listStoreIn, columnModelIn, viewIn);
    }

    public ValidatingGrid(ListStore<T> listStoreIn, ColumnModel<T> columnModelIn) {

        super(listStoreIn, columnModelIn);
    }

    public ValidatingGrid(ListStore<T> listStoreIn, ColumnModel<T> columnModelIn, boolean showBorderIn) {

        super(listStoreIn, columnModelIn, showBorderIn);
    }

    @Override
    public boolean isValid() {

        return true;
    }

    @Override
    public boolean isRequired() {

        return _isRequired;
    }

    @Override
    public boolean isConditionallyValid() {

        return true;
    }

    @Override
    public void setRequired(boolean isRequiredIn) {

        _isRequired = isRequiredIn;
    }

    public void setScrollOn() {

        getView().setAdjustForHScroll(true);
    }
}
