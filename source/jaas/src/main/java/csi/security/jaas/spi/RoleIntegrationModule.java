package csi.security.jaas.spi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import csi.security.jaas.JAASRole;

public class RoleIntegrationModule extends SimpleLoginModule {
    private static final String _dataSourceName = "java:comp/env/jdbc/MetaDB";
    private static final String _groupQuery = "SELECT group_name FROM GroupMembershipView WHERE (role_name=?)";

    private static boolean _doDebug = LOG.isDebugEnabled();

    private DataSource _dataSource = null;
    private String _userName = null;
    private Map<String, JAASRole> _groupMap = null;

    public static <T> T lookupResource(Class<T> clazz, String resUrl) throws NamingException {
        Context initCtx = null;
        try {
            initCtx = new InitialContext();
            Object obj = initCtx.lookup(resUrl);
            return (T) obj;
        } finally {
            try {
                if (initCtx != null) {
                    initCtx.close();
                }
            } catch (NamingException e) {
                // ignore
            }
        }
    }

    public boolean abort() throws LoginException {
        return false;
    }

    public boolean commit() throws LoginException {
        subject.getPrincipals().addAll(_groupMap.values());
        return true;
    }

    public boolean login() throws LoginException {
    	
        try
        {
	        Callback[] myCallbacks = new Callback[] { new NameCallback(" User name: ") };

            handler.handle(myCallbacks);
            
        	_userName = ((NameCallback)myCallbacks[0]).getName();
            _dataSource = lookupResource(DataSource.class, _dataSourceName);
            _groupMap = new HashMap<String, JAASRole>();
	        
        	getStoredRoles();
        	getConfiguredRoles();
        }
        catch (Exception myException)
        {
        	LoginException myLoginException = new LoginException("Caught exception integrating user roles: " + myException.getMessage());
        	myLoginException.setStackTrace(myException.getStackTrace());
            throw myLoginException;
        }
    	
        return true;
    }

    /**
     * Populates this user's roles with all known groups the user is a member.
     * 
     * This performs a nested search such that Groups of Groups are resolved.
     * For example if the user foo is a member of G1, G1 is a member of G2, then
     * the resolved roles for the user include both G1 and G2.
     * 
     * @param userIn
     * @throws Exception
     */
    private void getStoredRoles() throws Exception
    {
        Connection myConnection = null;
        try
        {
        	myConnection = _dataSource.getConnection();
            PreparedStatement myStatement = myConnection.prepareStatement(_groupQuery);
            myStatement.setString(1, _userName);
            ResultSet myResults = myStatement.executeQuery();
            if (_doDebug)
            {
                LOG.debug("Searching database for group membership");
            }
            Stack<String> myPendingList = new Stack<String>();
            // search for the first level of group membership for this user --
            // we'll
            // skip
            // any groups for which there is already an existing role name
            while (myResults.next())
            {
                String myGroup = myResults.getString(1);
                if (_doDebug)
                {
                   LOG.debug(" -- found stored group " + myGroup);
                }
                if (!_groupMap.containsKey(myGroup))
                {
	                if (_doDebug)
	                {
	                   LOG.debug("     -- add stored group " + myGroup);
	                }
                	_groupMap.put(myGroup, new JAASRole(myGroup));
                    myPendingList.push(myGroup);
                }
            }
            myResults.close();
            if (_doDebug)
            {
               LOG.debug("           - First Level Group search complete.");
               LOG.debug("           - Checking for nested group membership");
            }
            while (!myPendingList.empty())
            {
                String myParentRole = myPendingList.pop();
                myStatement.setString(1, myParentRole);
                myResults = myStatement.executeQuery();
                while (myResults.next())
                {
                    String myNestedGroup = myResults.getString(1);
	                if (_doDebug)
	                {
	                   LOG.debug(" -- found stored group " + myNestedGroup);
	                }
                    if (!_groupMap.containsKey(myNestedGroup))
                    {
		                if (_doDebug)
		                {
		                   LOG.debug("     -- add stored group " + myNestedGroup);
		                }
                    	_groupMap.put(myNestedGroup, new JAASRole(myNestedGroup));
                        myPendingList.push(myNestedGroup);
                    }
                }
                myResults.close();
           }
        }
        finally
        {
            if (null != myConnection)
            {
                try
                {
                	myConnection.close();
                }
                catch (Exception myException)
                {
                	LOG.error("           - caught exception:" + display(myException));
                }
            }
        }
    }

    private void getConfiguredRoles() {
        String myConfigurationString = (String) options.get(Constants.PROPERTY_ROLE_NAMES);
        if ((null != myConfigurationString) && (0 < myConfigurationString.length()))
        {
            String[] myGroups = myConfigurationString.split(",");
            
            if (_doDebug)
            {
               LOG.debug("Adding configured group membership.");
            }

	        for (int i = 0; myGroups.length > i; i++)
	        {
	            String myGroup = myGroups[i].trim();
	            
	            if (0 < myGroup.length())
	            {
	                if (_doDebug)
	                {
	                   LOG.debug(" -- found configured group " + myGroup);
	                }
	            	if (!_groupMap.containsKey(myGroup))
	            	{
		                if (_doDebug)
		                {
		                   LOG.debug("     -- add configured group " + myGroup);
		                }
	            		_groupMap.put(myGroup, new JAASRole(myGroup));
	            	}
	            }
	        }
        }
    }

    private static String display(Throwable exceptionIn)
    {
    	if ((null != exceptionIn) && (null != exceptionIn.getMessage())
    			&& (0 < exceptionIn.getMessage().length()))
    	{
    		return "\n" + exceptionIn.getMessage();
    	}
    	else
    	{
    		return "<null>";
    	}
    }
}
