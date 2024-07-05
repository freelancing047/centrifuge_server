package csi.client.gwt.edit_sources.presenters;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.dialogs.DataViewFromScratchSave;
import csi.client.gwt.csiwizard.dialogs.InstalledTableSave;
import csi.client.gwt.csiwizard.dialogs.TemplateFromScratchSave;
import csi.client.gwt.csiwizard.wizards.ResourceParameterWizard;
import csi.client.gwt.events.TransferCompleteEventHandler;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.Response;
import csi.server.common.dto.installed_tables.AdHocInstallRequest;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.interfaces.DataContainer;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;
import csi.server.common.util.Format;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by centrifuge on 5/18/2017.
 */
public class EditAdHocDataPresenter extends DataSourceEditorPresenter {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private DataContainer _container = null;
    private TransferCompleteEventHandler _handler = null;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public EditAdHocDataPresenter(AdHocDataSource requestIn, TransferCompleteEventHandler handlerIn) {

        super(requestIn);
        _handler = handlerIn;
    }

    @Override
    public boolean isFinal() {

        return false;
    }

    @Override
    public void saveResults(WizardDialog dialogIn) {

        try {

            _container = getResource();

            switch (_container.getResourceType()) {

                case DATAVIEW:

                    (new ResourceParameterWizard(dialogIn, (AdHocDataSource)_container, dialogIn.getDialogTitle(),
                                                    dialogIn.getDialogHelp(), _handler)).show();
                    break;

                case TEMPLATE:

                    (new TemplateFromScratchSave((AdHocDataSource)_container, dialogIn, dialogIn.getDialogTitle(),
                                                    dialogIn.getDialogHelp(), _handler)).show();
                    break;

                case DATA_TABLE:

                    (new ResourceParameterWizard(dialogIn, (AdHocDataSource)_container, dialogIn.getDialogTitle(),
                                                    dialogIn.getDialogHelp(), _handler)).show();
                    break;

                default:

                    throw new CentrifugeException("Unable to create "
                            + Format.value(_container.getResourceType()) + "using this wizard.");
            }

        } catch (Exception myException) {

            Display.error("EditAdHocDataPresenter", 1, myException);
        }
    }

    //
    // Request the list of required fields from the server
    //
    public void retrieveRequiredFieldList(VortexEventHandler<List<String>> handlerIn) {

        handlerIn.onSuccess(new ArrayList<>());
    }

    //
    // Request the list of required fields from the server
    //
    public void retrieveRequiredCoreFieldList(VortexEventHandler<List<String>> handlerIn) {

        handlerIn.onSuccess(new ArrayList<>());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

}
