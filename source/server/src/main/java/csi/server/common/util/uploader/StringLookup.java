package csi.server.common.util.uploader;

import java.util.List;

/**
 * Created by centrifuge on 11/11/2015.
 */
public class StringLookup {

    List<byte[]> _stringMap = null;

    public StringLookup() {

    }

    public StringLookup(List<byte[]> stringMapIn) {

        _stringMap = stringMapIn;
        decodeMap();
    }

    public void setStringMap(List<byte[]> stringMapIn) {

        _stringMap = stringMapIn;
        decodeMap();
    }

    public List<byte[]> getStringMap() {

        return _stringMap;
    }

    public byte[] getValue(int indexIn) {

        byte[] myValue = null;

        if ((0 <= indexIn) && (_stringMap.size() > indexIn)) {

            myValue = _stringMap.get(indexIn);
        }
        return myValue;
    }

    public byte[] getValue(String indexIn) {

        return getValue(getIndex(indexIn));
    }

    public byte[] getValue(byte[] indexIn) {

        return getValue(getIndex(indexIn));
    }

    public String getString(int indexIn) {

        String myString = null;

        if ((0 <= indexIn) && (_stringMap.size() > indexIn)) {

            myString = convertValue(_stringMap.get(indexIn));
        }
        return myString;
    }

    public String getString(String indexIn) {

        return getString(getIndex(indexIn));
    }

    public String getString(byte[] indexIn) {

        return getString(getIndex(indexIn));
    }

    private String convertValue(byte[] valueIn) {

        return (null != valueIn) ? new String(valueIn) : null;
    }

    private int getIndex(byte[] valueIn) {

        int myIndex = 0;

        for (int i = 0; valueIn.length > i; i++) {

            int myDigit = valueIn[i] - (int)'0';

            if ((0 > myDigit) || (9 < myDigit)) {

                break;
            }
            myIndex = (10 * myIndex) + myDigit;
        }

        return myIndex;
    }

    private int getIndex(String valueIn) {

        return getIndex(valueIn.getBytes());
    }

    private void decodeMap() {

        for (int i = 0; _stringMap.size() > i; i++) {

            htmlDecode(i);
        }
    }

    private void htmlDecode(int indexIn) {

        byte[] myValue = _stringMap.get(indexIn);

        for (int i = 0; myValue.length > i; i++) {

            if ('\0' != getSpecialCharacter(myValue, i)) {

                _stringMap.set(indexIn, processValue(myValue, i));
                break;
            }
        }
    }

    private byte[] processValue(byte[] valueIn, int offsetIn) {

        byte[] myBuffer = null;
        int myLimit = valueIn.length;
        int mySource = offsetIn;
        int myTarget = offsetIn;

        while (myLimit > mySource) {

            switch (getSpecialCharacter(valueIn, mySource)) {

                case '<' :

                    valueIn[myTarget++] = '<';
                    mySource += 4;
                    break;

                case '>' :

                    valueIn[myTarget++] = '>';
                    mySource += 4;
                    break;

                case '&' :

                    valueIn[myTarget++] = '&';
                    mySource += 5;
                    break;

                case '\r' :

                    valueIn[myTarget++] = '\r';
                    mySource += 7;
                    if ((myLimit > mySource) && ('\r' == valueIn[mySource])) {

                        mySource++;
                    }
                    break;

                default :

                    valueIn[myTarget++] = valueIn[mySource++];
                    break;
            }
        }
        myBuffer = new byte[myTarget];
        for (int i = 0; myTarget > i; i++) {

            myBuffer[i] = valueIn[i];
        }
        return myBuffer;
    }

    private char getCarriageReturn(byte[] valueIn, int offsetIn) {

        return (((valueIn.length - 7) >= offsetIn)
                && ('_' == valueIn[offsetIn])
                && ('x' == valueIn[offsetIn + 1])
                && ('0' == valueIn[offsetIn + 2])
                && ('0' == valueIn[offsetIn + 3])
                && ('0' == valueIn[offsetIn + 4])
                && ('D' == valueIn[offsetIn + 5])
                && ('_' == valueIn[offsetIn + 6])) ? '\r' : '\0';
    }

    private char getSpecialCharacter(byte[] valueIn, int offsetIn) {

        if ('&' == valueIn[offsetIn]) {

            if (((valueIn.length - 4) >= offsetIn)
                    && ('t' == valueIn[offsetIn + 2]) && (';' == valueIn[offsetIn + 3])) {

                if ('l' == valueIn[offsetIn + 1]) {

                    return '<';

                } else if ('g' == valueIn[offsetIn + 1]) {

                    return '>';
                }

            } else if (('&' == valueIn[offsetIn]) && ((valueIn.length - 5) >= offsetIn)
                    && ('a' == valueIn[offsetIn + 1]) && ('m' == valueIn[offsetIn + 2])
                    && ('p' == valueIn[offsetIn + 3]) && (';' == valueIn[offsetIn + 4])) {

                return '&';
            }
        }
        return getCarriageReturn(valueIn, offsetIn);
    }
}
