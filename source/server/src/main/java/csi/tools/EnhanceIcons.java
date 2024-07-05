package csi.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gwt.thirdparty.guava.common.collect.Lists;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.integration.spring.SpringLiquibase.SpringResourceOpener;
import liquibase.resource.ResourceAccessor;

import csi.security.jaas.JAASRole;
import csi.security.monitors.ResourceACLMonitor;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.icons.Icon;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.SqlUtil;

public class EnhanceIcons implements CustomTaskChange {
   private static final Logger LOG = LogManager.getLogger(EnhanceIcons.class);

   private static final String GRAPH_BASLINE = "Graph-Baseline";
   private static final String GRAPH_CIRCULAR = "Graph-Circular";
   private static final String MAP_CIRCULAR = "Map-Circular";
   private static final Integer GRAPH_MODE = Integer.valueOf(15);
   private static final Integer MAP_MODE = Integer.valueOf(16);

    private class IconHashInfo{
        String uuid;
        String name;
        List<String> tags;
        String hash;
        String base64;
    }

    private class ThemeInfo {

        String uuid;
        String name;
        Integer type;
    }

    private int iconNamingDigits = 0;
    private ResourceAccessor resourceAccessor;
    private ValidationErrors validationErrors = null;
    private String tagPath;
    private Map<String, IconHashInfo> fileIcons = new TreeMap<String,IconHashInfo>();
    private Map<String, String> existingIcons = new TreeMap<String, String>();
    private Map<String, String> processedIcons = new TreeMap<String, String>();
    private Map<String, String> oldToNewIdMap = new TreeMap<String, String>();
    private Map<String, ThemeInfo> themeMap = new TreeMap<String, ThemeInfo>();

    @Override
    public String getConfirmationMessage() {
        // TODO Auto-generated method stub
        return "Icon Tags Updated";
    }

    @Override
    public void setUp() throws SetupException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    @Override
    public ValidationErrors validate(Database database) {
        return validationErrors;
    }

    @Override
    public void execute(Database database) throws CustomChangeException {

        org.springframework.core.io.Resource resource = null;

        try {

            if(resourceAccessor instanceof SpringResourceOpener){

                SpringResourceOpener opener = (SpringResourceOpener)resourceAccessor;
                resource = opener.getResource(getTagPath());

                try (FileReader fileReader = new FileReader(resource.getFile());
                     BufferedReader bufferReader = new BufferedReader(fileReader)) {
                   for (String line = bufferReader.readLine(); line != null; line = bufferReader.readLine()) {
                      IconHashInfo info = createInfoFromLine(line);

                      fileIcons.put(info.hash, info);
                   }
                   LOG.info("Normalizing icon IDs and merging duplicate icons.");
                   normalizeIconIds();
                   LOG.info("Propagating icon changes.");
                   ResourceACLMonitor.deactivate();
                   propagateIconIds("plunkednode", "icon");
                   propagateIconIds("mapplace", "iconid");
                   propagateIconIds("placestyle", "iconid");
                   propagateIconIds("nodestyle", "iconid");
                   propagateIconIds("fielddef", "statictext");
                   propagateIconIds("acl", "uuid");
                   LOG.info("Icon processing complete.");
                   normalizeThemeIds();
                   propagateThemeIds("visualizationdef", "themeuuid");
                   propagateThemeIds("acl", "uuid");
                }
            }

        } catch (IOException e1) {
            LOG.error(e1);
            if(validationErrors == null) {
               validationErrors = new ValidationErrors();
            }

            validationErrors.addError(e1.getMessage());
        } catch (Exception e) {
            LOG.error(e);

            if(validationErrors == null) {
               validationErrors = new ValidationErrors();
            }
            validationErrors.addError(e.getMessage());
        } finally {
            ResourceACLMonitor.activate();
            CsiPersistenceManager.commit();
            CsiPersistenceManager.close();
            if((validationErrors != null) && validationErrors.hasErrors()){
                try {
                    database.rollback();
                } catch (DatabaseException e) {
                    LOG.error("Failed to rollback Update Icon Tags");

                }

                throw new CustomChangeException();
            } else {
            }
        }
    }

