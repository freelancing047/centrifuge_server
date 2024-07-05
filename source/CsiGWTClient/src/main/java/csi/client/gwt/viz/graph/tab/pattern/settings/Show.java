package csi.client.gwt.viz.graph.tab.pattern.settings;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings.PatternSettingsModel;
import csi.client.gwt.viz.graph.tab.pattern.settings.PatternSettings.PatternSettingsView;
import csi.server.common.model.CsiUUID;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.viz.graph.tab.pattern.settings.LinkPatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.NodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;
import csi.shared.gwt.viz.graph.tab.pattern.settings.TypeNodePatternCriterion;

public class Show extends AbstractPatternSettingsActivity {

    public Show(PatternSettings patternSettings) {
        super(patternSettings);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        PatternSettingsView view = patternSettings.getView();
        view.bind(this);
        view.hideCriteria();
    }

    @Override
    public void showNodeDetails(DrawPatternNode node) {
        PatternSettingsView view = getView();
        view.showCriteria(new CriteriaPanel(patternSettings, node.getNode()));
    }

    @Override
    public void hideNodeDetails() {
        PatternSettingsView view = getView();
        view.hideCriteria();
    }

    @Override
    public void pinDetails(PatternNode node) {
        patternSettings.editCriteria(node);
    }

    @Override
    public void editLink(PatternLink link) {
        patternSettings.editCriteria(link);
    }

    @Override
    public void addToPattern(GraphType type) {
        PatternSettingsModel model = getModel();
        PatternNode node = new PatternNode();
        node.setDrawX((Random.nextDouble() * .8) + .1);
        node.setDrawY((Random.nextDouble() * .8) + .1);
        node.setUuid(CsiUUID.randomUUID());
        node.setName(type.getName());
        NodePatternCriterion criterion = new TypeNodePatternCriterion();
        criterion.setValue(type.getName());
        criterion.setName(CentrifugeConstantsLocator.get().pattern_defaultTypeCriteriaName());
        node.addCriterion(criterion);
        model.getEditPattern().addItem(node);
        PatternSettingsView view = getView();
        view.addNodeToPattern(node);
    }


    @Override
    public void addLink(PatternNode node1, PatternNode node2) {
        PatternLink patternLink = new PatternLink(node1, node2);
        patternLink.setUuid(CsiUUID.randomUUID());
        getModel().getEditPattern().addLink(patternLink);
        PatternSettingsView view = getView();
        view.addLinkToPattern(patternLink);
    }

    @Override
    public void copyPattern() {
        GraphPattern editPattern = getModel().getEditPattern();
        GraphPattern graphPattern = new GraphPattern(CsiUUID.randomUUID());
        Set<String> names = Sets.newTreeSet();
        for (GraphPattern pattern : getModel().getPatterns()) {
            names.add(pattern.getName());
        }
        graphPattern.setName(UniqueNameUtil.getDistinctName(names, editPattern.getName()));

        Map<PatternNode, PatternNode> oldToNewPatternNodes = Maps.newHashMap();
        {
            Set<PatternNode> pn = Sets.newHashSet();
            for (PatternNode patternNode : editPattern.getPatternNodes()) {
                PatternNode copyNode = new PatternNode();
                copyNode.setUuid(CsiUUID.randomUUID());
                for (PatternCriterion patternCriterion : patternNode.getCriteria()) {
                    if (patternCriterion instanceof NodePatternCriterion) {
                        NodePatternCriterion nodePatternCriterion = (NodePatternCriterion) patternCriterion;
                        copyNode.addCriterion(nodePatternCriterion.deepCopy());
                    }
                }
                copyNode.setName(patternNode.getName());
                copyNode.setDrawX(patternNode.getDrawX());
                copyNode.setDrawY(patternNode.getDrawY());
                pn.add(copyNode);
                oldToNewPatternNodes.put(patternNode, copyNode);
            }
            graphPattern.setPatternNodes(pn);
        }
        {
            Set<PatternLink> pl = Sets.newHashSet();
            for (PatternLink patternLink : editPattern.getPatternLinks()) {
                PatternNode node1 = oldToNewPatternNodes.get(patternLink.getNode1());
                PatternNode node2 = oldToNewPatternNodes.get(patternLink.getNode2());
                PatternLink copyLink = new PatternLink(node1, node2);
                copyLink.setUuid(CsiUUID.randomUUID());
                for (PatternCriterion patternCriterion : patternLink.getCriteria()) {
                    if (patternCriterion instanceof LinkPatternCriterion) {
                        LinkPatternCriterion linkPatternCriterion = (LinkPatternCriterion) patternCriterion;
                        copyLink.addCriterion(linkPatternCriterion.deepCopy());
                    }
                }
                copyLink.setName(patternLink.getName());
                pl.add(copyLink);
            }
            graphPattern.setPatternLinks(pl);
        }

        patternSettings.addPattern(graphPattern);
    }

    @Override
    public boolean allowLinkDrawing() {
        return true;
    }
}
