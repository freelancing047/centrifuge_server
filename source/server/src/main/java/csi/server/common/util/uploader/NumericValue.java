package csi.server.common.util.uploader;

import csi.server.common.util.CsiSimpleBuffer;
import csi.server.common.util.EncodingByteValues;

/**
 * Created by centrifuge on 10/30/2015.
 */
public class NumericValue {


    private byte[] _byteBuffer = null;

    public NumericValue(byte[] bufferIn){

        _byteBuffer = bufferIn;
    }

    public CsiSimpleBuffer formatInteger(CsiSimpleBuffer bufferIn, byte[] nullIndicatorIn) {

        return processNumeric(bufferIn, nullIndicatorIn, false, null) ;
    }

    public CsiSimpleBuffer formatFloat(CsiSimpleBuffer bufferIn, byte[] nullIndicatorIn) {

        return processNumeric(bufferIn, nullIndicatorIn, true, null) ;
    }

    public CsiSimpleBuffer formatInteger(CsiSimpleBuffer bufferIn, byte[] nullIndicatorIn, Integer quoteIn) {

        return processNumeric(bufferIn, nullIndicatorIn, false, quoteIn) ;
    }

    public CsiSimpleBuffer formatFloat(CsiSimpleBuffer bufferIn, byte[] nullIndicatorIn, Integer quoteIn) {

        return processNumeric(bufferIn, nullIndicatorIn, true, quoteIn) ;
    }

    private CsiSimpleBuffer processNumeric(CsiSimpleBuffer bufferIn, byte[] nullIndicatorIn,
                                           boolean isFloatIn, Integer quoteIn) {

        int myBase = bufferIn.length();
        int myOffset = 0;
        boolean myDigitFound = false;
        boolean myDotFound = false;
        boolean myExponentFound = false;
        boolean myPlusFound = false;
        boolean myMinusFound = false;
        boolean myCurrencyFound = false;
        boolean mySuccess = true;
        int myLimit = _byteBuffer.length;

        while (mySuccess && (myLimit > myOffset)) {

            byte myByte = _byteBuffer[myOffset];

            if (((EncodingByteValues.asciiZero <= myByte) && (EncodingByteValues.asciiNine >= myByte)) || (EncodingByteValues.asciiDot == myByte)) {

                // Start number processing
                break;

            } else if (EncodingByteValues.asciiMinus == myByte) {

                if (myMinusFound) {

                    mySuccess = false;

                } else {

                    myMinusFound = true;
                    bufferIn.appendElement(EncodingByteValues.asciiMinus);
                }

            } else if (EncodingByteValues.asciiPlus == myByte) {

                if (myPlusFound) {

                    mySuccess = false;

                } else {

                    myPlusFound = true;
                }

            } else if ((EncodingByteValues.asciiDollar == myByte) || (EncodingByteValues.asciiHash == myByte)
                        || (EncodingByteValues.latinPound == myByte) || (EncodingByteValues.latinYen == myByte)) {

                if (myCurrencyFound) {

                    mySuccess = false;

                } else {

                    myCurrencyFound = true;
                }

            } else if (((myLimit - 2) > myOffset) && (EncodingByteValues.currencyOne == myByte)
                    && (EncodingByteValues.currencyTwo == _byteBuffer[myOffset + 1])
                    && (EncodingByteValues.currencyBase <= _byteBuffer[myOffset + 2]) && (EncodingByteValues.currencyTop >= _byteBuffer[myOffset + 2])) {

                if (myCurrencyFound) {

                    mySuccess = false;

                } else {

                    myCurrencyFound = true;
                    myOffset += 2;
                }

            } else if (EncodingByteValues.asciiBlank != myByte) {

                break;
            }
            myOffset++;
        }
        // Begin extracting numeric value
        while (mySuccess && (myLimit > myOffset)) {

            byte myByte = _byteBuffer[myOffset++];

            if ((EncodingByteValues.asciiZero <= myByte) && (EncodingByteValues.asciiNine >= myByte)) {

                myDigitFound = true;
                bufferIn.appendElement(myByte);

            } else if (EncodingByteValues.asciiComma == myByte) {

                if (myDigitFound && (!myDotFound) && (myLimit > (myOffset + 2))) {

                    byte myFirstDigit = _byteBuffer[myOffset];
                    byte mySecondDigit = _byteBuffer[myOffset + 1];
                    byte myThirdDigit = _byteBuffer[myOffset + 2];

                    if ((EncodingByteValues.asciiZero > myFirstDigit) || (EncodingByteValues.asciiNine < myFirstDigit)
                            || (EncodingByteValues.asciiZero > mySecondDigit) || (EncodingByteValues.asciiNine < mySecondDigit)
                            || (EncodingByteValues.asciiZero > myThirdDigit) || (EncodingByteValues.asciiNine < myThirdDigit)) {

                        break;
                    }

                } else {

                    break;
                }
                // Skip over comma

            } else if (EncodingByteValues.asciiDot == myByte) {

                if (myDotFound || (!isFloatIn)) {

//                    myOffset--;
                    break;
                }
                myDotFound = true;
                bufferIn.appendElement(myByte);

            } else if ((EncodingByteValues.asciiCapsE == myByte) || (EncodingByteValues.asciiLowerE == myByte)) {

                if (isFloatIn && (!myExponentFound) && (myLimit > myOffset)) {

                    byte myTestByte = _byteBuffer[myOffset++];

                    if ((EncodingByteValues.asciiMinus == myTestByte) || (EncodingByteValues.asciiPlus == myTestByte) ||
                            ((EncodingByteValues.asciiZero <= myTestByte) && (EncodingByteValues.asciiNine >= myTestByte))) {

                        myExponentFound = true;
                        bufferIn.appendElement(myByte);
                        bufferIn.appendElement(myTestByte);

                    } else {

                        break;
                    }
                }

            } else {

                break;
            }
        }

        if (myDigitFound) {

            if (null != quoteIn) {

                bufferIn.appendElement(quoteIn);
            }

        } else {

            bufferIn.truncate(myBase);
            if (null != quoteIn) {

                bufferIn.clip(1);
            }
            if (null != nullIndicatorIn) {

                bufferIn.appendCharacterBytes(nullIndicatorIn);
            }
        }

        return bufferIn;
    }
}
