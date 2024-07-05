package csi.server.common.util.uploader;

import java.io.IOException;

/**
 * Created by centrifuge on 10/30/2015.
 */
public interface CsiInputStream extends CsiSimpleInputStream {

    public int read() throws IOException;

    public int read(byte[] bufferIn, int offsetIn, int lengthIn) throws IOException;

    public void skipLines(final long lineCountIn) throws IOException;
}
