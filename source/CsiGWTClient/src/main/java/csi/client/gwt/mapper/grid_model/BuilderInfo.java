package csi.client.gwt.mapper.grid_model;

import com.google.gwt.cell.client.Cell;
import com.sencha.gxt.core.client.ValueProvider;

/**
 * Created by centrifuge on 3/27/2016.
 */
public class BuilderInfo<T, S> {

    ValueProvider<T, S> valueProvider;
    int width;
    String label;
    boolean sortable;
    boolean menuDisabled;
    Cell displayCell;

    public BuilderInfo(ValueProvider<T, S> valueProviderIn, int widthIn, String labelIn, boolean sortableIn, boolean menuDisabledIn, Cell displayCellIn) {

        valueProvider = valueProviderIn;
        width = widthIn;
        label = labelIn;
        sortable = sortableIn;
        menuDisabled = menuDisabledIn;
        displayCell = displayCellIn;
    }
}
