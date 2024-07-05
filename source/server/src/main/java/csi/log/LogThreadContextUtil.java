package csi.log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.Principal;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.ThreadContext;

import csi.security.Authorization;
import csi.security.jaas.JAASPrincipal;
import csi.security.jaas.LogThreadContextKey;
import csi.server.task.api.TaskContext;

/**
 * Utility methods for LogThreadContext manipulation
 */
public class LogThreadContextUtil {
   private static Locale locale;

   /**
    * Write values from the Http request to ThreadContext
    *
    * @param request
    */
   public static void putRequestThreadContext(HttpServletRequest request) {
      if (request != null) {
         Principal principal = request.getUserPrincipal();
         JAASPrincipal jaasPrincipal = (principal instanceof JAASPrincipal) ? (JAASPrincipal) principal : null;
         String methodName = request.getMethod();
         String remoteUser = trim(request.getRemoteUser());
         String sessionId = trim(request.getRequestedSessionId());
         String remoteAddr = trim(request.getRemoteAddr());
         String actionUri = trim(request.getRequestURI());
         String serverIp = trim(request.getLocalAddr());

         locale = request.getLocale();

         ThreadContext.put(LogThreadContextKey.APPLICATION_ID, "Centrifuge");

         if (principal != null) {
            String user = trim(principal.getName());

            if (user != null) {
               ThreadContext.put(LogThreadContextKey.USER, user);
            }
         }
         if (jaasPrincipal != null) {
            String dn = trim(jaasPrincipal.getDN());

            if (dn != null) {
               ThreadContext.put(LogThreadContextKey.DN, dn);
            }
         }
         if (methodName != null) {
            ThreadContext.put(LogThreadContextKey.METHOD_NAME, methodName);
         }
         if (remoteUser != null) {
            ThreadContext.put(LogThreadContextKey.REMOTE_USER, remoteUser);
         }
         if (sessionId != null) {
            ThreadContext.put(LogThreadContextKey.SESSION_ID, sessionId);
         }
         if (remoteAddr != null) {
            ThreadContext.put(LogThreadContextKey.REMOTE_ADDR, remoteAddr);
         }
         if (actionUri != null) {
            ThreadContext.put(LogThreadContextKey.ACTION_URI, actionUri);
         }
         if (serverIp != null) {
            ThreadContext.put(LogThreadContextKey.SERVER_IP, serverIp);
         }
      }
   }

   public static void putContextThreadContext(TaskContext context) {
      if (context != null) {
         Authorization authorization = context.getSecurityToken();
         String myMethod = context.getMethodName();
         String remoteUser = trim(context.getRemoteUser());
         String sessionId = trim(context.getSessionId());
         String remoteAddr = trim(context.getClientIP());
         String actionUri = trim(context.getRequestURL());
         String serverIp = getServerIp();

         ThreadContext.put(LogThreadContextKey.APPLICATION_ID, "Centrifuge");

         if (authorization != null) {
            String user = trim(authorization.getName());
            String dn = trim(authorization.getDistinguishedName());

            if (user != null) {
               ThreadContext.put(LogThreadContextKey.USER, user);
            }
            if (dn != null) {
               ThreadContext.put(LogThreadContextKey.DN, dn);
            }
         }
         if (myMethod != null) {
            ThreadContext.put(LogThreadContextKey.METHOD_NAME, myMethod);
         }
         if (remoteUser != null) {
            ThreadContext.put(LogThreadContextKey.REMOTE_USER, remoteUser);
         }
         if (sessionId != null) {
            ThreadContext.put(LogThreadContextKey.SESSION_ID, sessionId);
         }
         if (remoteAddr != null) {
            ThreadContext.put(LogThreadContextKey.REMOTE_ADDR, remoteAddr);
         }
         if (actionUri != null) {
            ThreadContext.put(LogThreadContextKey.ACTION_URI, actionUri);
         }
         if (serverIp != null) {
            ThreadContext.put(LogThreadContextKey.SERVER_IP, serverIp);
         }
      }
   }

   public static void putUserName(String userName) {
      if (userName != null) {
         ThreadContext.put(LogThreadContextKey.USER, userName);
      }
   }

   public static void putDistinguishedName(String distinguishedName) {
      if (distinguishedName != null) {
         ThreadContext.put(LogThreadContextKey.DN, distinguishedName);
      }
   }

   public static void clearDistinguishedName() {
      ThreadContext.remove(LogThreadContextKey.DN);
   }

   public static Locale getLocale() {
      return locale;
   }

   public static void clearThreadContext() {
      ThreadContext.remove(LogThreadContextKey.APPLICATION_ID);
      ThreadContext.remove(LogThreadContextKey.METHOD_NAME);
      ThreadContext.remove(LogThreadContextKey.USER);
      ThreadContext.remove(LogThreadContextKey.DN);
      ThreadContext.remove(LogThreadContextKey.REMOTE_USER);
      ThreadContext.remove(LogThreadContextKey.SESSION_ID);
      ThreadContext.remove(LogThreadContextKey.REMOTE_ADDR);
      ThreadContext.remove(LogThreadContextKey.ACTION_URI);
      ThreadContext.remove(LogThreadContextKey.SERVER_IP);
   }

   private static String getServerIp() {
      String myServerIp = null;

      try {
         myServerIp = InetAddress.getLocalHost().getHostAddress();
      } catch (Exception ignore1) {
      }
      if (myServerIp == null) {
         try {
            myServerIp = Inet4Address.getLocalHost().getHostAddress();
         } catch (Exception ignore2) {
         }
      }
      return trim(myServerIp);
   }

   private static String trim(String string) {
      return (string == null) ? null : string.trim();
   }
}
