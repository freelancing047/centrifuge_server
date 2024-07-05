package csi.server.util.uploader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.dto.installed_tables.ColumnParameters;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.util.CsiSimpleBuffer;
import csi.server.common.util.EncodingByteValues;
import csi.server.common.util.uploader.CsiBufferedStream;
import csi.server.common.util.uploader.NumericValue;

/**
 * Created by centrifuge on 11/29/2015.
 */
public class CsvReader {
   private static final Logger LOG = LogManager.getLogger(XlsxReader.class);

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    CsiBufferedStream _stream;
    Integer _delimiter = Integer.valueOf(EncodingByteValues.cpAsciiComma);
    Integer _quote = null;
    Integer _escape = null;
    String _nullIndicator = null;
    byte[] _nullIndicatorBytes = null;

    private CsiDataType[] _dataType;
    private boolean[] _include;
    private CsiSimpleBuffer _rowBuffer;
    private boolean _isFiltered = false;
    private boolean _handleBackSlash = false;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsvReader(CsiBufferedStream streamIn, Integer delimiterIn, Integer quoteIn,
                     Integer escapeIn, String nullIndicatorIn, boolean handleBackSlashIn) {

        _stream = streamIn;
        _rowBuffer = _stream.createBuffer();
        _nullIndicator = ((null != nullIndicatorIn) && (0 < nullIndicatorIn.length())) ? nullIndicatorIn : null;
        _nullIndicatorBytes = (null != _nullIndicator) ? _nullIndicator.getBytes(StandardCharsets.UTF_8) : null;
        _handleBackSlash = handleBackSlashIn;

        if (null != delimiterIn) {

            _delimiter = delimiterIn;

            if (null != quoteIn) {

                _quote = quoteIn;

                if (null != escapeIn) {

                    _escape = escapeIn;

                } else {

                    _escape = _quote;
                }
            }

        } else {

            _delimiter = EncodingByteValues.cpAsciiComma;
        }
        _isFiltered = false;
    }

    public boolean openWorksheet(ColumnParameters[] columnsIn) throws Exception {

        boolean mySuccess = false;

        if (null != columnsIn) {

            _dataType = new CsiDataType[columnsIn.length];
            _include = new boolean[columnsIn.length];

            for (int i = 0; columnsIn.length > i; i++) {

                _dataType[i] = columnsIn[i].getDataType();
                _include[i] = columnsIn[i].isIncluded();

                if (_include[i]) {

                    mySuccess = true;

                } else {

                    _isFiltered = true;
                }
            }
        }
        return mySuccess;
    }

    public boolean isFiltered() {

        return _isFiltered;
    }

    public CsiSimpleBuffer getNextRow() {

        CsiSimpleBuffer myRow = null;
        int myValue = _stream.getValue();

        // Test for end-of-stream
        if (-1 != myValue) {

            // Return test value to stream
            _stream.putValue(myValue);

            // Prepare to read next row
            myRow = _rowBuffer.truncate();

            // Process or skip each column based upon user's request
            for (int i = 0; _include.length > i; i++) {

                if (_include[i]) {

                    getField(_rowBuffer, _dataType[i]);
                    _rowBuffer.appendElement(_delimiter);

                } else {

                    skipField();
                }
            }
            _rowBuffer.clip(1);

            verifyEndOfLine();
        }
        return myRow;
    }

