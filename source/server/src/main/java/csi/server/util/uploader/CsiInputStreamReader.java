package csi.server.util.uploader;

import java.io.IOException;
import java.io.InputStreamReader;

import csi.server.common.enumerations.CsiEncoding;
import csi.server.common.util.CharacterBuffer;
import csi.server.common.util.CsiSimpleBuffer;
import csi.server.common.util.EncodingByteValues;
import csi.server.common.util.uploader.CsiBufferedStream;
import csi.server.common.util.uploader.CsiInputStream;

/**
 * Created by centrifuge on 12/2/2015.
 */
public class CsiInputStreamReader extends InputStreamReader implements CsiInputStream, CsiBufferedStream {
    CsiFileInputStream _stream;
    private Integer _currentValue = null;

    public CsiInputStreamReader(CsiFileInputStream streamIn, CsiEncoding encodingIn) throws IOException {

        super(streamIn, encodingIn.getJavaName());

        _stream = streamIn;
        _currentValue = null;
    }

    @Override
    public int read(byte[] bufferIn, int offsetIn, int lengthIn) throws IOException {

        _currentValue = null;
        return _stream.read(bufferIn, offsetIn, lengthIn);
    }

    @Override
    public void skipLines(long lineCountIn) throws IOException {

        _currentValue = null;
        _stream.skipLines(lineCountIn);
    }

    @Override
    public int read(byte[] bufferIn) throws IOException {

        _currentValue = null;
        return _stream.read(bufferIn);
    }

    @Override
    public int getProgress() {

        return _stream.getProgress();
    }

    public void putValue(int valueIn) {

        _currentValue = valueIn;
    }

    @Override
    public CsiSimpleBuffer createBuffer() {

        return new CharacterBuffer();
    }

    public CsiSimpleBuffer createBuffer(int sizeIn) {

        return new CharacterBuffer(sizeIn);
    }

    public int getValue() {

        int myValue = EncodingByteValues.cpEOS;

        if ((null == _currentValue) || (EncodingByteValues.cpEOS != _currentValue)) {

            if (null != _currentValue) {

                myValue = _currentValue;
                _currentValue = null;

            } else {

                try {

                    myValue = read();

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
}
