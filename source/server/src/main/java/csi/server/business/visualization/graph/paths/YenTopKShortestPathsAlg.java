package csi.server.business.visualization.graph.paths;

/*
 * Copyright (c) 2004-2008 Arizona State University. All rights
 * reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * THIS SOFTWARE IS PROVIDED BY ARIZONA STATE UNIVERSITY ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL ARIZONA STATE UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import prefuse.data.Graph;
import prefuse.data.Node;

import csi.server.task.api.TaskHelper;

/**
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision: 783 $
 * @latest $Id: YenTopKShortestPathsAlg.java 783 2009-06-19 19:19:27Z qyan $
 */
public class YenTopKShortestPathsAlg {
    private static int MAX_PATHS_TO_FIND = 10000;

    private Graph _graph = null;

    // intermediate variables
    private List<Path> resultPaths = new Vector<Path>();
    private Map<Path, Node> pathToSourceNodeMap = new HashMap<Path, Node>();
    private QYPriorityQueue<Path> potentialPaths = new QYPriorityQueue<Path>();

    private boolean _includeDirection = true;
    private boolean _includeHidden = false;

    // the ending vertices of the paths
    private Node _source_vertex = null;
    private Node _target_vertex = null;

    private int _max_length = Integer.MAX_VALUE;

    // variables for debugging and testing
    private int _generated_path_num = 0;

    private PathHelper _helper;

    /**
     * Default constructor.
     *
     * @param graph
     */
    public YenTopKShortestPathsAlg(Graph graph, boolean includeDirection, boolean includeHidden) {
        this(graph, null, null, includeDirection, includeHidden);
    }

    /**
     * Constructor 2
     *
     * @param graph
     * @param source_vt
     * @param target_vt
     */
    public YenTopKShortestPathsAlg(Graph graph, Node source_vt, Node target_vt, boolean includeDirection, boolean includeHidden) {
        if (graph == null) {
            throw new IllegalArgumentException("A NULL graph object occurs!");
        }

        _graph = graph;
        _source_vertex = source_vt;
        _target_vertex = target_vt;
        _includeDirection = includeDirection;
        _includeHidden = includeHidden;

        _init();
    }

    /**
     * Initiate members in the class.
     */
    private void _init() {
        clear();

        _helper = new PathHelper(_graph, _includeDirection, _includeHidden);

        // get the shortest path by default if both source and target exist
        if ((_source_vertex != null) && (_target_vertex != null)) {
            Path shortest_path = get_shortest_path(_source_vertex, _target_vertex);
            if (!shortest_path.get_vertices().isEmpty()) {
                //System.out.println("Putting shortest path on " + PathHelper.getLabel(_source_vertex));
                potentialPaths.add(shortest_path);
                pathToSourceNodeMap.put(shortest_path, _source_vertex);
            }
        }
    }

    /**
     * Clear the variables of the class.
     */
    public void clear() {
        potentialPaths = new QYPriorityQueue<Path>();
        pathToSourceNodeMap.clear();
        resultPaths.clear();
        _generated_path_num = 0;
    }

    /**
     * Obtain the shortest path connecting the source and the target, by using
     * the
     * classical Dijkstra shortest path algorithm.
     *
     * @param source_vt
     * @param target_vt
     * @return
     */
    public Path get_shortest_path(Node source_vt, Node target_vt) {
        DijkstraShortestPathAlg dijkstra_alg = new DijkstraShortestPathAlg(_graph, _helper, _includeDirection, _includeHidden);
        return dijkstra_alg.get_shortest_path(source_vt, target_vt, _max_length);
    }

    /**
     * Check if there exists a path, which is the shortest among all candidates.
     *
     * @return
     */
    public boolean has_next() {
        return !potentialPaths.isEmpty();
    }

