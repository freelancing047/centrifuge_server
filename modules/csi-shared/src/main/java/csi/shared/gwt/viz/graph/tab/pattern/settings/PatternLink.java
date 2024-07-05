package csi.shared.gwt.viz.graph.tab.pattern.settings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.List;

public class PatternLink implements IsSerializable, HasPatternCriteria {
    private String uuid;
    private String name;
    private PatternNode node1;
    private PatternNode node2;
    private List<LinkPatternCriterion> criteria = Lists.newArrayList();
    private boolean showInResults = false;

    public PatternLink(PatternNode node1, PatternNode node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public PatternLink() {
    }

    public void setShowInResults(boolean showInResults) {
        this.showInResults = showInResults;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void removeCriterion(PatternCriterion criterion) {
        if (criterion instanceof LinkPatternCriterion) {
            criteria.remove(criterion);
        }
    }

    public PatternNode getNode1() {
        return this.node1;
    }

    public void setNode1(PatternNode node1) {
        this.node1 = node1;
    }

    public PatternNode getNode2() {
        return this.node2;
    }

    public void setNode2(PatternNode node2) {
        this.node2 = node2;
    }

    @Override
    public void addCriterion(PatternCriterion criterion) {
        if (criterion instanceof LinkPatternCriterion) {
            LinkPatternCriterion linkPatternCriterion = (LinkPatternCriterion) criterion;
            criteria.add(linkPatternCriterion);
        }
    }

    @Override
    public ImmutableList<PatternCriterion> getCriteria() {
        ArrayList<PatternCriterion> criteriaList = Lists.newArrayList();
        criteriaList.addAll(criteria);
        return ImmutableList.copyOf(criteriaList);
    }

    @Override
    public boolean appliesToType(String type) {
        return false;
    }

    @Override
    public boolean showInResults() {
        return showInResults;
    }
}
