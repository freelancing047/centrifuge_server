package csi.config;

public class ChartConfig extends AbstractConfigurationSettings {

	private String numberFormat = "#,###.##";
    private int maxSpreadsheetDefault = 0;
    private int maxSizeDefault = 0;
    private int maxPieDefault = 0;
    private int maxDimensionNameLength = 50;
    private int maxChartCategories = 2000;
    private int maxTableCategories = 6000;  // DEPRICATED -- DO NOT USE, BUT DO NOT REMOVE !!
    private boolean hideOverviewByDefault = false;


    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }

    public int getMaxSpreadsheetDefault() {
        return maxSpreadsheetDefault;
    }

    public void setMaxSpreadsheetDefault(int maxSpreadsheetDefault) {
        this.maxSpreadsheetDefault = maxSpreadsheetDefault;
    }

    public int getMaxSizeDefault() {
        return maxSizeDefault;
    }

    public void setMaxSizeDefault(int maxSizeDefault) {
        this.maxSizeDefault = maxSizeDefault;
    }

    public int getMaxPieDefault() {
        return maxPieDefault;
    }

    public void setMaxPieDefault(int maxPieDefault) {
        this.maxPieDefault = maxPieDefault;
    }

    public int getMaxDimensionNameLength() {
        return maxDimensionNameLength;
    }

    public void setMaxDimensionNameLength(int maxDimensionNameLength) {
        this.maxDimensionNameLength = maxDimensionNameLength;
    }

	public int getMaxChartCategories() {
		return maxChartCategories;
	}

	public void setMaxChartCategories(int maxChartCategories) {
		this.maxChartCategories = maxChartCategories;
	}

    /*
            DEPRICATED -- DO NOT USE, BUT DO NOT REMOVE !!
    */
    public int getMaxTableCategories() {
        return maxTableCategories;
    }

    /*
            DEPRICATED -- DO NOT USE, BUT DO NOT REMOVE !!
    */
    public void setMaxTableCategories(int maxTableCategories) {
        this.maxTableCategories = maxTableCategories;
    }

    public void setHideOverviewByDefault(boolean hideOverviewByDefault) {this.hideOverviewByDefault = hideOverviewByDefault;}

    public boolean isHideOverviewByDefault() {return hideOverviewByDefault;}
}
