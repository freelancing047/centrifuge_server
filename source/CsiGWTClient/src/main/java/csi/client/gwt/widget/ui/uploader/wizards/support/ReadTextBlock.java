package csi.client.gwt.widget.ui.uploader.wizards.support;

import java.util.List;

import com.google.gwt.typedarrays.shared.Int8Array;

import csi.server.common.enumerations.CsiEncoding;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.EncodingByteValues;

/**
 * Created by centrifuge on 9/11/2015.
 */
public class ReadTextBlock extends ReadNonBinaryBlock {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ReadTextBlock(Int8Array dataSourceIn, boolean fixNullsIn) {

        super(dataSourceIn, fixNullsIn);
    }

    public ReadTextBlock(Int8Array dataSourceIn, List<Integer> characterBlockIn, CsiEncoding encodingIn, boolean fixNullsIn) {

        super(dataSourceIn, characterBlockIn, encodingIn, fixNullsIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected String getNextColumn() throws CentrifugeException {

        StringBuilder myBuffer = null;
        boolean myEscape = false;

        if (_limit > _offset) {

            myBuffer = new StringBuilder();

            for (int myValue = getChar();
                 _limit > _offset;
                 myValue = getChar()) {

                if (EncodingByteValues.cpEOS == myValue) {

                    _offset--;
                    break;

                } else {

                    if ((_delimiter == myValue) || (EncodingByteValues.cpAsciiNewLine == myValue) || (EncodingByteValues.cpAsciiReturn == myValue)) {

                        if (myEscape) {

                            myBuffer.appendCodePoint(myValue);

                        } else{

                            _offset--;
                            break;
                        }

                    } else {

                        myEscape = false;
                        myBuffer.appendCodePoint(myValue);
                    }
                    myEscape = (EncodingByteValues.cpAsciiBackSlash == myValue);
                }
            }
            checkIntegrity();
        }

        return (null != myBuffer) ? myBuffer.toString() : null;
    }

    protected String getRawRow() throws CentrifugeException {

        StringBuilder myBuffer = null;
        boolean myEscape = false;

        if (_limit > _offset) {

            myBuffer = new StringBuilder();

            while (_limit > _offset) {

                int myValue = getChar();

                if (EncodingByteValues.cpEOS == myValue) {

                    _offset--;
                    break;

                } else {

                    if ((EncodingByteValues.cpAsciiNewLine == myValue) || (EncodingByteValues.cpAsciiReturn == myValue)) {

                        if (myEscape) {

                            myBuffer.appendCodePoint(myValue);

                        } else{

                            _offset--;
                            break;
                        }

                    } else {

                        myEscape = false;
                        myBuffer.appendCodePoint(myValue);
                    }
                    myEscape = (EncodingByteValues.cpAsciiBackSlash == myValue);
                }
            }
            checkIntegrity();
        }

        return (null != myBuffer) ? myBuffer.toString() : null;
    }
}
