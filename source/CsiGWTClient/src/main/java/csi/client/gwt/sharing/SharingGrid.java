package csi.client.gwt.sharing;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.ui.GridInfo;
import csi.server.common.dto.SharingDisplay;


public class SharingGrid extends GridInfo<SharingDisplay> {
    
    interface GroupPropertyAccess extends PropertyAccess<SharingDisplay> {
        
        @Path("uuid")
        public ModelKeyProvider<SharingDisplay> key();

        @Path("name")
        public ValueProvider<SharingDisplay, String> name();

        @Path("owner")
        public ValueProvider<SharingDisplay, String> owner();

        @Path("remarks")
        public ValueProvider<SharingDisplay, String> remarks();

        @Path("size")
        public ValueProvider<SharingDisplay, Long> size();

        @Path("createDate")
        public ValueProvider<SharingDisplay, Date> createDate();

        @Path("changeDate")
        public ValueProvider<SharingDisplay, Date> changeDate();

        @Path("accessDate")
        public ValueProvider<SharingDisplay, Date> accessDate();

        @Path("readers")
        public ValueProvider<SharingDisplay, String> readers();

        @Path("writers")
        public ValueProvider<SharingDisplay, String> writers();

        @Path("destroyers")
        public ValueProvider<SharingDisplay, String> destroyers();
    }
    
    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    
    private static final String[] _columnHeaders = {
        
        _constants.sharingDialogs_SharingColumn_1(),
        _constants.sharingDialogs_SharingColumn_2(),
        _constants.sharingDialogs_SharingColumn_3(),
        _constants.sharingDialogs_SharingColumn_4(),
        _constants.sharingDialogs_SharingColumn_5(),
        _constants.sharingDialogs_SharingColumn_6(),
        _constants.sharingDialogs_SharingColumn_7(),
        _constants.sharingDialogs_SharingColumn_8(),
        _constants.sharingDialogs_SharingColumn_9(),
        _constants.sharingDialogs_SharingColumn_10()
    };

    private static final GroupPropertyAccess _propertyAccess = GWT.create(GroupPropertyAccess.class);


    private static final ArrayList<ColumnInfo<SharingDisplay>> _basicColumns;

    static {
        _basicColumns = new ArrayList<ColumnInfo<SharingDisplay>>();
        _basicColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[0], ColumnType.STRING, _propertyAccess.name(), 200, true, true));
        _basicColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[1], ColumnType.STRING, _propertyAccess.remarks(), 250, false, true));
        _basicColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[3], ColumnType.DATE_TIME, _propertyAccess.createDate(), 110, true, true));
        _basicColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[4], ColumnType.DATE_TIME, _propertyAccess.changeDate(), 110, true, true));
        _basicColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[5], ColumnType.DATE_TIME, _propertyAccess.accessDate(), 110, true, true));
        _basicColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[6], ColumnType.STRING, _propertyAccess.owner(), 100, false, true));
        _basicColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[7], ColumnType.STRING, _propertyAccess.destroyers(), 150, false, true));
        _basicColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[8], ColumnType.STRING, _propertyAccess.writers(), 150, false, true));
        _basicColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[9], ColumnType.STRING, _propertyAccess.readers(), 150, false, true));
    }

    private static final ArrayList<ColumnInfo<SharingDisplay>> _extraColumns;

    static {
        _extraColumns = new ArrayList<ColumnInfo<SharingDisplay>>();
        _extraColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[0], ColumnType.STRING, _propertyAccess.name(), 200, true, true));
        _extraColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[1], ColumnType.STRING, _propertyAccess.remarks(), 250, false, true));
        _extraColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[2], ColumnType.LONG, _propertyAccess.size(), 100, true, true));
        _extraColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[3], ColumnType.DATE_TIME, _propertyAccess.createDate(), 110, true, true));
        _extraColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[4], ColumnType.DATE_TIME, _propertyAccess.changeDate(), 110, true, true));
        _extraColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[5], ColumnType.DATE_TIME, _propertyAccess.accessDate(), 110, true, true));
        _extraColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[6], ColumnType.STRING, _propertyAccess.owner(), 100, true, true));
        _extraColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[7], ColumnType.STRING, _propertyAccess.destroyers(), 150, false, true));
        _extraColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[8], ColumnType.STRING, _propertyAccess.writers(), 150, false, true));
        _extraColumns.add(new ColumnInfo<SharingDisplay>(_columnHeaders[9], ColumnType.STRING, _propertyAccess.readers(), 150, false, true));
    }

    private ArrayList<ColumnInfo<SharingDisplay>> _columns;

    protected ModelKeyProvider<SharingDisplay> getKey() {
        
        return _propertyAccess.key();
    }

    public SharingGrid(boolean extendedIn) {

        super();
        _columns = (extendedIn) ? _extraColumns : _basicColumns;
    }

    public void createGridComponents()
    {
        addCheckColumn();

        for (ColumnInfo<SharingDisplay> myColumn : _columns) {
            addColumn(myColumn);
        }
    }
}