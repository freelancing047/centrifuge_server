package csi.log;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.startup.Bootstrap;

/**
 * Intercept requests and add request information to log4j ThreadContext in order to be
 * appended to each log event
 */
public class LogThreadContextCometFilter implements Filter {
   private static final Logger LOG = LogManager.getLogger(LogThreadContextCometFilter.class);

   @Override
   public void init(FilterConfig config) throws ServletException {
      LOG.info("Log ThreadContext Async Servlet Filter initialized");
   }

   @Override
   public void destroy() {
      LOG.info("Log ThreadContext Async Servlet Filter removed");
      Bootstrap.destroyContext();
   }

   /**
    * Filter normal Http Servlet requests
    */
   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
         throws IOException, ServletException {
      try {
         if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            LogThreadContextUtil.putRequestThreadContext(httpRequest);
         }
         LOG.trace("Log ThreadContext Servlet Filter doFilter");
         chain.doFilter(request, response);
      } finally {
         LogThreadContextUtil.clearThreadContext();
      }
   }
}
