package csi.server.common.service.api;

import java.util.List;

import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.vortex.VortexService;

public interface PatternActionsServiceProtocol extends VortexService {
	public List<GraphPattern> getGraphPatterns(String owner);

    public void saveGraphPatterns(String owner, List<GraphPattern> graphPatterns);
    
    public void saveGraphPattern(String owner, GraphPattern graphPattern);
    
    public void deleteGraphPattern(GraphPattern graphPattern);
    
    public void updateGraphPattern(GraphPattern graphPattern);
}
