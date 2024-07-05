package csi.server.common.model.visualization.pattern;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import csi.server.dao.CsiPersistenceManager;
import csi.shared.gwt.viz.graph.tab.pattern.settings.GraphPattern;

public class GraphPatternPersister {
   public static void persist(String owner, List<GraphPattern> graphPatterns) {
      for (GraphPattern graphPattern : graphPatterns) {
         persist(owner, graphPattern);
      }
   }

   public static void persist(String owner, GraphPattern graphPattern) {
//        PersistedPattern persistedPattern = PatternConverter.toPersistentPattern(owner, graphPattern);
//        EntityManager em = CsiPersistenceManager.getMetaEntityManager();
   }

   public static List<GraphPattern> retrieve(String owner) {
      List<GraphPattern> graphPatterns = new ArrayList<GraphPattern>();
      List<PersistedPattern> persistedPatterns = retrievePersistedPatterns(owner);

      for (PersistedPattern persistedPattern : persistedPatterns) {
         GraphPattern graphPattern = PatternConverter.toGraphPattern(persistedPattern);
         graphPatterns.add(graphPattern);
      }
      return graphPatterns;
   }

   private static List<PersistedPattern> retrievePersistedPatterns(String owner) {
      EntityManager em = CsiPersistenceManager.getMetaEntityManager();
      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<PersistedPattern> q = cb.createQuery(PersistedPattern.class);
      Root<PersistedPattern> c = q.from(PersistedPattern.class);
      ParameterExpression<String> p = cb.parameter(String.class);

      q.select(c).where(cb.equal(c.get("owner"), p));

      TypedQuery<PersistedPattern> query = em.createQuery(q);

      query.setParameter(p, owner);
      return query.getResultList();
   }

   public static void remove(GraphPattern graphPattern) {
      EntityManager em = CsiPersistenceManager.getMetaEntityManager();
      PersistedPattern persistedPattern = em.find(PersistedPattern.class, graphPattern.getUuid());

      if (persistedPattern != null) {
         em.remove(persistedPattern);
      }
   }

   public static void update(GraphPattern graphPattern) {
      EntityManager em = CsiPersistenceManager.getMetaEntityManager();
      PersistedPattern persistedPattern = em.find(PersistedPattern.class, graphPattern.getUuid());

      PatternConverter.updatePersistedPattern(persistedPattern, graphPattern);
   }
}
