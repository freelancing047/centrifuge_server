package csi.security.queries;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import csi.security.CsiSecurityManager;
import csi.server.common.dto.user.UserPreferences;
import csi.server.common.dto.user.preferences.DialogPreference;
import csi.server.common.dto.user.preferences.GeneralPreference;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.dao.CsiPersistenceManager;

/**
 * Created by centrifuge on 5/24/2016.
 */
public class UserEnvironment {
   private String key;


   public UserEnvironment(String keyIn) {
      key = keyIn.toLowerCase();
   }

   public static UserEnvironment getUserEnvironment() {
      UserEnvironment result = null;

      try {
         result = new UserEnvironment(CsiSecurityManager.getUserName());
      } catch (Exception myException) {
      }
      return result;
   }

   public UserPreferences getUserPreferences() {
      UserPreferences preferences = new UserPreferences();

      preferences.setResourceFilterList(getResourceFilters());
      preferences.setDialogPreferenceList(getDialogPreferences());
      preferences.setGeneralPreferenceList(getGeneralPreferences());
      return preferences;
   }

   public List<ResourceFilter> getResourceFilters() {
      List<ResourceFilter> result = null;

      try {
         EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
         Query query = manager.createQuery("SELECT d FROM ResourceFilter d WHERE logonId = :user");

         query.setParameter("user", key);

         result = query.getResultList();
      } catch (Exception ignore) {
      }
      return result;
   }

   public List<DialogPreference> getDialogPreferences() {
      List<DialogPreference> result = null;

      try {
         EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
         Query query = manager.createQuery("SELECT d FROM DialogPreference d WHERE logonId = :user");

         query.setParameter("user", key);

         result = query.getResultList();
      } catch (Exception ignore) {
      }
      return result;
   }

   public List<GeneralPreference> getGeneralPreferences() {
      List<GeneralPreference> result = null;

      try {
         EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
         Query query = manager.createQuery("SELECT d FROM GeneralPreference d WHERE logonId = :user");

         query.setParameter("user", key);

         result = query.getResultList();
      } catch (Exception ignore) {
      }
      return result;
   }

   public ResourceFilter addReplaceResourceFilter(ResourceFilter filterIn) {
      Long id = (filterIn == null) ? null : filterIn.getId();
      EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
      ResourceFilter oldFilter = (id == null) ? null : manager.find(ResourceFilter.class, id);

      if (filterIn != null) {
         filterIn.setLogonId(key);

         if (oldFilter == null) {
            oldFilter = filterIn;

            manager.persist(oldFilter);
         } else {
            oldFilter.copy(filterIn);
            manager.merge(oldFilter);
         }
      }
      return oldFilter;
   }

   public void deleteResourceFilter(Long idIn) {
      if (idIn != null) {
         EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
         ResourceFilter oldPreference = manager.find(ResourceFilter.class, idIn);

         if (oldPreference != null) {
            manager.remove(oldPreference);
         }
      }
   }

   public DialogPreference addReplaceDialogPreference(DialogPreference preferenceIn) {
      Long id = (preferenceIn == null) ? null : preferenceIn.getId();
      EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
      DialogPreference oldPreference = (id == null) ? null : manager.find(DialogPreference.class, id);

      if (preferenceIn != null) {
         preferenceIn.setLogonId(key);

         if (oldPreference == null) {
            oldPreference = preferenceIn;

            manager.persist(oldPreference);
         } else {
            oldPreference.copy(preferenceIn);
            manager.merge(preferenceIn);
         }
      }
      return oldPreference;
   }

   public void deleteDialogPreference(Long idIn) {
      if (idIn != null) {
         EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
         DialogPreference oldPreference = manager.find(DialogPreference.class, idIn);

         if (oldPreference != null) {
            manager.remove(oldPreference);
         }
      }
   }

   public GeneralPreference addReplaceGeneralPreference(GeneralPreference preferenceIn) {
      Long id = (preferenceIn == null) ? null : preferenceIn.getId();
      EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
      GeneralPreference oldPreference = (id == null) ? null : manager.find(GeneralPreference.class, id);

      if (preferenceIn != null) {
         preferenceIn.setLogonId(key);

         if (oldPreference == null) {
            oldPreference = preferenceIn;

            manager.persist(oldPreference);
         } else {
            oldPreference.copy(preferenceIn);
            manager.merge(preferenceIn);
         }
      }
      return oldPreference;
   }

   public void deleteGeneralPreference(Long idIn) {
      if (idIn != null) {
         EntityManager manager = CsiPersistenceManager.getMetaEntityManager();
         GeneralPreference oldPreference = manager.find(GeneralPreference.class, idIn);

         if (oldPreference != null) {
            manager.remove(oldPreference);
         }
      }
   }
}
