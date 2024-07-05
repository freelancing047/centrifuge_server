package csi.config.advanced;

import csi.config.AbstractConfigurationSettings;
import csi.config.advanced.graph.PatternConfig;
import csi.config.advanced.graph.PlayerConfig;
import csi.config.advanced.graph.TooltipAdvConfig;
import csi.server.common.model.visualization.graph.HierarchicalLayoutOrientation;
import csi.shared.core.color.ClientColorHelper;

import java.awt.*;

public final class GraphAdvConfig extends AbstractConfigurationSettings {

    private Color defaultLinkColor = new Color(0, 0, 0, 255);// was (194, 194, 194, 255)
    private Color defaultNewGenColor = new Color(255, 50, 50, 255);
    private Color defaultUpdateGenColor = new Color(50, 50, 255, 255);
    private Color defaultSelectionColor = new Color(255, 133, 10, 200);
    private Color highlightColor = new Color(40, 205, 255, 255);
    private Color pathHighlightColor = new Color(0, 0, 255, 100);
    private TooltipAdvConfig tooltips = new TooltipAdvConfig();
    private PlayerConfig playerConfig;
    private boolean persistAsync;
    private boolean saveBeforeLayout;
    private boolean useConcurrentLayout;
    private boolean dynamicallyCreatedTooltips;
    private PatternConfig patternConfig;
    private int maxLayoutIterations;
    private String defaultTheme;
    private int linkTargetingHelp = 10;
    private int defaultLayoutIterations = 50;
    private HierarchicalLayoutOrientation defaultHierarchicalLayoutOrientation = HierarchicalLayoutOrientation.LEFT_TO_RIGHT;

    public Color getDefaultLinkColor() {
        return defaultLinkColor;
    }

    public void setDefaultLinkColor(Color defaultLinkColor) {
        this.defaultLinkColor = defaultLinkColor;
    }

    public Color getDefaultNewGenColor() {
        return defaultNewGenColor;
    }

    public void setDefaultNewGenColor(Color defaultNewGenColor) {
        this.defaultNewGenColor = defaultNewGenColor;
    }

    public Color getDefaultUpdateGenColor() {
        return defaultUpdateGenColor;
    }

    public void setDefaultUpdateGenColor(Color defaultUpdateGenColor) {
        this.defaultUpdateGenColor = defaultUpdateGenColor;
    }

    public Color getDefaultSelectionColor() {
        return defaultSelectionColor;
    }

    public void setDefaultSelectionColor(Color defaultSelectionColor) {
        this.defaultSelectionColor = defaultSelectionColor;
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public Color getPathHighlightColor() {
        return pathHighlightColor;
    }

    public void setPathHighlightColor(Color pathHighlightColor) {
        this.pathHighlightColor = pathHighlightColor;
    }

    public TooltipAdvConfig getTooltips() {
        return tooltips;
    }

    public void setTooltips(TooltipAdvConfig tooltips) {
        this.tooltips = tooltips;
    }

    public PlayerConfig getPlayerConfig() {
        return playerConfig;
    }

    public void setPlayerConfig(PlayerConfig playerConfig) {
        this.playerConfig = playerConfig;
    }

    public GraphAdvConfigGWT toGWTsafe() {
        GraphAdvConfigGWT graphAdvConfigGWT = new GraphAdvConfigGWT();
        graphAdvConfigGWT.setDefaultLinkColor(ClientColorHelper.get().make(getDefaultLinkColor().getRGB()));
        graphAdvConfigGWT.setDefaultNewGenColor(ClientColorHelper.get().make(getDefaultNewGenColor().getRGB()));
        graphAdvConfigGWT.setDefaultUpdateGenColor(ClientColorHelper.get().make(getDefaultUpdateGenColor().getRGB()));
        graphAdvConfigGWT.setDefaultSelectionColor(ClientColorHelper.get().make(getDefaultSelectionColor().getRGB()));
        graphAdvConfigGWT.setHighlightColor(ClientColorHelper.get().make(getHighlightColor().getRGB()));
        graphAdvConfigGWT.setPathHighlightColor(ClientColorHelper.get().make(getPathHighlightColor().getRGB()));
        graphAdvConfigGWT.setTooltips(tooltips);
        graphAdvConfigGWT.setPlayerConfig(getPlayerConfig());
        graphAdvConfigGWT.setPatternConfig(getPatternConfig());
        graphAdvConfigGWT.setDefaultTheme(defaultTheme);
        graphAdvConfigGWT.setLinkTargetingHelp(linkTargetingHelp);
        graphAdvConfigGWT.setDefaultLayoutIterations(defaultLayoutIterations);
        graphAdvConfigGWT.setDefaultHierarchicalLayoutOrientation(defaultHierarchicalLayoutOrientation);

        return graphAdvConfigGWT;
    }

    public boolean isPersistAsync() {
        return persistAsync;
    }

    public void setPersistAsync(boolean persistAsync) {
        this.persistAsync = persistAsync;
    }

    public boolean saveBeforeLayout() {
        return saveBeforeLayout;
    }

    public void setSaveBeforeLayout(boolean saveBeforeLayout) {
        this.saveBeforeLayout = saveBeforeLayout;
    }

    public PatternConfig getPatternConfig() {
        return patternConfig;
    }

    public void setPatternConfig(PatternConfig patternConfig) {
        this.patternConfig = patternConfig;
    }

    public boolean useConcurrentLayout() {
        return useConcurrentLayout;
    }

    public void setUseConcurrentLayout(boolean useConcurrentLayout) {
        this.useConcurrentLayout = useConcurrentLayout;
    }

    public boolean dynamicallyCreatedTooltips() {
        return dynamicallyCreatedTooltips;
    }

    public void setDynamicallyCreatedTooltips(boolean dynamicallyCreatedTooltips) {
        this.dynamicallyCreatedTooltips = dynamicallyCreatedTooltips;
    }

    public int getMaxLayoutIterations() {
        return maxLayoutIterations;
    }

    public void setMaxLayoutIterations(int maxLayoutIteration) {
        this.maxLayoutIterations = maxLayoutIteration;
    }

    public String getDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(String defaultTheme) {
        this.defaultTheme = defaultTheme;
    }

    public int getLinkTargetingHelp() {
        return linkTargetingHelp;
    }

    public void setLinkTargetingHelp(int linkTargetingHelp) {
        this.linkTargetingHelp = linkTargetingHelp;
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
