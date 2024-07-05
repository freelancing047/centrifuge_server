package csi.server.util.uploader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.CsiSimpleBuffer;
import csi.server.common.util.EncodingByteValues;
import csi.server.common.util.uploader.CsiBufferedStream;
import csi.server.common.util.uploader.CsiInputStream;

/**
 * Created by centrifuge on 9/25/2015.
 */
public class CsiFileInputStream extends InputStream implements CsiBufferedStream, CsiInputStream {
   private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private FileChannel _file = null;
    private ByteBuffer _cache = null;
    private long _byteLocation = 0L;
    private long _size = 0L;
    private long _EOD = 0L;
    private long _BOD = 0L;
    private long _cacheBase = 0L;
    private long _cacheTop = 0L;
    private int _cacheSize = 0;
    private Integer _currentValue = null;
    private boolean _fixNulls = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiFileInputStream(Path pathIn, int cacheSizeIn, boolean fixNullsIn) throws IOException {

        _file = FileChannel.open(pathIn, StandardOpenOption.READ);
        _cacheSize = cacheSizeIn;
        _size = _file.size();
        _EOD = _size;
        _BOD = 0L;
        _byteLocation = 0L;
        _currentValue = null;
        _fixNulls = fixNullsIn;
    }

    public CsiFileInputStream(Path pathIn, int cacheSizeIn) throws IOException {

        this(pathIn, cacheSizeIn, false);
    }

    public CsiFileInputStream(Path pathIn) throws IOException {

        this(pathIn, DEFAULT_BUFFER_SIZE, false);
    }

    public CsiFileInputStream(Path pathIn, boolean fixNullsIn) throws IOException {

        this(pathIn, DEFAULT_BUFFER_SIZE, fixNullsIn);
    }

    public int getProgress() {

        return (0 < _EOD) ? (int)Math.min(100L, ((100L * (_byteLocation - _BOD)) / _EOD)) : 0;
    }

    public void skipLines(final long lineCountIn) throws IOException {

        _currentValue = null;
        if (0L < lineCountIn) {

            long myLineCount = 0L;

            for (int myValue = read(); EncodingByteValues.cpEOS != myValue; myValue = read()) {

                if (EncodingByteValues.cpAsciiNewLine == myValue) {

                    myLineCount++;
                }
                if (lineCountIn <= myLineCount) {

                    break;
                }
            }
        }
    }

    public long seek() throws IOException {

        return _byteLocation;
    }

    public CsiFileInputStream seek(long offsetIn) throws IOException {

        long myRequest = Math.min(offsetIn, _size);

        _currentValue = null;
        _byteLocation = _file.position(myRequest).position();

        return this;
    }

    public void accessSubStream(final long offsetIn, long sizeIn) throws IOException {

        _currentValue = null;
        if (offsetIn < _size) {

            _byteLocation = _file.position(offsetIn).position();

            _BOD = _byteLocation;
            _EOD = Math.min((_byteLocation + sizeIn), _size);

        } else {

            throw new IOException("Attempting to access substream beyond stream limit!");
        }
     }

    public boolean fixNulls() {

        return _fixNulls;
    }

    @Override
    public int read() throws IOException {

        int myValue = EncodingByteValues.cpEOS;

        _currentValue = null;
        if (_EOD > _byteLocation) {

            if ((null == _cache) || (_cacheTop <= _byteLocation)) {

                cacheNextBlock();
            }
            if ((null != _cache) && (_cacheTop > _byteLocation)) {

                myValue = (_cache.get((int)(_byteLocation - _cacheBase))) & 0xff;
            }
            if (EncodingByteValues.cpEOS != myValue) {

                _byteLocation++;
            }
        }

        return myValue;
    }

    @Override
    public int read(byte[] bufferIn) throws IOException {

        int myValue = EncodingByteValues.cpEOS;

        _currentValue = null;
        _byteLocation = _file.position(_byteLocation).position();
        _cacheTop = 0L;

        if (_EOD > _byteLocation) {

            int myRequest = (int)Math.min(bufferIn.length, (_EOD - _byteLocation));

            if (0 < myRequest) {

                ByteBuffer myBuffer = null;

                if (bufferIn.length == myRequest) {

                    myBuffer = ByteBuffer.wrap(bufferIn);

                } else {

                    myBuffer = ByteBuffer.wrap(bufferIn, 0, myRequest);
                }
                myValue = _file.read(myBuffer);

            } else {

                myValue = 0;
            }
        }
        _byteLocation = _file.position();

        return myValue;
    }

    @Override
    public int read(byte[] bufferIn, int offsetIn, int lengthIn) throws IOException {

        int myValue = EncodingByteValues.cpEOS;

        _currentValue = null;
        _byteLocation = _file.position(_byteLocation).position();
        _cacheTop = 0L;

        if (_EOD > _byteLocation) {

            int myRequest = (int)Math.min(lengthIn, (_EOD - _byteLocation));

            if (0 < myRequest) {

                ByteBuffer myBuffer = ByteBuffer.wrap(bufferIn, offsetIn, myRequest);

                myValue = _file.read(myBuffer);

            } else {

                myValue = 0;
            }
        }
        _byteLocation = _file.position();

        return myValue;
    }

    @Override
    public long skip(long requestIn) throws IOException {

        _currentValue = null;
        long myLocation = _file.position(_byteLocation).position();

        if ((_EOD > _byteLocation) && (0 < requestIn)) {

            long myRequest = Math.min(requestIn, (_EOD - _byteLocation));

            _file.position(myRequest + _byteLocation);
        }
        _byteLocation = _file.position();

        return _byteLocation - myLocation;
    }

    @Override
    public int available() {

        return (int)Math.max(0L, (_EOD - _byteLocation));
    }

    @Override
    public boolean markSupported() {

        return false;
    }

    @Override
    public void mark(int limitIn) {

    }

    @Override
    public void reset() throws IOException  {

    }

    @Override
    public void close() throws IOException {

        _file.close();
    }

    public void putValue(int valueIn) {

        _currentValue = valueIn;
    }

    @Override
    public CsiSimpleBuffer createBuffer() {

        return new csi.server.common.util.ByteBuffer();
    }

    public int getValue() {

        int myValue = EncodingByteValues.cpEOS;

        if ((null == _currentValue) || (EncodingByteValues.cpEOS != _currentValue)) {

            if (null != _currentValue) {

                myValue = _currentValue;
                _currentValue = null;

            } else {

                try {

                    if (_fixNulls) {

                        myValue = EncodingByteValues.cpAsciiNull;

                        while (EncodingByteValues.cpAsciiNull == myValue) {

                            myValue = read();
                        }

                    } else {

                        myValue = read();
                        if (EncodingByteValues.cpAsciiNull == myValue) {

                            throw new CentrifugeException("Encountered a null character in string data.")
;                        }
                    }

                } catch (Exception myException) {

                    myValue = EncodingByteValues.cpEOS;
                }
            }
        }
        if (EncodingByteValues.cpEOS == myValue) {

            _currentValue = EncodingByteValues.cpEOS;
        }
        return myValue;
    }

    private void cacheNextBlock() throws IOException {

        _cacheBase = _file.position(_byteLocation).position();

        int myRequest = (int)Math.min(_cacheSize, (_EOD - _cacheBase));

        _cache = ByteBuffer.allocate(myRequest);
        _file.read(_cache);
        _cacheTop = _file.position();
    }
}
