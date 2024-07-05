package csi.client.gwt.file_access.uploader;

import java.io.IOException;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.util.uploader.CsiRandomAccessConstants;

/**
 * Created by centrifuge on 11/17/2015.
 */
public abstract class CsiDirectFileAccess implements CsiRandomAccess {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private long _fileSize;
    private int _cacheBaseSize;
    private int _cacheSize;
    private int _cacheLastSize;

    private long _top;
    private long _base;
    private int _size;
    private int _offset;

    private long _dataStart;
    private long _endOfData;
    private long _pointer;
    private int _blockSize;

    protected abstract void loadCacheBlock(long pointerIn, int sizeIn);
    protected abstract void loadDataBlock(long pointerIn, int sizeIn, byte[] blockIn);
    protected abstract int getCacheByte(int offsetIn);


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiDirectFileAccess() {

    }

    public CsiDirectFileAccess(long fileSizeIn, int cacheSizeIn) {

        _fileSize = fileSizeIn;
        _cacheBaseSize = cacheSizeIn;
        resetStream();
    }

    protected void initialize(long fileSizeIn, int cacheSizeIn) {

        _fileSize = fileSizeIn;
        _cacheBaseSize = cacheSizeIn;
        resetStream();
    }

    public void resetStream() {

        _endOfData = _fileSize;
        _cacheSize = _cacheBaseSize;
        _cacheLastSize = 0;
        _size = 0;
        _offset = 0;
        _top = 0L;
        _base = 0L;
        _pointer = 0L;
        _blockSize = 0;
    }

    public int refreshCache(long pointerIn) throws IOException {

        if (_fileSize > pointerIn) {

            _base = pointerIn;
            _cacheLastSize = 0;
            clearAllCache();
            _cacheSize = _cacheBaseSize;
            loadCacheBlock(_base, (int)Math.min(_cacheSize, (_fileSize - _base)));

        } else {

            _base = _fileSize;
            _cacheLastSize = 0;
            clearAllCache();
            _cacheSize = _cacheBaseSize;
            recordCacheLoad(0);

            return CsiRandomAccessConstants.READ_EOF;
        }

        return _size;
    }

    public int doubleCache() throws IOException {

        if (_fileSize > _base) {

            _cacheLastSize = (int)((_top - _base) & 0x7FFFFFFF);
            clearAllCache();
            _cacheSize *= 2;
            loadCacheBlock(_base, (int)Math.min(_cacheSize, (_fileSize - _base)));

        } else {

            _base = _fileSize;
            _cacheLastSize = 0;
            clearAllCache();
            _cacheSize = _cacheBaseSize;
            recordCacheLoad(0);

            return CsiRandomAccessConstants.READ_EOF;
        }

        return _size;
    }

    public int read(byte[] inputBuffer) throws IOException {

        if (_endOfData > _pointer) {

            int mySize = inputBuffer.length;

            loadDataBlock(_pointer, (int) Math.min(mySize, (_fileSize - _pointer)), inputBuffer);

        } else {

            recordBlockRead(0);

            return (_fileSize > _endOfData) ? CsiRandomAccessConstants.READ_EOD : CsiRandomAccessConstants.READ_EOF;
        }

        return _blockSize;
    }

    public int readCache() throws IOException {

        int myValue = (_fileSize > _endOfData) ? CsiRandomAccessConstants.READ_EOD : CsiRandomAccessConstants.READ_EOF;

        if ((0 <= _offset) && (_size > _offset)) {

            myValue = getCacheByte(_offset++);

        } else if ((_fileSize > _top) && (_endOfData > _top)) {

            myValue = CsiRandomAccessConstants.READ_EOB;
        }
        return myValue;
    }

    public long skip(long offsetIn) throws IOException {

        long myStart = _pointer;

        if (0L < offsetIn) {

            _pointer = Math.min((_pointer + offsetIn), _endOfData);

        } else if (0L > offsetIn) {

            throw new IOException(_constants.csiDirectFileAccess_BadSkip());
        }

        return (_endOfData > _pointer) ? (_pointer - myStart) : (_fileSize > _endOfData) ? CsiRandomAccessConstants.SEEK_EOD : CsiRandomAccessConstants.SEEK_EOF;
    }

    public long getByteLocation() {

        return _pointer;
    }

    public long seek(long pointerIn) throws IOException {

        if (0L < pointerIn) {

            _pointer = Math.min(pointerIn, _endOfData);

        } else if (0L > pointerIn) {

            throw new IOException(_constants.csiDirectFileAccess_BadSeek());
        }

        return (_endOfData > _pointer) ? _pointer : (_fileSize > _endOfData) ? CsiRandomAccessConstants.SEEK_EOD : CsiRandomAccessConstants.SEEK_EOF;
    }

    public void setDataStart(long dataStartIn) {

        if (0 <= dataStartIn) {

            _dataStart = Math.min(_fileSize, dataStartIn);
        }
    }

    public long getDataStart() {

        return _dataStart;
    }

    public void clearDataStart() {

        _dataStart = 0L;
    }

    public void seekDataStart() {

        _pointer = _dataStart;
    }

    public void setEOD(long endOfDataIn) {

        if (0 <= endOfDataIn) {

            _endOfData = Math.min(_fileSize, endOfDataIn);
        }
    }

    public long getEOD() {

        return _endOfData;
    }

    public void clearEOD() {

        _endOfData = _fileSize;
    }

    public long seekEOD() {

        _pointer = _endOfData;
        _endOfData = _fileSize;

        return _pointer;
    }

    public void accessFileSection(long baseIn, long sizeIn) throws Exception {

        setDataStart(baseIn);
        setEOD(baseIn + sizeIn);
        seek(baseIn);
    }

    public long getCacheBasePointer() {

        return _base;
    }

    public long getCacheOffset() {

        return _offset;
    }

    public long getCacheOffsetLocation() {

        return _base + _offset;
    }

    public int getProgress() {

        long myProgress = 0;
        long mySize = _endOfData - _dataStart;

        if (0L < mySize) {

            myProgress = (Math.max(0, _pointer - _dataStart) * 100L) / mySize;
        }
        return (int)myProgress;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void clearAllCache() {

        _top = 0L;
    }

    protected void recordCacheLoad(int countIn) {

        _offset = 0;
        _size = Math.max(0, countIn);
        _top = _base + _size;
    }

    protected void recordBlockRead(int countIn) {

        _blockSize = Math.max(0, countIn);
        _pointer += _blockSize;
    }

    protected int getLastCacheSize() {

        return _cacheLastSize;
    }
}
