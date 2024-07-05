package csi.server.util;

import javax.persistence.EntityManager;

import csi.config.Configuration;
import csi.security.queries.AclRequest;
import csi.server.business.helper.DataViewHelper;
import csi.server.business.helper.ModelHelper;
import csi.server.business.service.GraphActionsService;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.dao.CsiPersistenceManager;

public class TemplateCacheUtil {

    public static final String PREBUILD_PROPERTY = "prebuild";
    //FIXME: This is unsafe because it goes below CSIpersistence Manager.
    //FIXME: please put under security package.
    public static void createCachedDataView(DataViewHelper dataViewHelper, String templateUuid) throws CentrifugeException {

        if(!Configuration.getInstance().getApplicationConfig().isEnableTemplateCache()){
            return;
        }

        EntityManager entityManager = CsiPersistenceManager.getMetaEntityManager();
        DataViewDef template = entityManager.find(DataViewDef.class, new CsiUUID(templateUuid));
        DataView dataView;
        {
            DataViewDef myMetaData = ModelHelper.cloneObject(template);
            myMetaData.setTemplate(false);
            GraphActionsService.augmentRelGraphViewDef(myMetaData);
            dataView = new DataView(myMetaData, template.getName() + "-" + PREBUILD_PROPERTY, "");
        }
        dataView.getClientProperties().put(PREBUILD_PROPERTY, Boolean.TRUE.toString());

        {//ModelHelper saveNew
            dataView = DataViewHelper.fixupPersistenceLinkage(dataView);
            CsiPersistenceManager.getMetaEntityManager().persist(dataView);
            CsiPersistenceManager.commit();
            CsiPersistenceManager.begin();
            dataView = entityManager.find(DataView.class, new CsiUUID(dataView.getUuid()));
        }
        // Guarantee no ACL exists for this DataView
        AclRequest.removeAcl(dataView.getUuid());

//        String uuid = dataView.getUuid();

//        entityManager.merge(CsiPersistenceManager.stripAllCredentials(dataView));
//        dataView = entityManager.find(DataView.class, new CsiUUID(uuid));

//        CsiPersistenceManager.commit();
        //dataViewHelper.cloneGraphPlayerSettings(dataView.getMeta(), template);
        entityManager.merge(dataView);
        CsiPersistenceManager.commit();
        DataView pulledDataview = entityManager.find(DataView.class, new CsiUUID(dataView.getUuid()));
        CsiPersistenceManager.addPrebuiltDataView(template.getUuid(), pulledDataview);

        //		jogFormer.transform(dataView);
    }

    public static void createCachedDataView(DataViewDef template) throws CentrifugeException {
        DataViewHelper dataViewHelper = new DataViewHelper();
        createCachedDataView(dataViewHelper, template.getUuid());
    }
}
