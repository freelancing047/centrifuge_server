package csi.server.common.dto.graph.pattern;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.IsSerializable;

import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class PatternResultSet implements IsSerializable{
    private GraphPattern pattern;
    private GraphPatternNotice notice;
    private Map<String, String> labelMap = Maps.newHashMap();
    private Set<PatternResult> results = Sets.newHashSet();
    private HashBasedTable<String, PatternCriterion, SafeHtml> criteriaValueMap = HashBasedTable.create();

    public Set<PatternResult> getResults() {
        return results;
    }

    public GraphPattern getPattern() {
        return pattern;
    }

    public void setPattern(GraphPattern pattern) {
        this.pattern = pattern;
    }

    public GraphPatternNotice getNotice() {
        return notice;
    }

    public void setNotice(GraphPatternNotice notice) {
        this.notice = notice;
    }

    public Map<String, String> getLabelMap() {
        return labelMap;
    }

    public void setLabelMap(Map<String, String> labelMap) {
        this.labelMap = labelMap;
    }

    public HashBasedTable<String, PatternCriterion, SafeHtml> getCriteriaValueMap() {
        return criteriaValueMap;
    }

    public void setCriteriaValueMap(HashBasedTable<String, PatternCriterion, SafeHtml> criteriaValueMap) {
        this.criteriaValueMap = criteriaValueMap;
    }
}
