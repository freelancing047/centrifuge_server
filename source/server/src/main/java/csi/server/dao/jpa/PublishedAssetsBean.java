package csi.server.dao.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import csi.server.common.publishing.Asset;
import csi.server.common.publishing.Tag;
import csi.server.dao.PublishedAssetDAO;

public class PublishedAssetsBean extends AbstractJPADAO<Asset,Long> implements PublishedAssetDAO {
   private static final String ALL_TAGS_QUERY = "select distinct LOWER(t.value) from Tag t";
   private static final String ASSETID_QUERY = "from Asset a where a.assetID = :id";
   private static final String NAME_QUERY = "from Asset a where a.name = :name";
   private static final String USER_QUERY = "from Asset a where a.createdBy = :name";
   private static final String TAG_UNREFERENCED_QUERY = "SELECT tg.id FROM tags tg INNER JOIN asset_tags atg ON (tg.id = atg.tags_id) WHERE tg.id = :id";

   public PublishedAssetsBean() {
   }

   public PublishedAssetsBean(EntityManager entityManager) {
      super(entityManager);
   }

   @SuppressWarnings("unchecked")
   public Asset findByAssetID(String id) {
      EntityManager entityManager = getEntityManager();
      Query query = entityManager.createQuery(ASSETID_QUERY);
      query.setParameter("id", id);

      List<Asset> resultList = query.getResultList();

      return resultList.isEmpty() ? null : resultList.get(0);
   }

   @SuppressWarnings("unchecked")
   public List<Asset> findByName(String name) {
      EntityManager entityManager = getEntityManager();
      Query query = entityManager.createQuery(NAME_QUERY);
      query.setParameter("name", name);
      return query.getResultList();
   }

   @SuppressWarnings("unchecked")
   public List<String> allTagValues() {
      EntityManager entityManager = getEntityManager();
      Query query = entityManager.createQuery(ALL_TAGS_QUERY);
      return query.getResultList();
   }

   @SuppressWarnings("unchecked")
   public boolean isTagUnreferenced(Long id) {
      EntityManager entityManager = getEntityManager();
      Query query = entityManager.createNativeQuery(TAG_UNREFERENCED_QUERY);
      query.setParameter("id", id);

      List<Long> valueList = query.getResultList();
      return ((valueList == null) || valueList.isEmpty());
   }

   public List<Asset> findByTagSet(SortedSet<String> tags) {
      List<Asset> all = findAll();

      ArrayList<Asset> withTags = new ArrayList<Asset>();

      for (Asset a : all) {
         if (a.containsTags(tags)) {
            withTags.add(a);
         }
      }

      return withTags;
   }

   @SuppressWarnings("unchecked")
   public List<Asset> findByUser(String user) {
      EntityManager entityManager = getEntityManager();
      Query query = entityManager.createQuery(USER_QUERY);
      query.setParameter("name", user);
      return query.getResultList();
   }

   public void removeTag(Tag tag) {
      EntityManager entityManager = getEntityManager();
      entityManager.remove(tag);
   }
}