    /**
     * Get the shortest path among all that connecting source with targe.
     *
     * @return
     */
    public Path next() {
        //System.out.println("Yen.next");

        // 3.1 prepare for removing vertices and arcs
        Path currPotentialPath = potentialPaths.poll();
        resultPaths.add(currPotentialPath);

        Node currPotentialSourceNode = pathToSourceNodeMap.get(currPotentialPath);
        List<?> subList = currPotentialPath.get_vertices().subList(0, currPotentialPath.get_vertices().indexOf(currPotentialSourceNode));
        int potentialPathHash = subList.hashCode();
        //System.out.println("\tGot hash " + potentialPathHash + " for list of length " + subList.size()+" (starting at "+PathHelper.getLabel(currPotentialSourceNode)+")");

        int count = resultPaths.size();

        // 3.2 remove the vertices and arcs in the graph
        for (int i = 0; i < (count - 1); ++i) {
            //System.out.println("Looking at result path " + i + " of " + (count - 1));

            Path currResultPath = resultPaths.get(i);

            int currSourceNodeIdx = currResultPath.get_vertices().indexOf(currPotentialSourceNode);

            // The latest source node isn't in the result path.
            if (currSourceNodeIdx < 0) {
                //System.out.println("Current source node is not in the current result path.");
                continue;
            }

            // Note that the following condition makes sure all candidates
            // should be considered. The algorithm in the paper is not correct
            // for removing some candidates by mistake.
            int path_hash = currResultPath.get_vertices().subList(0, currSourceNodeIdx).hashCode();
            if (path_hash != potentialPathHash) {
                //System.out.println("Current result path's sublist is different from the current potential path's sublist.");
                continue;
            }

            Node nextNode = currResultPath.get_vertices().get(currSourceNodeIdx + 1);

            _helper.remove_edge(currPotentialSourceNode, nextNode);
        }

        //System.out.println("Removing all nodes and links in current potential path from consideration.");
        int currPotentialPathNodeCount = currPotentialPath.get_vertices().size();
        List<Node> currPotentialPathNodes = currPotentialPath.get_vertices();
        for (int i = 0; i < (currPotentialPathNodeCount - 1); ++i) {
            _helper.remove_vertex(currPotentialPathNodes.get(i));
            _helper.remove_edge(currPotentialPathNodes.get(i), currPotentialPathNodes.get(i + 1));
        }

        TaskHelper.checkForCancel();
        //System.out.println("Getting shortest path flower starting at " + PathHelper.getLabel(_target_vertex));
        // 3.3 calculate the shortest tree rooted at target vertex in the graph
        DijkstraShortestPathAlg reverse_tree = new DijkstraShortestPathAlg(_graph, _helper, _includeDirection, _includeHidden);
        reverse_tree.get_shortest_path_flower(_target_vertex);

        TaskHelper.checkForCancel();
        // 3.4 recover the deleted vertices and update the cost and identify the
        // new candidate results
        boolean done = false;
        //System.out.println("Now, looping backwards over potential nodes from previous path.");
        for (int i = currPotentialPathNodeCount - 2; (i >= 0) && !done; --i) {
            // 3.4.1 get the vertex to be recovered
            Node cur_recover_vertex = currPotentialPathNodes.get(i);
            //System.out.println("\tRecover node: " + PathHelper.getLabel(cur_recover_vertex));
            _helper.recover_removed_vertex(cur_recover_vertex);

            // 3.4.2 check if we should stop continuing in the next iteration
            if (cur_recover_vertex.getRow() == currPotentialSourceNode.getRow()) {
                //System.out.println("\tLooking at source of this path; done=true.");
                done = true;
            }

            TaskHelper.checkForCancel();
            //System.out.println("\t----Updating cost forward starting from " + PathHelper.getLabel(cur_recover_vertex));
            // 3.4.3 calculate cost using forward star form
            Path sub_path = reverse_tree.update_cost_forward(cur_recover_vertex);

            // 3.4.4 get one candidate result if possible
            if (sub_path != null) {
                //System.out.println("\tUpdate produced a sub-path.");
                //System.out.println("\tSUB-PATH: ");
//                for (Node node : sub_path.get_vertices()) {
                    //System.out.println(PathHelper.getLabel(node));
//                }
                ++_generated_path_num;

                // 3.4.4.1 get the prefix from the concerned path
                double cost = 0;
                List<Node> pre_path_list = new ArrayList<Node>();
                TaskHelper.checkForCancel();
                //System.out.println("\tNow correct the cost backwards...");
                reverse_tree.correct_cost_backward(cur_recover_vertex);

                //System.out.println("\tNow, loop over the current potential path nodes until we find the current recover node ("+PathHelper.getLabel(cur_recover_vertex)+")");
                for (int j = 0; j < currPotentialPathNodeCount; ++j) {
                    Node cur_vertex = currPotentialPathNodes.get(j);
                    //System.out.println("\tcur_vertext: "+PathHelper.getLabel(cur_vertex));
                    if (cur_vertex.getRow() == cur_recover_vertex.getRow()) {
                        j = currPotentialPathNodeCount;
                    } else {
                        cost += _helper.getTrueEdgeWeight(cur_vertex, currPotentialPathNodes.get(j + 1));
                        //System.out.println("COST NOW: "+cost);
                        pre_path_list.add(cur_vertex);
                    }
                }
                pre_path_list.addAll(sub_path.get_vertices());

                // 3.4.4.2 compose a candidate
                sub_path.set_weight(cost + sub_path.get_weight());
                sub_path.get_vertices().clear();
                sub_path.get_vertices().addAll(pre_path_list);

                // 3.4.4.3 put it in the candidate pool if new
                if (!pathToSourceNodeMap.containsKey(sub_path)) {
                    //System.out.println("\tWe found a new potential path!");
                    potentialPaths.add(sub_path);
                    pathToSourceNodeMap.put(sub_path, cur_recover_vertex);
                }
            }

            //System.out.println("\tNow, do some restoration (restore the edge from the current node to its successor)");
            //System.out.println(" 3.4.5 restore the edge");
            Node succ_vertex = currPotentialPathNodes.get(i + 1);
            _helper.recover_removed_edge(cur_recover_vertex, succ_vertex);

            // 3.4.6 update cost if necessary
            double cost_1 = _helper.getEdgeWeight(cur_recover_vertex, succ_vertex) + reverse_tree.get_start_vertex_distance_index().get(succ_vertex);

            if (reverse_tree.get_start_vertex_distance_index().get(cur_recover_vertex) > cost_1) {
                //System.out.println("\tUpdated cost for current node to " + cost_1);
                reverse_tree.get_start_vertex_distance_index().put(cur_recover_vertex, cost_1);
                reverse_tree.get_predecessor_index().put(cur_recover_vertex, succ_vertex);
                //System.out.println("\t----Since we're updating the cost for the current node {" + PathHelper.getLabel(cur_recover_vertex) + "), update the previous node costs as well.");
                reverse_tree.correct_cost_backward(cur_recover_vertex);
            }
        }

        //System.out.println("\tRestoring it all.");
        // 3.5 restore everything
        _helper.recover_removed_edges();
        _helper.recover_removed_vertices();

        //
        return currPotentialPath;
    }

//    public List<Path> get_shortest_paths(Node source_vertex, Node target_vertex, int top_k) {
//        return get_shortest_paths(source_vertex, target_vertex, top_k, 1, Integer.MAX_VALUE);
//    }

