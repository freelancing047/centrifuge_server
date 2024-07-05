package csi.server.business.visualization.graph.cluster;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cern.colt.function.IntIntDoubleFunction;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/**
 * Computes the community structure of an input graph using the algorithm due to Newman. This algorithm uses a hierarchical agglomerative clustering approach to greedily optimize a
 * metric of network modularity or community clustering.
 *
 * The output of the algorithm is a list of the successive values of the community metric at each iteration of the algorithm, and a record of each agglomeration, allowing the
 * cluster tree to be later reconstructed.
 *
 * Nov 19, 2004 - jheer - Created class
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 *
 *         Updated some aspects of the original implementation. These include introduction of the Bridge class for representing edge entries between communities.
 *
 *         TODO: 1. The current approach for edge selection is a linear, exhaustive search over all edges. This is very expensive. A PriorityQueue is currently used, but not fully
 *         leveraged! Fully implementing this will get rid of the initial search--requires that modifcations to a bridge element (e.g. changing one of the endpoints) results in a
 *         recomputation and re-insertion of the element. Still cheaper than constantly going through all edges.
 *
 */

public class CommunityStructure {
   private static final Logger LOG = LogManager.getLogger(CommunityStructure.class.getName());

    private double dQ, maxDQ = 0.0;

    private int x, y;

    private int[] maxEdge = new int[] { 0, 0 };

    private DoubleMatrix2D e;

    private PriorityQueue<Bridge> E;

    private DoubleMatrix1D a;

    private List<int[]> plist;

    private double[] qval;

    /*
     * This class captures the matrix coords and the dQ of that cell. Intended to avoid having to constantly recompute the dQ value. <p> NB: The Comparable implementation is unique
     * in that we first check the dQ values. Only if they're the same do we do something somewhat different in that lower x,y coordinates in our matrix are considered to have
     * higher values. This ensures that we prefer lower coordinates over higher ones (row values taking precedence).
     */
    class Bridge implements Comparable<Bridge> {

        int x;
        int y;
        double dQ;

        @Override
        public int compareTo(Bridge o) {
            int results = -1;
            if (BigDecimal.valueOf(dQ).compareTo(BigDecimal.valueOf(o.dQ)) == 0) {
                if (x == o.x) {
                    if (y == o.y) {
                        results = 0;
                    } else {
                        results = (y > o.y) ? -1 : 1;
                    }
                } else {
                    results = (x > o.x) ? -1 : 1;
                }
            } else {
                results = (dQ < o.dQ) ? -1 : 1;
            }

            return results;
        }
    }

    public void run(DoubleMatrix2D g) {
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        int N = g.rows();
        double Q = 0, maxQ = 0, maxIter = 0;

        plist = new ArrayList<int[]>(N - 1);
        qval = new double[N - 1];

        e = g;

        e.forEachNonZero(new ClearDiagonal());

        ZSum zSum = new ZSum();
        e.forEachNonZero(zSum);

        // distribute to each 'edge' cell the ratio relative to all edges in the graph
        e.forEachNonZero(new Mult(2 / zSum.getSum()));

        // initialize column sums
        a = new DenseDoubleMatrix1D(N);
        e.forEachNonZero(new ColSum());

        // initialize edges
        E = new PriorityQueue<Bridge>(g.columns(), Collections.reverseOrder());
        e.forEachNonZero(new EdgeCollector());

        List<Bridge> updated = new ArrayList<Bridge>();

        for (int i = 0; (i < (N - 1)) && !E.isEmpty(); i++) {

            maxDQ = Double.NEGATIVE_INFINITY;

            Bridge maxBridge = E.peek();
            x = maxBridge.x;
            y = maxBridge.y;
            maxDQ = maxBridge.dQ;

//            double na = 0.0;
            for (int k = 0; k < N; k++) {
                double v;
                v = e.getQuick(x, k) + e.getQuick(y, k);

                if (BigDecimal.valueOf(v).compareTo(BigDecimal.ZERO) != 0) {
                    e.setQuick(x, k, v);
                    e.setQuick(k, x, v);
//                    na += v;
                }
                e.setQuick(y, k, 0);
                e.setQuick(k, y, 0);
            }

            // recompute value for the target of the merge...
            a.setQuick(x, e.viewRow(x).zSum());
            a.setQuick(y, 0.0);

            if ((i % 100) == 0) {
                e.trimToSize();
            }

            mergeCommunities(updated);
            if (!updated.isEmpty()) {
                recomputeUpdatedCommunities(updated);

            }

            // update Modularity (Q) with dQ of this pass
            Q += maxDQ;
            if (Q > maxQ) {
                maxQ = Q;
                maxIter = i + 1;
            }

            // capture current modularity and record merge information for later replay.
            qval[i] = Q;
            plist.add(new int[] { x, y });

            if (LOG.isDebugEnabled()) {
               LOG.debug(Q + "\t" + "iter " + i + "\t" + "nedges = " + E.size());
            }
        }
        stopWatch.stop();

        if (LOG.isDebugEnabled()) {
           LOG.debug("maxQ = " + maxQ + ", at iter " + maxIter + " (-" + (N - maxIter) + ")");
           LOG.debug((stopWatch.getTime() / 1000.0) + " seconds");
        }
    }

