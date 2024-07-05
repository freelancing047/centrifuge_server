package csi.server.business.visualization.graph.pattern.critic;

import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class LabelNodePatternCritic implements NodePatternCritic, LinkPatternCritic {
    @Override
    public boolean criticizeNode(String g, VisualItem item, NodeStore details, PatternCriterion criterion, String jobId) {

        return details.getLabel().equals(criterion.getValue());
    }

    @Override
    public SafeHtml getObservedValue(VisualItem item, NodeStore details, PatternCriterion criterion, String dvUuid) {
        return SafeHtmlUtils.fromString(details.getLabel());
    }

    @Override
    public boolean criticizeLink(String dvuuid, EdgeItem item, LinkStore details, PatternCriterion criterion) {
        return details.getLabel().equals(criterion.getValue());
    }

    @Override
    public SafeHtml getObservedValue(VisualItem edge, LinkStore details, PatternCriterion patternCriterion, String dvuuid) {
        return SafeHtmlUtils.fromString(details.getLabel());
    }

    @Override
    public boolean criticizeReverseLink(String dvuuid, EdgeItem item, LinkStore details, PatternCriterion patternCriterion) {
        return criticizeLink(dvuuid, item, details, patternCriterion);
    }
}
