package csi.security.jaas;

import org.apache.logging.log4j.ThreadContext;

/**
 * Created by centrifuge on 9/14/2017.
 */
public class LogThreadContextUtil {
   public static void putUserName(String userName) {
      if (userName != null) {
         ThreadContext.put(LogThreadContextKey.USER, trim(userName));
      }
   }

   public static void putDistinguishedName(String distinguishedName) {
      if (distinguishedName != null) {
         ThreadContext.put(LogThreadContextKey.DN, trim(distinguishedName));
      }
   }

   public static void clearDistinguishedName() {
      ThreadContext.remove(LogThreadContextKey.DN);
   }

   private static String trim(String string) {
      return (string == null) ? null : string.trim();
   }
}
