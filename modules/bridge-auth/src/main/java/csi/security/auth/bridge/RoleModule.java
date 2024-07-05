package csi.security.auth.bridge;

import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;

import csi.bridge.logging.BridgeConstants;

public class RoleModule implements LoginModule {

    private static final String PROPERTY_USER_ROLE_TABLE = "userRoleTable";
    private static final String GROUP_QUERY = "select group_name from GroupMembershipView where role_name=?";
    private static final String DEFAULT_DB = "jdbc:derby:MetaDB";

    class UserInfo {

        String name;

        Object credential;

        Map<String, BridgeRole> roles = new HashMap<String, BridgeRole>();
    }

    protected Logger log = Logger.getLogger(RoleModule.class);

    private String dbUrl = DEFAULT_DB;

    private String dbUserName = null;
    private String dbPassword = null;

    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> sharedState;
    private Map<String, ?> options;

    private UserInfo userInfo;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {

        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    @Override
    public boolean login() throws LoginException {
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        String userName = (String) sharedState.get(BridgeConstants.AUTHENTICATED_USER);
        if (userName == null || userName.length() == 0) {
            return false;
        }

        try {
            userInfo = new UserInfo();
            userInfo.name = userName;

            populateGroups(userInfo);
            populateSubject();

            return true;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to properly establish the roles for user " + userInfo.name + ".  Some functionality may not be present for this login session.");
            }
        }

        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        return false;
    }

    protected Connection getConnection() throws Exception {

        return DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
    }

    private void populateGroups(UserInfo user) throws Exception {
        Connection connection = null;
        try {
            connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(GROUP_QUERY);
            statement.setString(1, user.name);
            ResultSet results = statement.executeQuery();
            if (log.isDebugEnabled()) {
                log.debug("searching for group membership");
            }
            Stack<String> pendingList = new Stack<String>();
            // search for the first level of group membership for this user --
            // we'll
            // skip
            // any groups for which there is already an existing role name
            while (results.next()) {
                String groupRole = results.getString(1);
                if (!user.roles.containsKey(groupRole)) {
                    BridgeRole role = new BridgeRole(groupRole);
                    user.roles.put(groupRole, role);
                    pendingList.push(groupRole);
                }
            }
            results.close();
            if (log.isDebugEnabled()) {
                log.debug("First Level Group search complete.");
                log.debug("Checking for nested group membership");
            }
            while (!pendingList.empty()) {
                String name = pendingList.pop();
                statement.setString(1, name);
                results = statement.executeQuery();
                while (results.next()) {
                    String nested = results.getString(1);
                    if (!user.roles.containsKey(nested)) {
                        BridgeRole role = new BridgeRole(nested);
                        user.roles.put(nested, role);
                        pendingList.push(nested);
                    }
                }
                results.close();
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                }
            }
        }

    }

    private void populateSubject() {
        Set<Principal> principals = subject.getPrincipals();

        Collection<BridgeRole> values = userInfo.roles.values();
        principals.addAll(values);
    }
}
