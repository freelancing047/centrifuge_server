package csi.client.gwt.file_access.uploader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;

import csi.client.gwt.file_access.uploader.zip.Inflater;
import csi.client.gwt.file_access.uploader.zip.ZipDirectoryCompleteCallBack;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.EOBException;
import csi.server.common.exception.EOFException;
import csi.server.common.exception.SignatureException;
import csi.server.common.exception.ZipException;
import csi.server.common.util.Format;
import csi.server.common.util.uploader.BlockLoadedCallBack;
import csi.server.common.util.uploader.CacheLoadedCallBack;
import csi.server.common.util.uploader.CsiRandomAccessConstants;
import csi.server.common.util.uploader.zip.CsiZipConstants;
import csi.server.common.util.uploader.zip.CsiZipEntry;

public class CsiClientZipScanner extends CsiClientInflaterStream implements CacheLoadedCallBack {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private long _maxBufferSize;
    private Map<String, CsiZipEntry> _directory;
    private CsiZipEntry _zipEntry = null;
    private ZipDirectoryCompleteCallBack _initialCallback = null;
    private boolean _loadEntireStringTable = true;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                       Callbacks                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public void onCacheLoaded(int countIn) {

        if (0 < countIn) {

            try {

                CsiZipEntry myEntry = getNextEntry();

                if (null != myEntry) {

                    String myName = myEntry.getName();

                    if ((null != myName) && (0 < myName.length())) {

                        _directory.put(myName, myEntry);
                    }
                }

            } catch(SignatureException myException) {

                long mySignature = myException.getSignature();

                if ((CsiZipConstants.CENSIG == mySignature) || (CsiZipConstants.ENDSIG == mySignature)) {

                    _initialCallback.onDirectoryComplete(_directory);

                } else {

                    _initialCallback.onDirectoryCompleteError(myException);
                }

            } catch(Exception myException) {

                if (myException instanceof EOBException) {

                    try {

                        restartNextEntry();

                    } catch(Exception myError) {

                        _initialCallback.onDirectoryCompleteError(myError);
                    }

                } else {

                    _initialCallback.onDirectoryCompleteError(myException);
                }
            }

        } else {

            _initialCallback.onDirectoryCompleteError(new ZipException(_constants.csiClientZipScanner_CantReadInput()));
        }
    }

