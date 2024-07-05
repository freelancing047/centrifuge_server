package csi.server.business.visualization.graph.pattern;

import java.util.Map;

import com.google.common.collect.Maps;

import csi.server.business.visualization.graph.pattern.critic.DirectionLinkPatternCritic;
import csi.server.business.visualization.graph.pattern.critic.FieldDefNodePatternCritic;
import csi.server.business.visualization.graph.pattern.critic.LabelNodePatternCritic;
import csi.server.business.visualization.graph.pattern.critic.LinkPatternCritic;
import csi.server.business.visualization.graph.pattern.critic.NeighborNodePatternCritic;
import csi.server.business.visualization.graph.pattern.critic.NodePatternCritic;
import csi.server.business.visualization.graph.pattern.critic.OccurrenceCritic;
import csi.server.business.visualization.graph.pattern.critic.TypeNodePatternCritic;
import csi.shared.gwt.viz.graph.tab.pattern.settings.DirectionLinkPatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.FieldDefNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.LabelNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.NeighborNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.OccurrencePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.TypeNodePatternCriterion;

public class PatternCriticLocator{
    private static PatternCriticLocator instance;

    private static Map<Class, NodePatternCritic> nodeCriticMap;
    private static Map<Class, LinkPatternCritic> linkCriticMap;

    static {
        nodeCriticMap = Maps.newHashMap();
        nodeCriticMap.put(TypeNodePatternCriterion.class, new TypeNodePatternCritic());
        nodeCriticMap.put(LabelNodePatternCriterion.class, new LabelNodePatternCritic());
        nodeCriticMap.put(NeighborNodePatternCriterion.class, new NeighborNodePatternCritic());
        nodeCriticMap.put(FieldDefNodePatternCriterion.class, new FieldDefNodePatternCritic());
        nodeCriticMap.put(OccurrencePatternCriterion.class, new OccurrenceCritic());
        linkCriticMap = Maps.newHashMap();
        linkCriticMap.put(DirectionLinkPatternCriterion.class, new DirectionLinkPatternCritic());
    }
    private PatternCriticLocator() {
    }

    public static NodePatternCritic get(Class type) {
        return nodeCriticMap.get(type);
    }

    public static LinkPatternCritic getLinkCritic(Class<? extends PatternCriterion> patternCriterionClass) {
        return linkCriticMap.get(patternCriterionClass);
    }

    public PatternCriticLocator get() {
        if (instance == null) {
            instance = new PatternCriticLocator();
        }
        return instance;
    }
}
