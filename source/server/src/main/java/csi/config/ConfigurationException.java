package csi.config;

public class ConfigurationException
    extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = -3097438860987318830L;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

}
