package csi.web.filters;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class BlacklistFilter implements Filter {
   protected static final Logger LOG = LogManager.getLogger(BlacklistFilter.class);
   
    private static final String PARAMETER_DENY = "deny";
    static final String DEFAULT_DENY_ROLE = "Blacklisted"; //$NON-NLS-1$
    static final String GUEST_NAME = "Guest";

    protected String denyRole;
    protected Messages Messages = new Messages();

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest sRequest, ServletResponse sResponse, FilterChain chain) throws IOException, ServletException {
        if (sRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) sRequest;
            boolean denied = request.isUserInRole(denyRole);
            if (denied && log.isInfoEnabled()) {
                Principal user = request.getUserPrincipal();
                String name = (user == null) ? GUEST_NAME : user.toString();
                String logMessage = String.format(Messages.getString("block.user"), name);
                LOG.info(logMessage);
                HttpServletResponse response = (HttpServletResponse) sResponse;
                response.sendError(403);
                return;
            }
        }

        chain.doFilter(sRequest, sResponse);

    }

    @Override
    public void init(FilterConfig config) throws ServletException {

        denyRole = config.getInitParameter(PARAMETER_DENY);
        if (denyRole == null || denyRole.length() == 0) {
            denyRole = DEFAULT_DENY_ROLE;
        }

        if (LOG.isInfoEnabled()) {
           LOG.info(Messages.getString("initializing"));
            String template = Messages.getString("configured.role");
            String msg = String.format(template, denyRole);
            LOG.info(msg);
        }
    }

}
