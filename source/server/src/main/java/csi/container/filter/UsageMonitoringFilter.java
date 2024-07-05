package csi.container.filter;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.startup.Product;

public class UsageMonitoringFilter implements Filter {
   private static final Logger LOG = LogManager.getLogger(UsageMonitoringFilter.class);

   @Override
   public void init(final FilterConfig config) throws ServletException {
   }

   private static void monitorUsage(final HttpServletRequest request) {
      Principal principal = request.getUserPrincipal();

      if (principal != null) {
         String user = principal.getName();

         if (user != null) {
            String sessionId = request.getRequestedSessionId();

            if (sessionId != null) {
               Product.getLicense().sessionActivityForUser(user, sessionId);
            }
         }
      }
   }

   @Override
   public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
         throws IOException, ServletException {
      if (request instanceof HttpServletRequest) {
         monitorUsage((HttpServletRequest) request);
      }
      LOG.trace("UsageMonitoringFilter Servlet Filter doFilter");
      chain.doFilter(request, response);
   }

   @Override
   public void destroy() {
   }
}
