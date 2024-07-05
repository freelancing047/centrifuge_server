package csi.tools;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.google.gwt.thirdparty.guava.common.collect.Maps;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import csi.config.Configuration;
import csi.security.ACL;
import csi.security.queries.AclRequest;
import csi.server.common.enumerations.MapLayerType;
import csi.server.common.model.map.Basemap;
import csi.server.dao.CsiPersistenceManager;

public class GenerateBasemapsInitial implements CustomTaskChange {
    private static final Logger LOG = LogManager.getLogger(GenerateBasemapsInitial.class);

    private static Map<String, String> esriBasemapKeywordToUrl;
    private static Map<String, String> esriBasemapKeywordToReferenceUrl;

    static {
        esriBasemapKeywordToUrl = Maps.newHashMap();
        esriBasemapKeywordToUrl.put("streets",
                "https://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
        esriBasemapKeywordToUrl.put("satellite",
                "https://services.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer");
        esriBasemapKeywordToUrl.put("hybrid",
                "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer");
        esriBasemapKeywordToUrl.put("topo",
                "https://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");
        esriBasemapKeywordToUrl.put("gray",
                "https://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer");
        esriBasemapKeywordToUrl.put("dark-gray",
                "https://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer");
        esriBasemapKeywordToUrl.put("oceans",
                "https://services.arcgisonline.com/arcgis/rest/services/Ocean/World_Ocean_Base/MapServer");
        esriBasemapKeywordToUrl.put("national-geographic",
                "https://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer");
        esriBasemapKeywordToUrl.put("terrain",
                "https://services.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer");
        esriBasemapKeywordToReferenceUrl = Maps.newHashMap();
        esriBasemapKeywordToReferenceUrl.put("hybrid",
                "https://services.arcgisonline.com/ArcGIS/rest/services/Reference/World_Boundaries_and_Places/MapServer");
        esriBasemapKeywordToReferenceUrl.put("gray",
                "https://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Reference/MapServer");
        esriBasemapKeywordToReferenceUrl.put("dark-gray",
                "https://services.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Reference/MapServer");
        esriBasemapKeywordToReferenceUrl.put("oceans",
                "https://services.arcgisonline.com/arcgis/rest/services/Ocean/World_Ocean_Reference/MapServer");
        esriBasemapKeywordToReferenceUrl.put("terrain",
                "https://services.arcgisonline.com/ArcGIS/rest/services/Reference/World_Reference_Overlay/MapServer");
    }

    private static Set<String> getEsriBasemapKeywords() {
        return esriBasemapKeywordToUrl.keySet();
    }

    public static String getUrl(String keyword) {
        return esriBasemapKeywordToUrl.get(keyword);
    }

    public static boolean hasUrl(String keyword) {
        return esriBasemapKeywordToUrl.containsKey(keyword);
    }

    private static String getReferenceUrl(String keyword) {
        return esriBasemapKeywordToReferenceUrl.get(keyword);
    }

    private static boolean hasReferenceUrl(String keyword) {
        return esriBasemapKeywordToReferenceUrl.containsKey(keyword);
    }

    @Override
    public String getConfirmationMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setUp() throws SetupException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // TODO Auto-generated method stub

    }

    @Override
    public ValidationErrors validate(Database database) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        if (proceed()) {
            List<String> updateStatements = new ArrayList<>();
            CsiPersistenceManager.begin();
            for (String keyword : getEsriBasemapKeywords()) {
                String url = GenerateBasemapsInitial.getUrl(keyword);
                if (GenerateBasemapsInitial.hasReferenceUrl(keyword)) {
                    updateStatements.add(initializeBasemap(keyword, keyword));
                    updateStatements.add(initializeBasemap(keyword + " base", url));
                    String referenceUrl = GenerateBasemapsInitial.getReferenceUrl(keyword);
                    updateStatements.add(initializeBasemap(keyword + " reference", referenceUrl));
                } else {
                    updateStatements.add(initializeBasemap(keyword, url));
                }
            }
            CsiPersistenceManager.commit();
            CsiPersistenceManager.close();

            try (Connection conn = CsiPersistenceManager.getMetaConnection();
                 Statement stmt = conn.createStatement()) {
               for (String updateStatement : updateStatements) {
                  LOG.info(updateStatement);
                  stmt.executeUpdate(updateStatement);
               }
               conn.commit();
            } catch (Exception se) {
                LOG.error(se);
            }
        }
    }

   private boolean proceed() {
      boolean result = true;

      try (Connection conn = CsiPersistenceManager.getMetaConnection();
           Statement stmt = conn.createStatement();
           ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM basemap")) {
         result = (rs.next() && (rs.getInt(1) <= 0));
      } catch (Exception se) {
         LOG.error(se);
      }
      return result;
   }

    private Configuration getConfiguration() {
        final File f = new File(GenerateBasemapsInitial.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String fpath = f.getPath();
        LOG.error("fpath: " + fpath);
        fpath = fpath.replace("webapps\\Centrifuge\\WEB-INF\\lib\\centrifuge-services.jar", "");
        LOG.error("fpath: " + fpath);
        fpath = fpath.replace('\\', '/');
        LOG.error("fpath: " + fpath);
        String target = fpath + "conf/centrifuge/META-INF/ioc/spring-main.xml";
        LOG.error("target: " + target);
        // final String classpath = System.getProperty("java.class.path");
        // LOG.error("classpath: " + classpath);
        ApplicationContext ctx = new FileSystemXmlApplicationContext(target);
        Configuration config = ctx.getBean(Configuration.class);
        return config;
    }

    private String initializeBasemap(String name, String url) {
        Basemap basemap = new Basemap();
        basemap.setName(name);
        basemap.setUrl(url);
        basemap.setType(MapLayerType.ARCGIS_TILED.getLabel());
        CsiPersistenceManager.persist(basemap);
        ACL myAcl = AclRequest.getResourceACL(basemap.getUuid());
        CsiPersistenceManager.merge(myAcl);
        return "UPDATE mapsettings SET basemapfield = '" + basemap.getUuid() + "' WHERE basemapfield = '" + name + "'";
    }

}
