package csi.security.jaas.spi.gsss;

import gov.software.services.security.tomcat.X509Callback;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import csi.license.LicenseManager;
import csi.security.jaas.spi.SimpleLoginModule;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.identity.User;
import csi.server.dao.CsiPersistenceManager;
import csi.server.dao.Users;

public class AutoRegistrationModule extends SimpleLoginModule {

    protected Class<X509Callback> x509Callback = X509Callback.class;

    @Override
    public boolean login() throws LoginException {
        if (!enabled) {
            if (log.isTraceEnabled()) {
                log.trace("Auto-Registration security module is not enabled; nothing to do");
                return false;
            }
        }

        try {
            Callback[] callbacks = new Callback[1];
            callbacks[0] = getCertificateCallback();
            handler.handle(callbacks);

            X509Certificate[] peerChain = getCertificatePath(callbacks[0]);
            if (peerChain == null || peerChain.length == 0) {
                return false;
            }

            String userName = getUserDNFromCertificatePath(peerChain);

            User user = Users.findByName(userName);
            if (user == null) {
                addUser(userName);
                EntityManager em = CsiPersistenceManager.getMetaEntityManager();
                if (em != null) {
                    em.getTransaction().commit();
                }
                user = Users.findByName(userName);
            }
        } catch (IOException e) {
           LOG.warn("Auto-Register security module encountered an error" + e.getMessage());
            e.printStackTrace();
        } catch (UnsupportedCallbackException e) {
            LOG.warn("Auto-Register security module is not configured properly.  Please verify that there are not multiple copies of the csi-security-jaas.jar installed with your server" + e.getMessage());
        }
        return false;
    }

    private X509Certificate[] getCertificatePath(Callback callback) {

        X509Certificate[] path = null;
        if (x509Callback.isInstance(callback)) {
            path = x509Callback.cast(callback).getCertificates();
        }
        return path;
    }

    private Callback getCertificateCallback() {
        Callback callback = new X509Callback();
        return callback;
    }

    private String getUserDNFromCertificatePath(X509Certificate[] certPath) {

        X509Certificate cert = certPath[0];

        String dn = cert.getSubjectX500Principal().getName();
        String attr = null;
        String work = null;
        boolean hit = false;

        // break the string at commas (have to be careful here because you
        // might have values that contain commas. values like that will be
        // quoted and you'd be able to reconstruct the value so not too much
        // worry)
        StringTokenizer stk = new StringTokenizer(dn, ",");

        while (stk.hasMoreTokens()) {
            attr = (String) stk.nextToken();
            work = attr.toLowerCase().trim();
            // look for the common name attribute
            if (work.startsWith("cn")) {
                hit = true;
                break;
            }
        }

        String uname = null;

        if (hit) {
            int x = attr.indexOf("=");

            if (x > 0) {
                uname = attr.substring(x + 1);
                uname = uname.trim();
            }
        }
        return uname;
    }

    private boolean addUser(String username) {
        boolean rc = false;
        User user = new User();
        user.setName(username);
        user.setRemark("Auto-registered");

        UUID random = UUID.randomUUID();
        user.setPassword(random.toString());

        try {
            long allowed = LicenseManager.getUserCount();
            long current = Users.getUserCount();

            if (++current > allowed) {
                LOG.info("Cannot auto-register user " + username + "; licensed user count (" + allowed + ") exceeded");
            } else {
                EntityManager entityManager = CsiPersistenceManager.getMetaEntityManager();
                EntityTransaction txn = entityManager.getTransaction();
                txn.begin();

                Users.add(user);

                EntityManager em = CsiPersistenceManager.getMetaEntityManager();
                if (em != null) {
                    em.getTransaction().commit();
                }

                if (LOG.isInfoEnabled()) {
                   LOG.info("Auto-registering new user " + username);
                }

                txn.commit();
                rc = true;
            }
        } catch (CentrifugeException cex) {
           LOG.info("Auto-Registration encountered errors while attempting to add a new user " + username + cex.getMessage());
        }
        return rc;
    }

}
