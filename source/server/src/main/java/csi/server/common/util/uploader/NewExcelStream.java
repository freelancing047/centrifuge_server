package csi.server.common.util.uploader;

import csi.server.business.cachedb.CsiFormattedInputStream;
import csi.server.common.util.ByteBuffer;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 11/16/2015.
 */
public class NewExcelStream implements CsiFormattedInputStream {

    CsiInputStream _source;

    NewExcelStream(CsiInputStream sourceIn, ValuePair<Long, Long> dataSheetAccessIn) {

        _source = sourceIn;
    }

    public ByteBuffer getFirstRow() {
        return null;
    }

    public ByteBuffer getNextRow() {
        return null;
    }

    public CsiInputStream getSource() {

        return _source;
    }
}
