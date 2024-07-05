package csi.server.business.service;

import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.PayloadParam;
import csi.server.business.service.annotation.QueryParam;
import csi.server.business.service.annotation.Service;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.AnnotationDef;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.dao.CsiPersistenceManager;

@Service(path = "/actions/annotation")
public class AnnotationActionsService extends AbstractService {

    @Operation
    public void updateAnnotation(@QueryParam("anchorUUID") String anchoruuid, @PayloadParam AnnotationDef adef) throws CentrifugeException {
        CsiPersistenceManager.merge(adef);
    }

    private WorksheetDef getAnchorFromRequest(String anchoruuid) throws CentrifugeException {
        if (anchoruuid == null) {
            throw new CentrifugeException("Missing required parameter: anchorUUID");
        }
        WorksheetDef worksheet = CsiPersistenceManager.findObject(WorksheetDef.class, anchoruuid);
        if (null == worksheet) {
            throw new CentrifugeException("Unable to locate worksheet identified by " + anchoruuid);
        }
        return worksheet;
    }

    private AnnotationDef findAnnotation(String annotationUUID) throws CentrifugeException {
        AnnotationDef ann = CsiPersistenceManager.findObject(AnnotationDef.class, annotationUUID.toLowerCase());
        return ann;
    }

}
