package csi.client.gwt.file_access.uploader;

import java.io.IOException;

import csi.server.common.util.uploader.BlockLoadedCallBack;
import csi.server.common.util.uploader.CacheLoadedCallBack;

/**
 * Created by centrifuge on 11/16/2015.
 */
public interface CsiClientRandomAccess extends CsiRandomAccess {

    public void refreshCache(long pointerIn, CacheLoadedCallBack handlerIn) throws IOException;

    public void doubleCache(CacheLoadedCallBack handlerIn) throws IOException;

    public void read(byte[] inputBuffer, BlockLoadedCallBack callbackIn) throws IOException;

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
