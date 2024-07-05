package csi.config;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Centrifuge Systems, Inc.
 */
public class FeatureToggleConfiguration extends AbstractConfigurationSettings implements IsSerializable{

    private boolean showMapper;
    private boolean scriptingEnabled;
    private boolean precacheEnabled;
    private boolean sharingEnabled;
    private boolean showTimeline;
    private boolean showMap;
    private boolean showAdvancedParameter;
    private boolean showReportsTab;
    private boolean useNewLogoutPage;

    public boolean isShowMapper() {
        return showMapper;
    }

    public void setShowMapper(boolean showMapper) {
        this.showMapper = showMapper;
    }

    public boolean isScriptingEnabled() {
        return scriptingEnabled;
    }

    public void setScriptingEnabled(boolean scriptingEnabled) {
        this.scriptingEnabled = scriptingEnabled;
    }

    public boolean isPrecacheEnabled() {
        return precacheEnabled;
    }

    public void setPrecacheEnabled(boolean precacheEnabled) {
        this.precacheEnabled = precacheEnabled;
    }

    public boolean isSharingEnabled() {
        return sharingEnabled;
    }

    public void setSharingEnabled(boolean sharingEnabled) {
        this.sharingEnabled = sharingEnabled;
    }

	public boolean isShowTimeline() {
		return showTimeline;
	}

	public void setShowTimeline(boolean showTimeline) {
		this.showTimeline = showTimeline;
	}

    public boolean isShowMap() {
        return showMap;
    }
    
    public void setShowMap(boolean showMap) {
        this.showMap = showMap;
    }

    public boolean isShowAdvancedParameter() {
        return showAdvancedParameter;
    }

    public void setShowAdvancedParameter(boolean showAdvancedParameter) {
        this.showAdvancedParameter = showAdvancedParameter;
    }

    public boolean isShowReportsTab() { return showReportsTab; }

    public void setShowReportsTab(boolean showReportsTab) {
        this.showReportsTab = showReportsTab;
    }

    public boolean isUseNewLogoutPage() { return useNewLogoutPage; }

    public void setUseNewLogoutPage(boolean useNewLogoutPage) { this.useNewLogoutPage = useNewLogoutPage; }
}
