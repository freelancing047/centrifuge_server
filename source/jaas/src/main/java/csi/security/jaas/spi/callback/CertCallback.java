package csi.security.jaas.spi.callback;

import java.security.cert.X509Certificate;
import java.util.StringTokenizer;

import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;

import csi.security.jaas.spi.CallBackId;

public class CertCallback implements Callback
{
    static private X509Certificate[] EMPTY = new X509Certificate[0];

    private String _distinguishedName = null;
    private String _username = null;
	private boolean _initialized = false;

    protected X509Certificate[] _chain;

    public CertCallback()
    {
        _chain = new X509Certificate[0];
    }
	
	public void initialize(X509Certificate[] chainIn) throws LoginException
	{
		if (!_initialized)
		{
			if (null != chainIn)
			{
				_chain = chainIn;
			}
			else
			{
				_chain = EMPTY;
			}
            _username = null;
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

    public void setName(String nameIn)
    {
        _username = nameIn;
    }

    public String getName()
    {
        return _username;
    }

    public X509Certificate[] getChain()
    {
        return _chain;
    }
    
    @Override
    public String toString() {
        
        return CallBackId.CERTIFICATE.getKey();
    }
}
