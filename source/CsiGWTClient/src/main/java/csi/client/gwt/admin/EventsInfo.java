package csi.client.gwt.admin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.ui.GridInfo;
import csi.server.common.dto.EventsDisplay;

import java.util.ArrayList;
import java.util.Date;

public class EventsInfo extends GridInfo<EventsDisplay> {

    interface EventsPropertyAccess extends PropertyAccess<EventsDisplay> {

        @Path("id")
        public ModelKeyProvider<EventsDisplay> key();

        @Path("timestamp")
        public ValueProvider<EventsDisplay, String> timestamp();

        @Path("userId")
        public ValueProvider<EventsDisplay, String> userId();

        @Path("event")
        public ValueProvider<EventsDisplay, String> event();

    }

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static EventsInfo.EventsPropertyAccess _propertyAcess = GWT.create(EventsInfo.EventsPropertyAccess.class);

    private static final ArrayList<ColumnInfo<EventsDisplay>> _columns;

    static {
        _columns = new ArrayList<ColumnInfo<EventsDisplay>>();
        _columns.add(new ColumnInfo<EventsDisplay>("Timestamp", ColumnType.STRING,
                _propertyAcess.timestamp(), 140, true, true));
        _columns.add(new ColumnInfo<EventsDisplay>("User Id", ColumnType.STRING,
                _propertyAcess.userId(), 140, true, true));
        _columns.add(new ColumnInfo<EventsDisplay>("Event", ColumnType.STRING,
                _propertyAcess.event(), 120, true, true));
    }

    private boolean _isSecurityEnabled;

    protected ModelKeyProvider<EventsDisplay> getKey() {
        return _propertyAcess.key();
    }

    public EventsInfo(boolean _isSecurityEnabled) {
        _isSecurityEnabled = _isSecurityEnabled;
    }

    protected void createGridComponents() {
        for(ColumnInfo<EventsDisplay> myColumn : _columns) {
            addColumn(myColumn);
        }
    }
}
