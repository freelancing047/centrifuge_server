package csi.shared.gwt.viz.graph.tab.pattern.settings;

import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Set;

public class GraphPattern implements IsSerializable {
    private String uuid;
    private String name;
    private boolean requireDistinctNodes = true;
    private boolean requireDistinctLinks = true;
    private Set<PatternNode> patternNodes = Sets.newHashSet();
    private Set<PatternLink> patternLinks = Sets.newHashSet();

    public GraphPattern(String uuid) {

        this.uuid = uuid;
    }

    public GraphPattern() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addItem(PatternNode node) {
        patternNodes.add(node);

    }

    public Set<PatternNode> getPatternNodes() {
        return patternNodes;
    }

    public void setPatternNodes(Set<PatternNode> patternNodes) {
        this.patternNodes = patternNodes;
    }

    public void addLink(PatternLink patternLink) {
        patternLinks.add(patternLink);
    }

    public Set<PatternLink> getPatternLinks() {
        return patternLinks;
    }

    public void setPatternLinks(Set<PatternLink> patternLinks) {
        this.patternLinks = patternLinks;
    }

    public boolean isRequireDistinctNodes() {
        return requireDistinctNodes;
    }

    public void setRequireDistinctNodes(boolean requireDistinctNodes) {
        this.requireDistinctNodes = requireDistinctNodes;
    }

    public boolean isRequireDistinctLinks() {
        return requireDistinctLinks;
    }

    public void setRequireDistinctLinks(boolean requireDistinctLinks) {
        this.requireDistinctLinks = requireDistinctLinks;
    }
}
