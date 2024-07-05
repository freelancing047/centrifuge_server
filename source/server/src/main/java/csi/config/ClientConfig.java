package csi.config;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by centrifuge on 1/29/2016.
 */
public class ClientConfig extends AbstractConfigurationSettings implements IsSerializable {

    private static final long _oneK = 1024L;
    private static final long _oneMeg = _oneK * _oneK;
    private static final long _minimumMaxBufferSize = 64L * _oneK;
    private static final long _absoluteMaxBufferSize = 256L * _oneMeg;
    private static final long _defaultMaxBufferSize = 16L * _oneMeg;

    private boolean _provideSourceName = true;
    private boolean _bracketDefault = true;
    private boolean _incrementImmediately = true;
    private long _maxBufferSize = _defaultMaxBufferSize;

    public Boolean getProvideSourceName() {
        return _provideSourceName;
    }

    public void setProvideSourceName(Boolean provideSourceNameIn) {
        if (provideSourceNameIn == null) {
            _provideSourceName = Boolean.TRUE;
        } else {
            _provideSourceName = provideSourceNameIn;
        }
    }

    public Boolean getBracketDefault() {
        return _bracketDefault;
    }

    public void setBracketDefault(Boolean bracketDefaultIn) {
        if (bracketDefaultIn == null) {
            _bracketDefault = Boolean.TRUE;
        } else {
            _bracketDefault = bracketDefaultIn;
        }
    }

    public Boolean getIncrementImmediately() {
        return _incrementImmediately;
    }

    public void setIncrementImmediately(Boolean bracketDefaultIn) {
        if (bracketDefaultIn == null) {
            _incrementImmediately = Boolean.TRUE;
        } else {
            _incrementImmediately = bracketDefaultIn;
        }
    }

    public long getMaxClientBufferSize() {
        return _maxBufferSize;
    }

    public void setMaxBufferSize(String maxBufferSizeIn) {

        long myValue = extractValue(maxBufferSizeIn);

        if (0L < myValue) {

            _maxBufferSize = Math.min(Math.max(myValue, _minimumMaxBufferSize), _absoluteMaxBufferSize);
        }
    }

    private long extractValue(String maxBufferSizeIn) {

        long myValue = 0L;
        int i = 0;
        boolean myExitFlag = false;

        for (i = 0; maxBufferSizeIn.length() > i; i++) {

            char myCharacter = maxBufferSizeIn.charAt(i);

            if (myExitFlag) {

                break;
            }

            if (('0' <= myCharacter) && ('9' >= myCharacter)) {

                myValue = (myValue * 10L) + (long)(myCharacter - '0');

            } else if (('M' == myCharacter) || ('m' == myCharacter)) {

                myValue = myValue * _oneMeg;
                myExitFlag = true;

            } else if (('K' == myCharacter) || ('k' == myCharacter)) {

                myValue = myValue * _oneK;
                myExitFlag = true;
            }
        }

        if (maxBufferSizeIn.length() > i) {

            myValue = 0;
        }

        return myValue;
    }
}
