package csi.config;

public class ApplicationConfig
    extends AbstractConfigurationSettings
{

    private String applicationId;
    private boolean disableTestConnections;
    private boolean enableGeoIQ;
    private boolean enableTemplateCache;
    private String iconManagementAccess;
    private String iconManagementAccessDelimiter;
    private boolean purgeOldDataViews;
    private int dataViewPurgeAge;
    private boolean purgeOrphanDataViews;
    private boolean purgeSamples;
    private boolean displaySamples;
    private int dailyReaperCount;
    private int defaultRowLimit;
    private String allowInUseFieldTypeChange;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

	public boolean isDisableTestConnections() {
		return disableTestConnections;
	}

	public void setDisableTestConnections(boolean disableTestConnections) {
		this.disableTestConnections = disableTestConnections;
	}

	public boolean isEnableGeoIQ() {
		return enableGeoIQ;
	}

	public void setEnableGeoIQ(boolean enableGeoIQ) {
		this.enableGeoIQ = enableGeoIQ;
	}

    public boolean isEnableTemplateCache() {
        return enableTemplateCache;
    }

    public void setEnableTemplateCache(boolean enableTemplateCache) {
        this.enableTemplateCache = enableTemplateCache;
    }

    public String getIconManagementAccess() {
        return iconManagementAccess;
    }

    public void setIconManagementAccess(String iconManagementAccess) {
        this.iconManagementAccess = iconManagementAccess;
    }

    public String getIconManagementAccessDelimiter() {
        return iconManagementAccessDelimiter;
    }

    public void setIconManagementAccessDelimiter(String iconManagementAccessDelimiter) {
        this.iconManagementAccessDelimiter = iconManagementAccessDelimiter;
    }

    public boolean isPurgeOldDataViews() {
        return purgeOldDataViews;
    }

    public void setPurgeOldDataViews(boolean purgeOldDataViewsIn) {
        purgeOldDataViews = purgeOldDataViewsIn;
    }

    public int getDataViewPurgeAge() {
        return dataViewPurgeAge;
    }

    public void setDataViewPurgeAge(int dataViewPurgeAgeIn) {
        dataViewPurgeAge = dataViewPurgeAgeIn;
    }

    public boolean isPurgeOrphanDataViews() {
        return purgeOrphanDataViews;
    }

    public void setPurgeOrphanDataViews(boolean purgeOrphanDataViewsIn) {
        purgeOrphanDataViews = purgeOrphanDataViewsIn;
    }

    public boolean isPurgeSamples() {
        return purgeSamples;
    }

    public void setPurgeSamples(boolean purgeSamplesIn) {
        purgeSamples = purgeSamplesIn;
    }

    public boolean isDisplaySamples() {
        return displaySamples;
    }

    public void setDisplaySamples(boolean displaySamples) {
        this.displaySamples = displaySamples;
    }

    public int getDailyReaperCount() {
        return Math.min(Math.max(dailyReaperCount, 24), 2);
    }

    public void setDailyReaperCount(int dailyReaperCountIn) {
        dailyReaperCount = dailyReaperCountIn;
    }

    public void setDefaultRowLimit(int defaultRowLimit) {
        this.defaultRowLimit = defaultRowLimit;
    }

    public int getDefaultRowLimit() {
        return defaultRowLimit;
    }

    public String getAllowInUseFieldTypeChange() {
        return allowInUseFieldTypeChange;
    }

    public void setAllowInUseFieldTypeChange(String allowInUseFieldTypeChange) {
        this.allowInUseFieldTypeChange = allowInUseFieldTypeChange;
    }

}