    /**
     * Get the top-K shortest paths connecting the source and the target.
     * This is a batch execution of top-K results.
     */
    public List<Path> get_shortest_paths(Node source_vertex, Node target_vertex, int top_k, int minLength, int maxLength, int currentSearch, int currentPaths, int maxSearches, int pathId) {
        _source_vertex = source_vertex;
        _target_vertex = target_vertex;

        _max_length = maxLength;

        //System.out.println("Yen.get_shortest_paths between " + PathHelper.getLabel(source_vertex) + " and " + PathHelper.getLabel(target_vertex));
        double progress = 5d;
        double increment = 90d / top_k;

        _init();
        int count = 0;
        while (has_next() && (count < top_k) && (count < MAX_PATHS_TO_FIND)) {
        	TaskHelper.reportProgress("Searching "+ currentSearch + " of " + maxSearches + " paths found " + currentPaths, (int) (Math.round((progress/maxSearches)*currentSearch)) );
            next();
            Path lastPath = resultPaths.get(resultPaths.size() - 1);

            TaskHelper.checkForCancel();

            /*
             * This is not a great way of limiting paths based on their length.
             * There may be a clever way of doing length limitations in the Yen
             * algorithm, or we may need another algorithm. Fun reading is
             * available by googling "k shortest paths limit".
             */
            if (lastPath.get_weight() > maxLength){
                break;
            }
            else if ((lastPath.get_weight() >= minLength) && (lastPath.get_weight() <= _max_length)) {
                progress += increment;
                lastPath.setId(Integer.toString(++pathId));
                ++count;
            }
        }

        List<Path> filteredPaths = new ArrayList<Path>();
        for (Path path : resultPaths) {
            if ((path.get_weight() <= _max_length) && (path.get_weight() >= minLength)) {
                filteredPaths.add(path);
            }
        }

        return filteredPaths;
    }

    /**
     * Return the list of results generated on the whole.
     * (Note that some of them are duplicates)
     *
     * @return
     */
    public List<Path> get_result_list() {
        return resultPaths;
    }

    /**
     * The number of distinct candidates generated on the whole.
     *
     * @return
     */
    public int get_cadidate_size() {
        return pathToSourceNodeMap.size();
    }

    public int get_generated_path_size() {
        return _generated_path_num;
    }
}