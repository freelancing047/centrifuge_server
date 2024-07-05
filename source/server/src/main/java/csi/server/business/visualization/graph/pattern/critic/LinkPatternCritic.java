package csi.server.business.visualization.graph.pattern.critic;

import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

import com.google.gwt.safehtml.shared.SafeHtml;

import csi.server.business.visualization.graph.base.LinkStore;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public interface LinkPatternCritic {
    public boolean criticizeLink(String dvuuid, EdgeItem item, LinkStore details, PatternCriterion criterion);

    SafeHtml getObservedValue(VisualItem edge, LinkStore details, PatternCriterion patternCriterion, String dvuuid);

    boolean criticizeReverseLink(String dvuuid, EdgeItem item, LinkStore details, PatternCriterion patternCriterion);
}
