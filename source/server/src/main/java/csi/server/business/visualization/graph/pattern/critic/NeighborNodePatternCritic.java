package csi.server.business.visualization.graph.pattern.critic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import prefuse.Constants;
import prefuse.data.Node;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableNodeItem;

import csi.server.business.visualization.graph.GraphContext;
import csi.server.business.visualization.graph.GraphManager;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.server.business.visualization.graph.placement.BreadthFirstSearch;
import csi.shared.gwt.viz.graph.tab.pattern.settings.NeighborNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class NeighborNodePatternCritic implements NodePatternCritic {
    @Override
    public boolean criticizeNode(String dvuuid, VisualItem item, NodeStore details, PatternCriterion criterion, String jobId) {
        TableNodeItem tableNodeItem;
        NeighborNodePatternCriterion neighborCriterion;
        {//input sanitization
            if (!(criterion instanceof NeighborNodePatternCriterion)) {
                return false;
            }
            if (!(item instanceof TableNodeItem)) {
                return false;
            }
            tableNodeItem = (TableNodeItem) item;
            neighborCriterion = (NeighborNodePatternCriterion) criterion;
        }
        int numberOfneighbors = NeighborNodePatternCritic.calculateNumberOfNeighbors(tableNodeItem, neighborCriterion);
        return NeighborNodePatternCritic.criticizeValue(numberOfneighbors, neighborCriterion);
    }

    private static boolean criticizeValue(int numberOfneighbors, NeighborNodePatternCriterion criterion) {
        boolean include = true;
        if (!Strings.isNullOrEmpty(criterion.getValue())) {
            include = checkEqaulsValue(numberOfneighbors, criterion);
        }
        if (!Strings.isNullOrEmpty(criterion.getMinValue())) {
            include = include && checkGreaterThanMinValue(numberOfneighbors, criterion);
        }

        include = include && checkLessThanMaxValue(numberOfneighbors, criterion);

        return include;
    }

    private static int calculateNumberOfNeighbors(TableNodeItem item, NeighborNodePatternCriterion criterion) {
        boolean includeHidden = Boolean.parseBoolean(criterion.getIncludeHidden());
        boolean noTypeRequirement = Strings.isNullOrEmpty(criterion.getTypeOfNeighbor());

        if (noTypeRequirement && includeHidden) {
           //special case just use degree on the node
           return item.getDegree();
        }
        int neighborCount = 0;
        BreadthFirstSearch bfs = new BreadthFirstSearch();
        int degreesAway = 1;
        Iterator<TableNodeItem> selectedNodes = new ArrayList<TableNodeItem>(Arrays.asList(item)).iterator();
        bfs.init(selectedNodes, degreesAway, Constants.NODE_TRAVERSAL);
        while (bfs.hasNext()) {
            Node node = (Node) bfs.next();
            if (node.getRow() == item.getRow()) {
                continue;
            }
            if (includeHidden || GraphContext.Predicates.IsVisualizedAndDisplayable.test(node)) {
                if (noTypeRequirement|| GraphManager.getNodeDetails(node).getTypes().containsKey(criterion.getTypeOfNeighbor())) {
                    neighborCount++;
                }
            }
        }
        return neighborCount;
    }

    private static boolean checkLessThanMaxValue(int numberOfneighbors, NeighborNodePatternCriterion criterion) {
        String maxValueString = criterion.getMaxValue();
        if (Strings.isNullOrEmpty(maxValueString)) {
            return true;
        }
        try {
            int maxValueInt = Integer.parseInt(maxValueString);
            return numberOfneighbors < maxValueInt;
        } catch (NumberFormatException e) {
            //this is ok. Just ignore bad pattern criteria during evaluation.
        }
        return true;
    }

    private static boolean checkGreaterThanMinValue(int numberOfneighbors, NeighborNodePatternCriterion criterion) {
        String minValueString = criterion.getMinValue();
        if (Strings.isNullOrEmpty(minValueString)) {
            return true;
        }
        try {
            int minValueInt = Integer.parseInt(minValueString);
            return numberOfneighbors > minValueInt;
        } catch (NumberFormatException e) {
            //this is ok. Just ignore bad pattern criteria during evaluation.
        }
        return true;
    }

    private static boolean checkEqaulsValue(int numberOfneighbors, NeighborNodePatternCriterion criterion) {
        String valueString = criterion.getValue();
        if (Strings.isNullOrEmpty(valueString)) {
            return true;
        }
        try {
            int valueInt = Integer.parseInt(valueString);
            return numberOfneighbors == valueInt;
        } catch (NumberFormatException e) {
            //this is ok. Just ignore bad pattern criteria during evaluation.
        }
        return true;
    }

    @Override
    public SafeHtml getObservedValue(VisualItem item, NodeStore details, PatternCriterion criterion, String dvUuid) {
        TableNodeItem tableNodeItem;
        NeighborNodePatternCriterion neighborCriterion;
        {//input sanitization
            if (!(criterion instanceof NeighborNodePatternCriterion)) {
                return SafeHtmlUtils.fromString("");
            }
            if (!(item instanceof TableNodeItem)) {
                return SafeHtmlUtils.fromString("");
            }
            tableNodeItem = (TableNodeItem) item;
            neighborCriterion = (NeighborNodePatternCriterion) criterion;
        }
        int numberOfNeighbors = NeighborNodePatternCritic.calculateNumberOfNeighbors(tableNodeItem, neighborCriterion);
        return SafeHtmlUtils.fromString(String.valueOf(numberOfNeighbors));
    }
}
