package csi.security.jaas.spi.callback;

import javax.security.auth.callback.Callback;

import csi.security.jaas.spi.CallBackId;

public class InitializationCallback implements Callback
{
	private boolean _initialized = false;
	
	public boolean getInitialized()
	{
		return _initialized;
	}

	public InitializationCallback()
	{
		_initialized = false;
	}
	
	public void initialize()
	{
		_initialized = true;
	}
    
    @Override
    public String toString() {
        
        return CallBackId.INITIALIZE.getKey();
    }
}
