package csi.security.auth.bridge;

import gov.ic.dodiis.service.security.dias.v2_1.authservice.ErrorType;
import gov.ic.dodiis.service.security.dias.v2_1.authservice.GetUserGroupsResponseType;
import gov.ic.dodiis.service.security.dias.v2_1.authservice.ProjectType;
import gov.ic.dodiis.service.security.dias.v2_1.authservice.UserGroupRequestType;
import gov.ic.dodiis.service.security.dias.v2_1.authservice.UserIdentifierType;
import gov.ic.dodiis.service.security.dias.v2_1.authservice.WhitePageAttributesType;

import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.x500.X500Principal;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import centrifuge.security.jaas.spi.callback.X509Callback;

import com.oculusinfo.ncompass.als._3.ALEMessageType;
import com.oculusinfo.ncompass.als._3.ALSInterface;
import com.oculusinfo.ncompass.als._3.ALSService;
import com.oculusinfo.ncompass.als._3.SessionEvent;

import csi.bridge.logging.BridgeConstants;
import csi.bridge.logging.Provider;
import dias.DIASMessageReceiver;
import dias.DIASMessageReceiverService;
import dias.Error;

/*
 * TODO: 
 * 
 * 1.  Need to perform CRL checking
 */
public class BridgeLoginModule implements LoginModule {
   protected static final Logger LOG = LogManager.getLogger(BridgeLoginModule.class);

    private static final String SERVICE_ENDPOINT_URL_PROPERTY = "service.url";

    // payload structure for current authentication pass
    class UserInfo {

        String name;

        Object credential;

        WhitePageAttributesType bridgeUserInfo;

        List<String> groupNames;
    }

    enum State {
        INITIAL, AUTHENTICATED, FAILED, LOGGED_IN, LOGGED_OUT, ABORTED
    }

    protected Subject subject;

    protected CallbackHandler handler;

    protected Map<String, Object> sharedState;

    protected Map<String, Object> options;

    protected String serviceEndpointURL;

    protected DIASMessageReceiverService service;

    protected ALSInterface loggingService;

    protected UserInfo currentUser;

    protected X509Certificate[] peerChain;

    protected State state;

    protected DIASMessageReceiver dias;

    private Provider typeProvider;

    public void initialize(Subject subject, CallbackHandler handler, Map<String, ?> shared, Map<String, ?> options) {
        currentUser = null;
        this.subject = subject;
        this.handler = handler;
        this.sharedState = (Map<String, Object>) shared;
        this.options = (Map<String, Object>) options;

        state = State.INITIAL;

        if (service == null) {
            URL localWSDL = DIASMessageReceiverService.class.getClassLoader().getResource("META-INF/services/dias.wsdl");
            service = new DIASMessageReceiverService(localWSDL, new QName("urn:dias", "DIASMessageReceiverService"));

            URL alsWSDL = ALSService.class.getClassLoader().getResource("META-INF/services/als.wsdl");
            QName alsName = new QName("http://oculusinfo.com/ncompass/als/3.0", "ALSService");
            ALSService alsService = new ALSService(alsWSDL, alsName);
            loggingService = alsService.getALSPort();
            try {
                typeProvider = new Provider();
            } catch (DatatypeConfigurationException e) {
               LOG.warn("Could not properly obtain the type factory, event publication to Bridge-IC is currently disabled" + e.getMessage());
                loggingService = null;
            }

        }

        this.serviceEndpointURL = (String) options.get(SERVICE_ENDPOINT_URL_PROPERTY);
        if (serviceEndpointURL != null && serviceEndpointURL.length() == 0) {
           LOG.debug("Overriding DIAS end-point with configured option");
            serviceEndpointURL = null;
        }

    }

    public boolean login() throws LoginException {

        if (this.handler == null) {
           LOG.warn("No callback handler registerd, unable to authenticate to Bridge-IC");
            throw new LoginException("Unable to retrieve certificates, no callback handler registered.");
        }
        currentUser = new UserInfo();

        Callback[] callbacks = configureCallbacks();

        try {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Retrieving peer certificate chain for authentication");
            }
            handler.handle(callbacks);
        } catch (UnsupportedCallbackException e) {
            throw new LoginException("Authentication system does not support authentication using X.509 Certificates. " + e.getMessage());
        } catch (IOException e) {
            throw new LoginException(e.getMessage());
        }

        X509Callback callback = (X509Callback) callbacks[0];
        peerChain = callback.getChain();
        if (peerChain.length == 0) {
           LOG.info("Cannot perform authentication without a certificate chain");
            return false;
        }

        try {
            dias = getConfiguredSEI();
            authenticate(peerChain);
            populateGroups();
            state = State.AUTHENTICATED;

            sharedState.put(BridgeConstants.AUTHENTICATED_USER, currentUser.name);

            if (LOG.isDebugEnabled()) {
               LOG.debug("Authentication succeeded for " + currentUser.name);
            }
            publishAuthenticateEvent(true);
            return true;
        } catch (Error e) {
            ErrorType faultInfo = e.getFaultInfo();
            LOG.info("Failed validating user security token " + currentUser.name + ": " + formatErrorCode(faultInfo));
        } catch (Throwable t) {
           LOG.warn("An unexpected error was encountered attempting to authenticate the current user: " + t.getMessage());
        }