    private void normalizeIconIds() {
        try {
            try (Connection myConnection = CsiPersistenceManager.getMetaConnection();
                 Statement myStatement = myConnection.createStatement();
                 ResultSet myResults = myStatement.executeQuery("select ic.uuid, md5(lo_get(ic.image)) as md from icon ic")) {
                while (myResults.next()) {
                    String myUuid = myResults.getString(1);
                    String myMd5 = myResults.getString(2);

                    existingIcons.put(myMd5, myUuid);
                    oldToNewIdMap.put(myUuid, myMd5);
                }
            }
            ResourceACLMonitor.deactivate();
            for (Map.Entry<String, String> myEntry : oldToNewIdMap.entrySet()) {

                String myUuid = myEntry.getKey();
                String myHash = myEntry.getValue();

                if (processedIcons.containsKey(myHash)) {

                    mergeIcons(CsiUUID.getMd5IconId(myHash), myUuid);

                } else {

                    IconHashInfo myInfo = fileIcons.get(myHash);

                    if (null != myInfo) {

                        updateIcon(myUuid, myInfo);

                    } else if (!CsiUUID.getMd5IconId(myHash).equals(myUuid)) {

                        updateIconId(myUuid, CsiUUID.getMd5IconId(myHash));
                    }
                }
                processedIcons.put(myHash, myUuid);
            }
            ResourceACLMonitor.activate();
            for(Map.Entry<String, IconHashInfo> myEntry: fileIcons.entrySet()) {

                if (!existingIcons.containsKey(myEntry.getKey())) {

                    //Icon doesn't exit, we create it
                    createIcon(myEntry.getValue());
                }
            }

        } catch (Exception myException) {

            LOG.error("Caught exception:", myException);

        } finally {

            ResourceACLMonitor.activate();
        }
    }

   private void propagateIconIds(String tableIn, String columnIn) {
      String sql = "SELECT DISTINCT " + Format.value(columnIn) + " FROM " + Format.value(tableIn)
                   + " WHERE " + Format.value(columnIn) + " IS NOT NULL";

      try (Connection connection = CsiPersistenceManager.getMetaConnection()) {
         try (Statement statement = connection.createStatement();
              ResultSet results = statement.executeQuery(sql)) {
            List<String> oldUuidList = new ArrayList<String>();

            if (results != null) {
               while (results.next()) {
                  String oldUuid = results.getString(1);

                  if (oldUuid != null) {
                     oldUuidList.add(oldUuid);
                  }
               }
            }
            if (!oldUuidList.isEmpty()) {
               for (String oldUuid : oldUuidList) {
                  String newUuid = oldToNewIdMap.get(oldUuid);

                  if (newUuid != null) {
                     sql = "UPDATE " + Format.value(tableIn) + " SET " + Format.value(columnIn) + " = '"
                           + CsiUUID.getMd5IconId(newUuid) + "' WHERE " + Format.value(columnIn) + " = '" + oldUuid + "'";

                     statement.executeUpdate(sql);
                     connection.commit();
                  }
               }
            }
         } catch (Exception exception) {
            SqlUtil.quietRollback(connection);
            LOG.error("Caught exception processing table \"" + Format.value(tableIn) + "\", column \"" + Format.value(columnIn) + "\"", exception);
         }
      } catch (Exception exception) {
         LOG.error("Caught exception processing table \"" + Format.value(tableIn) + "\", column \"" + Format.value(columnIn) + "\"", exception);
      }
   }

