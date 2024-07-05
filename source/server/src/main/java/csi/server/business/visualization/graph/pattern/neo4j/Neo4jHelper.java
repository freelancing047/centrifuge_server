package csi.server.business.visualization.graph.pattern.neo4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import prefuse.data.Graph;
import prefuse.visual.VisualItem;

import csi.config.Configuration;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.pattern.NodeBatchBody;
import csi.server.business.visualization.graph.pattern.NodeLabelRequest;
import csi.server.business.visualization.graph.pattern.RelationshipBatchRequest;
import csi.server.common.dto.graph.pattern.GraphPatternNotice;
import csi.server.common.dto.graph.pattern.PatternResult;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class Neo4jHelper {
    public static final String BATCH_ENTRY_POINT = "batch";
    public static final String ROOT_PATH = "/db/data/";
    public static final String TRANSACTION_ENTRY_POINT = "transaction/commit";
    //FIXME: should this be externalized?
    private static final Integer CONNECTION_TIMEOUT = Integer.valueOf((int) TimeUnit.MINUTES.toMillis(5));

    public Neo4jHelper() {
    }

    public static String getRootUri() {
        String host = Configuration.getInstance().getGraphAdvConfig().getPatternConfig().getHost();
        return host + Neo4jHelper.ROOT_PATH;
    }

    public static String executeBatch(String batchRequestsJson) {
        if (Strings.isNullOrEmpty(batchRequestsJson)) {
            return null;
        }
        WebResource resource = Neo4jHelper.getBatchWebResource();
        ClientResponse response = null;
        try {
            response = Neo4jHelper.postBatchRequest(batchRequestsJson, resource);
            String entity = response.getEntity(String.class);
            response.close();
            return entity;
        } catch (ClientHandlerException exception) {
            if (response != null) {
                response.close();
            }
            return null;
        }
    }

    private static ClientResponse postBatchRequest(String batchRequestsJson, WebResource resource) {
        return resource.accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).entity(batchRequestsJson).post(ClientResponse.class);
    }

    private static WebResource getBatchWebResource() {
        String batchEntryPointUri = Neo4jHelper.getRootUri() + Neo4jHelper.BATCH_ENTRY_POINT;
        Client client = Client.create();
        client.setConnectTimeout(Neo4jHelper.CONNECTION_TIMEOUT);
        return client.resource(batchEntryPointUri);
    }

    public static void batchLoadGraph(Graph g, BiMap<String, PatternNode> typeId2patternNode, BiMap<PatternNode, List<Integer>> nodeMatchMap, BiMap<String, PatternLink> typeId2patternLink, Map<PatternLink, List<Integer>> linkMatchMap, BiMap<PatternLink, List<Integer>> reverseLinkMatches) {
       Collection<Object> requests = new ArrayList<Object>();
        {
            //need to track the numbered requests for back references, reuse will cause errors
            //NOTE: at the moment I do not need the concurrency feature, just the mutable version of Integer.
            AtomicInteger requestId = new AtomicInteger(0);
            Map<NodeStore, Integer> nodeRequestIdMap = new HashMap<NodeStore, Integer>();
            Neo4jHelper.createLoadRequestsForNodes(g, typeId2patternNode, nodeMatchMap, nodeRequestIdMap, requests, requestId);
            Neo4jHelper.createLoadRequestForEdges(g, nodeRequestIdMap, requests, requestId, typeId2patternLink, linkMatchMap);
            Neo4jHelper.createLoadRequestForReverseEdges(g, nodeRequestIdMap, requests, requestId, typeId2patternLink, reverseLinkMatches);
        }
        String batchRequestsJson = Neo4jHelper.createBatchPayload(requests);
        Neo4jHelper.executeBatch(batchRequestsJson);
    }

    private static String createBatchPayload(Collection<Object> requests) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(requests);
    }

    private static void createLoadRequestForEdges(Graph g, Map<NodeStore, Integer> nodeRequestIdMap, Collection<Object> requests, AtomicInteger requestId, BiMap<String, PatternLink> typeId2patternLink, Map<PatternLink, List<Integer>> linkMatchMap) {
        Iterator edges1 = g.edges();
        while (edges1.hasNext()) {
            try {
                Object e1 = edges1.next();
                if (e1 instanceof VisualItem) {
                    VisualItem edge1 = (VisualItem) e1;
                    Neo4jHelper.createNewEdgeRequest(nodeRequestIdMap, requests, requestId, edge1, typeId2patternLink, linkMatchMap);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void createLoadRequestForReverseEdges(Graph g, Map<NodeStore, Integer> nodeRequestIdMap, Collection<Object> requests, AtomicInteger requestId, BiMap<String, PatternLink> typeId2patternLink, Map<PatternLink, List<Integer>> linkMatchMap) {
        Iterator edges1 = g.edges();
        while (edges1.hasNext()) {
            try {
                Object e1 = edges1.next();
                if (e1 instanceof VisualItem) {
                    VisualItem edge1 = (VisualItem) e1;
                    Neo4jHelper.createNewReverseEdgeRequest(nodeRequestIdMap, requests, requestId, edge1, typeId2patternLink, linkMatchMap);
                }
            } catch (Exception ignored) {
            }
        }
    }


   private static void createNewEdgeRequest(Map<NodeStore,Integer> nodeRequestIdMap, Collection<Object> requests,
                                            AtomicInteger requestId, VisualItem edgeItem,
                                            BiMap<String,PatternLink> typeId2patternLink,
                                            Map<PatternLink,List<Integer>> linkMatchMap) {
      LinkStore details1 = GraphManager.getEdgeDetails(edgeItem);

      if (details1.isVisible() && details1.isVisualized()) {
         Integer node = nodeRequestIdMap.get(details1.getFirstEndpoint());
         Integer node1 = nodeRequestIdMap.get(details1.getSecondEndpoint());

         if ((node != null) && (node1 != null)) {
            String key = details1.getKey();

            for (Map.Entry<String,PatternLink> entry : typeId2patternLink.entrySet()) {
               String s = entry.getKey();
               PatternLink patternLink = entry.getValue();
               List<Integer> patternMatches = linkMatchMap.get(patternLink);

               if (patternMatches == null) {
                  continue;
               }
               if (patternMatches.contains(Integer.valueOf(edgeItem.getRow()))) {
                  int edgeRequestId = requestId.getAndIncrement(); // keep a local copy for immediate reuse, and increment for
                                                          // the next node
                  {
                     RelationshipBatchRequest request = new RelationshipBatchRequest(node, node1, key, edgeRequestId, s);
                     requests.add(request);
                  }
               }
            }
         }
      }
   }

   private static void createNewReverseEdgeRequest(Map<NodeStore,Integer> nodeRequestIdMap, Collection<Object> requests,
                                                   AtomicInteger requestId, VisualItem edgeItem,
                                                   BiMap<String,PatternLink> typeId2patternLink,
                                                   Map<PatternLink,List<Integer>> linkMatchMap) {
      LinkStore details1 = GraphManager.getEdgeDetails(edgeItem);

      if (details1.isVisible() && details1.isVisualized()) {
         // the reversing happens here
         Integer node = nodeRequestIdMap.get(details1.getSecondEndpoint()); // sic
         Integer node1 = nodeRequestIdMap.get(details1.getFirstEndpoint()); // sic

         if ((node != null) && (node1 != null)) {
            String key = details1.getKey();

            for (Map.Entry<String,PatternLink> entry : typeId2patternLink.entrySet()) {
               String s = entry.getKey();
               PatternLink patternLink = entry.getValue();
               List<Integer> patternMatches = linkMatchMap.get(patternLink);

               if (patternMatches == null) {
                  continue;
               }
               if (patternMatches.contains(Integer.valueOf(edgeItem.getRow()))) {
                  int edgeRequestId = requestId.getAndIncrement(); // keep a local copy for immediate reuse, and increment
                                                               // for the next node
                  {
                     RelationshipBatchRequest request = new RelationshipBatchRequest(node, node1, key, edgeRequestId, s);
                     requests.add(request);
                  }
               }
            }
         }
      }
   }

    private static void createLoadRequestsForNodes(Graph g, BiMap<String, PatternNode> typeId2patternNode, BiMap<PatternNode, List<Integer>> nodeMatchMap, Map<NodeStore, Integer> nodeRequestIdMap, Collection<Object> requests, AtomicInteger requestId) {
        Iterator nodes = g.nodes();

        while (nodes.hasNext()) {
            try {
                Object nodeObj = nodes.next();
                VisualItem nodeVI;
                if (nodeObj instanceof VisualItem) {
                    nodeVI = (VisualItem) nodeObj;
                    Neo4jHelper.createNewNodeRequest(typeId2patternNode, nodeMatchMap, nodeRequestIdMap, requests, requestId, nodeVI);
                }
            } catch (Exception ignored) {
                //if anything goes wrong, just try the next node.
            }
        }
    }

   private static void createNewNodeRequest(BiMap<String,PatternNode> typeId2patternNode,
                                            BiMap<PatternNode,List<Integer>> nodeMatchMap, Map<NodeStore,Integer> nodeRequestIdMap,
                                            Collection<Object> requests, AtomicInteger requestId, VisualItem nodeVI) {
       NodeStore nodeDetails = GraphManager.getNodeDetails(nodeVI);

       if (nodeDetails.isVisible() && nodeDetails.isVisualized()) {
          // FIXME: I'm not sure this is the correct place for enforcement. Would I ever
          // want to find nodes that are not visible, or are not visualized?
          int nodeRequestId = -1;
          boolean nodeAdded = false;

          for (Map.Entry<String,PatternNode> entry : typeId2patternNode.entrySet()) { // loop overall all the pattern nodes
             String neoType = entry.getKey();
             PatternNode patternNode = entry.getValue();
             List<Integer> patternMatches = nodeMatchMap.get(patternNode); // get the set of graph nodes that match the
                                                                           // pattern node
             if (patternMatches == null) {
                continue;
             }
             if (patternMatches.contains(Integer.valueOf(nodeVI.getRow()))) {// if this node is in the set of graph nodes that match the
                                                            // pattern node
                if (!nodeAdded) {// if this is the first time for the graph node, need to actually make the
                                 // request to put it in neo4j
                   nodeRequestId = requestId.getAndIncrement(); // keep a local copy for immediate reuse, and increment for
                                                                // the next node
                   NodeBatchRequest request = new NodeBatchRequest(new NodeBatchBody(nodeVI), nodeRequestId);
                   requests.add(request);// add to the collection of requests to be made in a batch
                   nodeRequestIdMap.put(nodeDetails, Integer.valueOf(nodeRequestId));// we keep track of this requestId for building links.
                   nodeAdded = true;
                }
                NodeLabelRequest request = new NodeLabelRequest(neoType, nodeRequestId); // this request will label the
                                                                                         // neo4j node, so that the label
                                                                                         // can be used in the match query
                requests.add(request);
             }
          }
       }
    }

    public static PatternResultSet findMatches(GraphPattern pattern, BiMap<PatternNode, String> nodeToQuery, BiMap<String, PatternNode> typeId2patternNode, BiMap<PatternLink, String> linkToQuery, BiMap<String, PatternLink> typeid2patternLink) {
        int combinationLimit = Configuration.getInstance().getGraphAdvConfig().getPatternConfig().getCombinationLimit();
        WebResource resource = Neo4jHelper.getTransactionWebResource();

        String query = "{\n"
                + "  \"statements\" : [ {\n"
                + "    \"statement\" : \"" + Neo4jHelper.makeQuery(pattern, typeId2patternNode, linkToQuery, nodeToQuery, typeid2patternLink) + "\"\n"
                + "  } ]\n"
                + "}";
        String entity;
        PatternResultSet resultSet = new PatternResultSet();
        try {
            ClientResponse response = resource.accept("application/json").type("application/json").entity(query).post(ClientResponse.class);
            if (response.getStatus() == 200) {
                entity = response.getEntity(String.class);
            }
            else {
                resultSet.setNotice(GraphPatternNotice.ERROR);
                return resultSet;
            }
            response.close();
        } catch (ClientHandlerException e) {
            resultSet.setNotice(GraphPatternNotice.ERROR);
            return resultSet;
        }
        Neo4jHelper.parseResults(nodeToQuery, linkToQuery, combinationLimit, entity, resultSet);

        return resultSet;
    }

    private static void parseResults(BiMap<PatternNode, String> nodeToQuery, BiMap<PatternLink, String> linkToQuery, int combinationLimit, String entity, PatternResultSet resultSet) {
        int permuationLimit = Configuration.getInstance().getGraphAdvConfig().getPatternConfig().getPermutationLimit();
        JsonParser p = new JsonParser();
        JsonElement e = p.parse(entity);
        Set<TreeSet<String>> resultSets = Neo4jHelper.getEmptySetWithTreeSetComparator();
        int permuations = 0;
        try {
            if (e instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) e;
                {
                    //check for errors
                    JsonElement errors = jsonObject.get("errors");
                    if (errors != null) {
                        if (errors.isJsonArray()) {
                            if (errors.getAsJsonArray().size()>0) {
                                resultSet.setNotice(GraphPatternNotice.ERROR);
                                return;
                            }
                        }
                    }
                }

                JsonElement results = jsonObject.get("results");
                if (results instanceof JsonArray) {
                    JsonArray elements = (JsonArray) results;
                    JsonElement r1 = elements.get(0);
                    if (r1 instanceof JsonObject) {
                        Map<Integer, PatternNode> pnList = new TreeMap<Integer, PatternNode>();
                        Map<Integer, PatternLink> plList = new TreeMap<Integer, PatternLink>();

                        JsonObject q1Result = (JsonObject) r1;

                        {
                            AtomicInteger position = new AtomicInteger(0);
                            JsonElement col = q1Result.get("columns");
                            if (col instanceof JsonArray) {
                                JsonArray colArray = (JsonArray) col;
                                for (JsonElement colElement : colArray) {
                                    if (colElement instanceof JsonPrimitive) {
                                        String colString = colElement.getAsString();
                                        PatternNode node = nodeToQuery.inverse().get(colString);
                                        pnList.put(position.getAndIncrement(), node);
                                        PatternLink link = linkToQuery.inverse().get(colString);
                                        plList.put(position.get(), link);
                                    }
                                }
                            }
                        }

                        JsonElement data = q1Result.get("data");
                        if (data instanceof JsonArray) {
                            JsonArray q1results = (JsonArray) data;
                            AtomicInteger position = new AtomicInteger(0);
                            for (JsonElement q1result : q1results) {
                                position.set(0);
                                Map<PatternNode, String> pnMap = new HashMap<PatternNode, String>();
                                Map<PatternLink, String> plMap = new HashMap<PatternLink, String>();
                                TreeSet<String> resultValues = new TreeSet<String>();
                                if (q1result instanceof JsonObject) {

                                    JsonElement row = ((JsonObject) q1result).get("row");
                                    if (row instanceof JsonArray) {
                                        for (JsonElement rowCol : ((JsonArray) row)) {
                                            if (rowCol instanceof JsonObject) {

                                                JsonElement csikey = ((JsonObject) rowCol).get("csikey");
                                                String csikeyAsString = csikey.getAsString();
                                                resultValues.add(csikeyAsString);

                                                PatternNode patternNode = pnList.get(position.getAndIncrement());
                                                if (patternNode != null) {
                                                    pnMap.put(patternNode, csikeyAsString);
                                                }
                                                PatternLink patternLink = plList.get(position.get());
                                                if (patternLink != null) {
                                                    plMap.put(patternLink, csikeyAsString);
                                                }

                                            }
                                        }
                                    }
                                }
                                if (resultSets.add(resultValues)) {
                                    //here i can decide to keep the positional information
                                    PatternResult patternResult = new PatternResult(resultValues);
                                    patternResult.setPatternNodeMap(pnMap);
                                    patternResult.setPatternLinkMap(plMap);
                                    resultSet.getResults().add(patternResult);
                                }
                                if (resultSets.size() >= combinationLimit) {
                                    resultSet.setNotice(GraphPatternNotice.COMBINATION_LIMIT_REACHED);
                                    return;
                                }
                                permuations++;
                                if (permuations >= permuationLimit) {
                                    resultSet.setNotice(GraphPatternNotice.PERMUTATION_LIMIT_REACHED);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            resultSet.setNotice(GraphPatternNotice.ERROR);
        }
    }

    private static Set<TreeSet<String>> getEmptySetWithTreeSetComparator() {
        return new TreeSet<TreeSet<String>>(new Comparator<TreeSet<String>>() {
            @Override
            public int compare(TreeSet<String> o1, TreeSet<String> o2) {
                Iterator<String> i1 = o1.iterator();
                Iterator<String> i2 = o2.iterator();
                while (i1.hasNext() && i2.hasNext()) {
                    String s1 = i1.next();
                    String s2 = i2.next();
                    int out = s1.compareTo(s2);
                    if (out != 0) {
                        return out;
                    }
                }
                if (i1.hasNext()) {
                    return 1;
                }
                if (i2.hasNext()) {
                    return -1;
                }
                return 0;
            }
        });
    }

    private static WebResource getTransactionWebResource() {
        String uri = Neo4jHelper.getRootUri() + Neo4jHelper.TRANSACTION_ENTRY_POINT;
        Client client = Client.create();
        client.setConnectTimeout(Neo4jHelper.CONNECTION_TIMEOUT);
        client.setReadTimeout(Neo4jHelper.CONNECTION_TIMEOUT);
        return client.resource(uri);
    }

    private static String makeQuery(GraphPattern pattern, BiMap<String, PatternNode> typeId2patternNode, BiMap<PatternLink, String> linkToQuery, BiMap<PatternNode, String> nodeToQuery, BiMap<String, PatternLink> typeId2patternLink) {
        StringBuilder sb = new StringBuilder();
        sb.append("MATCH ");
        List<String> linkBits = new ArrayList<String>();
        int i = 0;
        int j = 0;
        if (pattern.getPatternLinks().isEmpty()) {
            int size = pattern.getPatternNodes().size();
            List<PatternNode> pnList= new ArrayList<PatternNode>(pattern.getPatternNodes());
            for (i = 0; i < size; i++) {
                PatternNode patternNode = pnList.get(i);
                String nodeType = typeId2patternNode.inverse().get(patternNode);
                sb.append("(n" + (i) + ":`");
                sb.append(nodeType);
                sb.append("`)");
                nodeToQuery.put(patternNode, "n" + i);
                if(i<(size-1)){
                    sb.append(",");
                }
            }
        } else {
            for (PatternLink patternLink : pattern.getPatternLinks()) {
                String linkQueryName = "l" + (++j);
                linkToQuery.put(patternLink, linkQueryName); //NOTE: might not need this.
                String linkType = typeId2patternLink.inverse().get(patternLink);
                PatternNode node1 = patternLink.getNode1();
                PatternNode node2 = patternLink.getNode2();
                String n1Type = typeId2patternNode.inverse().get(node1);
                String n2Type = typeId2patternNode.inverse().get(node2);
                String n1QueryName = "";
                if (nodeToQuery.containsKey(node1)) {
                    n1QueryName = nodeToQuery.get(node1);
                } else {
                    n1QueryName = "n" + (++i);
                    nodeToQuery.put(node1, n1QueryName);
                }

                String n2QueryName = "";
                if (nodeToQuery.containsKey(node2)) {
                    n2QueryName = nodeToQuery.get(node2);
                } else {
                    n2QueryName = "n" + (++i);
                    nodeToQuery.put(node2, n2QueryName);
                }

                linkBits.add("(" + n1QueryName + ":`" + n1Type + "`)-[" + linkQueryName + ":`" + linkType + "`]->" + "(" + n2QueryName + ":`" + n2Type + "`)");
            }
            sb.append(linkBits.stream().collect(Collectors.joining(", ")));

            if (pattern.isRequireDistinctNodes() || pattern.isRequireDistinctLinks()) {
                sb.append(" WHERE ");
            }
            if (pattern.isRequireDistinctNodes()) {
                List<String> nodeNotEqualPairs = new ArrayList<String>();
                for (int nodeid = 1; nodeid <= i; nodeid++) {
                    for (int nodeid2 = nodeid + 1; nodeid2 <= i; nodeid2++) {
                        nodeNotEqualPairs.add("n" + nodeid + "<>" + "n" + nodeid2);
                    }
                }
                sb.append(nodeNotEqualPairs.stream().collect(Collectors.joining(" AND ")));

                if (pattern.isRequireDistinctLinks() && (j > 1)) {
                    sb.append(" AND ");
                }
            }
            if (pattern.isRequireDistinctLinks()) {
                List<String> nodeNotEqualPairs = new ArrayList<String>();
                for (int linkid = 1; linkid <= j; linkid++) {
                    for (int linkid2 = linkid + 1; linkid2 <= j; linkid2++) {
                        nodeNotEqualPairs.add("l" + linkid + ".csikey<>" + "l" + linkid2 + ".csikey");
                    }
                }
                sb.append(nodeNotEqualPairs.stream().collect(Collectors.joining(" AND ")));
            }
        }
        sb.append(" RETURN *");
        int permutationLimit = Configuration.getInstance().getGraphAdvConfig().getPatternConfig().getPermutationLimit();
        sb.append(" LIMIT ").append(permutationLimit);
        return sb.toString();

    }

    public static void checkDatabaseIsRunning() {
        WebResource resource = Client.create().resource(Neo4jHelper.getRootUri());
        ClientResponse response = resource.get(ClientResponse.class);
        //System.out.println(String.format("GET on [%s], status code [%d]", new Object[] { "http://localhost:7474/db/data/", Integer.valueOf(response.getStatus()) }));
        response.close();
    }
}
