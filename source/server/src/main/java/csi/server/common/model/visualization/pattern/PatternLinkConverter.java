package csi.server.common.model.visualization.pattern;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import csi.server.dao.CsiPersistenceManager;
import csi.shared.gwt.viz.graph.tab.pattern.settings.LinkPatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class PatternLinkConverter {
	public static PersistedPatternLink toPersistedPatternLink(PatternLink patternLink, Set<PersistedPatternNode> persistedPatternNodes) {
        EntityManager em = CsiPersistenceManager.getMetaEntityManager();
        PersistedPatternLink persistedPatternLink = em.find(PersistedPatternLink.class, patternLink.getUuid());
        if(persistedPatternLink == null) {
            persistedPatternLink = new PersistedPatternLink();
		    persistedPatternLink.setUuid(patternLink.getUuid());
            em.persist(persistedPatternLink);
        }
		persistedPatternLink.setName(patternLink.getName());
        persistedPatternLink.setShowInResults(patternLink.showInResults());
        persistedPatternLink.getPatternNodes().clear();
        for (PersistedPatternNode persistedPatternNode : persistedPatternNodes) {
            if (persistedPatternNode.getUuid().equals(patternLink.getNode1().getUuid())) {
				persistedPatternLink.setNode1(persistedPatternNode);
			}
		}
		for (PersistedPatternNode persistedPatternNode:persistedPatternNodes) {
			if (persistedPatternNode.getUuid().equals(patternLink.getNode2().getUuid())) {
				persistedPatternLink.setNode2(persistedPatternNode);
			}
		}

        persistedPatternLink.getCriteria().clear();
		for (PatternCriterion patternCriterion : patternLink.getCriteria()) {
			String criteriaString = PatternCriterionConverter.convertToString(patternCriterion);
			persistedPatternLink.getCriteria().add(criteriaString);
		}
		return persistedPatternLink;
	}

	public static PatternLink toPatternLink(PersistedPatternLink persistedPatternLink, Set<PatternNode> patternNodes) {
		PatternLink patternLink = new PatternLink();
		patternLink.setUuid(persistedPatternLink.getUuid());
		patternLink.setName(persistedPatternLink.getName());
        patternLink.setShowInResults(persistedPatternLink.isShowInResults());
        PatternNode node1 = null;
        String node1uuid = persistedPatternLink.getNode1().getUuid();
        for (PatternNode patternNode : patternNodes) {
            if (patternNode.getUuid().equals(node1uuid)) {
                node1 = patternNode;
                break;
            }
        }
        patternLink.setNode1(node1);

        PatternNode node2 = null;
        String node2uuid = persistedPatternLink.getNode2().getUuid();
        for (PatternNode patternNode : patternNodes) {
            if (patternNode.getUuid().equals(node2uuid)) {
                node2 = patternNode;
                break;
            }
        }
        patternLink.setNode2(node2);
		List<String> criteria = persistedPatternLink.getCriteria();
		for (String criterionString : criteria) {
			PatternCriterion patternCriterion = PatternCriterionConverter.convertToPatternCriterion(criterionString);
            if (patternCriterion instanceof LinkPatternCriterion) {
                LinkPatternCriterion linkPatternCriterion = (LinkPatternCriterion) patternCriterion;
                patternLink.addCriterion(linkPatternCriterion);
            }
        }
		return patternLink;
	}
}
