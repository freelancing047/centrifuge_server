package test.security.cas;

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

import org.apache.log4j.Logger;

import centrifuge.dao.Users;
import centrifuge.dao.jpa.GroupDAOBean;
import centrifuge.license.LicenseManager;
import csi.server.common.identity.Group;
import csi.server.common.identity.User;
import csi.server.common.exception.CentrifugeException;
import csi.server.ws.filter.JPATransactionFilter;
import centrifuge.ws.rest.services.user.RoleSupport;

public class Wrapper implements Filter {

    Logger log = Logger.getLogger(Wrapper.class);

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain chain) throws IOException, ServletException {
        ServletRequest request = sreq;
        ServletResponse response = sresp;

        request = new UserWrapper((HttpServletRequest) sreq);

        chain.doFilter(request, response);

    }

    @Override
    public void init(FilterConfig config) throws ServletException {

        log.info("Initializing our CAS servlet filter for wrapped retrieval of user info");

    }

    class UserWrapper extends HttpServletRequestWrapper {

        static final String GROUP_QUERY = "select group_name from GroupMembershipView where role_name=?";

        List<String> roles;
        String name;

        public UserWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public Principal getUserPrincipal() {
            Principal principal = super.getUserPrincipal();
            if (principal == null) {
                final String name = super.getRemoteUser();
                if (name != null) {
                    principal = new Principal() {

                        @Override
                        public String getName() {
                            return name;
                        }

                    };

                    User user = Users.findByName(name);
                    if (user == null) {
                        addUser(name);
                        user = Users.findByName(name);
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
                if (log.isDebugEnabled()) {
                    log.debug("searching for group membership");
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

        private Connection getConnection() throws SQLException {
            Connection connection = DriverManager.getConnection("jdbc:derby:MetaDB");

            return connection;
        }

        private boolean addUser(String username) {
            boolean rc = false;
            User user = new User();
            user.setName(username);
            user.setRemark("Auto-registered by CAS");

            UUID random = UUID.randomUUID();
            user.setPassword(random.toString());

            try {
                long allowed = LicenseManager.getUserCount();
                long current = Users.getUserCount();

                if (++current > allowed) {
                    log.info("Cannot auto-register unknown user " + username + "; licensed user count (" + allowed + ") exceeded");
                } else {
                    Users.add(user);
                    log.info("Previously unknown user " + username + " auto-registered by CAS");
                    GroupDAOBean bean = new GroupDAOBean();
                    bean.setEntityManager(JPATransactionFilter.getEntityManager());
                    Group group = bean.findByName(RoleSupport.AUTHENTICATED_ROLE);
                    if (group != null) {
                        group.getMembers().add(user);
                    }
                    bean.merge(group);
                    rc = true;
                }
            } catch (CentrifugeException cex) {
                log.info("CAS User filter encountered errors auto-registering user " + username);
                cex.printStackTrace();
            }
            return rc;
        }

    }

}
