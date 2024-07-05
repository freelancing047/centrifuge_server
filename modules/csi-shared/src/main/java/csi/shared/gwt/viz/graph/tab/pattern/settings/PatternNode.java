package csi.shared.gwt.viz.graph.tab.pattern.settings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.List;

public class PatternNode implements IsSerializable, HasPatternCriteria {
    private double xPos;
    private double yPos;
    private String uuid;
    private String name;
    private List<NodePatternCriterion> criteria = Lists.newArrayList();
    private boolean showInResults = true;

    public PatternNode() {
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
        if (criterion instanceof NodePatternCriterion) {
            criteria.remove(criterion);
        }
    }

    @Override
    public void addCriterion(PatternCriterion criterion) {
        if (criterion instanceof NodePatternCriterion) {
            NodePatternCriterion nodePatternCriterion = (NodePatternCriterion) criterion;
            criteria.add(nodePatternCriterion);
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
        for (NodePatternCriterion patternCriterion : criteria) {
            if (patternCriterion instanceof TypeNodePatternCriterion) {
                //FIXME:
                return patternCriterion.getValue().equals(type);
            }
        }
        return false;
    }

    @Override
    public boolean showInResults() {
        return showInResults;
    }

    @Override
    public void setShowInResults(boolean value) {
        showInResults = value;
    }

    public double getDrawY() {
        return yPos;
    }

    public void setDrawY(double yPos) {
        this.yPos = yPos;
    }

    public double getDrawX() {
        return xPos;
    }

    public void setDrawX(double xPos) {
        this.xPos = xPos;
    }

}
