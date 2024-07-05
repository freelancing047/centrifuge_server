package csi.security.jaas.spi;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleLoginModule implements LoginModule {
   protected static final Logger LOG = LogManager.getLogger(RoleAssignmentModule.class);  //NOTE different class
   
   private static final String ENABLED = "enabled";

   protected Subject subject;
   protected CallbackHandler handler;
   protected Map<String,?> options;
   protected Map<String,?> sharedState;
   protected boolean enabled;

   @Override
   public boolean abort() throws LoginException {
      return false;
   }

   @Override
   public boolean commit() throws LoginException {
      return false;
   }

   public void initialize(Subject subject, CallbackHandler handler, Map<String, ?> sharedState, Map<String, ?> options) {
      this.subject = subject;
      this.handler = handler;
      this.options = options;
      this.sharedState = sharedState;
      String s = options.containsKey(ENABLED) ? options.get(ENABLED).toString() : "false";
      enabled = Boolean.parseBoolean(s);
   }

   @Override
   public boolean login() throws LoginException {
      return false;
   }

   @Override
   public boolean logout() throws LoginException {
      return false;
   }
}
