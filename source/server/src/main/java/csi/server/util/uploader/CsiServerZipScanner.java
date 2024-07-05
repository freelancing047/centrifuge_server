package csi.server.util.uploader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.Inflater;

import csi.server.common.exception.CentrifugeException;
import csi.server.common.exception.SignatureException;
import csi.server.common.exception.ZipException;
import csi.server.common.util.uploader.CsiSimpleInputStream;
import csi.server.common.util.uploader.zip.CsiZipConstants;
import csi.server.common.util.uploader.zip.CsiZipEntry;

/**
 * Created by centrifuge on 11/18/2015.
 */
public class CsiServerZipScanner extends CsiServerInflatorStream implements CsiSimpleInputStream {
   private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    CsiFileInputStream _input;
    private Inflater _inflater;
    private CsiZipEntry _zipEntry = null;

    public CsiServerZipScanner(CsiFileInputStream inputIn, Inflater inflaterIn) throws Exception {

        this(inputIn, inflaterIn, DEFAULT_BUFFER_SIZE);
    }

    public CsiServerZipScanner(CsiFileInputStream inputIn, Inflater inflaterIn, int bufferSizeIn) throws Exception {

        super(inputIn, inflaterIn, bufferSizeIn);

        _input = inputIn;
        _inflater = inflaterIn;
    }

    public CsiServerZipScanner openDirectoryEntry(CsiZipEntry zipEntryIn) throws Exception {

        closeEntry();

        _zipEntry = zipEntryIn;

        if (null != _zipEntry) {

            _input.accessSubStream(_zipEntry.getDataPointer(), _zipEntry.getDeflatedSize());

            return this;
        }
        throw new CentrifugeException("Request to open undefined zip file entry!");
    }

    public void closeEntry()
    {
        if (null != _zipEntry) {

            _inflater.reset();
        }
        _zipEntry = null;
    }

    @Override
    public int read(byte[] blockIn) throws IOException
    {
        switch (_zipEntry.getMethod())
        {
            case CsiZipConstants.DEFLATED:

                return super.read(blockIn);

            case CsiZipConstants.STORED:

                return _input.read(blockIn);
        }
        return -1;
    }

    public int getProgress() {

        return _input.getProgress();
    }

    private int readLeShort() throws IOException {
        return _input.read() | (_input.read() << 8);
    }

    private int readLeInt() throws IOException {
        return readLeShort() | (readLeShort() << 16);
    }

    private void fillBuffer(byte[] bufferIn) throws IOException {

        if (null != bufferIn) {

            for (int i = 0; bufferIn.length > i; i++) {

                bufferIn[i] = (byte)(_input.read() & 0xff);
            }
        }
    }

    private CsiZipEntry getEntryHeader(long offsetIn) throws IOException
    {
        CsiZipEntry myEntry = new CsiZipEntry();

        myEntry.setBasePointer(_input.seek(offsetIn).seek());

        int myFlags;
        long mySignature = readLeInt() & 0xFFFFFFFFL;

        // Check for acceptable entry type
        if (CsiZipConstants.LOCSIG != mySignature) {

            throw new SignatureException(mySignature);
        }

        myEntry.setVersion(readLeShort());

        // Reject streaming data
        myFlags = readLeShort();
        if (0 != (0x08 & myFlags)) {

            throw new ZipException("Streaming data not supported!");
        }

        myEntry.setMethod(readLeShort());

        myEntry.setDateTime(readLeInt());
        myEntry.setCrc(readLeInt());
        myEntry.setDeflatedSize(readLeInt() & 0xffffffffL);
        myEntry.setInflatedSize(readLeInt() & 0xffffffffL);

        if ((CsiZipConstants.STORED == myEntry.getMethod()) && (myEntry.getDeflatedSize() != myEntry.getInflatedSize())) {

            throw new ZipException("Size conflict for data which has not been compressed!");
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
        myEntry.setDataPointer(_input.seek());

        return myEntry;
    }
}
