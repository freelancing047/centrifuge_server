package csi.client.gwt.mapper.data_model;

import csi.server.common.model.CsiUUID;

/**
 * Created by centrifuge on 4/1/2016.
 */
public class EmptyDragItem extends BasicDragItem {

    private String _key;

    public EmptyDragItem(String idIn) {

        super(idIn);

        _key = CsiUUID.randomUUID();
    }

    public String getKey() {

        return _key;
    }
}
