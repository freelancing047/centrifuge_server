package csi.client.gwt.widget.ui.uploader.wizards.support;

import java.util.List;

import com.google.gwt.typedarrays.shared.Int8Array;

import csi.server.common.enumerations.CsiEncoding;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.EncodingByteValues;

/**
 * Created by centrifuge on 9/11/2015.
 */
public class ReadCsvBlock extends ReadNonBinaryBlock {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    int _quote = -2;
    int _quoteCount = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ReadCsvBlock(Int8Array dataSourceIn, boolean fixNullsIn) {

        super(dataSourceIn, fixNullsIn);
    }

    public ReadCsvBlock(Int8Array dataSourceIn, List<Integer> characterBlockIn, CsiEncoding encodingIn, boolean fixNullsIn) {

        super(dataSourceIn, characterBlockIn, encodingIn, fixNullsIn);
    }

    @Override
    public ReadBlock restart() throws Exception {

        super.restart();

        clearQuoteCount();

        return this;
    }

    public void setQuote(int quoteIn) {

        _quote = quoteIn;
    }

    public void disableQuoting() {

        _quote = -2;
    }

    public void clearQuoteCount() {

        _quoteCount = 0;
    }

    public int getQuoteCount() {

        return _quoteCount;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected String getNextColumn() throws CentrifugeException {

        StringBuilder myBuffer = null;

        if (_limit > _offset) {

            myBuffer = new StringBuilder();

            if (_quote == peekChar()) {

                getQuotedCsvString(myBuffer);

            } else {

                getSimpleCsvString(myBuffer);
            }
            checkIntegrity();
        }

        return (null != myBuffer) ? myBuffer.toString() : null;
    }

    protected String getRawRow() throws CentrifugeException {

        StringBuilder myBuffer = null;

        if (_limit > _offset) {

            myBuffer = new StringBuilder();

            while (_limit > _offset) {

                int myValue = getChar();

                if ((EncodingByteValues.cpAsciiNewLine == myValue) || (EncodingByteValues.cpAsciiReturn == myValue) || (EncodingByteValues.cpEOS == myValue)) {

                    _offset--;
                    break;

                } else if (_quote == myValue) {

                    _quoteCount++;
                    copyQuote(myBuffer);

                } else {

                    myBuffer.appendCodePoint(myValue);
                }
            }
            checkIntegrity();
        }

        return (null != myBuffer) ? myBuffer.toString() : null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void getQuotedCsvString(StringBuilder bufferIn) throws CentrifugeException {

        if (_quote == getChar()) {

            _quoteCount++;
            while (_limit > _offset) {

                int myValue = getChar();

                if (_quote == myValue) {

                    if (_limit > _offset) {

                        myValue = getChar();

                        if (_quote == myValue) {

                            bufferIn.appendCodePoint(_quote);

                        } else {

                            _offset--;
                            _quoteCount++;
                            return;
                        }

                    } else {

                        _quoteCount++;
                        return;
                    }

                } else {

                    bufferIn.appendCodePoint(myValue);
                }
            }
            throw new CentrifugeException(_constants.installFileWizard_CellDataFormatError(_constants.installFileWizard_CellDataErrorReason1()));

        } else {

            throw new CentrifugeException(_constants.installFileWizard_CellDataFormatError(_constants.installFileWizard_CellDataErrorReason2()));
        }
    }

    private void getSimpleCsvString(StringBuilder bufferIn) throws CentrifugeException {

        int myBase = _offset;

        while (_limit > _offset) {

            int myValue = getChar();

            if (_quote == myValue) {

                boolean myCharFlag = false;

                for (int i = myBase; (_offset - 1) > i; i++) {

                    myValue = getChar(i);

                    if (32 < myValue) {

                        myCharFlag = true;
                        break;
                    }
                }
                if (myCharFlag) {

                    throw new CentrifugeException(_constants.installFileWizard_CellDataFormatError(_constants.installFileWizard_CellDataErrorReason3()));

                } else {

                    throw new CentrifugeException(_constants.installFileWizard_CellDataFormatError(_constants.installFileWizard_CellDataErrorReason4()));
                }

            } else if ((_delimiter == myValue) || (EncodingByteValues.cpAsciiNewLine == myValue) || (EncodingByteValues.cpAsciiReturn == myValue) || (EncodingByteValues.cpEOS == myValue)) {

                _offset--;
                return;

            } else {

                bufferIn.appendCodePoint(myValue);
            }
        }
    }

    private void stepOverQuote() throws CentrifugeException {

        while (_limit > _offset) {

            int myValue = getChar();

            if (_quote == myValue) {

                _quoteCount++;
                break;
            }
        }
    }

    private void copyQuote(StringBuilder bufferIn) throws CentrifugeException {

        bufferIn.appendCodePoint(_quote);
        for (int myValue = getChar();
             _limit > _offset;
             myValue = getChar()) {

            bufferIn.appendCodePoint(myValue);

            if (_quote == myValue) {

                _quoteCount++;
                break;
            }
        }
    }
}
