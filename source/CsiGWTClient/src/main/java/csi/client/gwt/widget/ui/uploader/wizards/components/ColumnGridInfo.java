package csi.client.gwt.widget.ui.uploader.wizards.components;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.ui.GridInfo;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 8/17/2015.
 */
public class ColumnGridInfo extends GridInfo<InstallerColumnDisplay> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                 Embedded Interfaces                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface ColumnPropertyAccess extends PropertyAccess<InstallerColumnDisplay> {

        @Editor.Path("columnNumber")
        public ModelKeyProvider<InstallerColumnDisplay> key();

        @Editor.Path("columnNumber")
        public ValueProvider<InstallerColumnDisplay, Integer> columnNumber();

        @Editor.Path("firstValue")
        public ValueProvider<InstallerColumnDisplay, String> firstValue();

        @Editor.Path("display")
        public ValueProvider<InstallerColumnDisplay, ValuePair<String, DisplayMode>> display();

        @Editor.Path("dataType")
        public ValueProvider<InstallerColumnDisplay, String> dataType();

        @Editor.Path("include")
        public ValueProvider<InstallerColumnDisplay, Boolean> include();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static final ColumnPropertyAccess _propertyAccess = GWT.create(ColumnPropertyAccess.class);

    private static final ArrayList<ColumnInfo<InstallerColumnDisplay>> _columns;

    private static final String[] _columnHeaders = {

            _constants.installFileWizard_GridColumn_1(),
            _constants.installFileWizard_GridColumn_2(),
            _constants.installFileWizard_GridColumn_3(),
            _constants.installFileWizard_GridColumn_4(),
            _constants.installFileWizard_GridColumn_5()
    };

    static {

        _columns = new ArrayList<ColumnInfo<InstallerColumnDisplay>>();
        _columns.add(new ColumnInfo<InstallerColumnDisplay>(_columnHeaders[0], ColumnType.INTEGER, _propertyAccess.columnNumber(), 60, false, true));
        _columns.add(new ColumnInfo<InstallerColumnDisplay>(_columnHeaders[1], ColumnType.STRING, _propertyAccess.firstValue(), 120, false, true));
        _columns.add(new ColumnInfo<InstallerColumnDisplay>(_columnHeaders[2], ColumnType.COLORED_STRING, _propertyAccess.display(), 120, false, true));
        _columns.add(new ColumnInfo<InstallerColumnDisplay>(_columnHeaders[3], ColumnType.STRING, _propertyAccess.dataType(), 80, false, true));
        _columns.add(new ColumnInfo<InstallerColumnDisplay>(_columnHeaders[4], ColumnType.YES_NO, _propertyAccess.include(), 60, false, true));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ColumnGridInfo() {

        super();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected ModelKeyProvider<InstallerColumnDisplay> getKey() {

        return _propertyAccess.key();
    }

    protected void createGridComponents()
    {
        addCheckColumn();

        for (ColumnInfo<InstallerColumnDisplay> myColumn : _columns) {
            addColumn(myColumn);
        }
    }
}
