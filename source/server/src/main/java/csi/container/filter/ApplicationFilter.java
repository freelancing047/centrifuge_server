package csi.container.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.Hashtable;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.catalina.Globals;

import csi.log.LogThreadContextUtil;
import csi.security.Authorization;
import csi.security.AuthorizationFactory;
import csi.security.CsiSecurityManager;
import csi.security.loginevent.EventReasons;
import csi.security.loginevent.LoginEventService;
import csi.startup.Product;

public class ApplicationFilter implements Filter, HttpSessionListener {
   private static final String AUTHORIZATION = "csi.authorization";

   private static ThreadLocal<ServletContext> webappReference = new ThreadLocal<ServletContext>();
   private static Map<String,Authorization> authorizations = new Hashtable<String,Authorization>();

   public static Authorization getAuthorization(String sessionId) {
      return authorizations.get(sessionId);
   }

   public static ServletContext getCurrentServletContext() {
      return webappReference.get();
   }

   @Override
   public void destroy() {
      webappReference = null;
   }

   @Override
   public void init(FilterConfig config) throws ServletException {
   }

   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
         throws IOException, ServletException {
      ServletContext servletContext = request.getServletContext();

      // prefer explicit setting instead of overriding initialValue on the ThreadLocal instance.
      webappReference.set(servletContext);

      try {
         if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpSession session = httpRequest.getSession();

            associateAuthorizationToken(httpRequest, session);
            LogThreadContextUtil.putRequestThreadContext(httpRequest);
         }
         chain.doFilter(request, response);
      } finally {
         LogThreadContextUtil.clearThreadContext();
         unassociateAuthorizationToken(request);
         webappReference.remove();
      }
   }

   private static void associateAuthorizationToken(HttpServletRequest request, HttpSession session) {
      Principal principal = request.getUserPrincipal();
      Subject subject = (Subject) session.getAttribute(Globals.SUBJECT_ATTR);
//        X509Certificate[] certs = (X509Certificate[]) request.getAttribute(Globals.CERTIFICATES_ATTR);
//        Authorization authorization = AuthorizationFactory.createAuthorization(certs, principal, subject);
      Authorization authorization = AuthorizationFactory.createAuthorization(principal, subject);

      session.setAttribute(AUTHORIZATION, authorization);

      if (!authorizations.containsKey(session.getId())) {
         authorizations.put(session.getId(), authorization);
      }
      CsiSecurityManager.setAuthorization(authorization);
   }

   private static void unassociateAuthorizationToken(ServletRequest request) {
      CsiSecurityManager.clearAuthorization();
      request.removeAttribute(AUTHORIZATION);
   }

   @Override
   public void sessionCreated(HttpSessionEvent sessionEvent) {
   }

   @Override
   public void sessionDestroyed(HttpSessionEvent sessionEvent) {
      HttpSession session = sessionEvent.getSession();
      String id = session.getId();

      if (authorizations.containsKey(id)) {
         String userName = ((Authorization) session.getAttribute(AUTHORIZATION)).getName();

         authorizations.remove(id);
         CsiSecurityManager.clearAuthorization();

         if (Product.getLicense().removeUserFromLicense(userName, EventReasons.LOGOUT_INACTIVITY_TIMEOUT)) {
            LoginEventService.saveLogoutTimeout(userName);
         }
      }
   }
}
