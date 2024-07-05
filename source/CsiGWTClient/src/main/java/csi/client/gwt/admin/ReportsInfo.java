package csi.client.gwt.admin;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.ui.GridInfo;
import csi.server.common.dto.ReportsDisplay;

import java.util.ArrayList;
import java.util.Date;

public class ReportsInfo extends GridInfo<ReportsDisplay> {

    interface ReportPropertyAccess extends PropertyAccess<ReportsDisplay> {

        @Path("id")
        public ModelKeyProvider<ReportsDisplay> key();

        @Path("date")
        public ValueProvider<ReportsDisplay, Date> date();

        @Path("activeUsers")
        public ValueProvider<ReportsDisplay, Integer> activeUsers();

        @Path("concurrentUsers")
        public ValueProvider<ReportsDisplay, Integer> concurrentUsers();

        @Path("maxLoginFailed")
        public ValueProvider<ReportsDisplay, Integer> maxLoginFailed();
    }

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static ReportPropertyAccess _propertyAcess = GWT.create(ReportPropertyAccess.class);

    private static final ArrayList<ColumnInfo<ReportsDisplay>> _columns;

    static {
        _columns = new ArrayList<ColumnInfo<ReportsDisplay>>();
        _columns.add(new ColumnInfo<ReportsDisplay>(_constants.administrationDialogs_ReportsColumn_1(),
                ColumnType.DATE, _propertyAcess.date(), 150, true, true));
        _columns.add(new ColumnInfo<ReportsDisplay>(_constants.administrationDialogs_ReportsColumn_2(),
                ColumnType.INTEGER, _propertyAcess.activeUsers(), 120, true, true));
        _columns.add(new ColumnInfo<ReportsDisplay>(_constants.administrationDialogs_ReportsColumn_3(),
                ColumnType.INTEGER, _propertyAcess.concurrentUsers(), 180, true, true));
        _columns.add(new ColumnInfo<ReportsDisplay>(_constants.administrationDialogs_ReportsColumn_5(),
                ColumnType.INTEGER, _propertyAcess.maxLoginFailed(), 150, true, true));
    }

    private boolean _isSecurityEnabled;

    protected ModelKeyProvider<ReportsDisplay> getKey() {
        return _propertyAcess.key();
    }

    public ReportsInfo(boolean _isSecurityEnabled) {
        _isSecurityEnabled = _isSecurityEnabled;
    }

    protected void createGridComponents() {
        for(ColumnInfo<ReportsDisplay> myColumn : _columns) {
            addColumn(myColumn);
        }
    }
}
