package csi.server.common.interfaces;


/**
 * Created by centrifuge on 2/20/2015.
 */
public interface DisplayListBuilderCallbacks<T, S> {

    public void beginItem(T objectIn, S valueIn);
    public void addSegment(S valueIn);
    public void addValue(T itemIn, S valueIn);
    public void addEmptyValue(final Integer ordinalIn);
    public void endItem(T objectIn, S valueIn);
}
