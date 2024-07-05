package csi.client.gwt.mapper.grid_model;

import com.sencha.gxt.core.client.ValueProvider;

import csi.client.gwt.mapper.data_model.BasicDragItem;

/**
 * Created by centrifuge on 3/27/2016.
 */

public class ObjectProvider<T extends BasicDragItem> implements ValueProvider<T, T> {

    private final String _path;

    public ObjectProvider() {

        this("");
    }

    public ObjectProvider(String pathIn) {

        _path = pathIn;
    }

    @Override
    public T getValue(T objectIn) {

        return objectIn;
    }

    @Override
    public void setValue(T objectIn, T valueIn) { }

    @Override
    public String getPath() {

        return _path;
    }
}
