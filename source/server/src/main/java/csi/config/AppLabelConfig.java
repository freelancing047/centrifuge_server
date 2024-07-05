package csi.config;

import csi.server.common.dto.ApplicationLabelConfig;

public class AppLabelConfig extends AbstractConfigurationSettings {

    private String headerLeftLabel;
    private String headerCenterLabel;
    private String headerRightLabel;
    private String headerLeftLink;
    private String headerCenterLink;
    private String headerRightLink;
    private String headerForegroundColor = "#000000";
    private String headerBackgroundColor = "#FFFFFF";
    private boolean includeFullScreenHeaderLabels = false;
    private boolean includeHeaderLabels;

    public String getHeaderLeftLabel() {
        return headerLeftLabel;
    }
    public void setHeaderLeftLabel(String headerLeftLabel) {
        this.headerLeftLabel = headerLeftLabel;
    }

    public String getHeaderCenterLabel() {
        return headerCenterLabel;
    }

    public void setHeaderCenterLabel(String headerCenterLabel) {
        this.headerCenterLabel = headerCenterLabel;
    }

    public String getHeaderRightLabel() {
        return headerRightLabel;
    }

    public void setHeaderRightLabel(String headerRightLabel) {
        this.headerRightLabel = headerRightLabel;
    }

    public String getHeaderLeftLink() {
        return headerLeftLink;
    }
    public void setHeaderLeftLink(String headerLeftLink) {
        this.headerLeftLink = headerLeftLink;
    }

    public String getHeaderCenterLink() {
        return headerCenterLink;
    }

    public void setHeaderCenterLink(String headerCenterLink) {
        this.headerCenterLink = headerCenterLink;
    }

    public String getHeaderRightLink() {
        return headerRightLink;
    }

    public void setHeaderRightLink(String headerRightLink) {
        this.headerRightLink = headerRightLink;
    }

    public String getHeaderForegroundColor() {
        return headerForegroundColor;
    }

    public void setHeaderForegroundColor(String headerForegroundColorIn) {
        this.headerForegroundColor = headerForegroundColorIn;
    }

    public String getHeaderBackgroundColor() {
        return headerBackgroundColor;
    }

    public void setHeaderBackgroundColor(String headerBackgroundColor) {
        this.headerBackgroundColor = headerBackgroundColor;
    }

    public boolean getIncludeFullScreenHeaderLabels() {
        return includeFullScreenHeaderLabels;
    }

    public void setIncludeFullScreenHeaderLabels(boolean includeFullScreenHeaderLabelsIn) {
        this.includeFullScreenHeaderLabels = includeFullScreenHeaderLabelsIn;
    }

    public boolean getIncludeHeaderLabels() {
        return includeHeaderLabels;
    }

    public void setIncludeHeaderLabels(boolean includeHeaderLabels) {
        this.includeHeaderLabels = includeHeaderLabels;
    }
    
    public ApplicationLabelConfig spawnDto() {

        return new ApplicationLabelConfig(headerLeftLabel, headerCenterLabel, headerRightLabel, headerLeftLink,
                                            headerCenterLink, headerRightLink, headerForegroundColor,
                                            headerBackgroundColor, includeFullScreenHeaderLabels, includeHeaderLabels);
    }
}