        publishAuthenticateEvent(false);
        state = State.FAILED;
        return false;
    }

    private void populateGroups() throws Error {
        UserGroupRequestType groupRequest = new UserGroupRequestType();
        groupRequest.setUserDN(currentUser.name);
        groupRequest.getProjectName().add("BRIDGE");

        GetUserGroupsResponseType userGroups = dias.getUserGroups(groupRequest);

        List<ProjectType> projects = userGroups.getProjects();
        ProjectType bridge = null;
        for (Iterator<ProjectType> i = projects.iterator(); i.hasNext();) {
            bridge = i.next();
            if ("BRIDGE".equalsIgnoreCase(bridge.getProjectName())) {
                currentUser.groupNames = bridge.getGroupNames();
                break;
            }
        }
    }

    private void authenticate(X509Certificate[] chain) throws Error, CertificateException {
        X509Certificate peer = chain[0];

        try {
            peer.checkValidity();
        } catch (CertificateException ce) {
           LOG.info("User certificate is outside of registered time period" + ce.getMessage());
            throw ce;
        }

        Principal subjectDN = peer.getSubjectDN();
        String token = subjectDN.toString();

        if (log.isInfoEnabled()) {
           LOG.info("Validating user security token " + token);
        }

        String filtered = token;
        if (token.indexOf("EMAILADDRESS") != -1) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Converting token to standardized format");
            }
            filtered = token.replace("EMAILADDRESS", "E");
        }

        UserIdentifierType request = new UserIdentifierType();
        request.setDN(filtered);

        WhitePageAttributesType response = dias.getUserInfoAttributes(request);

        currentUser.name = response.getUid();
        currentUser.credential = peer;
        currentUser.bridgeUserInfo = response;
    }

    private DIASMessageReceiver getConfiguredSEI() {
        DIASMessageReceiver sei = service.getDIAS();
        if (serviceEndpointURL != null) {
            ((BindingProvider) sei).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceEndpointURL);
        }

        return sei;
    }

    private String formatErrorCode(ErrorType faultInfo) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[ ").append(faultInfo.getErrorCode()).append(" ] ");
        buffer.append(faultInfo.getMessage());
        return buffer.toString();
    }

    public boolean logout() throws LoginException {
        state = State.LOGGED_OUT;
        currentUser = null;
        return false;
    }

    public boolean abort() throws LoginException {
        state = State.ABORTED;
        currentUser = null;
        publishLoginEvent(false);
        return false;
    }

    public boolean commit() throws LoginException {
        if (state != State.AUTHENTICATED) {
            currentUser = null;
            return false;

        }

        state = State.LOGGED_IN;
        populateSubject();
        publishLoginEvent(true);
        return true;
    }

    private void populateSubject() {
        BridgePrincipal principal = new BridgePrincipal(currentUser.name);
        Set<Principal> principals = subject.getPrincipals();
        principals.add(principal);

        X500Principal x500 = new X500Principal(((X509Certificate) currentUser.credential).getSubjectDN().toString());
        principals.add(x500);

        List<String> groupNames = currentUser.groupNames;
        for (String group : groupNames) {
            BridgeRole role = new BridgeRole(group);
            principals.add(role);
        }

        principals.add(new BridgeRole("Authenticated"));
    }

    private Callback[] configureCallbacks() {
        Callback[] callbacks = new Callback[1];
        callbacks[0] = new X509Callback();
        return callbacks;
    }

    private void publishLoginEvent(boolean successful) {
        try {

            SessionEvent sessionEvent = new SessionEvent();
            sessionEvent.setType("LOGIN");
            String status = successful ? "SUCCESS" : "FAILURE";
            sessionEvent.setStatus(status);

            ALEMessageType message = typeProvider.getMessageTemplate();
            message.setAnalysisLogEvent(sessionEvent);

            loggingService.publishALE(message);
        } catch (Throwable t) {
           LOG.warn("Failed submitting event to Application Logging Service" + t.getMessage());
        }

    }

    private void publishAuthenticateEvent(boolean success) {
        if (loggingService == null) {
           LOG.debug("No logging service present, skipped authentication event publication.");
            return;
        }
        try {
            SessionEvent sessionEvent = new SessionEvent();
            sessionEvent.setType("AUTHENTICATE");
            String status = success ? "SUCCESS" : "FAILURE";
            sessionEvent.setStatus(status);

            ALEMessageType message = typeProvider.getMessageTemplate();
            message.setAnalysisLogEvent(sessionEvent);
            loggingService.publishALE(message);
        } catch (Throwable t) {
           LOG.warn("Failed submitting event to Application Logging Service" + t.getMessage());
        }

    }

}
