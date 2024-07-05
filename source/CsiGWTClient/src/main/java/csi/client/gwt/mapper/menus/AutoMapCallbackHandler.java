package csi.client.gwt.mapper.menus;

import java.util.List;

import csi.client.gwt.mapper.data_model.SelectionDataAccess;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 4/1/2016.
 */
public abstract class AutoMapCallbackHandler<T1 extends SelectionDataAccess<?>, T2 extends SelectionDataAccess<?>> {

    public abstract void onMenuSelectionProcessed(List<ValuePair<T1, T2>> listIn);
}
