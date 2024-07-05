package csi.server.common.dto.graph.pattern;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.IsSerializable;

import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class PatternResult implements IsSerializable {
    private Set<String> nodes;
    private Set<String> links;
    private Map<PatternNode, String> patternNodeMap = Maps.newHashMap();
    private Map<PatternLink, String> patternLinkMap = Maps.newHashMap();


    public PatternResult(TreeSet<String> resultSet) {
        nodes = Sets.newTreeSet();
        links = Sets.newTreeSet();
        for (String s : resultSet) {
            if (s.contains("+")) {
                links.add(s);
            } else {
                nodes.add(s);
            }
        }
    }

    PatternResult() {
    }

    public Set<String> getLinks() {
        return links;
    }

    public void setLinks(Set<String> links) {
        this.links = links;
    }

    public Set<String> getNodes() {
        return nodes;
    }

    public void setNodes(Set<String> nodes) {
        this.nodes = nodes;
    }

    public Map<PatternNode, String> getPatternNodeMap() {
        return patternNodeMap;
    }

    public void setPatternNodeMap(Map<PatternNode, String> patternNodeMap) {
        this.patternNodeMap = patternNodeMap;
    }

    public Map<PatternLink, String> getPatternLinkMap() {
        return patternLinkMap;
    }

    public void setPatternLinkMap(Map<PatternLink, String> patternLinkMap) {
        this.patternLinkMap = patternLinkMap;
    }
}


