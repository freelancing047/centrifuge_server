package csi.server.common.util;

import java.nio.charset.StandardCharsets;

/**
 * Created by centrifuge on 12/9/2015.
 */
public class CharacterBuffer  implements CsiSimpleBuffer {

    static final int _defaultSize = 4096;
    static final char[] _digitCharacter = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    char[] _buffer;
    int _offset;
    int _base;

    public CharacterBuffer(int sizeIn) {

        _buffer = new char[sizeIn];
    }

    public CharacterBuffer() {

        this(_defaultSize);
    }

    public byte[] copyBytes() {

        return (_base < _offset) ? new String(_buffer, _base, _offset).getBytes(StandardCharsets.UTF_8) : new byte[0];
    }

    public byte[] copyBytes(int baseIn) {

        int myBase = _base + baseIn;

        return (myBase < _offset) ? new String(_buffer, myBase, _offset).getBytes(StandardCharsets.UTF_8) : new byte[0];
    }

    public byte[] copyBytes(int baseIn, int limitIn) {

        int myBase = _base + baseIn;
        int myOffset = Math.min(_offset, (_base + limitIn));

        return (myBase < myOffset) ? new String(_buffer, myBase, myOffset).getBytes(StandardCharsets.UTF_8) : new byte[0];
    }

    public char[] getCharacters() {

        return _buffer;
    }

    public char[] copyCharacters() {

        char[] myBuffer = new char[length()];

        for (int i = 0, j = start(); length() > i; i++) {

            myBuffer[i] = _buffer[j++];
        }

        return myBuffer;
    }

    public int start() {

        return _base;
    }

    public int length() {

        return _offset - _base;
    }

    public CharacterBuffer append(String stringIn) {

        return append(stringIn.toCharArray());
    }

    public CsiSimpleBuffer appendElement(byte byteIn) {

        return append((char)(byteIn & EncodingByteValues.byteMask));
    }

    public CsiSimpleBuffer appendCharacterBytes(byte[] bufferIn) {

        guaranteeSpace(bufferIn.length);
        for (int i = 0; bufferIn.length > i; i++) {

            _buffer[_offset++] = (char)(bufferIn[i] & EncodingByteValues.byteMask);
        }
        return this;
    }

    public CsiSimpleBuffer appendCharacterBytes(byte[] bufferIn, int baseIn, int limitIn) {

        guaranteeSpace(limitIn - baseIn);
        for (int i = baseIn; limitIn > i; i++) {

            _buffer[_offset++] = (char)(bufferIn[i] & EncodingByteValues.byteMask);
        }
        return this;
    }

    public CharacterBuffer append(char characterIn) {

        guaranteeSpace(1);
        _buffer[_offset++] = characterIn;
        return this;
    }

    public CharacterBuffer append(char[] bufferIn) {

        guaranteeSpace(bufferIn.length);
        for (int i = 0; bufferIn.length > i; i++) {

            _buffer[_offset++] = bufferIn[i];
        }
        return this;
    }

    public CharacterBuffer append(char[] bufferIn, int baseIn, int limitIn) {

        guaranteeSpace(limitIn - baseIn);
        for (int i = baseIn; limitIn > i; i++) {

            _buffer[_offset++] = bufferIn[i];
        }
        return this;
    }

    public CharacterBuffer append(int valueIn, int placesIn) {

        int myValue = valueIn;

        stepOver(placesIn);

        for (int i = 1; placesIn >= i; i++) {

            _buffer[_offset - i] = _digitCharacter[myValue % 10];
            myValue /= 10;
        }
        return this;
    }

    public CharacterBuffer appendElement(Integer valueIn) {

        return (null != valueIn) ? append((char)valueIn.intValue()) : this;
    }

    public CharacterBuffer appendElement(int valueIn) {

        return append((char)valueIn);
    }

    public CharacterBuffer truncate(int offsetIn) {

        if (_offset < offsetIn) {

            guaranteeSpace(offsetIn);
            for (int i = _offset; offsetIn > i; i++) {

                _buffer[_offset] = EncodingByteValues.cpAsciiBlank;
            }
        }
        _offset = offsetIn;
        _base = Math.min(_base, _offset);
        return this;
    }

    public CharacterBuffer truncate() {

        return truncate(0);
    }

    public CharacterBuffer clip(int lengthIn) {

        _offset = Math.max(0, (_offset - lengthIn));

        return this;
    }

    public CharacterBuffer stripQuotes() {

        if (((_base + 2) < _offset) && (EncodingByteValues.cpAsciiQuote == _buffer[_base]) && (EncodingByteValues.cpAsciiQuote == _buffer[_offset - 1])) {

            _base++;
            _offset -= 2;
        }
        return this;
    }

    public CharacterBuffer shiftOut(int locationIn) {

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
            int myLimit = valueIn.length();
            int myStart = Math.max(startIn, _base);
            int myStop = Math.min(stopIn, _offset);

            if ((myStop - myStart) == myLimit) {

                for (i = 0; myLimit > i; i++) {

                    if (valueIn.charAt(i) != _buffer[myStart++]) {

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

        if (_base < _offset) {

            return new String(_buffer, _base, _offset);

        } else {

            return null;
        }
    }

    public String subString(int baseIn) {

        if ((_base + baseIn) < _offset) {

            return new String(_buffer, (_base + baseIn), _offset);

        } else {

            return null;
        }
    }

    public String subString(int baseIn, int limitIn) {

        int myOffset = Math.min(_offset, (_base + limitIn));

        if ((_base + baseIn) < myOffset) {

            return new String(_buffer, (_base + baseIn), myOffset);

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

            char[] myBuffer = new char[_buffer.length * 2];

            for (int i = 0; _offset > i; i++) {

                myBuffer[i] = _buffer[i];
            }
            _buffer = myBuffer;
        }
    }
}
