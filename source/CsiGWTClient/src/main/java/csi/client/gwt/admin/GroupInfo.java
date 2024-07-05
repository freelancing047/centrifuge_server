package csi.client.gwt.admin;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.ui.GridInfo;
import csi.server.common.dto.GroupDisplay;
import csi.server.common.enumerations.GroupType;

public class GroupInfo extends GridInfo<GroupDisplay> {

    interface SharingPropertyAccess extends PropertyAccess<GroupDisplay> {

        @Path("id")
        public ModelKeyProvider<GroupDisplay> key();

        @Path("name")
        public ValueProvider<GroupDisplay, String> name();

        @Path("remarks")
        public ValueProvider<GroupDisplay, String> remarks();

        @Path("external")
        public ValueProvider<GroupDisplay, Boolean> external();

        @Path("parentGroups")
        public ValueProvider<GroupDisplay, String> parentGroups();
    }

    interface SecurityPropertyAccess extends SharingPropertyAccess {
    }

    interface CapcoPropertyAccess extends SecurityPropertyAccess {

        @Path("sectionName")
        public ValueProvider<GroupDisplay, String> sectionName();

        @Path("portionText")
        public ValueProvider<GroupDisplay, String> portionText();
    }

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    
    private static final String[] _sharingHeaders = {
        
            _constants.administrationDialogs_SharingColumn_1(),
            _constants.administrationDialogs_SharingColumn_2(),
            _constants.administrationDialogs_SharingColumn_3(),
            _constants.administrationDialogs_SharingColumn_4()
    };

    private static final String[] _securityHeaders = {

            _constants.administrationDialogs_SecurityColumn_1(),
            _constants.administrationDialogs_SecurityColumn_2(),
            _constants.administrationDialogs_SecurityColumn_3(),
            _constants.administrationDialogs_SecurityColumn_6()
    };

    private static final String[] _capcoHeaders = {

            _constants.administrationDialogs_SecurityColumn_1(),
            _constants.administrationDialogs_SecurityColumn_2(),
            _constants.administrationDialogs_SecurityColumn_3(),
            _constants.administrationDialogs_SecurityColumn_4(),
            _constants.administrationDialogs_SecurityColumn_5(),
            _constants.administrationDialogs_SecurityColumn_6(),
    };

    private static final SharingPropertyAccess _sharingPropertyAccess = GWT.create(SharingPropertyAccess.class);
    private static final SecurityPropertyAccess _securityPropertyAccess = GWT.create(SecurityPropertyAccess.class);
    private static final CapcoPropertyAccess _capcoPropertyAccess = GWT.create(CapcoPropertyAccess.class);


    private static final ArrayList<ColumnInfo<GroupDisplay>> _sharingColumns;

    static {
        _sharingColumns = new ArrayList<ColumnInfo<GroupDisplay>>();
        _sharingColumns.add(new ColumnInfo<GroupDisplay>(_sharingHeaders[0], ColumnType.STRING, _sharingPropertyAccess.name(), 200, true, true));
        _sharingColumns.add(new ColumnInfo<GroupDisplay>(_sharingHeaders[1], ColumnType.STRING, _sharingPropertyAccess.remarks(), 300, false, true));
        _sharingColumns.add(new ColumnInfo<GroupDisplay>(_sharingHeaders[2], ColumnType.YES_NO, _capcoPropertyAccess.external(), 60, false, true));
        _sharingColumns.add(new ColumnInfo<GroupDisplay>(_sharingHeaders[3], ColumnType.STRING, _sharingPropertyAccess.parentGroups(), 1080, false, true));
    }

    private static final ArrayList<ColumnInfo<GroupDisplay>> _securityColumns;

    static {
        _securityColumns = new ArrayList<ColumnInfo<GroupDisplay>>();
        _securityColumns.add(new ColumnInfo<GroupDisplay>(_securityHeaders[0], ColumnType.STRING, _securityPropertyAccess.name(), 150, true, true));
        _securityColumns.add(new ColumnInfo<GroupDisplay>(_securityHeaders[1], ColumnType.STRING, _securityPropertyAccess.remarks(), 300, false, true));
        _securityColumns.add(new ColumnInfo<GroupDisplay>(_securityHeaders[2], ColumnType.YES_NO, _capcoPropertyAccess.external(), 60, false, true));
        _securityColumns.add(new ColumnInfo<GroupDisplay>(_securityHeaders[3], ColumnType.STRING, _securityPropertyAccess.parentGroups(), 930, false, true));
    }

    private static final ArrayList<ColumnInfo<GroupDisplay>> _capcoColumns;

    static {
        _capcoColumns = new ArrayList<ColumnInfo<GroupDisplay>>();
        _capcoColumns.add(new ColumnInfo<GroupDisplay>(_capcoHeaders[0], ColumnType.STRING, _capcoPropertyAccess.name(), 150, true, true));
        _capcoColumns.add(new ColumnInfo<GroupDisplay>(_capcoHeaders[1], ColumnType.STRING, _capcoPropertyAccess.remarks(), 300, false, true));
        _capcoColumns.add(new ColumnInfo<GroupDisplay>(_capcoHeaders[2], ColumnType.YES_NO, _capcoPropertyAccess.external(), 60, false, true));
        _capcoColumns.add(new ColumnInfo<GroupDisplay>(_capcoHeaders[3], ColumnType.STRING, _capcoPropertyAccess.sectionName(), 100, true, true));
        _capcoColumns.add(new ColumnInfo<GroupDisplay>(_capcoHeaders[4], ColumnType.STRING, _capcoPropertyAccess.portionText(), 200, true, true));
        _capcoColumns.add(new ColumnInfo<GroupDisplay>(_capcoHeaders[5], ColumnType.STRING, _capcoPropertyAccess.parentGroups(), 730, false, true));
    }

    private GroupType _groupType = null;
    private SharingPropertyAccess _propertyAccess = null;
    private List<ColumnInfo<GroupDisplay>> _columns = null;
    private SharedItems _shared;

    protected ModelKeyProvider<GroupDisplay> getKey() {
        
        return _propertyAccess.key();
    }
    
    public GroupInfo(GroupType typeIn, SharedItems sharedIn) {
        
        _groupType = typeIn;
        _shared = sharedIn;

        if (GroupType.SECURITY.equals(_groupType)) {

            if (_shared.doCapco()) {

                _propertyAccess = _capcoPropertyAccess;
                _columns =_capcoColumns;

            } else {

                _propertyAccess = _securityPropertyAccess;
                _columns =_securityColumns;
            }

        } else {

            _propertyAccess = _sharingPropertyAccess;
            _columns =_sharingColumns;
        }
    }

    public void createGridComponents()
    {
        addCheckColumn();

        for (ColumnInfo<GroupDisplay> myColumn : _columns) {
            addColumn(myColumn);
        }
    }
}