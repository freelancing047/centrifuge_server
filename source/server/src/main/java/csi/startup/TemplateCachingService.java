package csi.startup;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.AbstractIdleService;

import csi.server.business.helper.DataViewHelper;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.TemplateCacheUtil;

public class TemplateCachingService extends AbstractIdleService {
   private static final Logger LOG = LogManager.getLogger(TemplateCachingService.class);

   @Override
   protected void startUp() throws Exception {
      try {
         LOG.info("Caching Templates....");
         CsiPersistenceManager.begin();

//         List<String> templates = Lists.newArrayList("d76459e1-b940-42f4-8f19-91301bb92904","f5822d17-7168-4186-a108-9ed93c3bd136");
         List<String> templates = new ArrayList<String>();
//         List<String> templates = AclRequest.listAllTemplateUuids();
         DataViewHelper dataViewHelper = new DataViewHelper();

         for (String primaryKey : templates) {
            LOG.error("Caching template with id: " + primaryKey);
            // DataViewDef template = entityManager.find(DataViewDef.class, new CsiUUID(primaryKey));
            TemplateCacheUtil.createCachedDataView(dataViewHelper, primaryKey);
         }
         CsiPersistenceManager.commit();
         CsiPersistenceManager.close();
         CsiPersistenceManager.releaseCacheConnection();
      } catch (Exception exception) {
         LOG.error("Failed to pre-load Templates", exception);
      }
   }

   @Override
   protected void shutDown() throws Exception {
   }
}
