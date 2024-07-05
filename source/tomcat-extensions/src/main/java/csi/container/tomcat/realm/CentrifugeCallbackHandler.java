package csi.container.tomcat.realm;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.apache.catalina.realm.JAASCallbackHandler;
import org.apache.catalina.realm.JAASRealm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.container.tomcat.ConfigResponse;
import csi.security.jaas.spi.AuthorizationMode;
import csi.security.jaas.spi.CallBackId;
import csi.security.jaas.spi.callback.AuthorizationCallback;
import csi.security.jaas.spi.callback.CertCallback;
import csi.security.jaas.spi.callback.ConfigurationCallback;
import csi.security.jaas.spi.callback.InitializationCallback;
import csi.security.jaas.spi.callback.LDAPCallback;

public class CentrifugeCallbackHandler extends JAASCallbackHandler {
   private static final Logger LOG = LogManager.getLogger(CentrifugeCallbackHandler.class);

	private static boolean _doLocalDebug = false;
	
	private AuthorizationMode _mode;

    static Callback[] EMPTY = new Callback[0];

    protected X509Certificate[] chain;
    protected java.lang.String _distinguishedName = null;
    protected java.lang.String _userName = null;
    protected java.lang.String _password = null;
    protected java.lang.String _passCode = null;
    protected java.lang.String _authenticationOrder = null;
    protected java.lang.String _userHeaderKey = null;
    protected java.lang.String _kerberosKeyTab = null;
    protected java.lang.String _kerberosPrincipal = null;
    private boolean _kerberosUseDomainName = false;
    protected boolean _doDebug = false;

    public CentrifugeCallbackHandler(JAASRealm realm, AuthorizationMode modeIn)
    {
        this(realm, null, null, modeIn);
    }

    public CentrifugeCallbackHandler(JAASRealm realm, java.lang.String usernameIn, java.lang.String passwordIn)
    {
        this(realm, usernameIn, passwordIn, null);
        _passCode = passwordIn;
    }

    public CentrifugeCallbackHandler(JAASRealm realm, java.lang.String usernameIn, java.lang.String passwordIn, AuthorizationMode modeIn)
    {
        super(realm, (null != usernameIn) ? usernameIn : "", (null != passwordIn) ? passwordIn : "");

        if (_doLocalDebug) LOG.info(">> >> >>  CentrifugeCallbackHandler::CentrifugeCallbackHandler(JAASRealm realm, "
                + display(usernameIn) + ", " + display(passwordIn, "*raw-password*") + "AuthorixationMode=\"" + modeIn.name() + "\")");

        _passCode = passwordIn;
        _userName = usernameIn;
        setAuthenticationMode(modeIn);
    }
    
    public ConfigResponse getConfigResponse() {
        
        return new ConfigResponse(_authenticationOrder, _userHeaderKey, _kerberosKeyTab, _kerberosPrincipal, _kerberosUseDomainName, _doDebug);
    }

    public void setAuthenticationMode(AuthorizationMode modeIn)
    {
    	_mode = modeIn;
    }

    public void setDistinguishedName(java.lang.String distinguishedNameIn)
    {
        _distinguishedName = distinguishedNameIn;
    }

    public java.lang.String getDistinguishedName()
    {
        return _distinguishedName;
    }

    public void setUserName(java.lang.String userNameIn)
    {
    	_userName = userNameIn;
    }

    public java.lang.String getUserName()
    {
    	return _userName;
    }

    public void setChain(X509Certificate[] chain)
    {
    	
    	if (_doLocalDebug) LOG.info(">> >> >>  CentrifugeCallbackHandler::setChain(X509Certificate[] chain)");
    	
        this.chain = chain;
    }

    public java.lang.String getAuthenticationOrder() throws LoginException
    {
        return _authenticationOrder;
    }

    public void setAuthenticationOrder(java.lang.String authenticationOrderIn) throws LoginException
    {
        _authenticationOrder = authenticationOrderIn;
    }

    public java.lang.String getUserHeaderKey() throws LoginException
    {
        return _userHeaderKey;
    }

    public void setUserHeaderKey(java.lang.String userHeaderKeyIn) throws LoginException
    {
        _userHeaderKey = userHeaderKeyIn;
    }

    public java.lang.String getKerberosKeyTab() throws LoginException
    {
        return _kerberosKeyTab;
    }

    public void setKerberosKeyTab(java.lang.String kerberosKeyTabIn) throws LoginException
    {
        _kerberosKeyTab = kerberosKeyTabIn;
    }

    public java.lang.String getKerberosPrincipal() throws LoginException
    {
        return _kerberosPrincipal;
    }

    public void setKerberosPrincipal(java.lang.String kerberosPrincipalIn) throws LoginException
    {
        _kerberosPrincipal = kerberosPrincipalIn;
    }

    public boolean getKerberosUseDomainName()
    {
        return _kerberosUseDomainName;
    }

    public void setKerberosUseDomainName(boolean kerberosUseDomainNameIn)
    {
        _kerberosUseDomainName = kerberosUseDomainNameIn;
    }

    public boolean getDoDebug() throws LoginException
    {
        return _doDebug;
    }

