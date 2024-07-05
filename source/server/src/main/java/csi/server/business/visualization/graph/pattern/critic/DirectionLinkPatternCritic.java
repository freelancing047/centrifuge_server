package csi.server.business.visualization.graph.pattern.critic;

import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import csi.server.business.visualization.graph.base.LinkStore;
import csi.shared.gwt.viz.graph.LinkDirection;
import csi.shared.gwt.viz.graph.tab.pattern.settings.DirectionLinkPatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class DirectionLinkPatternCritic implements LinkPatternCritic {
    @Override
    public boolean criticizeLink(String dvuuid, EdgeItem item, LinkStore details, PatternCriterion criterion) {
        if (criterion instanceof DirectionLinkPatternCriterion) {
            DirectionLinkPatternCriterion patternCriterion = (DirectionLinkPatternCriterion) criterion;
            LinkDirection direction = details.getDirection();
            switch (direction) {
                case NONE:
                    return patternCriterion.isUndirected();
                case FORWARD:
                    return patternCriterion.isForward();
                case REVERSE:
                    return patternCriterion.isReverse();
                case BOTH:
                    return patternCriterion.isBidirectional();
                case DYNAMIC:
                    return false;
            }
        }
        return false;
    }

    @Override
    public SafeHtml getObservedValue(VisualItem edge, LinkStore details, PatternCriterion patternCriterion, String dvuuid) {
        return SafeHtmlUtils.fromString(details.getDirection().toString());
    }

    @Override
    public boolean criticizeReverseLink(String dvuuid, EdgeItem item, LinkStore details, PatternCriterion criterion) {
        if (criterion instanceof DirectionLinkPatternCriterion) {
            DirectionLinkPatternCriterion patternCriterion = (DirectionLinkPatternCriterion) criterion;
            LinkDirection direction = details.getDirection();
            switch (direction) {
                case NONE:
                    return patternCriterion.isUndirected();
                case FORWARD:
                    return patternCriterion.isReverse(); //sic
                case REVERSE:
                    return patternCriterion.isForward(); //sic
                case BOTH:
                    return patternCriterion.isBidirectional();
                case DYNAMIC:
                    return false;
            }
        }
        return false;
    }
}
