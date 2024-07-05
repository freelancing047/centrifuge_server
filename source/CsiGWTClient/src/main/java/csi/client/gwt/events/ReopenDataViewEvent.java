/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.events;

        import com.google.gwt.event.shared.GwtEvent;

        import csi.client.gwt.dataview.AbstractDataViewPresenter;
        import csi.client.gwt.etc.BaseCsiEvent;
        import csi.server.common.model.dataview.DataView;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ReopenDataViewEvent extends BaseCsiEvent<ReopenDataViewEventHandler> {

    public static final GwtEvent.Type<ReopenDataViewEventHandler> type = new GwtEvent.Type<ReopenDataViewEventHandler>();

    private String _newUuid;
    private String _newName;

    public ReopenDataViewEvent(String newUuidIn, String newNameIn) {
        super();
        _newUuid = newUuidIn;
        _newName = newNameIn;
    }

    public OpenDataViewEvent getOpenEvent(String oldUUidIn, String oldWorksheetIn) {

        if ((null != oldWorksheetIn) && _newUuid.equals(oldUUidIn)) {

            return new OpenDataViewEvent(_newName, _newUuid, null, oldWorksheetIn);

        } else {

            return new OpenDataViewEvent(_newName, _newUuid);
        }
    }

    public OpenDataViewEvent getOpenEvent() {

        return getOpenEvent(null, null);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ReopenDataViewEventHandler> getAssociatedType() {
        return type;
    }

    @Override
    protected void dispatch(ReopenDataViewEventHandler handler) {
        handler.onDataViewReopen(this);
    }
}
