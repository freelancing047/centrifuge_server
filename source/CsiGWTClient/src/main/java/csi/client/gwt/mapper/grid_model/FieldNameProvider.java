package csi.client.gwt.mapper.grid_model;

import com.sencha.gxt.core.client.ValueProvider;
import csi.client.gwt.mapper.data_model.FieldDisplay;
import csi.client.gwt.mapper.data_model.SelectionPair;

/**
 * Created by centrifuge on 8/8/2017.
 */
public class FieldNameProvider<T extends SelectionPair<FieldDisplay, ?>> implements ValueProvider<T, String> {
    @Override
    public String getValue(T t) {

        return t.getLeftData().getName();
    }

    @Override
    public void setValue(T t, String nameIn) {

        t.getLeftData().setName(nameIn);
    }

    @Override
    public String getPath() {
        return "_leftItem._field.fieldName";
    }
}
