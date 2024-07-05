package csi.server.common.model;

/**
 * Created by centrifuge on 10/15/2014.
 */
public interface InPlaceUpdate<T> {

    public String getUuid();

    public void updateInPlace(T cloneIn);
}
