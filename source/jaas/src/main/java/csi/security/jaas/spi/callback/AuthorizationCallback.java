package csi.security.jaas.spi.callback;

import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;

import csi.security.jaas.spi.AuthorizationMode;
import csi.security.jaas.spi.CallBackId;

public class AuthorizationCallback implements Callback
{
	private boolean _initialized = false;
	private AuthorizationMode _mode = null;
    private String _distinguishedName = null;
    private String _username = null;
	private String _password = null;

    public void setMode(AuthorizationMode modeIn) throws LoginException
    {
        if (!_initialized)
        {
            throw new LoginException("Attempting to write to an uninitialized Authorization Callback!");
        }
        _mode = modeIn;
    }

    public AuthorizationMode getMode() throws LoginException
    {
        if (!_initialized)
        {
            throw new LoginException("Attempting to read from an uninitialized Authorization Callback!");
        }
        return _mode;
    }

    public void setDN(String dnIn) throws LoginException
    {
        if (!_initialized)
        {
            throw new LoginException("Attempting to write to an uninitialized Authorization Callback!");
        }
        _distinguishedName = dnIn;
    }

    public String getDN() throws LoginException
    {
        if (!_initialized)
        {
            throw new LoginException("Attempting to read from an uninitialized Authorization Callback!");
        }
        return _distinguishedName;
    }

    public void setUsername(String usernameIn) throws LoginException
    {
        if (!_initialized)
        {
            throw new LoginException("Attempting to write to an uninitialized Authorization Callback!");
        }
        _username = usernameIn;
    }

	public String getUsername() throws LoginException
	{
		if (!_initialized)
		{
			throw new LoginException("Attempting to read from an uninitialized Authorization Callback!");
		}
        //return (null != _username) ? _username.toLowerCase() : null;
        return _username;
	}
	
	public String getPassword() throws LoginException
	{
		if (!_initialized)
		{
			throw new LoginException("Attempting to read from an uninitialized Authorization Callback!");
		}
		return _password;
	}

	public void initialize(AuthorizationMode modeIn, String usernameIn, String passwordIn, String distinguishedNameIn) throws LoginException
	{
		if (!_initialized)
		{
			_mode = modeIn;
			_username = usernameIn;
            _distinguishedName = distinguishedNameIn;
			if (AuthorizationMode.JDBC == _mode || AuthorizationMode.FORM == _mode)
			{
				_password = passwordIn;
			}
			else
			{
				_password = null;
			}
			_initialized = true;
		}
		else
		{
			throw new LoginException("Attempting to initialize an Authorization Callback more than once!");
		}
	}
	
	public void clearPassword() throws LoginException
	{
		if (!_initialized)
		{
			throw new LoginException("Attempting to clear the password within an uninitialized Authorization Callback!");
		}
		_password = null;
	}
	
	@Override
	public String toString() {
	    
	    return CallBackId.AUTHORIZE.getKey();
	}
}
