package csi.client.gwt.mapper.grid_model;

import com.sencha.gxt.core.client.ValueProvider;
import csi.client.gwt.mapper.data_model.SelectionPair;
import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 8/3/2017.
 */
public class CastToTypeProvider<T extends SelectionPair<?, ?>> implements ValueProvider<T, CsiDataType> {
    @Override
    public CsiDataType getValue(T t) {

        return t.getCastToType();
    }

    @Override
    public void setValue(T t, CsiDataType typeIn) {

        t.setCastToType(typeIn);
    }

    @Override
    public String getPath() {
        return null;
    }
}
