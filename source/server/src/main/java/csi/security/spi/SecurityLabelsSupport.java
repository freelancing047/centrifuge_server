package csi.security.spi;

import java.util.List;

import javax.persistence.EntityManager;

import csi.server.common.model.CsiUUID;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.model.extension.ExtensionData;
import csi.server.common.model.extension.LabelsData;
import csi.server.dao.CsiPersistenceManager;

public class SecurityLabelsSupport
{
    public static LabelsData getLabelsData(DataViewDef def) {
        List<ExtensionData> list = def.getExtensionData();
        for (ExtensionData data : list) {
            if (data instanceof LabelsData) {
                return (LabelsData) data;
            }
        }
        return null;
    }

    public static LabelsData getLabelDataFrom(String resourceId) {

        String dvId = resourceId;

        // NB: do not use the other find/get methods on CsiPersistenceManager!
        // those calls result in a security check. It will
        // result in a stack overflow!!!
        EntityManager em = CsiPersistenceManager.getMetaEntityManager();
        DataView dv = em.find(DataView.class, new CsiUUID(dvId));

        if (dv != null) {
            DataViewDef def = dv.getMeta();
            return getLabelsData(def);
        }

        return null;
    }

}
