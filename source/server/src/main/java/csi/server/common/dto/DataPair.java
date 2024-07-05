package csi.server.common.dto;

import java.io.Serializable;


public class DataPair<S, T> implements Serializable {

    protected S _objectOne;
    protected T _objectTwo;
    
    public DataPair() {
        
    }

    public DataPair(S objectOneIn, T objectTwoIn) {
        
        _objectOne = objectOneIn;
        _objectTwo = objectTwoIn;
    }
    
    public S getObjectOne() {
        
        return _objectOne;
    }
    
    public void setObjectOne(S objectOneIn) {
        
        _objectOne = objectOneIn;
    }
    
    public T getObjectTwo() {
        
        return _objectTwo;
    }
    
    public void setObjectTwo(T objectTwoIn) {
        
        _objectTwo = objectTwoIn;
    }
}
