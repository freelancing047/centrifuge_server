package csi.client.gwt.admin;

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
import csi.server.common.dto.UserDisplay;


public class UserInfo extends GridInfo<UserDisplay> {
    
    interface UserPropertyAccess extends PropertyAccess<UserDisplay> {
        
        @Path("id")
        public ModelKeyProvider<UserDisplay> key();

        @Path("name")
        public ValueProvider<UserDisplay, String> name();
        
        @Path("firstName")
        public ValueProvider<UserDisplay, String> firstName();
        
        @Path("lastName")
        public ValueProvider<UserDisplay, String> lastName();
        
        @Path("email")
        public ValueProvider<UserDisplay, String> email();
        
        @Path("lastLogin")
        public ValueProvider<UserDisplay, Date> lastLogin();
        
        @Path("creationDate")
        public ValueProvider<UserDisplay, Date> creationDate();
        
        @Path("expirationDate")
        public ValueProvider<UserDisplay, Date> expirationDate();
        
        @Path("perpetual")
        public ValueProvider<UserDisplay, Boolean> perpetual();

        @Path("disabled")
        public ValueProvider<UserDisplay, Boolean> disabled();

        @Path("suspended")
        public ValueProvider<UserDisplay, Boolean> suspended();

        @Path("groups")
        public ValueProvider<UserDisplay, String> groups();
        
        @Path("clearance")
        public ValueProvider<UserDisplay, String> clearance();
        
        @Path("remarks")
        public ValueProvider<UserDisplay, String> remarks();
        
    }
    
    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static UserPropertyAccess _propertyAccess = GWT.create(UserPropertyAccess.class);
    
    private static final ArrayList<ColumnInfo<UserDisplay>> _columns;

    static {
        _columns = new ArrayList<ColumnInfo<UserDisplay>>();
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_01(), ColumnType.STRING, _propertyAccess.name(), 100, true, true));
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_02(), ColumnType.STRING, _propertyAccess.firstName(), 100, true, true));
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_03(), ColumnType.STRING, _propertyAccess.lastName(), 100, true, true));
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_04(), ColumnType.STRING, _propertyAccess.email(), 120, true, true));
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_05(), ColumnType.DATE, _propertyAccess.lastLogin(), 100, true, true));
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_06(), ColumnType.DATE, _propertyAccess.creationDate(), 100, true, true));
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_07(), ColumnType.DATE, _propertyAccess.expirationDate(), 100, true, true));
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_08(), ColumnType.BOOLEAN, _propertyAccess.perpetual(), 100, true, true));
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_09(), ColumnType.BOOLEAN, _propertyAccess.disabled(), 100, true, true));
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_10(), ColumnType.BOOLEAN, _propertyAccess.suspended(), 100, true, true));
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_11(), ColumnType.STRING, _propertyAccess.groups(), 100, false, true));
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_12(), ColumnType.STRING, _propertyAccess.clearance(), 100, false, true));
        _columns.add(new ColumnInfo<UserDisplay>(_constants.administrationDialogs_UserColumn_13(), ColumnType.STRING, _propertyAccess.remarks(), 120, false, true));
    }
    
    private boolean _isSecurityEnabled;

    protected ModelKeyProvider<UserDisplay> getKey() {
        
        return _propertyAccess.key();
    }
    
    public UserInfo(boolean isSecurityEnabledIn) {
    
        _isSecurityEnabled = isSecurityEnabledIn;   
    }

    public void createGridComponents()
    {
        addCheckColumn();
        for (int i =0; 11 > i; i++) {
            ColumnInfo<UserDisplay> myColumn = _columns.get(i);
            addColumn(myColumn);
        }
        if (_isSecurityEnabled) {
            
            addColumn(_columns.get(11));
        }
        addColumn(_columns.get(12));
    }
}
