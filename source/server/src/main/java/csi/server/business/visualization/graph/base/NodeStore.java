package csi.server.business.visualization.graph.base;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import prefuse.Constants;
import prefuse.data.Node;
import prefuse.data.util.BreadthFirstIterator;
import prefuse.util.io.XMLWriter;

import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.layout.Ring;
import csi.server.common.model.visualization.graph.GraphConstants;
import csi.server.task.api.TaskController;

//TODO: Remove circular Node placement code.
public class NodeStore extends AbstractGraphObjectStore {
   private static final Logger LOG = LogManager.getLogger(NodeStore.class);

    public static final String NODE_LEGEND_INFO = "nodeLegendInfo";
    protected String icon;

    protected String shape;

    protected double relativeSize = 1.0d;

    protected boolean anchored;

    // for Circular Layout
    protected transient int maxGroup; // index into groups with most members

    protected transient int nonMaxGroup; // index of cat and dogs group

    protected transient int ring; // Circular ring number

    protected transient int numVisibleNeighbors; // count of visible neighbors

    protected transient double radial; // angle around ring

    protected transient NodeStore ringMaster; // Circular master node for ring

    protected transient Vector<Vector<NodeStore>> groups; // grouped neighbors

    protected transient Map<GraphConstants.eLayoutAlgorithms, Point2D> positions = new HashMap<GraphConstants.eLayoutAlgorithms, Point2D>();

    public void writeGraphML(XMLWriter xml) {
        xml.start("object", "type", "NodeStore");
        if (icon != null) {
         xml.contentTag("icon", icon);
      }
        if (shape != null) {
         xml.contentTag("shape", shape);
      }
        if (anchored) {
         xml.contentTag("anchored", "true");
      }
        if (hideLabels) {
            xml.contentTag("hideLabels", "false");
        }
        super.writeSuperML(xml);
        xml.end();
    }

    public Point getPosition(GraphConstants.eLayoutAlgorithms layoutEnum) {
        Point point = null;
        if (this.positions != null) {
            point = (Point) positions.get(layoutEnum);
        }

        if (point == null) {
            point = new Point();
        }

        return point;
    }

