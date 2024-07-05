package csi.security.jaas.spi;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import csi.config.Configuration;
import csi.config.SecurityPolicyConfig;
import csi.security.queries.Users;

public class SecurityPolicyLoginModule extends SimpleLoginModule {

    @Override
    public void initialize(Subject subject, CallbackHandler handler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, handler, sharedState, options);

        SecurityPolicyConfig policyConfig = Configuration.getInstance().getSecurityPolicyConfig();

        Boolean enableUserExpiration = policyConfig.getEnableUserAccountExpiration();
        if (enableUserExpiration == null) {
            enableUserExpiration = Boolean.FALSE;
        }

        ((Map<String, Object>) sharedState).put("csi.security.policy.enableUserExpiration", enableUserExpiration);
    }

    @Override
    public boolean login() throws LoginException {
    	// Make sure expired user accounts have been disabled sometime today
    	Users.checkExpiration(false);
        return true;
    }

}
