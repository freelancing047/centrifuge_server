package csi.server.common.util.uploader;

import java.util.List;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.util.ByteBuffer;
import csi.server.common.util.EncodingByteValues;

/**
 * Created by centrifuge on 10/14/2015.
 */
public class XlsxColumn {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    CsiDataType _type;
    Integer _rowNumber;
    Integer _columnNumber;
    boolean _sharedString;
    Integer _style;
    byte[] _value;

    public XlsxColumn() {

    }

    public XlsxColumn(byte[] cellAddressIn, byte[] dataTypeIn, Integer styleIn, List<byte[]> valueBufferIn) {

        decodeCellAddress(cellAddressIn);
        _sharedString = false;
        _type = CsiDataType.Unsupported;
        if (null != dataTypeIn) {
            switch (dataTypeIn.length) {

                case 1:

                    if ('s' == dataTypeIn[0]) {

                        _type = CsiDataType.String;
                        _sharedString = true;

                    } else if ('b' == dataTypeIn[0]) {

                        _type = CsiDataType.Boolean;
                    }
                    break;

                case 3:

                    if (('s' == dataTypeIn[0]) && ('t' == dataTypeIn[1]) && ('r' == dataTypeIn[2])) {

                        _type = CsiDataType.String;
                    }
                    break;
            }
        }
        _style = styleIn;
        _value = ((valueBufferIn != null) && !valueBufferIn.isEmpty())
                ? valueBufferIn.get(0)
                : null;
    }

    public CsiDataType getType() {

        return _type;
    }

    public void setType(CsiDataType typeIn) {

        _type = typeIn;
    }

    public Integer getRowNumber() {

        return _rowNumber;
    }

    public void setRowNumber(Integer rowNumberIn) {

        _rowNumber = rowNumberIn;
    }

    public Integer getColumnNumber() {

        return _columnNumber;
    }

    public void setColumnNumber(Integer columnNumberIn) {

        _columnNumber = columnNumberIn;
    }

    public boolean isSharedString() {

        return _sharedString;
    }

    public void setSharedString(boolean sharedStringIn) {

        _sharedString = sharedStringIn;
    }

    public Integer getStyle() {

        return _style;
    }

    public void setStyle(Integer styleIn) {

        _style = styleIn;
    }

    public byte[] getValue() {

        return _value;
    }

    public void setValue(byte[] valueIn) {

        _value = valueIn;
    }

    public byte[] getValue(byte[] nullIndicatorIn) {

        return (null !=_value) ? _value : nullIndicatorIn;
    }

    public String getString(String nullIndicatorIn) {

        return (null !=_value) ? new String(_value) : nullIndicatorIn;
    }

    // Primary data source
    public byte[] getValue(StringLookup lookupIn) {

        return _sharedString ? lookupIn.getValue(_value) : _value;
    }

    public String getString() {

        return (null !=_value) ? new String(_value) : null;
    }

    public String getString(StringLookup lookupIn) {

        return (null !=_value) ? new String(getValue(lookupIn)) : null;
    }

    public String coerceValue(CsiDataType typeIn, byte[] nullIndicatorIn, StringLookup lookupIn) {

        return coerceValue(null, typeIn, nullIndicatorIn, lookupIn).toString();
    }

    public String coerceQuotedValue(CsiDataType typeIn, byte[] nullIndicatorIn, StringLookup lookupIn) {

        return coerceQuotedValue(null, typeIn, nullIndicatorIn, lookupIn).toString();
    }

    public ByteBuffer coerceValue(ByteBuffer bufferIn, CsiDataType typeIn, byte[] nullIndicatorIn, StringLookup lookupIn) {

        return coerceQuotedValue(bufferIn, typeIn, nullIndicatorIn, lookupIn).stripQuotes();
    }

    public ByteBuffer coerceQuotedValue(ByteBuffer bufferIn, CsiDataType typeIn, byte[] nullIndicatorIn, StringLookup lookupIn) {

        ByteBuffer myBuffer = (null != bufferIn) ? bufferIn : new ByteBuffer();
        byte[] myValue = getValue(lookupIn);

        if ((null != myValue) && (0 < myValue.length)) {

            if (_sharedString) {

                switch (typeIn) {

                    case Integer:

                        (new NumericValue(myValue)).formatInteger(myBuffer, nullIndicatorIn);
                        break;

                    case Number:

                        (new NumericValue(myValue)).formatFloat(myBuffer, nullIndicatorIn);
                        break;

                    case DateTime:
                    case Date:
                    case Time:
                    case String:

                        myBuffer.append(EncodingByteValues.asciiQuote).append(myValue).append(EncodingByteValues.asciiQuote);
                        break;

                    case Boolean:

                        myBuffer.append(extractBoolean(myValue, nullIndicatorIn));
                        break;

                    case Unsupported:
                    default:

                        myBuffer.append(nullIndicatorIn);
                        break;
                }

            } else {

                switch (typeIn) {

                    case Integer:

                        (new NumericValue(myValue)).formatInteger(myBuffer, nullIndicatorIn);
                        break;

                    case Number:

                        (new NumericValue(myValue)).formatFloat(myBuffer, nullIndicatorIn);
                        break;

                    case DateTime:

                        if (CsiDataType.Boolean == _type) {

                            myBuffer.append(nullIndicatorIn);

                        } else {

                            (new ExcelDate(myValue)).formatSqlDateTime(myBuffer, nullIndicatorIn);
                        }
                        break;

                    case Date:

                        if (CsiDataType.Boolean == _type) {

                            myBuffer.append(nullIndicatorIn);

                        } else {

                            (new ExcelDate(myValue)).formatSqlDate(myBuffer, nullIndicatorIn);
                        }
                        break;

                    case Time:

                        if (CsiDataType.Boolean == _type) {

                            myBuffer.append(nullIndicatorIn);

                        } else {

                            (new ExcelDate(myValue)).formatSqlTime(myBuffer, nullIndicatorIn);
                        }
                        break;

                    case String:
                    case Boolean:

                        if (CsiDataType.Boolean == _type) {

                            myValue = booleanSubstitution(myValue, nullIndicatorIn);
                        }
                        myBuffer.append(EncodingByteValues.asciiQuote).append(myValue).append(EncodingByteValues.asciiQuote);
                        break;

                    case Unsupported:
                    default:

                        myBuffer.append(nullIndicatorIn);
                        break;
                }

            }

        } else if (null != nullIndicatorIn) {

            myBuffer.append(nullIndicatorIn);
        }
        return myBuffer;
    }

