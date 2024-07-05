package csi.server.common.dto;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.config.FeatureToggleConfiguration;
import csi.config.advanced.GraphAdvConfigGWT;
import csi.config.advanced.KmlExportAdvConfig;
import csi.server.common.util.ValuePair;

/**
 * Client side configuration. Initialized when the application loads and exists
 * as a singleton, accessable from WebMain.
 */
public class ClientStartupInfo implements IsSerializable{

    private String releaseVersion;
    private String buildNumber;
    private GraphAdvConfigGWT graphAdvConfig;
    private FeatureToggleConfiguration featureConfigGWT;
    private ExternalLinkConfig externalLinkConfig;
    private boolean listeningByDefault;
    private boolean provideCapcoBanners;
    private boolean provideTagBanners;
    private boolean displayApplicationBanner;
    private boolean useAbreviations;
    private boolean enforceCapcoRestrictions;
    private boolean enforceSecurityTags;
    private boolean showSharingPanel;
    private String defaultBanner;
    private String tagBannerPrefix;
    private String tagBannerDelimiter;
    private String tagBannerSubDelimiter;
    private String tagInputDelimiter;
    private String tagBannerSuffix;
    private String tagItemPrefix;
    private int timelineTypeLimit;
    private boolean ownerSetsSecurity;
    private ApplicationLabelConfig applicationBannerConfiguration;
    private Map<String, ValuePair<String, String>> bannerControl;

    private int defaultRowCountLimit;

    private KmlExportAdvConfig KmlExportAdvConfig;
    private String graphInitialLayout;

    private boolean chartHideOverviewByDefault;
    private int chartMaxChartCategories; 
    private int chartMaxTableCategories;
    private List<String> fileNameOrder;

    public int getDefaultRowCountLimit() {
        return defaultRowCountLimit;
    }

    public void setDefaultRowCountLimit(int defaultRowCountLimit) {
        this.defaultRowCountLimit = defaultRowCountLimit;
    }

    /**
     * Used across all fielddef combo boxes to determine the default sort order. False means that the field lists wil be sorted Naturally
     */
    private boolean sortAlphabetically;

    private int matrixMinSelectionRadius;
    private boolean showSamples;

    /**
     * max number of rows per page for the table viz.
     */
    private int tableMaxPageSize;

    /**
     * Maximum number of cells that matrix will render
     */
    private int matrixMaxCells;

    public ClientStartupInfo() {
    }

    public boolean isShowSamples() {
        return showSamples;
    }

    public void setShowSamples(boolean showSamples) {
        this.showSamples = showSamples;
    }

    public int getMatrixMaxCells() {
        return matrixMaxCells;
    }

