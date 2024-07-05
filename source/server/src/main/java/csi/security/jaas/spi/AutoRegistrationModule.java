package csi.security.jaas.spi;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import csi.config.Configuration;
import csi.config.SecurityPolicyConfig;
import csi.security.jaas.spi.callback.X509Callback;
import csi.security.queries.Users;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.identity.User;
import csi.server.dao.CsiPersistenceManager;
import csi.startup.Product;

public class AutoRegistrationModule extends SimpleLoginModule {
   @Override
   public boolean login() throws LoginException {
      if (!enabled) {
         if (LOG.isTraceEnabled()) {
            LOG.trace("Auto-Registration security module is not enabled; nothing to do");
         }
         return false;
      }

      try {
         Callback[] callbacks = new Callback[1];
         callbacks[0] = new X509Callback();
         handler.handle(callbacks);

         X509Callback certCallback = (X509Callback) callbacks[0];
         X509Certificate[] peerChain = certCallback.getChain();
         if ((peerChain == null) || (peerChain.length == 0)) {
            return false;
         }

         // Make sure expired user accounts have been disabled sometime today
         Users.checkExpiration(false);

         String userName = certCallback.getName();

         User user = Users.getUserByName(userName);
         if (user == null) {
            addUser(userName);
            EntityManager em = CsiPersistenceManager.getMetaEntityManager();
            if (em != null) {
               em.getTransaction().commit();
            }
//            user = Users.getUserByName(userName);
         }
      } catch (IOException e) {
         LOG.warn("Auto-Register security module encountered an error", e);
         e.printStackTrace();
      } catch (UnsupportedCallbackException e) {
         LOG.warn("Auto-Register security module is not configured properly.  Please verify that "
               + "there are not multiple copies of the csi-security-jaas.jar installed with your server", e);
      } finally {
         CsiPersistenceManager.close();
      }

      return false;
   }

   private boolean addUser(String username) {
      boolean rc = false;
      SecurityPolicyConfig policy = Configuration.getInstance().getSecurityPolicyConfig();
      User user = new User();
      UUID random = UUID.randomUUID();

      user.setName(username);
      user.setRemark("Auto-registered");
      policy.applyExpirationPolicy(user);
      user.setPassword(random.toString());

      try {
         if (Product.getLicense().addingUserAllowed((int) Users.getUserCount())) {
            EntityManager entityManager = CsiPersistenceManager.getMetaEntityManager();
            EntityTransaction txn = entityManager.getTransaction();

            txn.begin();
            Users.add(user);

            EntityManager em = CsiPersistenceManager.getMetaEntityManager();

            if (em != null) {
               em.getTransaction().commit();
            }
            if (LOG.isInfoEnabled()) {
               LOG.info("Auto-registering new user {}", () -> username);
            }
            txn.commit();

            rc = true;
         } else {
            LOG.info("Cannot auto-register user {}; licensed user count ({}) exceeded", () -> username,
                  () -> Integer.toString(Product.getLicense().getUserCount()));
         }
      } catch (CentrifugeException cex) {
         LOG.info("Auto-Registration encountered errors while attempting to add a new user " + username, cex);
      }
      return rc;
   }
}
