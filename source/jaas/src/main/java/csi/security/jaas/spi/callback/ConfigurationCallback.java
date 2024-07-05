package csi.security.jaas.spi.callback;

import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;

import csi.security.jaas.spi.CallBackId;

public class ConfigurationCallback implements Callback
{
    private String _authenticationOrder = null;
    private String _userHeaderKey = null;
    private boolean _doDebug = false;
    private String _kerberosKeyTab = null;
    private String _kerberosPrincipal = null;
    private boolean _kerberosUseDomainName = false;

	public ConfigurationCallback()
	{
	}

    public String getAuthenticationOrder() throws LoginException
    {
        return _authenticationOrder;
    }

    public void setAuthenticationOrder(String authenticationOrderIn) throws LoginException
    {
        _authenticationOrder = authenticationOrderIn;
    }

    public String getUserHeaderKey() throws LoginException
    {
        return _userHeaderKey;
    }

    public void setUserHeaderKey(String userHeaderKeyIn) throws LoginException
    {
        _userHeaderKey = userHeaderKeyIn;
    }

    public String getKerberosKeyTab() throws LoginException
    {
        return _kerberosKeyTab;
    }

    public void setKerberosKeyTab(String kerberosKeyTabIn) throws LoginException
    {
        _kerberosKeyTab = kerberosKeyTabIn;
    }

    public String getKerberosPrincipal() throws LoginException
    {
        return _kerberosPrincipal;
    }

    public void setKerberosPrincipal(String kerberosPrincipalIn) throws LoginException
    {
        _kerberosPrincipal = kerberosPrincipalIn;
    }

    public boolean getDoDebug() throws LoginException
    {
        return _doDebug;
    }

    public void setDoDebug(boolean doDebugIn) throws LoginException
    {
        _doDebug = doDebugIn;
    }

    public boolean getKerberosUseDomainName() throws LoginException
    {
        return _kerberosUseDomainName;
    }

    public void setKerberosUseDomainName(boolean kerberosUseDomainNameIn)
    {
        _kerberosUseDomainName = kerberosUseDomainNameIn;
    }

    @Override
    public String toString() {
        
        return CallBackId.RESPONSE.getKey();
    }
}