    public void setPosition(GraphConstants.eLayoutAlgorithms layoutEnum, Point2D point) {
        if (this.positions == null) {
            this.positions = new HashMap<GraphConstants.eLayoutAlgorithms, Point2D>();
        }
        this.positions.put(layoutEnum, point);
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    // hack -- unbundling needs to be revamped to maintain idea of single node bundles/groups.
    protected void checkBundle() {
        if ((this.children != null) && (this.children.size() == 1)) {
            NodeStore singleChild = (NodeStore) this.children.get(0);
            singleChild.copyPositions();
        }

        super.checkBundle();
    }

    public void unBundle() {
        if (parent != null) {
            List<AbstractGraphObjectStore> siblings = parent.getChildren();
            siblings.remove(this);
            parent.rectifyBundle();

            copyPositions();
        }

        this.rectifyBundle();
        this.parent = null;
    }

    protected void copyPositions() {

    }

    public void initCircular() {
        this.ring = 0;
        this.ringMaster = null;
        this.maxGroup = -1;
        this.nonMaxGroup = -1;
        this.numVisibleNeighbors = 0;
        if (this.groups == null) {
         this.groups = new Vector<Vector<NodeStore>>();
      } else {
         this.groups.clear();
      }
    }

    public void groupNeighbors(Node myPrefuseNode) {
        BreadthFirstIterator bfs = new BreadthFirstIterator();
        bfs.init(myPrefuseNode, 1, Constants.NODE_TRAVERSAL);

        while (bfs.hasNext()) {
            TaskController.getInstance().checkForCancel();
            Node neighbor = (Node) bfs.next();
            NodeStore neighborStore = GraphManager.getNodeDetails(neighbor);
            if ((neighborStore != this) && neighborStore.isDisplayable()) {
                neighborStore.addToGroup(this, neighbor, -1);
                this.numVisibleNeighbors++;
            }
        }

        calcMaxGroup();
    }

    public int calcVisibleNeighbors(Node myPrefuseNode) {
        BreadthFirstIterator bfs = new BreadthFirstIterator();
        bfs.init(myPrefuseNode, 1, Constants.NODE_TRAVERSAL);
        this.numVisibleNeighbors = 0;

        while (bfs.hasNext()) {
            Node neighbor = (Node) bfs.next();
            NodeStore neighborStore = GraphManager.getNodeDetails(neighbor);
            if ((neighborStore != this) && neighborStore.isDisplayable()) {
                this.numVisibleNeighbors++;
            }
        }

        return this.numVisibleNeighbors;
    }

    private void addToGroup(NodeStore mainNodeStore, Node myPrefuseNode, int myGroup) {
        // has neighbor been encountered already
        for (Vector<NodeStore> group : mainNodeStore.groups) {
            if (group.indexOf(this) > -1) {
                return;
            }
        }

        if (myGroup == -1) {

            mainNodeStore.groups.add(new Vector<NodeStore>());
            myGroup = mainNodeStore.groups.size() - 1;

        } else if (myGroup >= mainNodeStore.groups.size()) {

           LOG.error(String.format("Called addToGroup with group ID %d, but only %d groups are in node store.", myGroup, mainNodeStore.groups.size()));

        }

        mainNodeStore.groups.get(myGroup).add(this);

        BreadthFirstIterator bfs = new BreadthFirstIterator();
        bfs.init(myPrefuseNode, 1, Constants.NODE_TRAVERSAL);

        while (bfs.hasNext()) {
            Node neighbor = (Node) bfs.next();
            NodeStore neighborStore = GraphManager.getNodeDetails(neighbor);
            if ((neighborStore != mainNodeStore) && (neighborStore != this) && neighborStore.isDisplayable()) {
               neighborStore.addToGroup(mainNodeStore, neighbor, myGroup);
            }
        }
    }

   private void calcMaxGroup() {
      int maxSize = 0;
      int howMany = this.groups.size();

      for (int i = 0; i < howMany; i++) {
         if (this.groups.get(i).size() > maxSize) {
            this.maxGroup = i;
            maxSize = this.groups.get(i).size();
         }
      }
   }

    public void combineNonMaxGroups(NodeStore winner, boolean init) {
        if (this.maxGroup == -1) {
            this.nonMaxGroup = -1;
            return;
        }

        // combine remaining members into one group
        if (init) {
            this.nonMaxGroup = groups.size();
            this.groups.add(new Vector<NodeStore>());

        } else {

            if (this.nonMaxGroup == -1) {
               return;
            }

            // if winner not in non_max_group, we're OK
            boolean found = false;
            for (NodeStore nodeStore : this.groups.get(this.nonMaxGroup)) {

                if (nodeStore == winner) {
                    found = true;
                    break;
                }
            }
            if (!found) {
               return;
            }

            this.groups.get(this.nonMaxGroup).clear();
        }

        Vector<NodeStore> groupNonMax = groups.get(this.nonMaxGroup);

        for (int j = 0; j < this.nonMaxGroup; j++) {
            if (j != this.maxGroup) {
                Vector<NodeStore> groupJ = groups.get(j);
                boolean skipGroup = false;

                if (winner != null) {
                    for (NodeStore nodeStore : groupJ) {
                        if (winner == nodeStore) {
                            skipGroup = true;
                            break;
                        }
                    }
                }

                if (!skipGroup) {
                    for (NodeStore nodeStore : groupJ) {
                        groupNonMax.add(nodeStore);
                    }
                }
            }
        }

        if ((this != winner) && (this.nonMaxGroup == 2)) {
            groupNonMax.clear();
            this.maxGroup = this.nonMaxGroup = -1;
        }

        return;
    }

    public void setGroupRing(int selectedGroup, int selectedRing, int ringMinNodes, boolean doMostConnected) {
        if (selectedGroup == -1) {
         return;
      }

        while (true) {
            int maxSize = 0, numRemaining = 0;
            NodeStore winner = null;

            for (NodeStore nodeStore : this.groups.get(selectedGroup)) {
                if (nodeStore.getRing() == 0) {
                  numRemaining++;
               }
            }

            // find most highly connected group member
            // OR the one with longest non-max group
            for (NodeStore nodeStore : this.groups.get(selectedGroup)) {
                if ((nodeStore.getRing() == 0) && (nodeStore.getNonMaxGroup() > -1)) {
                    int subringSize = nodeStore.groups.get(nodeStore.getNonMaxGroup()).size();
                    if ((doMostConnected && (nodeStore.numVisibleNeighbors > maxSize)) || (!doMostConnected && (subringSize > maxSize))) {
                        if ((subringSize >= ringMinNodes) && ((numRemaining - subringSize) >= ringMinNodes)) {
                            winner = nodeStore;
                            maxSize = (doMostConnected ? nodeStore.numVisibleNeighbors : subringSize);
                        }
                    }
                }
            }

            if (winner == null) {
                break;
            } else {
                // remove non-max group members for doMostConnected option
                if (doMostConnected) {
                    for (NodeStore nodeStore : this.groups.get(selectedGroup)) {
                        if ((nodeStore.getRing() == 0) && (nodeStore.getNonMaxGroup() > -1)) {
                           if (nodeStore.groups.get(nodeStore.getNonMaxGroup()).size() > winner.groups.get(winner.getNonMaxGroup()).size()) {
                              nodeStore.combineNonMaxGroups(winner, false);
                           }
                        }
                    }
                }

                winner.setRing(selectedRing);
                winner.setRingMaster(this);
                winner.setGroupRing(winner.getNonMaxGroup(), selectedRing + 1, ringMinNodes, doMostConnected);
            }
        }

        for (NodeStore nodeStore : this.groups.get(selectedGroup)) {
            if (nodeStore.getRing() == 0) {
                nodeStore.setRing(selectedRing);
                nodeStore.setRingMaster(this);
            }
        }

        return;
    }

    public void placeGroupRing(int selectedGroup, int selectedRing, int maxRing, int ringSubFactor, boolean ringSizeByRingNum, Vector<Ring> rings) {
        int count = 0;
        boolean anyNonNegative = false;

        // count members on this ring
        for (NodeStore nodeStore : this.groups.get(selectedGroup)) {
            if (nodeStore.getRing() == selectedRing) {
               count++;
            }
        }

        // order members on this ring
        int i = 0;
        int groupSize = groups.get(selectedGroup).size();

        NodeOrder nodeOrder[] = new NodeOrder[count];

        for (int j = 0; j < groupSize; j++) {
            NodeStore member = groups.get(selectedGroup).get(j);
            if (member.getRing() == selectedRing) {
                nodeOrder[i] = new NodeOrder();
                nodeOrder[i].index = j;
                nodeOrder[i].groupSize = (member.getNonMaxGroup() > -1 ? member.groups.get(member.getNonMaxGroup()).size() : -1);
                if (nodeOrder[i].groupSize > -1) {
                  anyNonNegative = true;
               }
                i++;
            }
        }

        if (anyNonNegative) {
            Comparator<NodeOrder> groupSizeCompare = new GroupSizeComparator();
            Arrays.sort(nodeOrder, groupSizeCompare);
        }

        groupSize = ((Double) (count + (((groupSize - count) * ringSubFactor) / 100.))).intValue();
        double radius = (100. * (groupSize < 4 ? 4 : groupSize)) / (2. * Math.PI);
        if (ringSizeByRingNum) {
         radius *= Math.sqrt(maxRing / ring);
      }
        Point center = this.getPosition(GraphConstants.eLayoutAlgorithms.circle);
        double xcenter = center.getX() + (radius * Math.cos(radial) * (selectedRing > 1 ? 1 : -1));
        double ycenter = center.getY() + (radius * Math.sin(radial) * (selectedRing > 1 ? 1 : -1));
        double angle1 = radial - (Math.PI * (selectedRing > 1 ? 1 : 0));
        double angle, astep = (2. * Math.PI) / (count + 1);

        Ring ring = new Ring();
        ring.setRing(selectedRing);
        ring.setRingMaster(this);
        ring.setNumMembers(count + 1); // ring master not in count
        ring.setXCenter(xcenter);
        ring.setYCenter(ycenter);
        rings.add(ring);

        boolean taken[] = new boolean[count];
        for (int k = 0; k < count; k++) {
         taken[k] = false;
      }

        for (int k = 0; k < count; k++) {
            int pos = 0;
            NodeStore member = groups.get(selectedGroup).get(nodeOrder[k].index);
            if (member.getRing() == selectedRing) {
                if ((nodeOrder[k].groupSize == -1) || (k > 6)) { // find next free index
                    for (int j = 0; j < count; j++) {
                        if (!taken[j]) {
                            pos = j + 1;
                            break;
                        }
                    }
                } else { // distribute groups around circle so as to preclude overlap of the largest ones
                    if (count < 7) {
                        int posArray[][] = { {}, { 0 }, { 0, 1 }, { 1, 0, 2 }, { 2, 0, 3, 1 }, { 2, 0, 4, 1, 3 }, { 3, 1, 5, 2, 0, 4 }, { 3, 5, 1, 4, 0, 6, 2 } };
                        pos = posArray[count][k] + 1;
                    } else {
                        double dPos = 0d;
                        switch (k) {
                        case 0:
                            dPos = 0.5 * (count + 1);
                            break;
                        case 1:
                            dPos = 0.25 * (count + 1);
                            break;
                        case 2:
                            dPos = 0.75 * (count + 1);
                            break;
                        case 3:
                            dPos = 0.625 * (count + 1);
                            break;
                        case 4:
                            dPos = 0.875 * (count + 1);
                            break;
                        case 5:
                            dPos = 0.375 * (count + 1);
                            break;
                        case 6:
                            dPos = 0.125 * (count + 1);
                            break;
                        }
                        pos = Long.valueOf(Math.round(dPos)).intValue();
                        if (taken[pos - 1]) {
                            if (BigDecimal.valueOf(Math.round(dPos)).compareTo(BigDecimal.valueOf(Math.ceil(dPos))) == 0) {
                              pos = Double.valueOf(Math.floor(dPos)).intValue();
                           } else {
                              pos = Double.valueOf(Math.ceil(dPos)).intValue();
                           }
                        }
                    }
                }

                taken[pos - 1] = true;
                angle = angle1 + (pos * astep);
                member.setRadial(angle);
                Point point = new Point();
                point.setLocation(xcenter + (radius * Math.cos(angle)), ycenter + (radius * Math.sin(angle)));
                member.setPosition(GraphConstants.eLayoutAlgorithms.circle, point);

                // is this group member a ring master for next level?
                int memberNonMax = member.getNonMaxGroup();
                if (memberNonMax > -1) {
                    Vector<NodeStore> memberNonMaxGroup = member.groups.get(memberNonMax);
                    if (!memberNonMaxGroup.isEmpty() && (memberNonMaxGroup.get(0).getRingMaster() == member)) {
                       member.placeGroupRing(memberNonMax, member.getRing() + 1, maxRing, ringSubFactor, ringSizeByRingNum, rings);
                  }
                }
            }
        }
    }

    public int getMaxGroup() {
        return maxGroup;
    }

    public int getMaxGroupSize() {
        if (this.maxGroup > 0) {
         return this.groups.get(this.maxGroup).size();
      } else {
         return -1;
      }
    }

    public int getNonMaxGroup() {
        return nonMaxGroup;
    }

    public int getRing() {
        return ring;
    }

    public void setRing(int ring) {
        this.ring = ring;
    }

    public double getRadial() {
        return radial;
    }

    public void setRadial(double radial) {
        this.radial = radial;
    }

    public NodeStore getRingMaster() {
        return ringMaster;
    }

    public void setRingMaster(NodeStore ringMaster) {
        this.ringMaster = ringMaster;
    }

    public int getNumVisibleNeighbors() {
        return numVisibleNeighbors;
    }

    public boolean isAnchored() {
        return anchored;
    }

    public void setAnchored(boolean anchored) {
        this.anchored = anchored;
    }

    public double getRelativeSize() {
        return relativeSize;
    }

    public void setRelativeSize(double value) {
        relativeSize = value;
    }

    public void addType(String type) {
        if (type.length() == 0) {
            type = GraphConstants.UNSPECIFIED_NODE_TYPE;
        }
        super.addType(type);

        String typeName = type.trim();
        if (this.type.equals(GraphConstants.UNSPECIFIED_NODE_TYPE)) {
            this.type = typeName;
        }
    }

    private class NodeOrder {

        public int index;

        public int groupSize;
    }

    private class GroupSizeComparator implements Comparator<NodeOrder> {

        public int compare(NodeOrder nodeOrder1, NodeOrder nodeOrder2) {
            if (nodeOrder1.groupSize > nodeOrder2.groupSize) {
               return -1;
            } else if (nodeOrder1.groupSize < nodeOrder2.groupSize) {
               return 1;
            } else {
               return 0;
            }
        }
    }
}
