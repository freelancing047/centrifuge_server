package csi.server.business.cachedb;

import csi.server.common.util.ByteBuffer;
import csi.server.common.util.uploader.CsiInputStream;

/**
 * Created by centrifuge on 11/16/2015.
 */
public interface CsiFormattedInputStream {

    public ByteBuffer getFirstRow();
    public ByteBuffer getNextRow();
    public CsiInputStream getSource();
}
