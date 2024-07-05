package csi.server.common.dto.user;

import java.util.List;

import csi.security.CsiSecurityManager;
import csi.server.common.enumerations.SystemParameter;
import csi.server.common.model.query.QueryParameterDef;
import csi.server.common.util.SystemValueProvider;
import csi.server.task.api.TaskHelper;

/**
 * Created by centrifuge on 7/25/2016.
 */
public class UserSystemValues implements SystemValueProvider {
    @Override
    public List<QueryParameterDef> loadValues(List<QueryParameterDef> listIn) {

        listIn.get(SystemParameter.USER.ordinal()).setSingleValue(CsiSecurityManager.getUserName());
        listIn.get(SystemParameter.CLIENT.ordinal()).setSingleValue(TaskHelper.getCurrentContext().getClientIP());
        listIn.get(SystemParameter.REMOTE_USER.ordinal()).setSingleValue(TaskHelper.getCurrentContext().getRemoteUser());
        listIn.get(SystemParameter.URL.ordinal()).setSingleValue(TaskHelper.getCurrentContext().getRequestURL());
        listIn.get(SystemParameter.DN.ordinal()).setSingleValue(CsiSecurityManager.getDistinguishedName());

        return listIn;
    }
}
