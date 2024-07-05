package csi.client.gwt.mapper.menus;

import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 3/22/2016.
 */
public interface MappingItem {

    public int getOrdinal();
    public String getMappingName();
    public CsiDataType getType();
}
