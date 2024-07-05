package csi.server.common.interfaces;

import java.util.Map;

import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 5/12/2015.
 */
public interface MapByDataType extends ColumnKeys {

    public void mapDoubleMapByType(CsiDataType dataTypeIn, Map<String, String> idMapIn, Map<String, String> nameMapIn);

    public String getName();
    public CsiDataType getDataType();
    public String getLocalId();
}
