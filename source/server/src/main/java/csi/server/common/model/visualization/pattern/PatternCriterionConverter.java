package csi.server.common.model.visualization.pattern;

import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import csi.shared.gwt.viz.graph.tab.pattern.settings.DirectionLinkPatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.FieldDefNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.LabelNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.NeighborNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.NullNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.TypeNodePatternCriterion;

public class PatternCriterionConverter {
	public static String convertToString(PatternCriterion patternCriterion) {
		StringBuilder sb = new StringBuilder();
		Gson gson = new Gson();
		sb.append(patternCriterion.getType());
		sb.append(";");
		sb.append(gson.toJson(patternCriterion));
		return sb.toString();
	}
	
	public static PatternCriterion convertToPatternCriterion(String criterionString) {
        List<String> values = Lists.newArrayList(Splitter.on(';').trimResults().split(criterionString));
        String type = values.get(0);
        String value = values.get(1);
        PatternCriterion patternCriterion = null;
        Gson gson = new Gson();
        switch (type) {
            case "Field Value":
                patternCriterion = gson.fromJson(value, FieldDefNodePatternCriterion.class);
                break;
            case "Label":
                patternCriterion = gson.fromJson(value, LabelNodePatternCriterion.class);
                break;
            case "Number of Neighbors":
                patternCriterion = gson.fromJson(value, NeighborNodePatternCriterion.class);
                break;
            case "Type":
                patternCriterion = gson.fromJson(value, TypeNodePatternCriterion.class);
                break;
            case "Direction":
                patternCriterion = gson.fromJson(value, DirectionLinkPatternCriterion.class);
                break;
            case "Occurrence":
                patternCriterion = gson.fromJson(value, DirectionLinkPatternCriterion.class);
                break;
            case "":
                patternCriterion = gson.fromJson(value, NullNodePatternCriterion.class);
                break;
        }
        return patternCriterion;
    }
}