    private void recomputeUpdatedCommunities(List<Bridge> updated) {
        for (Bridge b : updated) {
            if (b.x != b.y) {
                b.dQ = 2.0d * (e.getQuick(b.x, b.y) - (a.getQuick(b.x) * a.getQuick(b.y)));
            } else {
                b.dQ = Double.NEGATIVE_INFINITY;
            }
            E.add(b);
        }
    }

    private void mergeCommunities(List<Bridge> updated) {
        updated.clear();
        Iterator<Bridge> iter = E.iterator();
        while (iter.hasNext()) {
            Bridge bridge = iter.next();

            if (((bridge.x == x) && (bridge.y == y)) || ((bridge.x == y) && (bridge.y == x))) {
                iter.remove();
                continue;
            } else if (bridge.x == y) {
                bridge.x = x;
                iter.remove();
                updated.add(bridge);
            } else if (bridge.y == y) {
                bridge.y = x;
                iter.remove();
                updated.add(bridge);
            }
        }
    }

    public List<int[]> getMergeList() {
        return plist;
    }

    public double[] getQValues() {
        return qval;
    }

    // -- helpers -------------------------------------------------------------

    /*
     * Set the diagonal to zeros
     */
    public class ClearDiagonal implements IntIntDoubleFunction {

        public double apply(int arg0, int arg1, double arg2) {
            return (arg0 == arg1 ? 0.0 : arg2);
        }
    }

    public class EdgeCollector implements IntIntDoubleFunction {

        public double apply(int x, int y, double value) {
            if (x < y) {
                Bridge bridge = new Bridge();
                bridge.x = x;
                bridge.y = y;
                bridge.dQ = 2.0d * (e.getQuick(x, y) - (a.getQuick(x) * a.getQuick(y)));
                E.add(bridge);
            }

            return value;
        }
    }

    public class ColSum implements IntIntDoubleFunction {

        public double apply(int arg0, int arg1, double arg2) {
            a.setQuick(arg1, a.getQuick(arg1) + arg2);
            return arg2;
        }
    }

    public class Mult implements IntIntDoubleFunction {

        private double scalar;

        public Mult(double s) {
            scalar = s;
        }

        public double apply(int arg0, int arg1, double arg2) {
            return arg2 * scalar;
        }
    }

    public class ZSum implements IntIntDoubleFunction {

        double sum = 0;

        public double apply(int arg0, int arg1, double arg2) {
            sum += arg2;
            return arg2;
        }

        public void reset() {
            sum = 0;
        }

        public double getSum() {
            return sum;
        }
    }

}
