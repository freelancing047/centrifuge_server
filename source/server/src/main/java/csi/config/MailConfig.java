package csi.config;

public class MailConfig extends AbstractConfigurationSettings {

    private String defaultToEmailAddress;
    private String defaultFromEmailAddress;
    private boolean useMaxActive;
    private boolean useMaxKnown;
    private boolean useTotalUnique;


    public String getDefaultToEmailAddress() { return defaultToEmailAddress; }

    public void setDefaultToEmailAddress(String defaultToEmailAddress) { this.defaultToEmailAddress = defaultToEmailAddress; }

    public String getDefaultFromEmailAddress() { return defaultFromEmailAddress; }

    public void setDefaultFromEmailAddress(String defaultFromEmailAddress) { this.defaultFromEmailAddress = defaultFromEmailAddress; }

    public boolean getUseMaxActive() { return useMaxActive; }

    public void setUseMaxActive(boolean useMaxActive) { this.useMaxActive = useMaxActive; }

    public boolean getUseMaxKnown() { return useMaxKnown; }

    public void setUseMaxKnown(boolean useMaxKnown) { this.useMaxKnown = useMaxKnown; }

    public boolean getUseTotalUnique() { return useTotalUnique; }

    public void setUseTotalUnique(boolean useTotalUnique) { this.useTotalUnique = useTotalUnique; }
}
