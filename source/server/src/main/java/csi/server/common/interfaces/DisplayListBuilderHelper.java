package csi.server.common.interfaces;

import java.util.Map;

import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 3/8/2015.
 */
public interface DisplayListBuilderHelper<T, S> {

    public void buildDisplay(SqlTokenValueCallback valueCallbackIn, DisplayListBuilderCallbacks<T, S> builderIn, Map<T, S> mapIn) throws CentrifugeException;
    public void replaceDisplay(SqlTokenValueCallback valueCallbackIn, DisplayListBuilderCallbacks<T, S> builderIn, Map<T, S> mapIn, T objectIn) throws CentrifugeException;
}
