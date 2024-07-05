package csi.client.gwt.csi_resource.filters;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.ui.GridInfo;
import csi.server.common.dto.SelectionListData.OptionBasics;
import csi.server.common.dto.SelectionListData.OptionBasics;

/**
 * Created by centrifuge on 5/5/2017.
 */
public class FilterGridInfo extends GridInfo<OptionBasics> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

  
    interface ResourceFilterPropertyAccess extends PropertyAccess<OptionBasics> {

        public ModelKeyProvider<OptionBasics> key();

        public ValueProvider<OptionBasics, String> name();

        public ValueProvider<OptionBasics, String> remarks();

        public ValueProvider<OptionBasics, Boolean> defaultOption();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Static Values                                      //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtNameColumn = _constants.resourceFilterListDialog_NameColumnTitle();
    private static final String _txtDescriptionColumn = _constants.resourceFilterListDialog_DescriptionColumnTitle();
    private static final String _txtDefaultColumn = _constants.resourceFilterListDialog_DefaultColumnTitle();

    private static final ResourceFilterPropertyAccess _propertyAccess = GWT.create(ResourceFilterPropertyAccess.class);

    private static final ArrayList<ColumnInfo<OptionBasics>> _columns;

    static {
        _columns = new ArrayList<ColumnInfo<OptionBasics>>();
        _columns.add(new ColumnInfo<OptionBasics>(_txtNameColumn, ColumnType.STRING, _propertyAccess.name(), 150, true, true));
        _columns.add(new ColumnInfo<OptionBasics>(_txtDescriptionColumn, ColumnType.STRING, _propertyAccess.remarks(), 250, false, true));
        _columns.add(new ColumnInfo<OptionBasics>(_txtDefaultColumn, ColumnType.BOOLEAN_IMAGE, _propertyAccess.defaultOption(), 60, false, true));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ChoiceMadeEventHandler _handler = null;

    public FilterGridInfo(ChoiceMadeEventHandler handlerIn) {

        _handler = handlerIn;
    }

    public void createGridComponents()
    {
        addCheckColumn();

        for (ColumnInfo<OptionBasics> myColumn : _columns) {
            addColumn(myColumn, _handler);
        }
    }

    protected ModelKeyProvider<OptionBasics> getKey() {

        return _propertyAccess.key();
    }

    public FilterGridInfo() {

        super();
    }
}
