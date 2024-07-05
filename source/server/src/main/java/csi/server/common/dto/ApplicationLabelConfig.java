package csi.server.common.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by centrifuge on 5/26/2015.
 */
public class ApplicationLabelConfig implements IsSerializable {

    private String headerLeftLabel;
    private String headerCenterLabel;
    private String headerRightLabel;
    private String headerLeftLink;
    private String headerCenterLink;
    private String headerRightLink;
    private String headerForegroundColor;
    private String headerBackgroundColor;
    private boolean includeFullScreenHeaderLabels;
    private boolean includeHeaderLabels;

    public ApplicationLabelConfig() {

    }

    public ApplicationLabelConfig(String headerLeftLabelIn, String headerCenterLabelIn, String headerRightLabelIn,
                                  String headerLeftLinkIn, String headerCenterLinkIn, String headerRightLinkIn,
                                  String headerForegroundColorIn, String headerBackgroundColorIn,
                                  boolean includeFullScreenHeaderLabelsIn, boolean includeHeaderLabelsIn) {

        headerLeftLabel = headerLeftLabelIn;
        headerCenterLabel = headerCenterLabelIn;
        headerRightLabel = headerRightLabelIn;
        headerLeftLink = headerLeftLinkIn;
        headerCenterLink = headerCenterLinkIn;
        headerRightLink = headerRightLinkIn;
        headerForegroundColor = headerForegroundColorIn;
        headerBackgroundColor = headerBackgroundColorIn;
        includeFullScreenHeaderLabels = includeFullScreenHeaderLabelsIn;
        includeHeaderLabels = includeHeaderLabelsIn;
    }

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
}
