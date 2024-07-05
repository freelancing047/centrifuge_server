package csi.config;

public class RelGraphConfig
        extends AbstractConfigurationSettings {
    private static final int DEFAULT_RENDER_THRESHOLD = 500;

    private static final int DEFAULT_AUTO_BUNDLE_THRESHOLD = 1000;

    private static final int DEFAULT_TOOLTIP_OPEN_DELAY_TIME = 1500;

    private String dotPath;

    private String initialLayout;

    private String defaultTheme = "Baseline";

    private int typeLimit;

    private int renderThreshold;

    private int searchResultsLimit = 500;

    private boolean autoBundleEnabled;

    private int autoBundleThreshold;

    private int layoutAnimationThreshold;
    private int tooltipOpenDelayTime = -1;
    private boolean qualifyNodeByTypeDefault = false;
    
	private int maxLabelLength = 40;

    public String getDefaultTheme() {
        return defaultTheme;
    }


    public void setDefaultTheme(String defaultTheme) {
        this.defaultTheme = defaultTheme;
    }


    public boolean isQualifyNodeByTypeDefault() {
        return qualifyNodeByTypeDefault;
    }

    public void setQualifyNodeByTypeDefault(boolean qualifyNodeByTypeDefault) {
        this.qualifyNodeByTypeDefault = qualifyNodeByTypeDefault;
    }

    public int getSearchResultsLimit() {
        return searchResultsLimit;
    }

    public void setSearchResultsLimit(int value) {
        this.searchResultsLimit = value;
    }

    public int getRenderThreshold() {
        if (renderThreshold <= 0) {
            renderThreshold = DEFAULT_RENDER_THRESHOLD;
        }
        return renderThreshold;
    }

    public String getDotPath() {
        return dotPath;
    }

    public void setDotPath(String dotPath) {
        this.dotPath = dotPath;
    }

    public String getInitialLayout() {
        return initialLayout;
    }

    public int getAutoBundleThreshold() {
        if (autoBundleThreshold == 0) {
            autoBundleThreshold = DEFAULT_AUTO_BUNDLE_THRESHOLD;
        }
        return autoBundleThreshold;
    }

    public int getTooltipOpenDelayTime() {
        if (tooltipOpenDelayTime == -1) {
            tooltipOpenDelayTime = DEFAULT_TOOLTIP_OPEN_DELAY_TIME;
        }
        return tooltipOpenDelayTime;
    }

    public void setAutoBundleThreshold(int autoBundleThreshold) {
        this.autoBundleThreshold = autoBundleThreshold;
    }

    public boolean isAutoBundleEnabled() {
        return autoBundleEnabled;
    }

    public void setAutoBundleEnabled(boolean autoBundleEnabled) {
        this.autoBundleEnabled = autoBundleEnabled;
    }

    public void setInitialLayout(String initialLayout) {
        this.initialLayout = initialLayout;
    }

    public void setRenderThreshold(int renderThreshold) {
        this.renderThreshold = renderThreshold;
    }

    public void setTooltipOpenDelayTime(int tooltipOpenDelayTime) {
        this.tooltipOpenDelayTime = tooltipOpenDelayTime;
    }

    /* 5/7/2014 this doesn't seem to do anything expect break when not set to 0. */
    @Deprecated
    public int getLayoutAnimationThreshold() {
        return layoutAnimationThreshold;
    }

    public void setLayoutAnimationThreshold(int layoutAnimationThreshold) {
        this.layoutAnimationThreshold = layoutAnimationThreshold;
    }

	public int getMaxLabelLength() {
		return maxLabelLength;
	}

	public void setMaxLabelLength(int maxLabelLength) {
		this.maxLabelLength = maxLabelLength;
	}

    public int getTypeLimit() {
        return typeLimit;
    }

    public void setTypeLimit(int typeLimit) {
        this.typeLimit = typeLimit;
    }
}
