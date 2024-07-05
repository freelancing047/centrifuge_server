package csi.server.common.util;

public class BitMask {

    long[] _data;
    
    public BitMask(long[] dataIn) {
        
        initialize(dataIn.length * 64);
        copy(dataIn);
    }
    
    public BitMask(int sizeIn) {
        
        initialize(sizeIn);
        zero(0);
    }
    
    public BitMask() {
        
        this(0);
    }
    
    public void setBit(HasOrdinal enumIn) {
        
        setBit(enumIn.getOrdinal());
    }
    
    public void setBit(int bitNumberIn) {
        
        guarantee(bitNumberIn);
        _data[bitNumberIn/64] |= (1L << (bitNumberIn % 64));
    }
    
    public boolean isSet(HasOrdinal enumIn) {
        
        return isSet(enumIn.getOrdinal());
    }
    
    public boolean isSet(int bitNumberIn) {
        
        boolean myIsSet = false;
        int myIndex = bitNumberIn / 64;
        
        if (_data.length > myIndex) {

            myIsSet = (0 != (_data[myIndex] & (1L << (bitNumberIn % 64))));
        }
        return myIsSet;
    }
    
    private void guarantee(int bitNumberIn) {
        
        if ((bitNumberIn/64) >= _data.length) {
            
            long[] myOldData = _data;
            
            initialize(bitNumberIn);
            copy(myOldData);
            zero(myOldData.length);
        }
    }
    
    private void initialize(int sizeIn) {
        
        _data = new long[1 + (sizeIn / 64)];
    }
    
    private void copy(long[] dataIn) {
        int myLimit = (dataIn.length < _data.length) ? dataIn.length : _data.length;
        System.arraycopy(dataIn, 0, _data, 0, myLimit);
    }
    
    private void zero(int baseIndex) {
        
        for (int i = baseIndex; _data.length > i; i++) {
            
            _data[i] = 0L;
        }
    }
}
