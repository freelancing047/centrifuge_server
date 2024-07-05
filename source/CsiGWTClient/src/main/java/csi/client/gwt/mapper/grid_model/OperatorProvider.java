package csi.client.gwt.mapper.grid_model;

import com.sencha.gxt.core.client.ValueProvider;
import csi.client.gwt.mapper.data_model.SelectionPair;
import csi.server.common.enumerations.ComparingToken;

/**
 * Created by centrifuge on 8/3/2017.
 */
public class OperatorProvider<T extends SelectionPair<?, ?>> implements ValueProvider<T, ComparingToken> {
    @Override
    public ComparingToken getValue(T t) {

        return t.getComparingToken();
    }

    @Override
    public void setValue(T t, ComparingToken tokenIn) {

        t.setComparingToken(tokenIn);
    }

    @Override
    public String getPath() {
        return null;
    }
}
