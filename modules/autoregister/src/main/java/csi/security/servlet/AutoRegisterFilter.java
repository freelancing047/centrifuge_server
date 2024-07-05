package csi.security.servlet;

import java.io.IOException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import centrifuge.dao.Users;
import centrifuge.dao.jpa.GroupDAOBean;
import centrifuge.license.LicenseManager;
import csi.server.common.identity.Group;
import csi.server.common.identity.User;
import csi.server.common.exception.CentrifugeException;
import centrifuge.ws.rest.services.user.RoleSupport;

public class AutoRegisterFilter implements Filter {
   protected static final Logger LOG = LogManager.getLogger(AutoRegisterFilter.class);

    static final String DEFAULT_DATASOURCE_URL = "jdbc:derby:MetaDB";

    protected String jdbcUrl;

    @Override
    public void init(FilterConfig configuration) throws ServletException {
        if (LOG.isInfoEnabled()) {
           LOG.info("Initializing user auto-registration filter.");
        }

        String dsname = configuration.getInitParameter("datasource.url");
        jdbcUrl = (dsname == null) ? "" : dsname.trim();
        if (jdbcUrl.length() == 0) {
            jdbcUrl = DEFAULT_DATASOURCE_URL;
        }

        try {
            Connection connection = getConnection();
            connection.close();
        } catch (SQLException e) {
            StringBuffer msg = new StringBuffer();
            msg.append("Auto-registration filter failed to initialize properly.  ");
            msg.append("If you are using an external administrative database, ensure that the proper location is specified.  ");
            msg.append("The configuration is currently specified as\n\t");
            msg.append(jdbcUrl);
            LOG.warn(msg.toString(), e);
        }

    }

    @Override
    public void destroy() {
        if (LOG.isInfoEnabled()) {
           LOG.info("Removing auto-registration filter.");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //
        // wrap the request if necessary and proceed with the chained filters...
        //
        if (request instanceof HttpServletRequest) {
            request = new RequestWrapper((HttpServletRequest) request);
        }

        chain.doFilter(request, response);
    }

    private Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);

        return connection;
    }

    private class RequestWrapper extends HttpServletRequestWrapper {

        static final String GROUP_QUERY = "select group_name from GroupMembershipView where role_name=?";

        private List<String> roles;

        private String name;

        public RequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public Principal getUserPrincipal() {
            Principal principal = super.getUserPrincipal();
            if (principal == null) {
                final String remoteName = super.getRemoteUser();
                if (remoteName != null) {
                    principal = new Principal() {

                        @Override
                        public String getName() {
                            return remoteName;
                        }
                    };

                    User user = Users.findByName(remoteName);
                    if (user == null) {
                        addUser(remoteName);
                        user = Users.findByName(remoteName);
                    }
                }

            }

            if (principal != null) {
                name = principal.getName();
            }

            return principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            if (roles == null) {
                populateRoles();

            }

            if (roles.contains(role)) {
                return true;
            }

            return super.isUserInRole(role);
        }

        private void populateRoles() {
            roles = new ArrayList<String>();
            if (name == null) {
                return;
            }

            try {
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(GROUP_QUERY);
                statement.setString(1, name);
                ResultSet results = statement.executeQuery();
                if (LOG.isDebugEnabled()) {
                   LOG.debug("Searching for group membership");
                }
                Stack<String> pendingList = new Stack<String>();
                // search for the first level of group membership for this user
                // --
                // we'll
                // skip
                // any groups for which there is already an existing role name
                while (results.next()) {
                    String groupRole = results.getString(1);
                    if (!roles.contains(groupRole)) {
                        roles.add(groupRole);
                        pendingList.push(groupRole);
                    }
                }
                results.close();
                if (LOG.isDebugEnabled()) {
                   LOG.debug("First Level Group search complete.");
                   LOG.debug("Checking for nested group membership");
                }
                while (!pendingList.empty()) {
                    String name = pendingList.pop();
                    statement.setString(1, name);
                    results = statement.executeQuery();
                    while (results.next()) {
                        String nested = results.getString(1);
                        if (!roles.contains(nested)) {
                            roles.add(nested);
                            pendingList.push(nested);
                        }
                    }
                    results.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private boolean addUser(String username) {
            boolean rc = false;
            User user = new User();
            user.setName(username);
            user.setRemark("Auto-registered");

            UUID random = UUID.randomUUID();
            user.setPassword(random.toString());

            try {
                long allowed = LicenseManager.getUserCount();
                long current = Users.getUserCount();

                if (++current > allowed) {
                   LOG.info("Cannot auto-register user " + username + "; licensed user count (" + allowed + ") exceeded");
                } else {
                    Users.add(user);
                    if (LOG.isInfoEnabled()) {
                       LOG.info("Auto-registering new user " + username);
                    }
                    GroupDAOBean bean = new GroupDAOBean();
                    Group group = bean.findByName(RoleSupport.AUTHENTICATED_ROLE);
                    if (group != null) {
                        group.getMembers().add(user);
                    }
                    bean.merge(group);
                    rc = true;
                }
            } catch (CentrifugeException cex) {
               LOG.info("Auto-Registration encountered errors while attempting to add a new user " + username, cex);
            }
            return rc;
        }

    }

}
