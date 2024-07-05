package csi.server.business.service.pattern;

import java.util.List;

import csi.server.common.model.visualization.pattern.GraphPatternPersister;
import csi.server.common.service.api.PatternActionsServiceProtocol;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;

public class PatternActionsService implements PatternActionsServiceProtocol {

	@Override
	public List<GraphPattern> getGraphPatterns(String owner) {
		return GraphPatternPersister.retrieve(owner);
	}

	@Override
	public void saveGraphPatterns(String owner, List<GraphPattern> graphPatterns) {
		GraphPatternPersister.persist(owner, graphPatterns);
	}

	@Override
	public void saveGraphPattern(String owner, GraphPattern graphPattern) {
		GraphPatternPersister.persist(owner, graphPattern);
	}

	@Override
	public void deleteGraphPattern(GraphPattern graphPattern) {
		GraphPatternPersister.remove(graphPattern);
	}

	@Override
	public void updateGraphPattern(GraphPattern graphPattern) {
		GraphPatternPersister.update(graphPattern);
	}
}
