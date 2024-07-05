package csi.config.advanced.graph;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TooltipAdvConfig implements IsSerializable {

    private LineBehavior defaultLineBehavior = LineBehavior.HOVER;
    private int minWidth = 125;
    private int maxWidth = 10000;
    private int minHeight = 75;
    private int maxHeight = 10000;
    private int delay_ms = 1000;
    // dashes are defined by lengths of on, off, on, off...
    // Alternative configuration {10,5}
    private Integer[] dashes = { 100, 0 };
    private boolean requireSafeURI = true;
    // TODO:would like to know if i should escape html elements or not?
    // public boolean requireSafeHTML = true;
    private String[] UriSchemeWhiteList = {};
    private String[] UriPatternWhiteList = {};

    public LineBehavior getDefaultLineBehavior() {
        return defaultLineBehavior;
    }

    public void setDefaultLineBehavior(LineBehavior defaultLineBehavior) {
        this.defaultLineBehavior = defaultLineBehavior;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getDelay_ms() {
        return delay_ms;
    }

    public void setDelay_ms(int delay_ms) {
        this.delay_ms = delay_ms;
    }

    public Integer[] getDashes() {
        return dashes;
    }

    public void setDashes(Integer[] dashes) {
        this.dashes = dashes;
    }

    public boolean isRequireSafeURI() {
        return requireSafeURI;
    }

    public void setRequireSafeURI(boolean requireSafeURI) {
        this.requireSafeURI = requireSafeURI;
    }

    public String[] getUriSchemeWhiteList() {
        return UriSchemeWhiteList;
    }

    public void setUriSchemeWhiteList(String[] uriSchemeWhiteList) {
        UriSchemeWhiteList = uriSchemeWhiteList;
    }

    public String[] getUriPatternWhiteList() {
        return UriPatternWhiteList;
    }

    public void setUriPatternWhiteList(String[] uriPatternWhiteList) {
        UriPatternWhiteList = uriPatternWhiteList;
    }
}