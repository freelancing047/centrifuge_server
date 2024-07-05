package csi.config.advanced;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.config.advanced.graph.PatternConfig;
import csi.config.advanced.graph.PlayerConfig;
import csi.config.advanced.graph.TooltipAdvConfig;
import csi.server.common.model.visualization.graph.HierarchicalLayoutOrientation;
import csi.shared.core.color.ClientColorHelper.Color;

public class GraphAdvConfigGWT implements IsSerializable {

    private Color DefaultLinkColor;

    private Color DefaultNewGenColor;

    private Color DefaultUpdateGenColor;

    private Color DefaultSelectionColor;

    private Color HighlightColor;

    private Color PathHighlightColor;
    
    private TooltipAdvConfig tooltips;
    private PlayerConfig playerConfig;
    private PatternConfig patternConfig;
    private String defaultTheme;
    private int linkTargetingHelp;
    private int defaultLayoutIterations;
    private HierarchicalLayoutOrientation defaultHierarchicalLayoutOrientation;


    public TooltipAdvConfig getTooltips() {
        return tooltips;
    }
    
    
    public void setTooltips(TooltipAdvConfig tooltips) {
        this.tooltips = tooltips;
    }
    
    
    public Color getDefaultLinkColor() {
        return DefaultLinkColor;
    }

    
    public void setDefaultLinkColor(Color defaultLinkColor) {
        DefaultLinkColor = defaultLinkColor;
    }

    
    public Color getDefaultNewGenColor() {
        return DefaultNewGenColor;
    }

    
    public void setDefaultNewGenColor(Color defaultNewGenColor) {
        DefaultNewGenColor = defaultNewGenColor;
    }

    
    public Color getDefaultUpdateGenColor() {
        return DefaultUpdateGenColor;
    }

    
    public void setDefaultUpdateGenColor(Color defaultUpdateGenColor) {
        DefaultUpdateGenColor = defaultUpdateGenColor;
    }

    
    public Color getDefaultSelectionColor() {
        return DefaultSelectionColor;
    }

    
    public void setDefaultSelectionColor(Color defaultSelectionColor) {
        DefaultSelectionColor = defaultSelectionColor;
    }

    
    public Color getHighlightColor() {
        return HighlightColor;
    }

    
    public void setHighlightColor(Color highlightColor) {
        HighlightColor = highlightColor;
    }

    
    public Color getPathHighlightColor() {
        return PathHighlightColor;
    }

    
    public void setPathHighlightColor(Color pathHighlightColor) {
        PathHighlightColor = pathHighlightColor;
    }

    public PlayerConfig getPlayerConfig() {
        return playerConfig;
    }

    public void setPlayerConfig(PlayerConfig playerConfig) {
        this.playerConfig = playerConfig;
    }

    public void setPatternConfig(PatternConfig patternConfig) {
        this.patternConfig = patternConfig;
    }

    public PatternConfig getPatternConfig() {
        return patternConfig;
    }

    public String getDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(String defaultTheme) {
        this.defaultTheme = defaultTheme;
    }

    public void setLinkTargetingHelp(int linkTargetingHelp) {

        this.linkTargetingHelp = linkTargetingHelp;
    }

    public int getLinkTargetingHelp() {
        return linkTargetingHelp;
    }


    public int getDefaultLayoutIterations() {
        return defaultLayoutIterations;
    }


    public void setDefaultLayoutIterations(int defaultLayoutIterations) {
        this.defaultLayoutIterations = defaultLayoutIterations;
    }

    public HierarchicalLayoutOrientation getDefaultHierarchicalLayoutOrientation() {
        return defaultHierarchicalLayoutOrientation;
    }

    public void setDefaultHierarchicalLayoutOrientation(HierarchicalLayoutOrientation defaultHierarchicalLayoutOrientation) {
        this.defaultHierarchicalLayoutOrientation = defaultHierarchicalLayoutOrientation;
    }
}