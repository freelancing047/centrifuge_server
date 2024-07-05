package csi.client.gwt.admin;

import com.google.gwt.event.shared.HandlerRegistration;

import csi.client.gwt.events.DataChangeEventHandler;
import csi.server.common.dto.GroupDisplay;

/**
 * Created by centrifuge on 4/10/2015.
 */
public interface GroupInfoPopup {

    public void show();
    public GroupDisplay getGroupInfo();
    public HandlerRegistration addDataChangeEventHandler( DataChangeEventHandler handler);
}
