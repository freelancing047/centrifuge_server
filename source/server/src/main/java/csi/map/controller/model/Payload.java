package csi.map.controller.model;

import csi.server.common.model.map.ExtentInfo;

import java.util.ArrayList;
import java.util.List;

public class Payload {
    private boolean deferToNewCache = false;
    private ExtentInfo extentInfo;
    private Layer layer = new Layer();
    private String iconProviderUrl = "/Centrifuge/iconProvider";
    private String defaultShapeString;
    private List<TypeSymbol> typeSymbols;
    private List<AssociationSymbol> associationSymbols;
    private List<String> heatmapColors;
    private Double blurValue;
    private Double maxValue;
    private Double minValue;
    private boolean useBundle = false;
    private boolean drilledToBottom = true;
    private List<String> breadcrumb = new ArrayList<>();
    private boolean showLeaves = false;
    private boolean showLabel = true;
    private boolean pointLimitReached = false;
    private boolean linkLimitReached = false;
    private boolean trackTypeLimitReached = false;
    private boolean placeTypeLimitReached = false;
    private int itemsInViz;
    private boolean useSummary = false;
    private int summaryLevel;
    private boolean useMultitypeDecorator = false;
    private boolean useLinkupDecorator = false;
    private int sequenceNumber;
    private boolean cacheNotAvailable = false;
    private float nodeTransparency = 1.0f;
    private boolean noData = false;

    public ExtentInfo getExtentInfo() {
        return extentInfo;
    }

    public void setExtentInfo(ExtentInfo extentInfo) {
        this.extentInfo = extentInfo;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public String getIconProviderUrl() {
        return iconProviderUrl;
    }

    public void setIconProviderUrl(String iconProviderUrl) {
        this.iconProviderUrl = iconProviderUrl;
    }

    public String getDefaultShapeString() {
        return defaultShapeString;
    }

    public void setDefaultShapeString(String defaultShapeString) {
        this.defaultShapeString = defaultShapeString;
    }

    public List<TypeSymbol> getTypeSymbols() {
        return typeSymbols;
    }

    public void setTypeSymbols(List<TypeSymbol> typeSymbols) {
        this.typeSymbols = typeSymbols;
    }

    public List<AssociationSymbol> getAssociationSymbols() {
        return associationSymbols;
    }

    public void setAssociationSymbols(List<AssociationSymbol> associationSymbols) {
        this.associationSymbols = associationSymbols;
    }

    public List<String> getHeatmapColors() {
        return heatmapColors;
    }

    public void setHeatmapColors(List<String> heatmapColors) {
        this.heatmapColors = heatmapColors;
    }

    public Double getBlurValue() {
        return blurValue;
    }

    public void setBlurValue(Double blurValue) {
        this.blurValue = blurValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public boolean isUseBundle() {
        return useBundle;
    }

    public void setUseBundle(boolean useBundle) {
        this.useBundle = useBundle;
    }

    public boolean isDrilledToBottom() {
        return drilledToBottom;
    }

    public void setDrilledToBottom(boolean drilledToBottom) {
        this.drilledToBottom = drilledToBottom;
    }

    public List<String> getBreadcrumb() {
        return breadcrumb;
    }

    public void setBreadcrumb(List<String> breadcrumb) {
        this.breadcrumb = breadcrumb;
    }

    public boolean isShowLeaves() {
        return showLeaves;
    }

    public void setShowLeaves(boolean showLeaves) {
        this.showLeaves = showLeaves;
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    public boolean isPointLimitReached() {
        return pointLimitReached;
    }

    public void setPointLimitReached(boolean pointLimitReached) {
        this.pointLimitReached = pointLimitReached;
    }

    public boolean isLinkLimitReached() {
        return linkLimitReached;
    }

    public void setLinkLimitReached(boolean linkLimitReached) {
        this.linkLimitReached = linkLimitReached;
    }

    public boolean isPlaceTypeLimitReached() {
        return placeTypeLimitReached;
    }

    public void setPlaceTypeLimitReached() {
        this.placeTypeLimitReached = true;
    }

    public boolean isTrackTypeLimitReached() {
        return trackTypeLimitReached;
    }

    public void setTrackTypeLimitReached() {
        this.trackTypeLimitReached = true;
    }

    public int getItemsInViz() {
        return itemsInViz;
    }

    public void setItemsInViz(int itemsInViz) {
        this.itemsInViz = itemsInViz;
    }

    public void setUseSummary() {
        useSummary = true;
    }

    public boolean isUseSummary() {
        return useSummary;
    }

    public int getSummaryLevel() {
        return summaryLevel;
    }

    public void setSummaryLevel(int summaryLevel) {
        this.summaryLevel = summaryLevel;
    }

    public boolean isUseMultitypeDecorator() {
        return useMultitypeDecorator;
    }

    public void setUseMultitypeDecorator(boolean useMultitypeDecorator) {
        this.useMultitypeDecorator = useMultitypeDecorator;
    }

    public boolean isUseLinkupDecorator() {
        return useLinkupDecorator;
    }

    public void setUseLinkupDecorator(boolean useLinkupDecorator) {
        this.useLinkupDecorator = useLinkupDecorator;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public boolean isCacheNotAvailable() {
        return cacheNotAvailable;
    }

    public void setCacheNotAvailable() {
        this.cacheNotAvailable = true;
    }

    public float getNodeTransparency() {
        return nodeTransparency;
    }

    public void setNodeTransparency(float nodeTransparency) {
        this.nodeTransparency = nodeTransparency;
    }

    public boolean isDeferToNewCache() {
        return deferToNewCache;
    }

    public void setDeferToNewCache() {
        this.deferToNewCache = true;
    }

    public boolean isNoData() {
        return noData;
    }

    public void setNoData(boolean noData) {
        this.noData = noData;
    }
}
