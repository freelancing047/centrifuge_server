package csi.server.business.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import csi.config.Configuration;
import csi.security.queries.AclRequest;
import csi.server.business.helper.DataViewHelper;
import csi.server.common.codec.xstream.XStreamHelper;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.map.Basemap;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.RegexFileNameFilter;

public class SamplesManager {
   private static final Logger LOG = LogManager.getLogger(SamplesManager.class);

   private static final SamplesManager INSTANCE = new SamplesManager();
    public static SamplesManager getInstance() {
        return INSTANCE;
    }

    private static final String SAMPLES_DIR = "samples/templates";
    private static final String TEMPLATE_FILE_REGEX = "(?i).*\\.xml";
    private static final RegexFileNameFilter TEMPLATE_FILTER = new RegexFileNameFilter(TEMPLATE_FILE_REGEX, true);

    private SamplesManager() {
        //Singleton
    }

   public void loadSamples(String appRoot) {
      File myRoot = new File(appRoot);
      List<File> mySampleFiles = getSampleFiles(myRoot);

      try {
         CsiPersistenceManager.begin();
         purgeOriginalSamples();

         for (File myFile : mySampleFiles) {
            try {
               DataViewDef myResource = loadSample(myFile);

               if ((myResource != null) && ((AclResourceType.ADMIN_TOOL == myResource.getResourceType())
                     || ((AclResourceType.SAMPLE == myResource.getResourceType())
                           && !Configuration.getInstance().getApplicationConfig().isPurgeSamples()))) {
                  DataViewHelper.fixupPersistenceLinkage(myResource);

                  if (myResource.getName().equals("Sample_I-94")) {
                     Basemap basemap = null;

                     try {
                        //This is to stop any possible chance that there could be multiple topos in the database.
                        basemap =  CsiPersistenceManager.getMetaEntityManager().createQuery("SELECT e from Basemap e where e.name = 'topo'",
                              Basemap.class).setMaxResults(1).getResultList().get(0);
                     } catch (Exception myException) {
                        LOG.error("Caught exception getting topo basemap "
                              + Format.value(myFile.getName()) + " :: " + Format.value(myException));
                        throw new Exception(myException);
                     }
                     if (basemap != null) {
                        for (VisualizationDef viz : myResource.getModelDef().getVisualizations()) {
                           if (viz instanceof MapViewDef) {
                              MapViewDef mapViewDef = (MapViewDef) viz;

                              mapViewDef.getMapSettings().getTileLayers().get(0).setLayerId(basemap.getUuid());
                           }
                        }
                     }
                  }
                  CsiPersistenceManager.getMetaEntityManager().persist(myResource);
                  CsiPersistenceManager.commit();
                  CsiPersistenceManager.begin();
               }
            } catch(Exception myException) {
               CsiPersistenceManager.rollback();
               CsiPersistenceManager.begin();

               LOG.error("Caught exception loading sample "
                            + Format.value(myFile.getName()) + " :: " + Format.value(myException));
            }
         }
      } finally {
         CsiPersistenceManager.close();
      }
   }

    private void purgeOriginalSamples() {

        try {

            List<DataViewDef> mySampleList = AclRequest.loadAllOriginalSampleTemplatesAvoidingSecurity();

            for (DataViewDef mySample : mySampleList) {

                try {

                    AclRequest.removeAcl(mySample.getUuid());
                    CsiPersistenceManager.commit();
                    CsiPersistenceManager.begin();
                    CsiPersistenceManager.deleteForSystem(mySample);
                    CsiPersistenceManager.commit();
                    CsiPersistenceManager.begin();

                } catch (Exception myException) {

                    CsiPersistenceManager.rollback();
                    CsiPersistenceManager.begin();
                    LOG.error("Caught exception purging sample " + Format.value(mySample) + ".", myException);
                }
            }

        } catch (Exception myException) {

            LOG.error("Caught exception purging samples.", myException);
        }
    }

    private List<File> getSampleFiles(File root){
        File samplesDir = new File(root, SAMPLES_DIR);
        if (!samplesDir.exists()) {
            return new ArrayList<File>();
        }
        return listSampleFiles(samplesDir, TEMPLATE_FILTER);
    }

   private static DataViewDef loadSample(File file) {
      DataViewDef result = null;
      XStream codec = XStreamHelper.getImportExportCodec();

      try (FileInputStream fin = new FileInputStream(file)) {
         result = (DataViewDef) codec.fromXML(fin);
      } catch (ClassCastException e) {
         LOG.warn("Failed to import file: " + file.getName() + ".  File does not contain a template definition.", e);
      } catch (FileNotFoundException e) {
         LOG.warn("Failed to import file.  File " + file.getName() + " not found", e);
      } catch (Exception e) {
         LOG.warn("Failed to import file " + file.getName(), e);
      }
      return result;
   }

    /**
     * Recursively find all files in the given directory.
     * @param dir The root directory
     * @param filter Removes unwanted extensions
     * @return A list of files.
     */
    private List<File> listSampleFiles(File dir, FilenameFilter filter) {
        List<File> list = new ArrayList<File>();
        String[] children = dir.list(filter);

        for (String child : children) {
            File f = new File(dir, child);
            if (f.isDirectory()) {
                list.addAll(listSampleFiles(f, filter));
            } else {
                list.add(f);
            }
        }

        return list;
    }

}
