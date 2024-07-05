package csi.client.gwt.csiwizard.dialogs;

import csi.client.gwt.csiwizard.wizards.DataSourceWizard;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.DataSourceDef;

/**
 * Created by centrifuge on 6/23/2017.
 */
public class DataSourceDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private DataChangeEventHandler _finishHandler = null;
    private DataSourceDef _dataSource = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DataSourceDialog(DataChangeEventHandler handlerIn) {

        _finishHandler = handlerIn;
    }

    public DataSourceDialog(DataChangeEventHandler handlerIn, DataSourceDef dataSourceIn) {

        _finishHandler = handlerIn;
        _dataSource = dataSourceIn;
    }

    public void show() {

        if(null != _dataSource) {

            (new DataSourceWizard(_constants.newSourceWizard_DialogTitle(),
                                    _constants.fromScratchWizard_HelpTarget(AclResourceType.DATAVIEW.getLabel()),
                                    _dataSource, _finishHandler)).show();

        } else if (_finishHandler != null) {

            (new ConnectorSelectionDialog(_constants.newSourceWizard_DialogTitle(),
                    _constants.fromScratchWizard_HelpTarget(AclResourceType.DATAVIEW.name()), _finishHandler)).show();
        }
    }
}
