package csi.server.business.visualization.graph.pattern.critic;

import java.util.List;
import java.util.Map;

import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableNodeItem;

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.shared.gwt.viz.graph.tab.pattern.settings.OccurrencePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class OccurrenceCritic implements NodePatternCritic, LinkPatternCritic {
    @Override
    public boolean criticizeNode(String dvuuid, VisualItem item, NodeStore details, PatternCriterion criterion, String jobId) {
        OccurrencePatternCriterion occurrenceCrition;
        {//input sanitization
            if (!(criterion instanceof OccurrencePatternCriterion)) {
                return false;
            }
            if (!(item instanceof TableNodeItem)) {
                return false;
            }
            occurrenceCrition = (OccurrencePatternCriterion) criterion;
        }
        int numberOfneighbors = OccurrenceCritic.calculateNumberOfOccurrence(details);
        return OccurrenceCritic.criticizeValue(numberOfneighbors, occurrenceCrition);
    }

    private static boolean criticizeValue(int numberOfneighbors, OccurrencePatternCriterion criterion) {
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

    private static int calculateNumberOfOccurrence(LinkStore details) {
        int value = 0;
        Map<String, List<Integer>> rows = details.getRows();

        value = sumRows(value, rows);
        return value;
    }

    private static int sumRows(int value, Map<String, List<Integer>> rows) {
        for (Map.Entry<String, List<Integer>> entry : rows.entrySet()) {
            if (entry.getValue() != null) {
                value += entry.getValue().size();
            }
        }
        return value;
    }

    private static boolean checkLessThanMaxValue(int numberOfneighbors, OccurrencePatternCriterion criterion) {
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

    private static boolean checkGreaterThanMinValue(int numberOfneighbors, OccurrencePatternCriterion criterion) {
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

    private static boolean checkEqaulsValue(int numberOfneighbors, OccurrencePatternCriterion criterion) {
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
        int numberOfNeighbors = OccurrenceCritic.calculateNumberOfOccurrence(details);
        return SafeHtmlUtils.fromString(String.valueOf(numberOfNeighbors));
    }

    private static int calculateNumberOfOccurrence(NodeStore details) {
        int value = 0;
        Map<String, List<Integer>> rows = details.getRows();
        value = sumRows(value, rows);
        return value;
    }

    @Override
    public boolean criticizeLink(String dvuuid, EdgeItem item, LinkStore details, PatternCriterion criterion) {
        OccurrencePatternCriterion occurrenceCrition;
        {//input sanitization
            if (!(criterion instanceof OccurrencePatternCriterion)) {
                return false;
            }
            if (!(item instanceof TableNodeItem)) {
                return false;
            }
            occurrenceCrition = (OccurrencePatternCriterion) criterion;
        }
        int numberOfneighbors = OccurrenceCritic.calculateNumberOfOccurrence(details);
        return OccurrenceCritic.criticizeValue(numberOfneighbors, occurrenceCrition);
    }

    @Override
    public SafeHtml getObservedValue(VisualItem edge, LinkStore details, PatternCriterion patternCriterion, String dvuuid) {
        int numberOfNeighbors = OccurrenceCritic.calculateNumberOfOccurrence(details);
        return SafeHtmlUtils.fromString(String.valueOf(numberOfNeighbors));
    }

    @Override
    public boolean criticizeReverseLink(String dvuuid, EdgeItem item, LinkStore details, PatternCriterion criterion) {
        return criticizeLink(dvuuid, item, details, criterion);
    }
}
