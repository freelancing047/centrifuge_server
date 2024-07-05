package csi.client.gwt.mapper.grid_model;

import com.sencha.gxt.core.client.ValueProvider;

import csi.client.gwt.mapper.data_model.SelectionPair;

/**
 * Created by centrifuge on 3/27/2016.
 */

public class RightGroupProvider<T extends SelectionPair<?, ?>> implements ValueProvider<T, String> {

    private final String _path;

    public RightGroupProvider() {

        this("");
    }

    public RightGroupProvider(String pathIn) {

        _path = pathIn;
    }

    @Override
    public String getValue(T objectIn) {

        String myGroup = (null != objectIn) ? objectIn.getRightGroupDisplayName() : "";

        return (null != myGroup) ? myGroup : "";
    }

    @Override
    public void setValue(T objectIn, String valueIn) { }

    @Override
    public String getPath() {

        return _path;
    }
}
