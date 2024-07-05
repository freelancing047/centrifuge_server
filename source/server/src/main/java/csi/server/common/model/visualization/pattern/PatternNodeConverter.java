package csi.server.common.model.visualization.pattern;

import java.util.List;

import javax.persistence.EntityManager;

import csi.server.dao.CsiPersistenceManager;
import csi.shared.gwt.viz.graph.tab.pattern.settings.NodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class PatternNodeConverter {
	public static PersistedPatternNode toPersistedPatternNode(PatternNode patternNode) {
        EntityManager em = CsiPersistenceManager.getMetaEntityManager();
        PersistedPatternNode persistedPatternNode = em.find(PersistedPatternNode.class, patternNode.getUuid());
        if(persistedPatternNode == null) {
            persistedPatternNode = new PersistedPatternNode();
		    persistedPatternNode.setUuid(patternNode.getUuid());
            em.persist(persistedPatternNode);
        }
		persistedPatternNode.setName(patternNode.getName());
        persistedPatternNode.setShowInResults(patternNode.showInResults());
        persistedPatternNode.setDrawX(patternNode.getDrawX());
        persistedPatternNode.setDrawY(patternNode.getDrawY());
        persistedPatternNode.getCriteria().clear();
        for (PatternCriterion patternCriterion : patternNode.getCriteria()) {
			String criterionString = PatternCriterionConverter.convertToString(patternCriterion);
			persistedPatternNode.getCriteria().add(criterionString);
		}
		return persistedPatternNode;
	}

	public static PatternNode toPatternNode(PersistedPatternNode persistedPatternNode) {
		PatternNode patternNode = new PatternNode();
		patternNode.setUuid(persistedPatternNode.getUuid());
		patternNode.setName(persistedPatternNode.getName());
        patternNode.setShowInResults(persistedPatternNode.isShowInResults());
        patternNode.setDrawY(persistedPatternNode.getDrawY());
        patternNode.setDrawX(persistedPatternNode.getDrawX());
		List<String> criteria = persistedPatternNode.getCriteria();
		for (String criterionString : criteria) {
            PatternCriterion patternCriterion = PatternCriterionConverter.convertToPatternCriterion(criterionString);
            if (patternCriterion instanceof NodePatternCriterion) {
                NodePatternCriterion nodePatternCriterion = (NodePatternCriterion) patternCriterion;
                patternNode.addCriterion(nodePatternCriterion);
            }
        }
        return patternNode;
	}
}
