package csi.server.common.model.visualization.pattern;

import java.util.Set;

import javax.persistence.EntityManager;

import com.google.common.collect.Sets;

import csi.server.dao.CsiPersistenceManager;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternLink;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternNode;

public class PatternConverter {
    public static PersistedPattern toPersistentPattern(String owner, GraphPattern graphPattern) {
        EntityManager em = CsiPersistenceManager.getMetaEntityManager();
        PersistedPattern persistedPattern = em.find(PersistedPattern.class, graphPattern.getUuid());
        if (persistedPattern == null) {
            persistedPattern = new PersistedPattern();
            persistedPattern.setUuid(graphPattern.getUuid());
            em.persist(persistedPattern);
        }
        persistedPattern.setOwner(owner);
        persistedPattern.setName(graphPattern.getName());
        persistedPattern.setRequireDistinctNodes(graphPattern.isRequireDistinctNodes());
        persistedPattern.setRequireDistinctLinks(graphPattern.isRequireDistinctLinks());
        Set<PersistedPatternNode> persistedPatternNodes = persistedPattern.getPatternNodes();
        for (PatternNode node : graphPattern.getPatternNodes()) {
            boolean newNode = true;
            for (PersistedPatternNode persistedPatternNode : persistedPatternNodes) {
                if (persistedPatternNode.getUuid().equals(node.getUuid())) {
                    newNode = false;
                    //update the node
                    PatternNodeConverter.toPersistedPatternNode(node);
                }
            }
            if (newNode) {
                persistedPatternNodes.add(PatternNodeConverter.toPersistedPatternNode(node));
            }
        }
        //remove deleted nodes
        {
            Set<PersistedPatternNode> nodesToDelete = Sets.newHashSet();
            for (PersistedPatternNode persistedPatternNode : persistedPatternNodes) {
                boolean deleteMe = true;
                for (PatternNode node : graphPattern.getPatternNodes()) {
                    if (persistedPatternNode.getUuid().equals(node.getUuid())) {
                        deleteMe = false;
                    }
                }
                if (deleteMe) {
                    nodesToDelete.add(persistedPatternNode);
                }
            }
            for (PersistedPatternNode persistedPatternNode : nodesToDelete) {
                persistedPatternNodes.remove(persistedPatternNode);
            }
        }
        //add new links and update existing ones
        Set<PersistedPatternLink> persistedPatternLinks = persistedPattern.getPatternLinks();
        for (PatternLink link : graphPattern.getPatternLinks()) {
            boolean newLink = true;
            for (PersistedPatternLink persistedPatternLink : persistedPatternLinks) {
                if (persistedPatternLink.getUuid().equals(link.getUuid())) {
                    newLink = false;
                    //update Link
                    PatternLinkConverter.toPersistedPatternLink(link, persistedPatternNodes);
                }
            }
            if (newLink) {
                persistedPatternLinks.add(PatternLinkConverter.toPersistedPatternLink(link, persistedPatternNodes));
            }
        }

        //remove deleted links
        {
            Set<PersistedPatternLink> linksToDelete = Sets.newHashSet();
            for (PersistedPatternLink persistedPatternLink : persistedPatternLinks) {
                boolean deleteMe = true;
                for (PatternLink link : graphPattern.getPatternLinks()) {
                    if (persistedPatternLink.getUuid().equals(link.getUuid())) {
                        if (persistedPatternNodes.contains(persistedPatternLink.getNode1())) {
                            if (persistedPatternNodes.contains(persistedPatternLink.getNode2())) {
                                deleteMe = false;
                            }
                        }
                    }
                }
                if (deleteMe) {
                    linksToDelete.add(persistedPatternLink);
                }
            }
            for (PersistedPatternLink persistedPatternLink : linksToDelete) {
                persistedPatternLinks.remove(persistedPatternLink);
            }
        }

        return persistedPattern;
    }

    public static GraphPattern toGraphPattern(PersistedPattern persistedPattern) {
        GraphPattern graphPattern = new GraphPattern();
        graphPattern.setUuid(persistedPattern.getUuid());
        graphPattern.setName(persistedPattern.getName());
        graphPattern.setRequireDistinctNodes(persistedPattern.isRequireDistinctNodes());
        graphPattern.setRequireDistinctLinks(persistedPattern.isRequireDistinctLinks());
        for (PersistedPatternNode node : persistedPattern.getPatternNodes()) {
            graphPattern.getPatternNodes().add(PatternNodeConverter.toPatternNode(node));
        }
        for (PersistedPatternLink link : persistedPattern.getPatternLinks()) {
            graphPattern.getPatternLinks().add(PatternLinkConverter.toPatternLink(link, graphPattern.getPatternNodes()));
        }
        return graphPattern;
    }

    public static void updatePersistedPattern(PersistedPattern persistedPattern, GraphPattern graphPattern) {
        persistedPattern.setName(graphPattern.getName());
        Set<PersistedPatternNode> persistedPatternNodes = persistedPattern.getPatternNodes();
        persistedPatternNodes.clear();
        for (PatternNode node : graphPattern.getPatternNodes()) {
            persistedPatternNodes.add(PatternNodeConverter.toPersistedPatternNode(node));
        }
        Set<PersistedPatternLink> persistedPatternLinks = persistedPattern.getPatternLinks();
        persistedPatternLinks.clear();
        for (PatternLink link : graphPattern.getPatternLinks()) {
            persistedPatternLinks.add(PatternLinkConverter.toPersistedPatternLink(link, persistedPatternNodes));
        }
    }
}
