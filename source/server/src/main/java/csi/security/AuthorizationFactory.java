package csi.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

public class AuthorizationFactory {
   public static Authorization createAuthorization(Principal user, Subject subject) {
      return new GenericAuthorization(user, subject);
   }

   /*
    * public static Authorization createAuthorization(X509Certificate[] certs, Principal principal, Subject subject) {
    *    return new GenericAuthorization(certs, principal, subject);
    * }
    */
   public static Authorization getAuthorization(String name, String digestedPassword) throws GeneralSecurityException {
      LoginContext context = new LoginContext("centrifuge", new LocalCallbackHandler(name, digestedPassword));

      context.login();
      return new GenericAuthorization(name, context.getSubject());
   }

   public static class LocalCallbackHandler implements CallbackHandler {
      private String name;
      private String digestedPass;

      public LocalCallbackHandler(String name, String digested) {
         this.name = name;
         this.digestedPass = digested;

         if (digestedPass == null) {
            digestedPass = "";
         }
      }

      @Override
      public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
         for (int i = 0; i < callbacks.length; i++) {
            Callback cb = callbacks[i];

            if (cb instanceof NameCallback) {
               NameCallback nameCB = (NameCallback) cb;
               nameCB.setName(name);
            } else if (cb instanceof PasswordCallback) {
               PasswordCallback pwdCB = (PasswordCallback) cb;
               pwdCB.setPassword(digestedPass.toCharArray());
            } else {
               throw new UnsupportedCallbackException(cb);
            }
         }
      }
   }
}
