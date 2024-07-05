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

package csi.server.business.visualization.graph.paths;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision: 430 $
 * @latest $Date: 2008-07-27 16:31:56 -0700 (Sun, 27 Jul 2008) $
 */
public class DijkstraShortestPathAlg {
    // Input
   private boolean _includeDirection = true;

    // Intermediate variables
    private Set<Node> _determined_vertex_set = new HashSet<Node>();

    public PriorityQueue<Node> _vertex_candidate_queue = new PriorityQueue<Node>(1, new Comparator<Node>() {
        public int compare(Node one, Node two) {
            double diff = _helper.getNodeWeight(one) - _helper.getNodeWeight(two);
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    });

    private Map<Node, Double> _start_vertex_distance_index = new HashMap<Node, Double>();

    private Map<Node, Node> _predecessor_index = new HashMap<Node, Node>();

    private PathHelper _helper;

    /**
     * Default constructor.
     *
     * @param graph
     */
    public DijkstraShortestPathAlg(final Graph graph, PathHelper helper, boolean includeDirection, boolean includeHidden) {
        _helper = helper;
        _includeDirection = includeDirection;
    }

    /**
     * Clear intermediate variables.
     */
    public void clear() {
        _determined_vertex_set.clear();
        _vertex_candidate_queue.clear();
        _start_vertex_distance_index.clear();
        _predecessor_index.clear();
    }

    /**
     * Getter for the distance in terms of the start vertex
     *
     * @return
     */
    public Map<Node, Double> get_start_vertex_distance_index() {
        return _start_vertex_distance_index;
    }

    /**
     * Getter for the index of the predecessors of vertices
     *
     * @return
     */
    public Map<Node, Node> get_predecessor_index() {
        return _predecessor_index;
    }

    /**
     * Construct a flower rooted at "root" with
     * the shortest paths from the other vertices.
     *
     * @param root
     */
    public void get_shortest_path_flower(Node root) {
        //System.out.println("DSP.get_shortest_path_flower with root " + PathHelper.getLabel(root));
        determine_shortest_paths(null, root, false);
    }

    /**
     * Do the work
     */
    protected void determine_shortest_paths(Node source_vertex, Node sink_vertex, boolean is_source2sink) {
        //System.out.println("DSP.determine_shortest_paths source:" + PathHelper.getLabel(source_vertex) + ", target: " + PathHelper.getLabel(sink_vertex) + ", source2sink? " + is_source2sink);
        // 0. clean up variables
        clear();

        // 1. initialize members
        Node end_vertex = is_source2sink ? sink_vertex : source_vertex;
        Node start_vertex = is_source2sink ? source_vertex : sink_vertex;
        _start_vertex_distance_index.put(start_vertex, 0d);
        _helper.setNodeWeight(start_vertex, 0d);
        _vertex_candidate_queue.add(start_vertex);

        //System.out.println("Done initializing");

        // 2. start searching for the shortest path
        while (!_vertex_candidate_queue.isEmpty()) {
            Node cur_candidate = _vertex_candidate_queue.poll();
            //System.out.println("\tCandidate: " + PathHelper.getLabel(cur_candidate));

            if (cur_candidate.equals(end_vertex)) {
                //System.out.println("\tCANDIDATE == END.  BREAK.");
                break;
            }

            _determined_vertex_set.add(cur_candidate);

            _improve_to_vertex(cur_candidate, is_source2sink);
        }
    }

    /**
     * Update the distance from the source to the concerned vertex.
     *
     * @param vertex
     */
    private void _improve_to_vertex(Node vertex, boolean is_source2sink) {
        //System.out.println("\t\tITV: " + PathHelper.getLabel(vertex) + ", include direction: " + _includeDirection);
        // 1. get the neighboring vertices

        Iterator<?> neighbor_vertex_list = null;
        String dir = PathHelper.ANY;
        if (_includeDirection) {
            dir = is_source2sink ? PathHelper.OUTBOUND : PathHelper.INBOUND;
        }
        neighbor_vertex_list = _helper.getNeighborNodes(vertex, dir);

        // 2. update the distance passing on current vertex
        while (neighbor_vertex_list.hasNext()) {
            Node cur_adjacent_vertex = (Node) neighbor_vertex_list.next();
            //System.out.println("\t\t\tChecking neighbor " + PathHelper.getLabel(cur_adjacent_vertex));

            // 2.1 skip if visited before
            if (_determined_vertex_set.contains(cur_adjacent_vertex)) {
                //System.out.println("\t\t\t\tWe already visited this node.");
                continue;
            }

            // 2.2 calculate the new distance
            double distance = _start_vertex_distance_index.containsKey(vertex)
                                 ? _start_vertex_distance_index.get(vertex)
                                 : Constants.DISCONNECTED;

            //System.out.println("\t\t\t\tDistance to " + PathHelper.getLabel(vertex) + ": " + distance);
            //Not 100% sure this is right -- might need to take direction into account here...
            distance += is_source2sink ? _helper.getEdgeWeight(vertex, cur_adjacent_vertex) : _helper.getEdgeWeight(cur_adjacent_vertex, vertex);

            //System.out.println("\t\t\t\tDetermined distance to " + PathHelper.getLabel(cur_adjacent_vertex) + ": " + distance + " (updating? "                    + !_start_vertex_distance_index.containsKey(cur_adjacent_vertex) + ")");

            // 2.3 update the distance if necessary
            if (!_start_vertex_distance_index.containsKey(cur_adjacent_vertex) ||
                (_start_vertex_distance_index.get(cur_adjacent_vertex) > distance)) {
                //System.out.println("\t\t\t\tUpdating distance: " + PathHelper.getLabel(cur_adjacent_vertex) + " is " + distance + " from the start");
                _start_vertex_distance_index.put(cur_adjacent_vertex, distance);

                _predecessor_index.put(cur_adjacent_vertex, vertex);

                _helper.setNodeWeight(cur_adjacent_vertex, distance);

                _vertex_candidate_queue.add(cur_adjacent_vertex);
            }
        }
    }

    /**
     * Note that, the source should not be as same as the sink! (we could extend
     * this later on)
     *
     * @param source_vertex
     * @param sink_vertex
     * @return
     */
    public Path get_shortest_path(Node source_vertex, Node sink_vertex, int maxLength) {
        //System.out.println("DSP.get_shortest_path");
        determine_shortest_paths(source_vertex, sink_vertex, true);
        //
        List<Node> vertex_list = new ArrayList<Node>();
        double weight = _start_vertex_distance_index.containsKey(sink_vertex)
                           ? _start_vertex_distance_index.get(sink_vertex)
                           : Constants.DISCONNECTED;

        if ((BigDecimal.valueOf(weight).compareTo(BigDecimal.valueOf(Constants.DISCONNECTED)) != 0) &&
            (weight <= maxLength)) {
            Node cur_vertex = sink_vertex;
            do {
                vertex_list.add(cur_vertex);
                cur_vertex = _predecessor_index.get(cur_vertex);
            } while ((cur_vertex != null) && (cur_vertex != source_vertex));
            //
            vertex_list.add(source_vertex);
            Collections.reverse(vertex_list);
        }

        return new Path(vertex_list, weight);
    }

    // / for updating the cost

    /**
     * Calculate the distance from the target vertex to the input
     * vertex using forward star form.
     * (FLOWER)
     *
     * @param vertex
     */
    public Path update_cost_forward(Node vertex) {
        //System.out.println("--->DSP.update_cost_forward for " + PathHelper.getLabel(vertex));
        double cost = Constants.DISCONNECTED;

        // 1. get the set of successors of the input vertex
        Iterator<?> adj_vertex_set = _helper.getNeighborNodes(vertex, _includeDirection ? PathHelper.OUTBOUND : PathHelper.ANY);

        // 2. make sure the input vertex exists in the index
        if (!_start_vertex_distance_index.containsKey(vertex)) {
            //System.out.println("\t\tDon't have distance for this node");
            _start_vertex_distance_index.put(vertex, Constants.DISCONNECTED);
        } else {
            //System.out.println("\t\tHave distance for this node: " + _start_vertex_distance_index.get(vertex));
        }

        // 3. update the distance from the root to the input vertex if necessary
        while (adj_vertex_set.hasNext()) {
            Node cur_vertex = (Node) adj_vertex_set.next();
            //System.out.println("\t\t\tLooking at adjacent node " + PathHelper.getLabel(cur_vertex));

            // 3.1 get the distance from the root to one successor of the input
            // vertex
            double distance = _start_vertex_distance_index.containsKey(cur_vertex) ? _start_vertex_distance_index.get(cur_vertex) : Constants.DISCONNECTED;
            //System.out.println("\t\t\tCurrent distance for " + PathHelper.getLabel(cur_vertex) + ": " + distance);

            // 3.2 calculate the distance from the root to the input vertex
            distance += _helper.getEdgeWeight(vertex, cur_vertex);

            // 3.3 update the distance if necessary
            double cost_of_vertex = _start_vertex_distance_index.get(vertex);

            //System.out.println("\t\t\tcost of " + PathHelper.getLabel(vertex) + ": " + cost_of_vertex + ", current node's distance: " + distance);
            if (cost_of_vertex > distance) {
                //System.out.println("\t\t\tUpdating distance of " + PathHelper.getLabel(vertex) +" to be "+distance);
                //System.out.println("\t\t\tUpdating predecessor of " + PathHelper.getLabel(vertex) + " to be " + PathHelper.getLabel(cur_vertex));
                _start_vertex_distance_index.put(vertex, distance);
                _predecessor_index.put(vertex, cur_vertex);
                cost = distance;
            }
        }

        // 4. create the sub_path if exists
        Path sub_path = null;
        if (cost < Constants.DISCONNECTED) {
            sub_path = new Path();
            sub_path.set_weight(cost);
            List<Node> vertex_list = sub_path.get_vertices();
            vertex_list.add(vertex);

            Node sel_vertex = _predecessor_index.get(vertex);
            while (sel_vertex != null) {
                vertex_list.add(sel_vertex);
                sel_vertex = _predecessor_index.get(sel_vertex);
            }
        }

        //System.out.println("<----END DSP.update_cost_forward for " + PathHelper.getLabel(vertex));
        return sub_path;
    }

    /**
     * Correct costs of successors of the input vertex using backward star form.
     * (FLOWER)
     *
     * @param vertex
     */
    public void correct_cost_backward(Node vertex) {
        //System.out.println("---->DSP.correct_cost_backward for " + PathHelper.getLabel(vertex));
        // 1. initialize the list of vertex to be updated
        List<Node> vertex_list = new LinkedList<Node>();
        vertex_list.add(vertex);

        // 2. update the cost of relevant precedents of the input vertex
        while (!vertex_list.isEmpty()) {
            Node cur_vertex = vertex_list.remove(0);
            double cost_of_cur_vertex = _start_vertex_distance_index.get(cur_vertex);
            //System.out.println("\t\tCur node: " + PathHelper.getLabel(cur_vertex) + ", cost: " + cost_of_cur_vertex);
            Iterator<?> pre_vertex_set = _helper.getNeighborNodes(cur_vertex, _includeDirection ? PathHelper.OUTBOUND : PathHelper.ANY);
            while (pre_vertex_set.hasNext()) {
                Node pre_vertex = (Node) pre_vertex_set.next();
                double cost_of_pre_vertex = _start_vertex_distance_index.containsKey(pre_vertex) ? _start_vertex_distance_index.get(pre_vertex) : Constants.DISCONNECTED;

                double fresh_cost = cost_of_cur_vertex + _helper.getEdgeWeight(pre_vertex, cur_vertex);

                //System.out.println("\t\t\tPRE VERTEX: "+PathHelper.getLabel(pre_vertex)+" old cost: " + cost_of_pre_vertex + ", new cost: " + fresh_cost);
                if (cost_of_pre_vertex > fresh_cost) {
                    //System.out.println("\t\t\t\tUpdating cost for " + PathHelper.getLabel(pre_vertex));
                    _start_vertex_distance_index.put(pre_vertex, fresh_cost);
                    _predecessor_index.put(pre_vertex, cur_vertex);
                    vertex_list.add(pre_vertex);
                }
            }
        }

        //System.out.println("<----END DSP.correct_cost_backward for " + PathHelper.getLabel(vertex));
    }
}
