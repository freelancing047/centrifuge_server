package csi.server.business.visualization.graph.pattern;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeHtml;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.pattern.critic.LinkPatternCritic;
import csi.server.business.visualization.graph.pattern.critic.NodePatternCritic;
import csi.server.business.visualization.graph.pattern.neo4j.Neo4jHelper;
import csi.server.common.dto.graph.pattern.GraphPatternNotice;
import csi.server.common.dto.graph.pattern.PatternResult;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.server.common.model.CsiUUID;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class GraphDatabaseHelper {
    private BiMap<String, PatternLink> typeId2patternLinkMap;
    private BiMap<String, PatternNode> typeId2patternNodeMap;
    private BiMap<PatternNode, String> nodeToQueryMap = HashBiMap.create();
    private BiMap<PatternLink, String> linkToQueryMap = HashBiMap.create();
    private BiMap<PatternNode, List<Integer>> nodeMatches = HashBiMap.create();
    private BiMap<PatternLink, List<Integer>> linkMatches = HashBiMap.create();
    private BiMap<PatternLink, List<Integer>> reverseLinkMatches = HashBiMap.create();
    private GraphPattern pattern;
    private String dvuuid;
    private GraphContext context;

    public GraphDatabaseHelper() {
    }


    public PatternResultSet findPattern(GraphPattern pattern, String dvuuid, GraphContext context) throws URISyntaxException {
        this.pattern = pattern;
        this.dvuuid = dvuuid;
        this.context = context;

        //check input
        if (pattern.getPatternNodes().isEmpty()) {
            PatternResultSet patternResultSet = new PatternResultSet();
            patternResultSet.setNotice(GraphPatternNotice.NOOP);
            return patternResultSet;
        }

        //Process graph, determine intermediary types
        createPatternTypes();
        processGraphToNewTypes();

        //get PatternEngine?

        try {
            Neo4jHelper.checkDatabaseIsRunning();
        } catch (Exception e) {
            PatternResultSet patternResultSet = new PatternResultSet();
            patternResultSet.setNotice(GraphPatternNotice.NEO4J_UNREACHABLE);
            return patternResultSet;
        }
        {
            Graph g = context.getVisualGraph();
            Neo4jHelper.batchLoadGraph(g, typeId2patternNodeMap, nodeMatches, typeId2patternLinkMap, linkMatches, reverseLinkMatches);
        }
        PatternResultSet matches = Neo4jHelper.findMatches(pattern, nodeToQueryMap, typeId2patternNodeMap, linkToQueryMap, typeId2patternLinkMap);
        {
            matches.setPattern(pattern);
            populateLabels(matches);
            populateCriteriaValues(matches);
        }
        return matches;

    }

    private void populateCriteriaValues(PatternResultSet matches) {
        Graph g = context.getVisualGraph();
        GraphPattern pattern = matches.getPattern();
        Set<PatternResult> results = matches.getResults();
        if (pattern != null) {

            populateNodeCriteriaValues(matches, g, dvuuid, pattern, results);
            populateLinkCriteriaValues(matches, g, dvuuid, pattern, results);
        }
    }

    private void populateLinkCriteriaValues(PatternResultSet matches, Graph g, String dvuuid, GraphPattern pattern, Set<PatternResult> results) {
        Set<PatternLink> patternLinks = pattern.getPatternLinks();
        if (patternLinks != null) {
            for (PatternLink patternLink : patternLinks) {
                for (PatternCriterion patternCriterion : patternLink.getCriteria()) {
                    LinkPatternCritic linkPatternCritic = PatternCriticLocator.getLinkCritic(patternCriterion.getClass());

                    for (PatternResult patternResult : results) {
                        if (patternResult == null) {
                            continue;
                        }
                        Map<PatternLink, String> linkMap = patternResult.getPatternLinkMap();
                        if (linkMap == null) {
                            continue;
                        }
                        String s = linkMap.get(patternLink);
                        if (Strings.isNullOrEmpty(s)) {
                            continue;
                        }
                        if (matches.getCriteriaValueMap().contains(s, patternCriterion)) {
                            continue;
                        }
                        Map<String, Edge> edgeKeyIndex = context.getEdgeKeyIndex();
                        Edge edge = edgeKeyIndex.get(s);
                        Edge edge1 = g.getEdge(edge.getRow());

                        LinkStore details = GraphManager.getEdgeDetails(edge);
                        SafeHtml observedValue = linkPatternCritic.getObservedValue((VisualItem) edge1, details, patternCriterion, dvuuid);
                        matches.getCriteriaValueMap().put(s, patternCriterion, observedValue);
                    }
                }
            }
        } 
    }

    private void populateNodeCriteriaValues(PatternResultSet matches, Graph g, String dvUuid, GraphPattern pattern, Set<PatternResult> results) {
        Set<PatternNode> patternNodes = pattern.getPatternNodes();
        if (patternNodes != null) {
            for (PatternNode patternNode : patternNodes) {
                for (PatternCriterion patternCriterion : patternNode.getCriteria()) {
                    NodePatternCritic nodePatternCritic = PatternCriticLocator.get(patternCriterion.getClass());

                    for (PatternResult patternResult : results) {
                        if (patternResult == null) {
                            continue;
                        }
                        Map<PatternNode, String> nodeMap = patternResult.getPatternNodeMap();
                        if (nodeMap == null) {
                            continue;
                        }
                        String s = nodeMap.get(patternNode);
                        if (Strings.isNullOrEmpty(s)) {
                            continue;
                        }
                        if (matches.getCriteriaValueMap().contains(s, patternCriterion)) {
                            continue;
                        }
                        Node node = g.getNode(Integer.parseInt(s));
                        NodeStore details = GraphManager.getNodeDetails(node);
                        SafeHtml observedValue = nodePatternCritic.getObservedValue((VisualItem) node, details, patternCriterion, dvUuid);
                        matches.getCriteriaValueMap().put(s, patternCriterion, observedValue);
                    }
                }
            }
        }
    }

    private void populateLabels(PatternResultSet matches) {
        Graph g = context.getVisualGraph();
        Map<String, String> labelMap = matches.getLabelMap();
        for (PatternResult patternResult : matches.getResults()) {
            populateNodeLabels(g, labelMap, patternResult);
            populateLinkLabels(labelMap, patternResult, context);
        }
    }

    private void populateNodeLabels(Graph g, Map<String, String> labelMap, PatternResult patternResult) {

        for (String s : patternResult.getNodes()) {
            Node node = g.getNode(Integer.parseInt(s));
            NodeStore details = GraphManager.getNodeDetails(node);
            labelMap.put(s, details.getLabel());
        }
    }

    private void populateLinkLabels(Map<String, String> labelMap, PatternResult patternResult, GraphContext context) {

        Map<String, Edge> linkIndex = context.getEdgeKeyIndex();
        for (String s : patternResult.getLinks()) {
            Edge edge = linkIndex.get(s);
            LinkStore details = GraphManager.getEdgeDetails(edge);
            labelMap.put(s, details.getLabel());
        }
    }

    private void processGraphToNewTypes() {
        processNodesToNewTypes();
        processLinkToNewTypes();

    }

    private void processLinkToNewTypes() {
        VisualGraph graph = context.getVisualGraph();
        for (PatternLink patternLink : pattern.getPatternLinks()) {
            Iterator<EdgeItem> edges = graph.edges();
            while (edges.hasNext()) {
                try {
                    EdgeItem item = edges.next();
                    LinkStore details = GraphManager.getEdgeDetails(item);
                    if (criticize(dvuuid, item, patternLink, details)) {
                        List<Integer> integers = linkMatches.get(patternLink);
                        if (integers == null) {
                            integers = Lists.newArrayList();
                            linkMatches.put(patternLink, integers);
                        }
                        integers.add(item.getRow());
                    }
                    if (criticizeReverse(dvuuid, item, patternLink, details)) {
                        List<Integer> integers = reverseLinkMatches.get(patternLink);
                        if (integers == null) {
                            integers = Lists.newArrayList();
                            reverseLinkMatches.put(patternLink, integers);
                        }
                        integers.add(item.getRow());
                    }
                } catch (Exception ignored) {

                }
            }
        }
    }

    private boolean criticizeReverse(String dvuuid, EdgeItem item, PatternLink patternLink, LinkStore details) {
        for (PatternCriterion patternCriterion : patternLink.getCriteria()) {
            LinkPatternCritic linkPatternCritic = PatternCriticLocator.getLinkCritic(patternCriterion.getClass());
            if (!linkPatternCritic.criticizeReverseLink(dvuuid, item, details, patternCriterion)) {
                return false;
            }
        }
        return true;
    }

    private boolean criticize(String dvuuid, EdgeItem item, PatternLink patternLink, LinkStore details) {
        //need to map criterion N
        for (PatternCriterion patternCriterion : patternLink.getCriteria()) {
            LinkPatternCritic linkPatternCritic = PatternCriticLocator.getLinkCritic(patternCriterion.getClass());
            if (!linkPatternCritic.criticizeLink(dvuuid, item, details, patternCriterion)) {
                return false;
            }
        }
        return true;
    }

    private void processNodesToNewTypes() {
        Graph g = context.getVisualGraph();
        for (PatternNode patternNode : pattern.getPatternNodes()) {

            Iterator nodes = g.nodes();
            String jobId = CsiUUID.randomUUID();
            while (nodes.hasNext()) {
                try {
                    Object nodeObj = nodes.next();
                    if (nodeObj instanceof VisualItem) {
                        VisualItem nodeVI = (VisualItem) nodeObj;
                        NodeStore nodeDetails = GraphManager.getNodeDetails(nodeVI);
                        if (criticize(dvuuid, nodeVI, patternNode, nodeDetails, jobId)) {
                            List<Integer> integers = nodeMatches.get(patternNode);
                            if (integers == null) {
                                integers = Lists.newArrayList();
                                nodeMatches.put(patternNode, integers);
                            }
                            integers.add(nodeVI.getRow());
                        }
                    }
                } catch (Exception ignored) {
                    //keep processing
                }
            }
        }
    }

    private boolean criticize(String dvuuid, VisualItem item, PatternNode patternNode, NodeStore nodeDetails, String jobId) {
        //need to map criterion N
        for (PatternCriterion patternCriterion : patternNode.getCriteria()) {
            NodePatternCritic nodePatternCritic = PatternCriticLocator.get(patternCriterion.getClass());
            if (!nodePatternCritic.criticizeNode(dvuuid, item, nodeDetails, patternCriterion, jobId)) {
                return false;
            }
        }
        return true;
    }

    private void createPatternTypes() {
        //will use uuid for type name for the moment.
        typeId2patternNodeMap = HashBiMap.create();
        typeId2patternLinkMap = HashBiMap.create();
        for (PatternNode patternNode : pattern.getPatternNodes()) {
            typeId2patternNodeMap.put(CsiUUID.randomUUID(), patternNode);
        }
        for (PatternLink patternLink : pattern.getPatternLinks()) {
            typeId2patternLinkMap.put(CsiUUID.randomUUID(), patternLink);
        }
    }
}