    private void decodeCellAddress(byte[] bufferIn) {

        int myColumnIndex;
        int myRowIndex;
        int myRow = 0;
        int myColumn = 0;

        for (myColumnIndex = 0; bufferIn.length > myColumnIndex; myColumnIndex++) {

            byte myByte = bufferIn[myColumnIndex];

            if (('A' > myByte) || ('Z' < myByte)) {

                break;
            }
            myColumn = ((myColumn * 26) + 1 + bufferIn[myColumnIndex]) - 'A';
        }

        for (myRowIndex = myColumnIndex; bufferIn.length > myRowIndex; myRowIndex++) {

            byte myByte = bufferIn[myRowIndex];

            if (('0' > myByte) || ('9' < myByte)) {

                break;
            }
            myRow = ((myRow * 10) + bufferIn[myRowIndex]) - '0';
        }
        if ((0 < myColumnIndex) && (myColumnIndex < myRowIndex)) {

            _columnNumber = myColumn;
            _rowNumber = myRow;

        } else {

            _columnNumber = null;
            _rowNumber = null;
        }
    }

    private byte[] booleanSubstitution(byte[] valueIn, byte[] nullIndicatorIn) {

        if ((null != valueIn) && (0 < valueIn.length)) {

            if ((1 == valueIn.length) && (EncodingByteValues.asciiZero == valueIn[0])) {

                return NewExcelConstants._false;
            }
            return NewExcelConstants._true;
        }
        return nullIndicatorIn;
    }

    private byte[] extractBoolean(byte[] valueIn, byte[] nullIndicatorIn) {

        if ((null != valueIn) && (0 < valueIn.length)) {

            if (1 == valueIn.length) {

                if ((EncodingByteValues.asciiZero == valueIn[0]) || (EncodingByteValues.asciiCapsF == valueIn[0]) || (EncodingByteValues.asciiCapsN == valueIn[0])
                        || (EncodingByteValues.asciiLowerF == valueIn[0]) || (EncodingByteValues.asciiLowerN == valueIn[0])) {

                    return NewExcelConstants._false;

                } else if (((EncodingByteValues.asciiZero < valueIn[0]) && (EncodingByteValues.asciiNine >= valueIn[0])) || (EncodingByteValues.asciiCapsT == valueIn[0])
                        || (EncodingByteValues.asciiCapsY == valueIn[0]) || (EncodingByteValues.asciiLowerT == valueIn[0]) || (EncodingByteValues.asciiLowerY == valueIn[0])) {

                    return NewExcelConstants._true;
                }

            } else if ((EncodingByteValues.asciiZero < valueIn[0]) && (EncodingByteValues.asciiNine >= valueIn[0])) {

                return NewExcelConstants._true;

            } else if (((('F' == valueIn[0]) || ('f' == valueIn[0])) && (('A' == valueIn[0]) || ('a' == valueIn[0]))
                    && (('L' == valueIn[0]) || ('l' == valueIn[0])) && (('S' == valueIn[0]) || ('s' == valueIn[0]))
                    && (('E' == valueIn[0]) || ('e' == valueIn[0])))
                    || ((('N' == valueIn[0]) || ('n' == valueIn[0])) && (('O' == valueIn[0]) || ('o' == valueIn[0])))) {

                return NewExcelConstants._false;

            } else if (((('T' == valueIn[0]) || ('t' == valueIn[0])) && (('R' == valueIn[0]) || ('r' == valueIn[0]))
                    && (('U' == valueIn[0]) || ('u' == valueIn[0])) && (('E' == valueIn[0]) || ('e' == valueIn[0])))
                    || ((('Y' == valueIn[0]) || ('y' == valueIn[0])) && (('E' == valueIn[0]) || ('e' == valueIn[0]))
                    && (('S' == valueIn[0]) || ('s' == valueIn[0])) && (('S' == valueIn[0]) || ('s' == valueIn[0])))) {

                return NewExcelConstants._true;
            }
        }
        return nullIndicatorIn;
    }
}
