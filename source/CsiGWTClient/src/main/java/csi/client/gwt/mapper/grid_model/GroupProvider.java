package csi.client.gwt.mapper.grid_model;

import com.sencha.gxt.core.client.ValueProvider;

import csi.client.gwt.mapper.data_model.SelectionDataAccess;

/**
 * Created by centrifuge on 3/27/2016.
 */

public class GroupProvider<T extends SelectionDataAccess<?>> implements ValueProvider<T, String> {

    private final String _path;

    public GroupProvider() {

        this("");
    }

    public GroupProvider(String pathIn) {

        _path = pathIn;
    }

    @Override
    public String getValue(T objectIn) {

        String myGroup = (null != objectIn) ? objectIn.getGroupDisplayName() : "";

        return (null != myGroup) ? myGroup : "";
    }

    @Override
    public void setValue(T objectIn, String valueIn) { }

    @Override
    public String getPath() {

        return _path;
    }
}
