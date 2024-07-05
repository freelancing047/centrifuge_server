package csi.server.common.util.uploader;

import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.CsiSimpleBuffer;
import csi.server.common.util.EncodingByteValues;

/**
 * Created by centrifuge on 12/2/2015.
 */
public interface CsiBufferedStream {

    public int getValue();
    public void putValue(int valueIn);
    public CsiSimpleBuffer createBuffer();
}
