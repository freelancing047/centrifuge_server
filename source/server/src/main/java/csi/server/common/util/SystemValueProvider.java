package csi.server.common.util;

import java.util.List;

import csi.server.common.model.query.QueryParameterDef;

/**
 * Created by centrifuge on 7/25/2016.
 */
public interface SystemValueProvider {

    public List<QueryParameterDef> loadValues(List<QueryParameterDef> listIn);
}
