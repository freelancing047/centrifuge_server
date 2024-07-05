package csi.client.gwt.file_access.uploader;

import java.io.IOException;

/**
 * Created by centrifuge on 10/2/2015.
 */
public interface CsiRandomAccess {

    public long seek(long pointerIn) throws IOException;

    public int read(byte[] inputBuffer) throws IOException;

    public void close() throws IOException;

    public int getProgress();

    public void resetStream();

    public int refreshCache(long pointerIn) throws IOException;

    public int doubleCache() throws IOException;

    public int readCache() throws IOException;

    public long skip(long offsetIn) throws IOException;

    public long getByteLocation();

    public void setDataStart(long dataStartIn);

    public long getDataStart();

    public void clearDataStart();

    public void seekDataStart();

    public void setEOD(long endOfDataIn);

    public long getEOD();

    public void clearEOD();

    public long seekEOD();

    public void accessFileSection(long baseIn, long sizeIn) throws Exception;

    public long getCacheBasePointer();

    public long getCacheOffset();

    public long getCacheOffsetLocation();
}
