package csi.client.gwt.events;

import csi.client.gwt.etc.BaseCsiEventHandler;
import csi.server.common.dto.SelectionListData.ExtendedInfo;


public abstract class MappingChangeEventHandler<S extends ExtendedInfo, T extends ExtendedInfo> extends BaseCsiEventHandler {

    public abstract void onMappingChange(MappingChangeEvent<S, T> event);
}
