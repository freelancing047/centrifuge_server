package csi.server.business.service.icon;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.CacheMode;

import csi.security.queries.AclRequest;
import csi.server.business.helper.QueryHelper;
import csi.server.business.service.annotation.Operation;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.AuthorizationException;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.Resource;
import csi.server.common.model.icons.Icon;
import csi.server.common.model.icons.IconResultDto;
import csi.server.common.service.api.IconActionsServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.shared.core.icon.IconInfoDto;

/**
 * Class is used for managing Icons in Centrifuge. These are not the static icons
 * that are used throughout centrifuge, but are within themes.
 *
 * @author jdanberg
 */
public class IconActionsService implements IconActionsServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(IconActionsService.class);

    public static final double BASE64_SIZE_RATIO = .75;
    public static final double MAX_IMAGE_SIZE = 1024 * 128;
    private static final int MAX = 100;

    /**
     * @param
     * @return
     */
   public static BufferedImage convertBase64ToImage(String base64) {
      BufferedImage result = null;

      try {
         byte[] imageData = Base64.getDecoder().decode(base64);

         try (InputStream in = new ByteArrayInputStream(imageData)) {
            result = ImageIO.read(in);
         }
      } catch (Exception exception) {
         LOG.error("Could not convert base64 to image", exception);
      }
      return result;
   }

    public static BufferedImage getImageForServer(String uuid) throws CentrifugeException {
        Icon icon;

        String dataUrl = null;

        //        if (!CsiSecurityManager.isAuthorized(uuid, AclControlType.READ)) {
        //
        //            throw new CentrifugeException("Access denied.  Not authorized to view image.");
        //        }
        try {
            icon = CsiPersistenceManager.findObject(Icon.class, uuid);
            if (icon == null) {
                return null;
            }
            dataUrl = icon.getImage();
        } catch (Exception e) {

           LOG.error("No icon found with uuid:" + uuid, e);
        }

        if ((dataUrl == null) || (dataUrl.length() == 0)) {
            return null;
        }

        String encodingPrefix = "base64,";

        int contentStartIndex = dataUrl.indexOf(encodingPrefix) + encodingPrefix.length();
        dataUrl = dataUrl.substring(contentStartIndex);

        return convertBase64ToImage(dataUrl);
    }

    /**
     * @param base64Image
     * @return string representation of file extension
     */
    public static String determineExtension(String base64Image) {
       String result = null;
       byte[] imageByteArray = Base64.getDecoder().decode(base64Image);

       try (InputStream is = new ByteArrayInputStream(imageByteArray)) {
          String mimeType = URLConnection.guessContentTypeFromStream(is);
          String delimiter = "[/]";
          String[] tokens = mimeType.split(delimiter);
          result = tokens[1];
       } catch (IOException ioException) {
          LOG.error("Couldn't determine image extension", ioException);
       }
       return result;
    }

   private static List<Icon> retrieveIcons(List<Object> normalizedIds, String filter) {
      EntityManager em = CsiPersistenceManager.getMetaEntityManager();
      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<Icon> q = cb.createQuery(Icon.class);
      Root<Icon> root = q.from(Icon.class);
      Path<Set<String>> tags = root.get("tags");
      ParameterExpression<List> p = cb.parameter(List.class);

      if (filter != null) {
         q.select(root).where(root.get(CsiPersistenceManager.PRIMARY_KEY_NAME).in(p),
                                       cb.isMember(filter, tags));
      } else {
         q.select(root).where(root.get(CsiPersistenceManager.PRIMARY_KEY_NAME).in(p));
      }
      TypedQuery<Icon> query = em.createQuery(q);

      query.setHint("org.hibernate.cacheable", Boolean.FALSE);
      query.setHint("org.hibernate.cacheMode", CacheMode.GET);
      query.setParameter(p, normalizedIds);
      return query.getResultList();
   }

   private static List<Icon> retrieveIcons(String filter, Integer start, Integer end) {
      EntityManager em = CsiPersistenceManager.getMetaEntityManager();
      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<Icon> q = cb.createQuery(Icon.class);
      Root<Icon> root = q.from(Icon.class);
      Path<Set<String>> tags = root.get("tags");
//      ParameterExpression<List> p = cb.parameter(List.class);
      Subquery<Icon> subQ = q.subquery(Icon.class);
      Root<Icon> subRoot = subQ.from(Icon.class);
//      Predicate predicate = cb.equal(subRoot.get(CsiPersistenceManager.PRIMARY_KEY_NAME), root);

      subQ.select(subRoot).where(cb.isMember(filter, tags));
      //q.select(root).where(root.get(CsiPersistenceManager.PRIMARY_KEY_NAME).in(subQ));
      q.select(root).where(cb.exists(subQ));

      TypedQuery<Icon> query = em.createQuery(q);

      query.setMaxResults(end.intValue() - start.intValue());
      query.setFirstResult(start.intValue());
      query.setHint("org.hibernate.cacheable", Boolean.FALSE);
      query.setHint("org.hibernate.cacheMode", CacheMode.GET);
      return query.getResultList();
   }

    public static List<Icon> findObjects(List<String> ids, String filterValue) {
        List<Icon> objectsFound = null;

        List<Object> normalizedIds = new ArrayList<Object>();
        for (Object id : ids) {
            normalizedIds.add(CsiPersistenceManager.getNormalizedId(Icon.class, id));
        }

        objectsFound = retrieveIcons(normalizedIds, filterValue);
        if (objectsFound == null) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Can't find objects of type " + Icon.class.getCanonicalName());
            }
            objectsFound = new ArrayList<Icon>();
        } else if (!objectsFound.isEmpty() && (objectsFound.get(0) instanceof Resource)) {
            //                for(Object objectFound: objectsFound){
            //                    ((Resource)objectFound).resetTransients();
            //                }
        }

        return objectsFound;
    }

    public static List<Icon> findObjects(String filter, int start, int end) {
        List<Icon> objectsFound = null;


        objectsFound = retrieveIcons(filter, start, end);
        if (objectsFound == null) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("Can't find objects of type " + Icon.class.getCanonicalName());
            }
            objectsFound = new ArrayList<Icon>();
        } else if (!objectsFound.isEmpty() && (objectsFound.get(0) instanceof Resource)) {
            //                for(Object objectFound: objectsFound){
            //                    ((Resource)objectFound).resetTransients();
            //                }
        }

        return objectsFound;
    }

    @Override
    public String getBase64Image(String uuid) throws CentrifugeException {
        String dataUrl = getDataUrlImage(uuid);

        if ((dataUrl == null) || (dataUrl.length() == 0)) {
            return "";
        }
        String encodingPrefix = "base64,";
        int contentStartIndex = dataUrl.indexOf(encodingPrefix) + encodingPrefix.length();
        return (0 <= contentStartIndex) ? dataUrl.substring(contentStartIndex) : "";
    }

    @Override
    public String getDataUrlImage(String uuid) throws CentrifugeException {

        Icon icon = null;

        try {

            if (null != uuid) {

                icon = CsiPersistenceManager.findObject(Icon.class, uuid);
            }

        } catch (Exception e) {

           LOG.error("Failed to retrieve icon with uuid:" + uuid, e);
        }

        return (null != icon) ? icon.getImage() : null;
    }

    @Override
    public Icon getIcon(String uuid) throws CentrifugeException {
        Icon icon;
        //        if (!CsiSecurityManager.isAuthorized(uuid, new AclControlType[]{AclControlType.READ})) {
        //
        //            throw new CentrifugeException("Access denied.  Not authorized to view image.");
        //        }
        try {
            icon = CsiPersistenceManager.findObject(Icon.class, uuid);
        } catch (Exception e) {

           LOG.error("No icon found with uuid:" + uuid, e);

            return null;
        }

        return icon;
    }

    /* (non-Javadoc)
     * @see csi.server.common.service.api.IconActionsServiceProtocol#uploadIcon(java.lang.String)
     */
    @Override
    public String uploadIcon(String base64, String fileName) throws CentrifugeException {
        if (!CsiPersistenceManager.hasIconManagementAccess()) {
            throw new AuthorizationException("Access denied. Not authorized to upload icons.");
        }

        if ((base64 == null) || (base64.length() == 0)) {
            throw new CentrifugeException("No image data found for " + fileName + ", canceling upload");
        }

        Icon icon = new Icon(fileName, base64, null);

        if ((base64.length() * BASE64_SIZE_RATIO) > MAX_IMAGE_SIZE) {
            throw new CentrifugeException("File is too large to upload, must be less than " + MAX_IMAGE_SIZE);
        }

        CsiPersistenceManager.persist(icon);

        return icon.getUuid();
    }

   @Override
   public String countIcons(String tag) throws CentrifugeException {
      return (tag == null)
                ? Long.toString(AclRequest.countResource(AclResourceType.ICON,
                                                         new AclControlType[]{AclControlType.READ}).longValue())
                : Integer.toString(countIconsWithFilter(tag));
   }

    @Override
   public IconInfoDto getIconInfo(ResourceFilter filter) throws CentrifugeException {
        IconInfoDto info = new IconInfoDto();
        LOG.trace("GetIconInfo with Filter = " + filter.getMatchPattern());

        filter.clearAclList();

        List<ResourceBasics> resources = AclRequest.filterResources(AclResourceType.ICON, filter, new AclControlType[]{AclControlType.READ});
        info.setCount(Integer.valueOf(resources.size()));
        info.setEditAccess(CsiPersistenceManager.hasIconManagementAccess());
        LOG.trace("Returning " + resources.size() + " icon infos " + filter.getMatchPattern());
        return info;
    }

    @Override
   public IconInfoDto getIconInfo(ResourceFilter filter, String tagText) throws CentrifugeException {
       LOG.info("GetIconInfo with Filter = " + filter.getMatchPattern() + " and tag " + tagText);
        IconInfoDto info = new IconInfoDto();
        info.setEditAccess(CsiPersistenceManager.hasIconManagementAccess());
        if (tagText != null) {
            List<ResourceBasics> resources = AclRequest.filterResources(AclResourceType.ICON, filter, new AclControlType[]{AclControlType.READ});
            try {
                if (!resources.isEmpty()) {
                    info.setCount(Integer.valueOf(countIconsWithTag(resources, tagText)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return getIconInfo(filter);
        }

        return info;
    }

    @Override
    public IconInfoDto getIconInfo(String tag) throws CentrifugeException {
        IconInfoDto info = new IconInfoDto();
        String result = countIcons(tag);

        try {
            info.setCount(Integer.decode(result));

        } catch (Exception e) {
            //no icons
        }

        info.setEditAccess(CsiPersistenceManager.hasIconManagementAccess());

        return info;
    }

    private int countIconsWithFilter(String tag) throws CentrifugeException {
        List<ResourceBasics> authorizedIcons = listAllAuthorizedUserIcons(AclControlType.READ);
        return countIconsWithTag(authorizedIcons, tag);
    }

   private int countIconsWithTag(List<ResourceBasics> resources, String tag) {
      int result = 0;
      String query = buildDistinctQueryRestriction(resources, tag);

      try (Connection connection = CsiPersistenceManager.getMetaConnection()) {
         LOG.debug(query);

         try (ResultSet rs = QueryHelper.executeSingleQuery(connection, query, null)) {
            if (rs.next()) {
               result = rs.getInt(1);
            }
         }
      } catch (Exception exception) {
         LOG.error(exception);
      }
      return result;
   }

    /**
     * @param start
     * @param end
     * @param tagText    - tag
     * @param totalCount
     * @param lastDbRow
     * @return
     * @throws CentrifugeException
     */
    @Override
    public IconResultDto listIcons(Integer start, Integer end, String tagText, Integer totalCount, Integer lastDbRow, ResourceFilter resourceFilter) throws CentrifugeException {
        int total = end - start;
        if (total <= 0) {
            return null;
        }
        if (lastDbRow != null) {
            start = lastDbRow;
            end = lastDbRow + total;
        }
        //Min query
        if ((total < 25) && (tagText != null)) {
            int count = countIconsWithFilter(tagText);
            if (count == 0) {
               return null;
            }

            //Prevents bad state of making filtered queries of 1.
            end += (count / 10);
        }

        // if we have a tag filter coming in - get icons matching tag.
        if ((tagText != null) && !tagText.isEmpty() && (resourceFilter == null)) {
            return getIconsByTag(start, end, tagText);
        } else if (resourceFilter != null) {
            resourceFilter.clearAclList();
            return getIconsByTagAndFilter(start, end, tagText, resourceFilter, total);
        } else {
            return getAllIcons(start, end, tagText, total);
        }
    }

    private IconResultDto getAllIcons(Integer start, Integer end, String tagText, int total) throws CentrifugeException {
        LinkedHashSet<Icon> results = new LinkedHashSet<Icon>();
        //Putting a loop here in case I add constraints to the second query
        while ((results.size() < total) && (results.size() < MAX)) {
            List<ResourceBasics> authorizedIconResources = listAuthorizedUserIcons(AclControlType.READ, start, end);
            List<String> uuids = new ArrayList<String>();
            for (ResourceBasics resource : authorizedIconResources) {
                uuids.add(resource.getUuid());
            }
            if (uuids.isEmpty() && (end >= total)) {
                break;
            } else {

                List<Icon> foundObjects = findObjects(uuids, tagText);

                if ((results.size() + foundObjects.size()) > MAX) {

                    foundObjects = foundObjects.subList(0, foundObjects.size() - 1 - ((results.size() + foundObjects.size()) - MAX));
                }
                if (tagText != null) {
                    results.addAll(foundObjects);
                } else {
                    results.addAll(foundObjects);

                    if (results.isEmpty()) {
                        return null;
                    }
                }
            }

            start = end;
            end = end + total;

        }

        List<Icon> finalResult = new ArrayList<Icon>();
        if (results.size() > MAX) {
           LOG.error("Went beyond max limit query for icons");

        } else {
            finalResult.addAll(results);
        }

        //logger.error(results.size() + " : start-"  + start + " : end-" + end);
        IconResultDto result = new IconResultDto();
        result.setResults(finalResult);
        result.setLastRow(end);
        return result;
    }

    private IconResultDto getIconsByTagAndFilter(Integer start, Integer end, String tagText, ResourceFilter resourceFilter, int total) {
        List<Icon> results = filterIcons(tagText, resourceFilter, "");
        LOG.info("Results size: " + results.size() + " S:" + start + " end: " + end);

        if (results.size() > total) {
            results = results.subList(start, end);
        }
        List<Icon> finalResult = new ArrayList<Icon>();
        finalResult.addAll(results);
        IconResultDto dto = new IconResultDto();
        dto.setResults(finalResult);
        dto.setLastRow(end);

        return dto;
    }

    private IconResultDto getIconsByTag(Integer start, Integer end, String tagText) {
        LinkedHashSet<Icon> results = new LinkedHashSet<Icon>();

        List<Icon> foundObjects = findObjects(tagText, start, end);

        if ((results.size() + foundObjects.size()) > MAX) {
            foundObjects = foundObjects.subList(0, foundObjects.size() - 1 - ((results.size() + foundObjects.size()) - MAX));
        }
        results.addAll(foundObjects);

        List<Icon> finalResult = new ArrayList<Icon>();
        if (results.size() > MAX) {
           LOG.error("Went beyond max query");

        } else {
            finalResult.addAll(results);
        }

        IconResultDto result = new IconResultDto();
        result.setResults(finalResult);
        result.setLastRow(null);
        return result;
    }

    @Operation
    private List<ResourceBasics> listAuthorizedUserIcons(AclControlType permissionsIn, Integer start, Integer end) throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listAuthorizedUserIcons(new AclControlType[]{permissionsIn}, start, end);

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

    @Operation
    private List<ResourceBasics> listAllAuthorizedUserIcons(AclControlType permissionsIn) throws CentrifugeException {
        List<ResourceBasics> myResults = AclRequest.listAuthorizedUserIcons(new AclControlType[]{permissionsIn});

        return (null != myResults) ? myResults : new ArrayList<ResourceBasics>();
    }

   @Override
   public List<String> listAvailableTags() {
      List<String> result = null;
      //TODO: I don't  like this, but you can't use hibernate to query embedded children
      try (Connection connection = CsiPersistenceManager.getMetaConnection()) {
         String query = buildDistinctQuery();

         LOG.debug(query);

         try (ResultSet rs = QueryHelper.executeSingleQuery(connection, query, null)) {
            result = new ArrayList<String>();

            while (rs.next()) {
               result.add(rs.getString(1));
            }
         }
      } catch (Exception exception) {
         LOG.error(exception);
      }
      return result;
   }

    @Override
    public void addTagsToIcons(List<String> ids, List<String> tags) throws CentrifugeException {
        for(String s : ids){
            addTag(s, tags);
        }
    }

    private String buildDistinctQuery() {

        String selection = "SELECT DISTINCT tag from icon_tags;";

        return selection;
    }

   public String buildDistinctQueryRestriction(List<ResourceBasics> resources, String tag) {
      StringBuilder selection = new StringBuilder("SELECT COUNT(DISTINCT icon_uuid) from icon_tags WHERE icon_uuid IN (");
      boolean isNotFirst = false;

      for (ResourceBasics resource : resources) {
         if (isNotFirst) {
            selection.append(",");
         } else {
            isNotFirst = true;
         }
         selection.append("'").append(resource.getUuid()).append("'");
      }
      return selection.append(")").append("AND tag = ").append("'").append(tag).append("';").toString();
   }

    @Override
    public void addTag(String iconUuid, List<String> items) throws CentrifugeException {
        if (!CsiPersistenceManager.hasIconManagementAccess()) {
            throw new AuthorizationException("Access denied. Not authorized to edit icons.");
        }
        Icon icon = CsiPersistenceManager.findObject(Icon.class, iconUuid);
        icon.getTags().addAll(items);
        CsiPersistenceManager.merge(icon);
    }

    @Override
    public boolean hasIconManagementAccess() {
        return hasIconManagementAccess();
    }

    @Override
    public void removeTag(String iconUuid, String item) throws CentrifugeException {
        if (!CsiPersistenceManager.hasIconManagementAccess()) {
            throw new AuthorizationException("Access denied. Not authorized to edit icons.");
        }
        Icon icon = CsiPersistenceManager.findObject(Icon.class, iconUuid);
        icon.getTags().remove(item);
        CsiPersistenceManager.merge(icon);
    }

    @Override
    public String editIconData(String base64, String uuid) throws CentrifugeException {
        if (!CsiPersistenceManager.hasIconManagementAccess()) {
            throw new AuthorizationException("Access denied. Not authorized to edit icons.");
        }

        if ((base64 == null) || (base64.length() == 0)) {
            throw new CentrifugeException("No image data found for " + uuid + ", canceling upload");
        }
        if ((base64.length() * BASE64_SIZE_RATIO) > MAX_IMAGE_SIZE) {
            throw new CentrifugeException("File is too large to upload, must be less than " + MAX_IMAGE_SIZE);
        }
        Icon icon = CsiPersistenceManager.findObject(Icon.class, uuid);
        icon.setImage(base64);

        CsiPersistenceManager.merge(icon);

        return base64;
    }

    @Override
   public void deleteIcon(String uuid) throws CentrifugeException {
        if (CsiPersistenceManager.hasIconManagementAccess()) {
         CsiPersistenceManager.deleteObject(Icon.class, uuid);
      } else {
         throw new AuthorizationException("Access denied.  Not authorized to delete icons.");
      }
    }

    @Override
    public void deleteIcons(List<String> iconUuids) throws CentrifugeException {
        for(String s : iconUuids){
            deleteIcon(s);
            LOG.info("Deleting icon with UUID " + s);
        }
    }

    public void close() {
        CsiPersistenceManager.close();
    }

    @Override
    public void editName(String text, String iconUuid) {

        if (CsiPersistenceManager.hasIconManagementAccess()) {
            Icon icon = CsiPersistenceManager.findObject(Icon.class, iconUuid);
            icon.setName(text);
            CsiPersistenceManager.merge(icon);
        } else {
            throw new AuthorizationException("Access denied.  Not authorized to edit icons.");
        }
    }

    /**
     * @param tagText
     * @param filter
     * @return
     */
    @Override
    public List<Icon> filterIcons(String tagText, ResourceFilter filter, String overrideString) {
        try {
            List<ResourceBasics> resources;
            List<Icon> icons = new ArrayList<>();

            if (filter != null) {
                resources = AclRequest.filterResources(AclResourceType.ICON, filter, new AclControlType[]{AclControlType.READ});
            } else {
                resources = AclRequest.filterResourcesBasedOnString(AclResourceType.ICON, overrideString, new AclControlType[]{AclControlType.READ});
            }

            for (ResourceBasics resource : resources) {
                String uuid = resource.getUuid();
                Icon icon = getIcon(uuid);
                if (icon != null) {
                    if(tagText != null) {
                        if(icon.getTags().contains(tagText)){
                            icons.add(icon);
                        }
                    }else {
                        icons.add(icon);
                    }
                }
            }

            return icons;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                e.printStackTrace();
            }
            LOG.error("Error filtering icons " + e.getMessage());
        }

        return null;
    }
}
