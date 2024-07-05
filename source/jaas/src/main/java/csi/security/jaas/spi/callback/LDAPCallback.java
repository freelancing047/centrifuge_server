package csi.security.jaas.spi.callback;

import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;

import csi.security.jaas.spi.CallBackId;

public class LDAPCallback implements Callback
{
    private String _passCode = null;
    private String _distinguishedName = null;
	private boolean _initialized = false;
	
	public String getPassCode() throws LoginException
	{
		if (!_initialized)
		{
			throw new LoginException("Attempting read from an uninitialized LDAP Callback!");
		}
		return _passCode;
	}	

	public LDAPCallback()
	{
		_passCode = null;
        _distinguishedName = null;
		_initialized = false;
	}
	
	public void initialize(String passCodeIn) throws LoginException
	{
		if (!_initialized)
		{
			_passCode = passCodeIn;
            _distinguishedName = null;
			_initialized = true;
		}
		else
		{
			throw new LoginException("Attempting to initialize an LDAP Callback more than once!");
		}
	}

    public void setDistinguishedName(String distinguishedNameIn)
    {
        _distinguishedName = distinguishedNameIn;
    }

    public String getDistinguishedName()
    {
        return _distinguishedName;
    }

    public void clearPassCode() throws LoginException
	{
		if (!_initialized)
		{
			throw new LoginException("Attempting to clear the password within an uninitialized LDAP Callback!");
		}
		_passCode = null;
	}
    
    @Override
    public String toString() {
        
        return CallBackId.LDAP_PASSWORD.getKey();
    }
}
