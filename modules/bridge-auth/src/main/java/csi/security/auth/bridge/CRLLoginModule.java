package csi.security.auth.bridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;

import centrifuge.security.jaas.spi.callback.X509Callback;

/**
 * 
 * This class performs a certificate revocation check against a provided CRL
 * list.
 * 
 * The actions of CRL checks is separated from authentication. This is to induce
 * re-use and composability of the various actions that go into the
 * authentication phase.
 * 
 * @author Centrifuge Systems Inc.
 * 
 */
public class CRLLoginModule implements LoginModule {

    private static final long ONE_DAY = 24 * 60 * 60 * 1000;

    static synchronized void ensureRetrievalTask(String revocationListURL, String localCopyURL) {
        try {
            if (fetcher == null) {
                File localFile = new File(localCopyURL);

                URL remote = new URL(revocationListURL);
                URL local = localFile.toURI().toURL();
                fetcher = new URLRetrievalTask(remote, new File(local.toString()));
                timer = new Timer(true);

                // tweak off by 10 seconds....
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, 10);
                Date delta = calendar.getTime();
                timer.schedule(fetcher, delta, ONE_DAY);
            }
        } catch (MalformedURLException e) {
            Logger.getRoot().warn("Failed initializing CRL retrieval", e);
        }
    }

    static Timer timer;
    static TimerTask fetcher;

    static final String CRL_REMOTE_LOCATION_PROPERTY = "crl.url";

    static final String CRL_LOCATION_DEFAULT = "conf/localCheck.crl";

    private Logger log = Logger.getLogger(CRLLoginModule.class);

    private CallbackHandler handler;

    private Map<String, ?> sharedState;

    private Map<String, ?> options;

    private Subject subject;

    private X509Certificate[] peer;

    private String revocationListURL;

    private String localCopyURL;

    private Collection<X509CRL> crls;

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean commit() throws LoginException {
        return false;
    }

    @Override
    public void initialize(Subject subject, CallbackHandler handler, Map<String, ?> sharedState, Map<String, ?> options) {

        this.subject = subject;
        this.handler = handler;
        this.sharedState = sharedState;
        this.options = options;

        String uri = (String) options.get(CRL_REMOTE_LOCATION_PROPERTY);
        uri = (uri == null || uri.trim().length() == 0) ? null : uri.trim();
        if (uri != null) {
            revocationListURL = uri;
        }

        localCopyURL = CRL_LOCATION_DEFAULT;

        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            FileInputStream istream = new FileInputStream(localCopyURL);
            crls = (Collection<X509CRL>) factory.generateCRLs(istream);
        } catch (Throwable t) {
            log.warn("Unable to load the revocation list", t);
        }

        ensureRetrievalTask(revocationListURL, localCopyURL);

    }

    @Override
    public boolean login() throws LoginException {

        if (crls == null || crls.size() == 0) {
            log.warn("No CRLs present for revocation checking.");
            if (log.isInfoEnabled()) {
                log.info("Ensure CRLs are properly configured.");
            }
            return false;
        }

        if (this.handler == null) {
            log.warn("No callback handler registerd, unable to authenticate to perform revocation checks");
            return false;
        }

        Callback[] callbacks = configureCallbacks();

        // !!! strict failures of requiring client certificates must be handled
        // by the
        // web-container.
        // assume that any conditions regarding strong authentication are either
        // handled by
        // the container or configured login modules prior to this one !!!
        try {
            handler.handle(callbacks);
        } catch (IOException e) {
            log.warn("Encountered an unexpected error while retrieving the peer's certificate", e);
            return false;
        } catch (UnsupportedCallbackException e) {
            log.warn("Unable to retrieve the peer's certificates", e);
            log.info("Ensure that your configuration requires peer certificates for authentication");
            return false;
        }

        X509Callback chainCB = (X509Callback) callbacks[0];
        peer = chainCB.getChain();

        if (peer == null || peer.length == 0) {
            log.info("Peer did not authenticate using certificates, skipping revocation checks");
            return false;
        }

        if (isRevoked(peer[0])) {
            log.info("Issuing authority has revoked the certificate for " + peer[0].getSubjectDN().toString());
            throw new LoginException("Peer certificate is revoked");
        }

        return false;
    }

    /*
     * Assumption is that we really have only _one_ CRL; but we'll deal with
     * multiple CRLs, just in case.
     */
    private boolean isRevoked(X509Certificate certificate) {
        boolean revoked = false;
        Iterator<X509CRL> iterator = crls.iterator();
        while (iterator.hasNext() && !revoked) {
            X509CRL crl = iterator.next();
            if (crl.isRevoked(certificate)) {
                revoked = true;
            }
        }

        return revoked;
    }

    @Override
    public boolean logout() throws LoginException {
        return false;
    }

    private Callback[] configureCallbacks() {
        Callback[] callbacks = new Callback[1];
        callbacks[0] = new X509Callback();
        return callbacks;
    }

}
