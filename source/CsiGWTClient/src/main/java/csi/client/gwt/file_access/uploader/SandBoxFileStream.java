
package csi.client.gwt.file_access.uploader;

import java.io.IOException;

import org.vectomatic.file.Blob;
import org.vectomatic.file.File;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;

import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.Int8Array;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.util.uploader.BlockLoadedCallBack;
import csi.server.common.util.uploader.CacheLoadedCallBack;
import csi.server.common.util.uploader.CsiRandomAccessConstants;

/**
 * Created by centrifuge on 8/4/2015.
 */
public class SandBoxFileStream extends CsiDirectFileAccess implements CsiClientRandomAccess {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private File _file;
    private FileReader _fileReader;

    private Int8Array _cache;
    private byte[] _returnBlock;

    private BlockLoadedCallBack _blockReadCallback = null;
    private CacheLoadedCallBack _cacheLoadCallback = null;

    private LoadEndHandler _onLoadComplete = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                       Callbacks                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private LoadEndHandler handleCacheLoadComplete
            = new LoadEndHandler() {

        @Override
        public void onLoadEnd(LoadEndEvent loadEndEvent) {

            if (null != _cacheLoadCallback) {

                try {

                    ArrayBuffer myDataBlock = (null != _fileReader) ? _fileReader.getArrayBufferResult() : null;

                    final int myLastSize = getLastCacheSize();

                    if ((null != myDataBlock) && (myLastSize < myDataBlock.byteLength())) {

                        _cache = ClientUtil.createInt8Array(myDataBlock);

                        recordCacheLoad(_cache.length());

                        _cacheLoadCallback.onCacheLoaded(_cache.length());

                    } else if (0 == myLastSize) {

                        _cacheLoadCallback.onCacheLoaded(0);

                    } else {

                        _cacheLoadCallback.onCacheLoadError(new IOException(
                                _constants.sandBoxFileStream_CacheExceedsMax(Integer.toString(myLastSize))));
                    }

                } catch (Exception myException) {

                    _cacheLoadCallback.onCacheLoadError(myException);
                }
            }
        }
    };

    private LoadEndHandler handleBlockLoadComplete
            = new LoadEndHandler() {

        @Override
        public void onLoadEnd(LoadEndEvent loadEndEvent) {

            if (null != _blockReadCallback) {

                try {

                    Int8Array myDataSource = null;
                    ArrayBuffer myDataBlock = (null != _fileReader) ? _fileReader.getArrayBufferResult() : null;

                    if ((null != myDataBlock) && (0 < myDataBlock.byteLength())) {

                        myDataSource = ClientUtil.createInt8Array(myDataBlock);
                    }

                    if (null != myDataSource) {

                        int mySize = 0;

                        if (null != _returnBlock) {

                            mySize = Math.min(_returnBlock.length, myDataSource.length());

                            for (int i = 0; mySize > i; i++) {

                                _returnBlock[i] = myDataSource.get(i);
                            }
                        }
                        recordBlockRead(mySize);

                        _blockReadCallback.onBlockLoaded(_returnBlock, mySize);

                    } else {

                        _blockReadCallback.onBlockLoadError(new IOException(_constants.sandBoxFileStream_LoadFailure()));
                    }

                } catch (Exception myException) {

                    _blockReadCallback.onBlockLoadError(myException);
                }
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SandBoxFileStream(File fileIn) {

        this(fileIn, 4096);
    }

    public SandBoxFileStream(File fileIn, int cacheSizeIn) {

        super(fileIn.getSize(), cacheSizeIn);

        _file = fileIn;
    }

    public void close() throws IOException {

        if (null != _file) {

            _file = null;
            _cache = null;
        }
    }

    @Override
    public void resetStream() {

        super.resetStream();

        _cache = null;
        _returnBlock = null;
    }

    public void refreshCache(long pointerIn, CacheLoadedCallBack handlerIn) throws IOException {

        _cacheLoadCallback = handlerIn;
        _onLoadComplete = handleCacheLoadComplete;

        refreshCache(pointerIn);
    }

    public void doubleCache(CacheLoadedCallBack handlerIn) throws IOException {

        _cacheLoadCallback = handlerIn;
        _onLoadComplete = handleCacheLoadComplete;

        doubleCache();
    }

    public void read(byte[] blockIn, BlockLoadedCallBack handlerIn) throws IOException {

        _blockReadCallback = handlerIn;
        _onLoadComplete = handleBlockLoadComplete;

        int myResponse = read(blockIn);

        if ((CsiRandomAccessConstants.READ_EOD == myResponse) || (CsiRandomAccessConstants.READ_EOF == myResponse)) {

            _fileReader = null;
            _onLoadComplete.onLoadEnd(null);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void clearAllCache() {

        super.clearAllCache();

        _cache = null;
        _returnBlock = null;
    }

    protected int getCacheByte(int offsetIn) {

        return (_cache.get(offsetIn)) & 0xFF;
    }

    protected void loadCacheBlock(long pointerIn, int sizeIn) {

        readBlock(null, pointerIn, sizeIn);
    }

    @Override
    protected void loadDataBlock(long pointerIn, int sizeIn, byte[] blockIn) {

        readBlock(blockIn, pointerIn, sizeIn);
    }

    protected void readBlock(byte[] blockIn, long pointerIn, int countIn) {

        long myLimit = _file.getSize() - pointerIn;
        int myRequest = (int)Math.min(((null != blockIn) ? blockIn.length : countIn), myLimit);

//        int myRequest = (null != blockIn) ? blockIn.length : countIn;
        Blob myBlock = _file.slice(pointerIn, myRequest + pointerIn);

        _returnBlock = blockIn;

        _fileReader = new FileReader();
        _fileReader.addLoadEndHandler(_onLoadComplete);
        _fileReader.readAsArrayBuffer(myBlock);
    }
}