package csi.server.common.exception;

/**
 * Created by centrifuge on 8/11/2015.
 */
public class SignatureException extends ZipException {

    long _signature;

    public SignatureException() {

    }

    public SignatureException(long signatureIn) {

        _signature = signatureIn;
    }

    public void setSignature(long signatureIn) {

        _signature = signatureIn;
    }

    public long getSignature() {

        return _signature;
    }
}
