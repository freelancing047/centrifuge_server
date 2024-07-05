package csi.server.common.util;

import java.io.UnsupportedEncodingException;

import csi.server.common.enumerations.CsiEncoding;

/**
 * Created by centrifuge on 10/30/2015.
 */
public class ByteBuffer implements CsiSimpleBuffer {

    static final int _defaultSize = 4096;
    static final byte[] _digitByte = {0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39};

    CsiEncoding _encoding = null;
    byte[] _buffer;
    int _offset;
    int _base;

    public ByteBuffer(int sizeIn, CsiEncoding encodingIn) {

        _base = 0;
        _offset = 0;
        _buffer = new byte[sizeIn];
        _encoding = encodingIn;
    }

    public ByteBuffer(int sizeIn) {

        this(sizeIn, null);
    }

    public ByteBuffer(CsiEncoding encodingIn) {

        this(_defaultSize, encodingIn);
    }

    public ByteBuffer() {

        this(_defaultSize, null);
    }

    public byte[] getBytes() {

        return _buffer;
    }

    public byte[] copyBytes() {

        byte[] myBuffer = new byte[length()];

        for (int i = 0, j = start(); length() > i; i++) {

            myBuffer[i] = _buffer[j++];
        }

        return myBuffer;
    }

    public byte[] copyBytes(int baseIn) {

        int myBase = baseIn + start();
        int mySize = Math.max(0, (_offset - myBase));

        byte[] myBuffer = new byte[mySize];

        for (int i = 0, j = myBase; mySize > i; i++) {

            myBuffer[i] = _buffer[j++];
        }

        return myBuffer;
    }

    public byte[] copyBytes(int baseIn, int limitIn) {

        int myBase = baseIn + start();
        int myTop = Math.min(_offset, (limitIn + _base));
        int mySize = Math.max(0, (myTop - myBase));

        byte[] myBuffer = new byte[mySize];

        for (int i = 0, j = myBase; mySize > i; i++) {

            myBuffer[i] = _buffer[j++];
        }

        return myBuffer;
    }

    public char[] copyCharacters() {

        byte[] myBuffer = new byte[length()];

        for (int i = 0, j = start(); length() > i; i++) {

            myBuffer[i] = _buffer[j++];
        }

        return myBuffer.toString().toCharArray();
    }

    public int start() {

        return _base;
    }

    public int length() {

        return _offset - _base;
    }

    public ByteBuffer append(String stringIn) {

        return append(stringIn.getBytes());
    }

    public CsiSimpleBuffer appendElement(byte byteIn) {

        return append(byteIn);
    }

    public CsiSimpleBuffer appendCharacterBytes(byte[] bufferIn) {

        return append(bufferIn);
    }

    public CsiSimpleBuffer appendCharacterBytes(byte[] bufferIn, int baseIn, int limitIn) {

        return append(bufferIn, baseIn, limitIn);
    }

    public ByteBuffer append(byte byteIn) {

        guaranteeSpace(1);
        _buffer[_offset++] = byteIn;
        return this;
    }

    public ByteBuffer append(byte[] bufferIn) {

        guaranteeSpace(bufferIn.length);
        for (int i = 0; bufferIn.length > i; i++) {

            _buffer[_offset++] = bufferIn[i];
        }
        return this;
    }

    public ByteBuffer append(byte[] bufferIn, int baseIn, int limitIn) {

        guaranteeSpace(limitIn - baseIn);
        for (int i = baseIn; limitIn > i; i++) {

            _buffer[_offset++] = bufferIn[i];
        }
        return this;
    }

    public ByteBuffer append(int valueIn, int placesIn) {

        int myValue = valueIn;

        stepOver(placesIn);

        for (int i = 1; placesIn >= i; i++) {

            _buffer[_offset - i] = _digitByte[myValue % 10];
            myValue /= 10;
        }
        return this;
    }

    public ByteBuffer appendElement(Integer valueIn) {

        return (null != valueIn) ? append(valueIn.byteValue()) : this;
    }

    public ByteBuffer appendElement(int valueIn) {

        return append((byte)valueIn);
    }

    public ByteBuffer truncate(int offsetIn) {

        if (_offset < offsetIn) {

            guaranteeSpace(offsetIn);
            for (int i = _offset; offsetIn > i; i++) {

                _buffer[_offset] = EncodingByteValues.asciiBlank;
            }
        }
        _offset = offsetIn;
        _base = Math.min(_base, _offset);
        return this;
    }

    public ByteBuffer truncate() {

        return truncate(0);
    }

    public ByteBuffer clip(int lengthIn) {

        _offset = Math.max(0, (_offset - lengthIn));

        return this;
    }

    public ByteBuffer stripQuotes() {

        if (((_base + 2) < _offset) && (EncodingByteValues.asciiQuote == _buffer[_base]) && (EncodingByteValues.asciiQuote == _buffer[_offset - 1])) {

            _base++;
            _offset -= 2;
        }
        return this;
    }

    public ByteBuffer shiftOut(int locationIn) {

        if ((_base <= locationIn) && (_offset > locationIn)) {

            _offset--;

            for (int i = locationIn; _offset > i; i++) {

                _buffer[i] = _buffer[(i + 1)];
            }
        }
        return this;
    }

    public boolean equals(String valueIn, int startIn) {

        return equals(valueIn, startIn, _offset);
    }

    public boolean equals(String valueIn, int startIn, int stopIn) {

        if (null != valueIn) {

            int i = 0;
            byte[] myValue = valueIn.getBytes();
            int myLimit = myValue.length;
            int myStart = Math.max(startIn, _base);
            int myStop = Math.min(stopIn, _offset);

            if ((myStop - myStart) == myLimit) {

                for (i = 0; myLimit > i; i++) {

                    if (myValue[i] != _buffer[myStart++]) {

                        break;
                    }
                }
                return (i == myLimit);
            }
        }
        return false;
    }

    public boolean lastValueEquals(final Integer valueIn) {

        return (valueIn != null) && (valueIn.intValue() == _buffer[_offset - 1]);
    }

   @Override
    public String toString() {

       if (0 < _offset) {

           if (null != _encoding) {

               try {

                   return new String(_buffer, _base, _offset, _encoding.getJavaName());

               } catch(UnsupportedEncodingException myException) {

                   return new String(_buffer, _base, _offset);
               }
           }

           return new String(_buffer, _base, _offset);

       } else {

           return null;
       }
    }

    private void stepOver(int lengthIn) {

        guaranteeSpace(lengthIn);
        _offset += lengthIn;
    }

    private void guaranteeSpace(int lengthIn) {

        if (_buffer.length < (_offset + lengthIn)) {

            byte[] myBuffer = new byte[_buffer.length * 2];

            for (int i = 0; _offset > i; i++) {

                myBuffer[i] = _buffer[i];
            }
            _buffer = myBuffer;
        }
    }
}
