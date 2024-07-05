package csi.server.common.util.uploader;

import java.io.IOException;

/**
 * Created by centrifuge on 11/18/2015.
 */
public interface CsiSimpleInputStream {

    public int read(byte[] bufferIn) throws IOException;

    public int getProgress();
}
