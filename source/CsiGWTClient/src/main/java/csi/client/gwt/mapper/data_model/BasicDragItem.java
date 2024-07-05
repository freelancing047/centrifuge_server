package csi.client.gwt.mapper.data_model;

/**
 * Created by centrifuge on 3/28/2016.
 */
public abstract class BasicDragItem {

    public abstract String getKey();

    private String _gridId;

    public BasicDragItem(String idIn) {

        _gridId = idIn;
    }

    public String getGridId() {

        return _gridId;
    }
}
