package csi.server.business.visualization.graph.pattern.critic;

import prefuse.visual.VisualItem;

import com.google.gwt.safehtml.shared.SafeHtml;

import csi.server.business.visualization.graph.base.NodeStore;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public interface NodePatternCritic {
    boolean criticizeNode(String dvUuid, VisualItem item, NodeStore details, PatternCriterion criterion, String jobId);

    SafeHtml getObservedValue(VisualItem item, NodeStore details, PatternCriterion criterion, String dvUuid);

}
