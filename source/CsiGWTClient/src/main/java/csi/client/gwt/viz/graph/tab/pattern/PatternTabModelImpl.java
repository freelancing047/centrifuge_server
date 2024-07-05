package csi.client.gwt.viz.graph.tab.pattern;

import java.util.Map;

import com.google.common.collect.Maps;

import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.shared.core.color.ClientColorHelper;

public class PatternTabModelImpl implements PatternTab.PatternTabModel {
    private final Map<PatternResultSet, ClientColorHelper.Color> colorMap = Maps.newHashMap();
    private PatternResultSet currentPattern;

    @Override
    public void setCurrentPatternColor(ClientColorHelper.Color color) {
        colorMap.put(currentPattern, color);
    }

    @Override
    public void setCurrentResults(PatternResultSet result) {
        currentPattern = result;
    }
}
