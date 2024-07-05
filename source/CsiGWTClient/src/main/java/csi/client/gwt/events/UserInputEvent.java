package csi.client.gwt.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;


public class UserInputEvent<T> extends BaseCsiEvent<UserInputEventHandler> {

    private String[] _inputArray = null;
    private T _key = null;
    private boolean _canceled = false;

    public static final GwtEvent.Type<UserInputEventHandler> type = new GwtEvent.Type<UserInputEventHandler>();
    
    public UserInputEvent(boolean canceledIn) {
        
        _canceled = canceledIn;
    }
    
    public UserInputEvent(T keyIn, boolean canceledIn) {
        
        _key = keyIn;
        _canceled = canceledIn;
    }
    
    public UserInputEvent(String[] inputArrayIn) {
        
        _inputArray = new String[inputArrayIn.length];
        
        for (int i = 0; inputArrayIn.length > i; i++) {
            
            _inputArray[i] = inputArrayIn[i];
        }
    }
    
    public UserInputEvent(String inputIn) {
        
        _inputArray = new String[] {inputIn};
    }
    
    public UserInputEvent(T keyIn, String[] inputArrayIn) {
        
        _key = keyIn;
        _inputArray = new String[inputArrayIn.length];
        
        for (int i = 0; inputArrayIn.length > i; i++) {
            
            _inputArray[i] = inputArrayIn[i];
        }
    }
    
    public UserInputEvent(T keyIn, String inputIn) {
        
        _key = keyIn;
        _inputArray = new String[] {inputIn};
    }

    public String getInput(int indexIn) {
        
        return ((0 <= indexIn) && (_inputArray.length > indexIn)) ? _inputArray[indexIn] : null;
    }

    public String getInput() {
        
        return (0 <= _inputArray.length) ? _inputArray[0] : null;
    }
    
    public int getSize() {
        
        return _inputArray.length;
    }
    
    public void setKey(T keyIn) {
        
        _key = keyIn;
    }
    
    public T getKey() {
        
        return _key;
    }
    
    public boolean isCanceled() {
        
        return _canceled;
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<UserInputEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(UserInputEventHandler handler) {
        handler.onUserInput(this);
    }
}
