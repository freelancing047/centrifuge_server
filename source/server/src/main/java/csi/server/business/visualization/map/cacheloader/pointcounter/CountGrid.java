package csi.server.business.visualization.map.cacheloader.pointcounter;

import java.util.BitSet;

import csi.config.Configuration;

public class CountGrid {
    private int pointLimit;
    private RootTreeNode root;
    private int[] countByLevel;

    CountGrid(int pointLimit) {
        this.pointLimit = pointLimit;
        root = new RootTreeNode();
        countByLevel = new int[3 + Configuration.getInstance().getMapConfig().getDetailLevel()];
    }

    void addCoordinate(String latitude, String longitude) {
        root.addCoordinate(latitude, longitude);
    }

    public int getCount(int precision) {
        return countByLevel[precision + 2];
    }

    class RootTreeNode {
        private NegTwoLevelNode[][] quads;

        public RootTreeNode() {
            quads = new NegTwoLevelNode[2][2];
        }

        public void addCoordinate(String latitude, String longitude) {
            int x = longitude.charAt(0) == '-' ? 0 : 1;
            int y = latitude.charAt(0) == '-' ? 0 : 1;
            NegTwoLevelNode quad = quads[x][y];
            if (quad == null) {
                quad = new NegTwoLevelNode();
                quads[x][y] = quad;
            }
            quad.addCoordinate(latitude, longitude);
        }
    }

    class NegTwoLevelNode extends AbstractHundredTreeNode {
        private HundredTreeIntermediateNode[] children;

        public NegTwoLevelNode() {
            childrenPrecision = -2;
            children = new HundredTreeIntermediateNode[2];
        }

        @Override
        public void addCoordinate(String latitude, String longitude) {
            int y = getKey(latitude);
            HundredTreeIntermediateNode child = children[y];
            if ((child == null) && (countByLevel[childrenPrecision + 2] <= pointLimit)) {
                countByLevel[childrenPrecision + 2]++;
                child = new HundredTreeIntermediateNode(childrenPrecision + 1);
                children[y] = child;
            }
            if (child != null) {
               child.addCoordinate(latitude, longitude);
            }
        }
    }

    class HundredTreeIntermediateNode extends AbstractHundredTreeNode {
        private int[] idx;
        private AbstractHundredTreeNode[] children;
        private int cursor;

        public HundredTreeIntermediateNode(int childrenPrecision) {
            this.childrenPrecision = childrenPrecision;
            idx = new int[1];
            children = new AbstractHundredTreeNode[1];
            cursor = -1;
        }

        @Override
        public void addCoordinate(String latitude, String longitude) {
            int x = getKey(longitude);
            int y = getKey(latitude);
            int index = (y * 10) + x;
            AbstractHundredTreeNode child = getChild(index);
            if ((child == null) && (countByLevel[childrenPrecision + 2] <= pointLimit)) {
                countByLevel[childrenPrecision + 2]++;
                int nextChildrenPrecision = childrenPrecision + 1;
                if (nextChildrenPrecision == Configuration.getInstance().getMapConfig().getDetailLevel()) {
                  child = new HundredTreeLeaveNode();
               } else {
                  child = new HundredTreeIntermediateNode(nextChildrenPrecision);
               }
                addChild(index, child);
            }
            if (child != null) {
               child.addCoordinate(latitude, longitude);
            }
        }

        private AbstractHundredTreeNode getChild(int index) {
            for (int i = 0; i <= cursor; i++) {
                if (idx[i] == index) {
                  return children[i];
               }
            }
            return null;
        }

        private void addChild(int index, AbstractHundredTreeNode child) {
            cursor++;
            if (cursor == idx.length) {
               expandArray();
            }
            idx[cursor] = index;
            children[cursor] = child;
        }

        private void expandArray() {
            int newLen = idx.length + 5;
            int[] newIdx = new int[newLen];
            AbstractHundredTreeNode[] newChildren = new AbstractHundredTreeNode[newLen];
            for (int i = 0; i < cursor; ++i) // copy items
            {
                newIdx[i] = idx[i];
                newChildren[i] = children[i];
            }
            idx = newIdx;
            children = newChildren;
        }
    }

    class HundredTreeLeaveNode extends AbstractHundredTreeNode {
        private BitSet children;

        public HundredTreeLeaveNode() {
            childrenPrecision = Configuration.getInstance().getMapConfig().getDetailLevel();
            children = new BitSet(100);
        }

        @Override
        public void addCoordinate(String latitude, String longitude) {
            int key = (getKey(latitude) * 10) + getKey(longitude);
            if (!children.get(key) && (countByLevel[childrenPrecision + 2] <= pointLimit)) {
                countByLevel[childrenPrecision + 2]++;
                children.set(key);
            }
        }
    }
}
