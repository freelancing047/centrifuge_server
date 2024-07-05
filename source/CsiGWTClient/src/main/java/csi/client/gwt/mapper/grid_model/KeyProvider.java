package csi.client.gwt.mapper.grid_model;

import com.sencha.gxt.data.shared.ModelKeyProvider;

import csi.client.gwt.mapper.data_model.BasicDragItem;

/**
 * Created by centrifuge on 3/27/2016.
 */
class KeyProvider<T extends BasicDragItem> implements ModelKeyProvider<T> {

    public String getKey(T objectIn) {

        return objectIn.getKey();
    }
}