    public void setMatrixMaxCells(int matrixMaxCells) {
        this.matrixMaxCells = matrixMaxCells;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public GraphAdvConfigGWT getGraphAdvConfig() {
        return graphAdvConfig;
    }

    public void setGraphAdvConfig(GraphAdvConfigGWT graphAdvConfig) {
        this.graphAdvConfig = graphAdvConfig;
    }

    public boolean isListeningByDefault() {
        return listeningByDefault;
    }

    public void setListeningByDefault(boolean listeningByDefault) {
        this.listeningByDefault = listeningByDefault;
    }

    public boolean isProvideBanners() {
        return provideCapcoBanners || provideTagBanners;
    }

    public boolean isProvideCapcoBanners() {
        return provideCapcoBanners;
    }

    public boolean isProvideTagBanners() {
        return provideTagBanners;
    }

    public void setUseAbreviations(boolean useAbreviations) {
        this.useAbreviations = useAbreviations;
    }

    public boolean getUseAbreviations() {
        return useAbreviations;
    }

    public void setProvideCapcoBanners(boolean provideBanners) {
        this.provideCapcoBanners = provideBanners;
    }

    public void setProvideTagBanners(boolean provideBanners) {
        this.provideTagBanners = provideBanners;
    }

    public boolean getDisplayApplicationBanner() {

        return displayApplicationBanner;
    }

    public void setDisplayApplicationBanner(boolean displayApplicationBannerIn) {
        this.displayApplicationBanner = displayApplicationBannerIn;
    }

    public boolean isEnforceCapcoRestrictions() {
        return enforceCapcoRestrictions;
    }

    public void setEnforceCapcoRestrictions(boolean enforceAccessRestrictions) {
        this.enforceCapcoRestrictions = enforceAccessRestrictions;
    }

    public boolean isEnforceSecurityTags() {
        return enforceSecurityTags;
    }

    public void setEnforceSecurityTags(boolean enforceSecurityTags) {
        this.enforceSecurityTags = enforceSecurityTags;
    }

    public boolean isShowSharingPanel() {
        return showSharingPanel;
    }

    public void setShowSharingPanel(boolean showSharingPanel) {
        this.showSharingPanel = showSharingPanel;
    }

    public String getDefaultBanner() {
        return defaultBanner;
    }

    public void setDefaultBanner(String defaultBanner) {
        this.defaultBanner = defaultBanner;
    }

    public String getTagBannerPrefix() {
        return tagBannerPrefix;
    }

    public void setTagBannerPrefix(String tagBannerPrefixIn) {
        this.tagBannerPrefix = tagBannerPrefixIn;
    }

    public String getTagBannerDelimiter() {
        return tagBannerDelimiter;
    }

    public void setTagBannerDelimiter(String tagBannerDelimiterIn) {
        this.tagBannerDelimiter = tagBannerDelimiterIn;
    }

    public String getTagBannerSubDelimiter() {
        return tagBannerSubDelimiter;
    }

    public void setTagBannerSubDelimiter(String tagBannerSubDelimiterIn) {
        this.tagBannerSubDelimiter = tagBannerSubDelimiterIn;
    }

    public String getTagInputDelimiter() {
        return tagInputDelimiter;
    }

    public void setTagInputDelimiter(String tagInputDelimiterIn) {
        this.tagInputDelimiter = tagInputDelimiterIn;
    }

    public String getTagBannerSuffix() {
        return tagBannerSuffix;
    }

    public void setTagBannerSuffix(String tagBannerSuffixIn) {
        this.tagBannerSuffix = tagBannerSuffixIn;
    }

    public String getTagItemPrefix() {
        return tagItemPrefix;
    }

    public void setTagItemPrefix(String tagItemPrefixIn) {
        this.tagItemPrefix = tagItemPrefixIn;
    }

    public boolean getOwnerSetsSecurity() {
        return ownerSetsSecurity;
    }

    public void setOwnerSetsSecurity(boolean ownerSetsSecurityIn) {
        this.ownerSetsSecurity = ownerSetsSecurityIn;
    }

    public Map<String, ValuePair<String, String>> getBannerControl() {
        return bannerControl;
    }

    public void setBannerControl(Map<String, ValuePair<String, String>> bannerControl) {
        this.bannerControl = bannerControl;
    }

    public FeatureToggleConfiguration getFeatureConfigGWT() {
        return featureConfigGWT;
    }

    public void setFeatureConfigGWT(FeatureToggleConfiguration featureConfigGWT) {
        this.featureConfigGWT = featureConfigGWT;
    }

    public KmlExportAdvConfig getKmlExportAdvConfig() {
        return KmlExportAdvConfig;
    }

    public void setKmlExportAdvConfig(KmlExportAdvConfig KmlExportAdvConfig) {
        this.KmlExportAdvConfig = KmlExportAdvConfig;
    }

    public void setApplicationBannerConfiguration(ApplicationLabelConfig applicationBannerConfigurationIn) {

        applicationBannerConfiguration = applicationBannerConfigurationIn;
    }

    public ApplicationLabelConfig getApplicationBannerConfiguration() {

        return applicationBannerConfiguration;
    }

    public String getGraphInitialLayout() {
        return graphInitialLayout;
    }


    public void setGraphInitialLayout(String graphInitialLayout) {
        this.graphInitialLayout = graphInitialLayout;
    }


	public int getChartMaxChartCategories() {
		return chartMaxChartCategories;
	}

	public void setChartMaxChartCategories(int chartMaxChartCategories) {
		this.chartMaxChartCategories = chartMaxChartCategories;
	}

	public boolean isChartHideOverviewByDefault() {
        return chartHideOverviewByDefault;
    }

	public void setChartHideOverviewByDefault(boolean hideOverviewByDefault) {
        this.chartHideOverviewByDefault = hideOverviewByDefault;
    }
	
	public int getChartMaxTableCategories() {
		return chartMaxTableCategories;
	}

	public void setChartMaxTableCategories(int chartMaxTableCategories) {
		this.chartMaxTableCategories = chartMaxTableCategories;
	}

	public ExternalLinkConfig getExternalLinkConfig() {
        return externalLinkConfig;
    }

    public void setExternalLinkConfig(ExternalLinkConfig externalLinkConfig) {
        this.externalLinkConfig = externalLinkConfig;
    }

    public int getTableMaxPageSize() {
        return tableMaxPageSize;
    }

    public void setTableMaxPageSize(int tableMaxPageSize) {
        this.tableMaxPageSize = tableMaxPageSize;
    }

    public int getMatrixMinSelectionRadius() {
        return matrixMinSelectionRadius;
    }

    public void setMatrixMinSelectionRadius(int matrixMinSelectionRadius) {
        this.matrixMinSelectionRadius = matrixMinSelectionRadius;
    }

    public boolean isSortAlphabetically() {
        return sortAlphabetically;
    }

    public void setSortAlphabetically(boolean sortAlphabetically) {
        this.sortAlphabetically = sortAlphabetically;
    }

    public int getTimelineTypeLimit() {
        return timelineTypeLimit;
    }

    public void setTimelineTypeLimit(int timelineTypeLimit) {
        this.timelineTypeLimit = timelineTypeLimit;
    }

    public void setExportFileNameComponentOrder(List<String> order) {
        this.fileNameOrder = order;
    }

    public List<String> getExportFileNameComponentOrder() {
        return fileNameOrder;
    }
}