    public void onCacheLoadError(Exception exceptionIn) {

        _initialCallback.onDirectoryCompleteError(exceptionIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiClientZipScanner(CsiClientRandomAccess inputIn, ZipDirectoryCompleteCallBack callbackIn, long maxSizeIn) {

        super(inputIn, new Inflater(true));

        _maxBufferSize = maxSizeIn;
        _initialCallback = callbackIn;
        _directory = new TreeMap<String, CsiZipEntry>();

        requestNextEntry(0L);
    }

    public Map<String, CsiZipEntry> getDirectory() {

        return _directory;
    }

    public void loadCompleteEntry(String entryPathIn, BlockLoadedCallBack callbackIn) throws Exception {

        if (openDirectoryEntry(entryPathIn)) {

            loadCompleteEntry(callbackIn);

        } else {

            callbackIn.onBlockLoadError(new CentrifugeException(_constants.csiClientZipScanner_BadZipEntry(Format.value(entryPathIn))));
        }
    }

    public void loadPartialEntry(String entryPathIn, long sizeIn, BlockLoadedCallBack callbackIn) throws Exception {

        if (openDirectoryEntry(entryPathIn)) {

            loadPartialEntry(sizeIn, callbackIn);

        } else {

            callbackIn.onBlockLoadError(new CentrifugeException(_constants.csiClientZipScanner_BadZipEntry(Format.value(entryPathIn))));
        }
    }

    public CsiZipEntry retrieveDirectoryEntry(String entryPathIn) throws IOException {

        CsiZipEntry myEntry = null;

        if ((null != entryPathIn) && (0 < entryPathIn.length())) {

            myEntry = _directory.get(entryPathIn);
        }
        return myEntry;
    }

    public boolean openDirectoryEntry(String entryPathIn) throws IOException {

        boolean mySuccess = false;

        closeEntry();

        if ((null != entryPathIn) && (0 < entryPathIn.length())) {

            _zipEntry = _directory.get(entryPathIn);

            if (null != _zipEntry) {

                input.seek(_zipEntry.getDataPointer());
                input.setEOD(_zipEntry.getDataPointer() + _zipEntry.getDeflatedSize());
                mySuccess = true;
            }
        }
        return mySuccess;
    }

    public void loadCompleteEntry(BlockLoadedCallBack callbackIn) throws Exception {

        if (_loadEntireStringTable) {

            super.read(new byte[(int)_zipEntry.getInflatedSize()], callbackIn);

        } else {

            loadPartialEntry(callbackIn);
        }
    }

    public void loadPartialEntry(long sizeIn, BlockLoadedCallBack callbackIn) throws Exception {

        byte[] myOutput = null;
        long myRequiredSize = Math.min(Math.min(_zipEntry.getInflatedSize(), _maxBufferSize), sizeIn);

        myOutput = new byte[(int)myRequiredSize];

        super.read(myOutput, callbackIn);
    }

    public void loadPartialEntry(BlockLoadedCallBack callbackIn) throws Exception {

        byte[] myOutput = null;
        long myRequiredSize = Math.min(_zipEntry.getInflatedSize(), _maxBufferSize);

        myOutput = new byte[(int)myRequiredSize];

        super.read(myOutput, callbackIn);
    }

    public void closeEntry()
    {
        if (_zipEntry == null) {
         return;
      }

        input.seekEOD();
        if (CsiZipConstants.DEFLATED == _zipEntry.getMethod()) {
         inflater.reset();
      }
        _zipEntry = null;
    }

    @Override
    public void read(byte[] blockIn, BlockLoadedCallBack callbackIn) throws IOException
    {
        switch (_zipEntry.getMethod())
        {
            case CsiZipConstants.DEFLATED:

                super.read(blockIn, callbackIn);
                break;

            case CsiZipConstants.STORED:

                input.read(blockIn, callbackIn);
                break;
        }
    }

    @Override
    public void close() throws IOException
    {
        super.close();
        _zipEntry = null;
        _directory = null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void requestNextEntry(long locationIn)
    {
        try {

            input.refreshCache(locationIn, this);

        } catch(Exception myException) {

            _initialCallback.onDirectoryCompleteError(myException);
        }
    }

    private int readByte() throws IOException {

        int myValue = input.readCache();

        switch (myValue) {

            case CsiRandomAccessConstants.READ_EOB:

                throw new EOBException();

            case CsiRandomAccessConstants.READ_EOF:

                throw new EOFException();

            default:

                return myValue;
        }
    }

    private int readLeShort() throws IOException {
        return readByte() | (readByte() << 8);
    }

    private int readLeInt() throws IOException {
        return readLeShort() | (readLeShort() << 16);
    }

    private void fillBuffer(byte[] bufferIn) throws IOException {

        if (null != bufferIn) {

            for (int i = 0; bufferIn.length > i; i++) {

                bufferIn[i] = (byte)(readByte() & 0xff);
            }
        }
    }

    private void restartNextEntry() throws IOException
    {
        input.doubleCache();
    }

    private CsiZipEntry getNextEntry() throws IOException
    {
        CsiZipEntry myEntry = new CsiZipEntry();

        myEntry.setBasePointer(input.getCacheBasePointer());

        int myFlags;
        long mySignature = (readLeInt()) & 0xFFFFFFFFL;

        // Check for acceptable entry type
        if (CsiZipConstants.LOCSIG != mySignature) {

            throw new SignatureException(mySignature);
        }

        myEntry.setVersion(readLeShort());

        // Reject streaming data
        myFlags = readLeShort();
        if (0 != (0x08 & myFlags)) {

            throw new ZipException(_constants.csiClientZipScanner_StreamNotSupported());
        }

        myEntry.setMethod(readLeShort());

        myEntry.setDateTime(readLeInt());
        myEntry.setCrc(readLeInt());
        myEntry.setDeflatedSize((readLeInt()) & 0xffffffffL);
        myEntry.setInflatedSize((readLeInt()) & 0xffffffffL);

        if ((CsiZipConstants.STORED == myEntry.getMethod()) && (myEntry.getDeflatedSize() != myEntry.getInflatedSize())) {

            throw new ZipException(_constants.csiClientZipScanner_BadInflatedDataSize());
        }

        int myNameLen = readLeShort();
        int myExtraLen = readLeShort();
        byte[] myNameBuffer = new byte[myNameLen];
        byte[] myExtra = (0 < myExtraLen) ? new byte[myExtraLen] : null;

        fillBuffer(myNameBuffer);

        // Currently not being used -- will be needed for ZIP64
        fillBuffer(myExtra);

        try
        {
            myEntry.setName(new String(myNameBuffer, "UTF-8"));
        }
        catch (UnsupportedEncodingException myException)
        {
            throw new AssertionError(myException);
        }
        myEntry.setDataPointer(input.getCacheOffsetLocation());

        requestNextEntry(myEntry.getDataPointer() + myEntry.getDeflatedSize());

        return myEntry;
    }
}