    public void setDoDebug(boolean doDebugIn) throws LoginException
    {
        _doDebug = doDebugIn;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
    	
    	if (_doLocalDebug) LOG.info(">> >> >>  CentrifugeCallbackHandler::handle(Callback[] callbacks)");
    	
    	boolean myReadOption = false;
    	
        List<Callback> updated = new ArrayList<Callback>();
        for (int i = 0; i < callbacks.length; i++)
        {
            try
            {
                java.lang.String myTestString = callbacks[i].toString();
                CallBackId myId = CallBackId.getValue(myTestString);
                
                switch (myId) {
                    
                    case INITIALIZE :
                        
                        if (_doLocalDebug) LOG.info("           - (" + Integer.toString(i) + ") Initialization callback");
                        
                        myReadOption = ((InitializationCallback)callbacks[i]).getInitialized();
                        
                        if (myReadOption)
                        {
                            _password = password;
                            
                            if (_doLocalDebug) LOG.info("             - set _password to " + display(_password));
                            
                        }
                        else
                        {
                            ((InitializationCallback)callbacks[i]).initialize();
                        }
                        break;
                        
                    case AUTHORIZE :
                        
                        if (_doLocalDebug) LOG.info("           - (" + Integer.toString(i) + ") authorization callback");
                        
                        if (myReadOption)
                        {
                            _mode = ((AuthorizationCallback) callbacks[i]).getMode();
                            _userName = ((AuthorizationCallback) callbacks[i]).getUsername();
                            _distinguishedName = ((AuthorizationCallback) callbacks[i]).getDN();

                            if (_doLocalDebug) LOG.info("             - set _mode to " + display(_mode));
                            
                        }
                        else
                        {
                            java.lang.String myUser = (AuthorizationMode.CERT == _mode) ? _userName : username;
                            ((AuthorizationCallback) callbacks[i]).initialize(_mode, myUser, _password, _distinguishedName);
                        }
                        break;
                        
                    case CERTIFICATE :
                        
                        if (_doLocalDebug) LOG.info("           - (" + Integer.toString(i) + ") X509 callback");
                        
                        if (myReadOption)
                        {
                            if (AuthorizationMode.CERT == _mode)
                            {
                                _userName = ((CertCallback) callbacks[i]).getName();
                                _distinguishedName = ((CertCallback) callbacks[i]).getDistinguishedName();

                                if (_doLocalDebug) LOG.info("             - set _userName to " + display(_userName));
                            }
                        }
                        else
                        {
                            ((CertCallback) callbacks[i]).initialize(chain);
                        }
                        break;
                        
                    case LDAP_PASSWORD :
                        
                        if (_doLocalDebug) LOG.info("           - (" + Integer.toString(i) + ") LDAP callback");
                        
                        if (myReadOption)
                        {
                            if (AuthorizationMode.LDAP == _mode) {

                                _passCode = ((LDAPCallback) callbacks[i]).getPassCode();
                                _distinguishedName = ((LDAPCallback) callbacks[i]).getDistinguishedName();

                                if (_doLocalDebug) LOG.info("             - set _passCode to *raw_password*");
                            }
                        }
                        else
                        {
                            ((LDAPCallback) callbacks[i]).initialize(_passCode);
                        }
                        break;
                        
                    case RESPONSE :
                        
                        if (_doLocalDebug) LOG.info("           - (" + Integer.toString(i) + ") Response callback");
                        
                        if (myReadOption)
                        {
                            _authenticationOrder = ((ConfigurationCallback) callbacks[i]).getAuthenticationOrder();
                            _userHeaderKey = ((ConfigurationCallback) callbacks[i]).getUserHeaderKey();
                            _kerberosKeyTab = ((ConfigurationCallback) callbacks[i]).getKerberosKeyTab();
                            _kerberosPrincipal = ((ConfigurationCallback) callbacks[i]).getKerberosPrincipal();
                            _kerberosUseDomainName = ((ConfigurationCallback) callbacks[i]).getKerberosUseDomainName();
                            _doDebug = ((ConfigurationCallback) callbacks[i]).getDoDebug();

                            if (_doLocalDebug) LOG.info("             - set _authenticationOrder to " + display(_authenticationOrder));
                            if (_doLocalDebug) LOG.info("             - set _userHeaderKey to " + display(_userHeaderKey));
                            if (_doLocalDebug) LOG.info("             - set _kerberosKeyTab to " + display(_kerberosKeyTab));
                            if (_doLocalDebug) LOG.info("             - set _kerberosPrincipal to " + display(_kerberosPrincipal));
                            if (_doLocalDebug) LOG.info("             - set _doDebug to " + display(_doDebug));

                        }
                        break;

                    default :
                        
                        if (_doLocalDebug) LOG.info("           - (" + Integer.toString(i) + ") " + callbacks[i].toString());
                        
                        if (!myReadOption)
                        {
                            updated.add(callbacks[i]);
                        }
                        break;
                }
            }
            catch (LoginException myException)
            {
            	throw new UnsupportedCallbackException(callbacks[i], myException.toString());
            }
        }

        if (updated.size() > 0) {
            Callback[] pruned = updated.toArray(EMPTY);
            super.handle(pruned);
        }

    }
    
    private static java.lang.String display(java.lang.String stringIn, java.lang.String replacementIn)
    {
    	if ((null != stringIn) && (0 < stringIn.length()))
    	{
    		if (null != replacementIn)
    		{
        		return replacementIn;
    		}
    		else
    		{
        		return "\"" + stringIn + "\"";
    		}
    	}
    	else
    	{
    		return "<null>";
    	}
    }
    
    private static java.lang.String display(java.lang.String stringIn)
    {
    	if (null != stringIn)
    	{
    		return "\"" + stringIn + "\"";
    	}
    	else
    	{
    		return "<null>";
    	}
    }
    
    private static java.lang.String display(Object objectIn)
    {
    	if (null != objectIn)
    	{
    		return objectIn.toString();
    	}
    	else
    	{
    		return "<null>";
    	}
    }
}