    private void normalizeThemeIds() {

        try {
           String sql = "SELECT d.uuid, d.name, d.resourcetype, d.owner FROM modelresource d"
                 + " WHERE d.resourcetype = " + GRAPH_MODE + " OR d.resourcetype = " + MAP_MODE;

            try (Connection connection = CsiPersistenceManager.getMetaConnection();
                 Statement statement = connection.createStatement();
                 ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    ThemeInfo myInfo = new ThemeInfo();
                    String myKey = results.getString(1);
                    String myOwner = results.getString(4);

                    myInfo.name = results.getString(2);
                    myInfo.type = Integer.valueOf(results.getInt(3));
                    myInfo.uuid = JAASRole.ADMIN_ROLE_NAME.equals(myOwner)
                                     ? ((GRAPH_BASLINE.equalsIgnoreCase(myInfo.name))
                                          ? GRAPH_BASLINE.toLowerCase()
                                          : (GRAPH_CIRCULAR.equalsIgnoreCase(myInfo.name))
                                               ? GRAPH_CIRCULAR.toLowerCase()
                                               : (MAP_CIRCULAR.equalsIgnoreCase(myInfo.name))
                                                    ? MAP_CIRCULAR.toLowerCase()
                                                    : CsiUUID.formatThemeId(myInfo.uuid))
                                     : CsiUUID.formatThemeId(myInfo.uuid);

                    themeMap.put(myKey, myInfo);
                }
            }
            for (Map.Entry<String, ThemeInfo> myEntry : themeMap.entrySet()) {

                ThemeInfo myInfo = myEntry.getValue();
                Integer myType = myInfo.type;
                String myNewId = myInfo.uuid;
                String myOldId = myEntry.getKey();

                if (GRAPH_MODE.equals(myType)) {

                    updateGraphThemeId(myOldId, myNewId);

                } else if (MAP_MODE.equals(myType)) {

                    updateMapThemeId(myOldId, myNewId);
                }
            }

        } catch (Exception myException) {

            LOG.error("Caught exception:", myException);
        }
    }

   private void propagateThemeIds(String tableIn, String columnIn) {
      try (Connection connection = CsiPersistenceManager.getMetaConnection()) {
         try (Statement statement = connection.createStatement()) {
            for (Map.Entry<String, ThemeInfo> entry : themeMap.entrySet()) {
               ThemeInfo info = entry.getValue();
               Integer type = info.type;
               String newId = info.uuid;
               String oldId = entry.getKey();

               if ((oldId != null) && (newId != null) && !oldId.equals(newId) &&
                   (GRAPH_MODE.equals(type) || MAP_MODE.equals(type))) {
                  String mySql = "UPDATE " + Format.value(tableIn) + " SET " + Format.value(columnIn) + " = '"
                                 + newId + "' WHERE " + Format.value(columnIn) + " = '" + oldId + "'";

                  statement.executeUpdate(mySql);
                  connection.commit();
               }
            }
         } catch (Exception exception) {
            SqlUtil.quietRollback(connection);
            LOG.error("Caught exception processing table " + Format.value(tableIn) + ", column " + Format.value(columnIn) + "", exception);
        }
      } catch (Exception exception) {
         LOG.error("Caught exception processing table \"" + Format.value(tableIn) + "\", column \"" + Format.value(columnIn) + "\"", exception);
      }
    }

    private Icon createIcon(IconHashInfo iconHashInfoIn) {

        Icon myNewIcon = null;

        CsiPersistenceManager.begin();
        try {

            myNewIcon = new Icon();
            myNewIcon.setUuid(iconHashInfoIn.uuid);
            myNewIcon.setName(determineName(null, myNewIcon.getName()));
            addTags(myNewIcon, iconHashInfoIn.tags);
            myNewIcon.setImage(iconHashInfoIn.base64);
            CsiPersistenceManager.persist(myNewIcon);
            CsiPersistenceManager.commit();

        } catch (Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("EnhanceIcons.createIcon", myException);
        }
        return myNewIcon;
    }

    private Icon mergeIcons(String targetIdIn, String sourceIdIn) {

        Icon myTargetIcon = null;

        CsiPersistenceManager.begin();
        try {

            Icon mySourceIcon = CsiPersistenceManager.findObject(Icon.class, sourceIdIn);

            myTargetIcon = CsiPersistenceManager.findObject(Icon.class, targetIdIn);
            myTargetIcon.setName(determineName(mySourceIcon.getName(), myTargetIcon.getName()));
            addTags(myTargetIcon, mySourceIcon.getTags());

            CsiPersistenceManager.merge(myTargetIcon);
            CsiPersistenceManager.deleteForSystem(mySourceIcon);
            CsiPersistenceManager.commit();

        } catch (Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("EnhanceIcons.mergeIcons", myException);
        }
        return myTargetIcon;
    }

    private Icon updateIcon(String uuidIn, IconHashInfo iconHashInfoIn) {

        Icon myNewIcon = null;

        CsiPersistenceManager.begin();
        try {

            Icon myOldIcon = CsiPersistenceManager.findObject(Icon.class, uuidIn);
            String myHash = iconHashInfoIn.hash;

            myNewIcon = myOldIcon.clone();
            myNewIcon.setUuid(CsiUUID.getMd5IconId(myHash));
            myNewIcon.setName(determineName(myOldIcon.getName(), iconHashInfoIn.name));
            addTags(myNewIcon, iconHashInfoIn.tags);
            CsiPersistenceManager.persist(myNewIcon);
            CsiPersistenceManager.deleteForSystem(myOldIcon);
            CsiPersistenceManager.commit();

        } catch (Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("EnhanceIcons.updateIcon", myException);
        }
        return myNewIcon;
    }

    private Icon updateIconId(String oldUuidIn, String newUuidIn) {

        Icon myNewIcon = null;

        CsiPersistenceManager.begin();
        try {

            Icon myOldIcon = CsiPersistenceManager.findObject(Icon.class, oldUuidIn);

            myNewIcon = myOldIcon.clone();
            myNewIcon.setUuid(newUuidIn);
            CsiPersistenceManager.persist(myNewIcon);
            CsiPersistenceManager.deleteForSystem(myOldIcon);
            CsiPersistenceManager.commit();

        } catch (Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("EnhanceIcons.updateIconId", myException);
        }
        return myNewIcon;
    }

    private GraphTheme updateGraphThemeId(String oldUuidIn, String newUuidIn) {

        GraphTheme myNewTheme = null;

        CsiPersistenceManager.begin();
        try {

            GraphTheme myOldTheme = CsiPersistenceManager.findObject(GraphTheme.class, oldUuidIn);

            myNewTheme = myOldTheme.clone();
            myNewTheme.setUuid(newUuidIn);
            CsiPersistenceManager.persist(myNewTheme);
            CsiPersistenceManager.deleteForSystem(myOldTheme);
            CsiPersistenceManager.commit();

        } catch (Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("EnhanceIcons.updateIconId", myException);
        }
        return myNewTheme;
    }

    private MapTheme updateMapThemeId(String oldUuidIn, String newUuidIn) {

        MapTheme myNewTheme = null;

        CsiPersistenceManager.begin();
        Collection<MapSettings> listE = CsiPersistenceManager.getMetaEntityManager().createQuery("SELECT e from MapSettings e", MapSettings.class).getResultList();
        try {

            MapTheme myOldTheme = CsiPersistenceManager.findObject(MapTheme.class, oldUuidIn);
            for (MapSettings mapSettings : listE) {
                String myUuid = mapSettings.getThemeUuid();
                if ((null != myUuid) && myUuid.equals(oldUuidIn)) {
                    mapSettings.setThemeUuid(newUuidIn);
                }
            }
            myNewTheme = myOldTheme.clone();
            myNewTheme.setUuid(newUuidIn);
            CsiPersistenceManager.persist(myNewTheme);
            CsiPersistenceManager.commit();

        } catch (Exception myException) {

            CsiPersistenceManager.rollback();
            LOG.error("EnhanceIcons.updateIconId", myException);
        }
        return myNewTheme;
    }

    private static void addTags(Icon iconIn, Collection<String>tagsIn) {

        Set<String> myTagSet = iconIn.getTags();

        if ((tagsIn != null) && !tagsIn.isEmpty()) {

            myTagSet.addAll(tagsIn);
        }
    }

    private IconHashInfo createInfoFromLine(String line) {
        IconHashInfo info = new IconHashInfo();
        //File is formatted name | md5 | tags+delimited | dataUrl
        String[] fields = line.split("\\|");
        String[] tags = fields[2].split("\\+");

        info.name = fields[0];
        info.hash = fields[1];
        info.uuid = CsiUUID.getMd5IconId(info.hash);

        if((tags != null) && (tags.length > 0)) {
            info.tags = (Lists.newArrayList(tags));
        } else {
            info.tags = Lists.newArrayList();
        }
        info.base64 = fields[3];

        return info;
    }

    private String determineName(String oldNameIn, String newNameIn) {
        return StringUtils.isNotEmpty(oldNameIn)
                    ? oldNameIn
                    : StringUtils.isNotEmpty(newNameIn) ? newNameIn : nextIconName();
    }

    private String nextIconName() {

        return "Icon_" + String.format("%04d", iconNamingDigits++);
    }

    public String getTagPath() {
        return tagPath;
    }

    public void setTagPath(String tagPath) {
        this.tagPath = tagPath;
    }
}