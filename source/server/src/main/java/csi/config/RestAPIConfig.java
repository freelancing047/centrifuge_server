package csi.config;

/**
 * This config class contains properties used by the REST API
 * @author Pat Hayes
 *
 */
public class RestAPIConfig extends AbstractConfigurationSettings {

    private String version = "api-v1";
    private int inactivityTimeout = 300;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getInactivityTimeout() {
        return inactivityTimeout;
    }

    public void setInactivityTimeout(int inactivityTimeout) {
        this.inactivityTimeout = inactivityTimeout;
    }
}