    public void skipLines(long lineCountIn) throws IOException {

        for (int i = 0; lineCountIn > i; i++) {

            if (null == getNextRow()) {

                throw new IOException("Failed advancing to data within spreadsheet.");
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void getField(CsiSimpleBuffer bufferIn, CsiDataType dataTypeIn) {

        int myFirstByte = _stream.getValue();
        boolean myFloatFlag = (CsiDataType.Number == dataTypeIn);
        boolean mySpecialProcessing = myFloatFlag || (CsiDataType.Integer == dataTypeIn);
        int myValue = myFirstByte;

        if (-1 != myValue) {

            int myBase = (null != _quote) ? bufferIn.appendElement(_quote).length() : bufferIn.length();

            if ((null != _quote) && (_quote == myValue)) {

                for (myValue = _stream.getValue(); -1 != myValue; myValue = _stream.getValue()) {

                    if (_quote == myValue) {

                        myValue = _stream.getValue();

                        if (!_escape.equals(_quote) || !_quote.equals(myValue)) {

                            break;

                        } else {

                            bufferIn.appendElement(myValue);
                            bufferIn.appendElement(myValue);
                        }

                    } else if (_escape == myValue) {

                        bufferIn.appendElement(myValue);
                        bufferIn.appendElement(_stream.getValue());

                    } else {

                        bufferIn.appendElement(myValue);
                    }
                }

                if ((EncodingByteValues.cpAsciiReturn == myValue) || (EncodingByteValues.cpAsciiNewLine == myValue)) {

                    _stream.putValue(myValue);

                } else if (_delimiter != myValue) {

                   LOG.error("Bad CSV format!");
                }

            } else if (_handleBackSlash && (EncodingByteValues.cpAsciiBackSlash == myValue)) {

                _stream.putValue(myValue);
                myValue = _stream.getValue();
                if (-1 != myValue) {

                    _stream.putValue(myValue);

                    for (myValue = _stream.getValue(); -1 != myValue; myValue = _stream.getValue()) {

                        if (_handleBackSlash && (EncodingByteValues.cpAsciiBackSlash == myValue)) {

                            _stream.putValue(myValue);
                            myValue = _stream.getValue();

                        } else if (_delimiter == myValue) {

                            break;

                        } else if ((EncodingByteValues.cpAsciiReturn == myValue) || (EncodingByteValues.cpAsciiNewLine == myValue)) {

                            _stream.putValue(myValue);
                            break;

                        }
                        bufferIn.appendElement(myValue);
                    }
                }

            } else if ((EncodingByteValues.cpAsciiReturn == myValue) || (EncodingByteValues.cpAsciiNewLine == myValue)) {

                _stream.putValue(myValue);

            } else if (_delimiter != myValue) {

                _stream.putValue(myValue);

                for (myValue = _stream.getValue(); -1 != myValue; myValue = _stream.getValue()) {

                    if (_delimiter == myValue) {

                        break;

                    } else if ((EncodingByteValues.cpAsciiReturn == myValue) || (EncodingByteValues.cpAsciiNewLine == myValue)) {

                        _stream.putValue(myValue);
                        break;

                    } else {

                        bufferIn.appendElement(myValue);
                    }
                }
            }
            if ((null != _nullIndicator) && (_quote != myFirstByte) && (bufferIn.equals(_nullIndicator, myBase))) {

                if (null != _quote){

                    bufferIn.shiftOut(myBase - 1);
                }

            } else {

                if (bufferIn.length() == myBase) {

                    if (null != _quote) {

                        bufferIn.clip(1);
                    }
                    if (null != _nullIndicator) {

                        bufferIn.append(_nullIndicator);
                    }

                } else {

                    if (mySpecialProcessing) {

                        byte[] myValueBytes = bufferIn.copyBytes(myBase, bufferIn.length());
                        NumericValue myFormatter = new NumericValue(myValueBytes);

                        bufferIn.truncate(myBase).length();

                        if (myFloatFlag) {

                            myFormatter.formatFloat(bufferIn, _nullIndicatorBytes, _quote);

                        } else {

                            myFormatter.formatInteger(bufferIn, _nullIndicatorBytes, _quote);
                        }

                    } else if (null != _quote) {

                        bufferIn.appendElement(_quote);
                    }
                }
            }
        }
    }

    private void skipField() {

        int myValue = _stream.getValue();

        if (-1 != myValue) {

            if ((null != _quote) && (_quote == myValue)) {

                for (myValue = _stream.getValue(); -1 != myValue; myValue = _stream.getValue()) {

                    if (_quote == myValue) {

                        myValue = _stream.getValue();

                        if (!_escape.equals(_quote) || !_quote.equals(myValue)) {

                            break;
                        }

                    } else if (_escape == myValue) {

                        _stream.getValue();
                    }
                }

                if ((EncodingByteValues.cpAsciiReturn == myValue) || (EncodingByteValues.cpAsciiNewLine == myValue)) {

                    _stream.putValue(myValue);

                } else if (_delimiter != myValue) {

                   LOG.error("Bad CSV format!");
                }

            } else if ((EncodingByteValues.cpAsciiReturn == myValue) || (EncodingByteValues.cpAsciiNewLine == myValue)) {

                _stream.putValue(myValue);

            } else if (_delimiter != myValue) {

                for (myValue = _stream.getValue(); -1 != myValue; myValue = _stream.getValue()) {

                    if (_delimiter == myValue) {

                        break;

                    } else if ((EncodingByteValues.cpAsciiReturn == myValue) || (EncodingByteValues.cpAsciiNewLine == myValue)) {

                        _stream.putValue(myValue);
                        break;
                    }
                }
            }
        }
    }

    private void verifyEndOfLine() {

        int myValue = _stream.getValue();

        if (-1 != myValue) {

            if (EncodingByteValues.cpAsciiNewLine != myValue) {

                if (EncodingByteValues.cpAsciiReturn == myValue) {

                    myValue = _stream.getValue();

                    if (EncodingByteValues.cpAsciiNewLine != myValue) {

                        _stream.putValue(myValue);
                    }

                } else {

                    _stream.putValue(-1);
                }
            }
        }
    }
}
