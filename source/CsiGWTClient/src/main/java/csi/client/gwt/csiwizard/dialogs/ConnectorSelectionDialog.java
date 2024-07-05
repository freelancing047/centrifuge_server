package csi.client.gwt.csiwizard.dialogs;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.csiwizard.wizards.DataSourceWizard;
import csi.client.gwt.events.DataChangeEvent;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.SelectionListData.DriverBasics;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.dataview.AdHocDataSource;

/**
 * Created by centrifuge on 6/21/2017.
 */
public class ConnectorSelectionDialog extends WizardDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final String _txtConnectorSelectionInfo = _constants.dataviewFromScratchWizard_InfoString();

    private DataChangeEventHandler _finishHandler = null;
    private AclResourceType _targetResource = null;


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

    public ConnectorSelectionDialog(String titleIn, String helpIn, DataChangeEventHandler handlerIn) {

        super(new ResourceSelectorPanel<DriverBasics>(null, ResourceSelectorPanel.SelectorMode.READ_ONLY, true),
                titleIn, helpIn, _constants.dataviewFromScratchWizard_ConnectorPanel(Dialog.txtNextButton));

        try{

            _finishHandler = handlerIn;

        } catch (Exception myException) {

            Display.error("ConnectorSelectionDialog", 1, myException);
        }
    }

    public ConnectorSelectionDialog(AclResourceType targetResourceIn, WizardInterface priorDialogIn, String titleIn, String helpIn) {

        super(priorDialogIn, new ResourceSelectorPanel<DriverBasics>(null, ResourceSelectorPanel.SelectorMode.READ_ONLY, true),
                titleIn, helpIn, _constants.dataviewFromScratchWizard_ConnectorPanel(Dialog.txtNextButton));
        _targetResource = targetResourceIn;
        try{

        } catch (Exception myException) {

            Display.error("ConnectorSelectionDialog", 1, myException);
        }
    }

    @Override
    protected void createPanel() {

        ResourceSelectorPanel myPanel = (ResourceSelectorPanel)super.getCurrentPanel();
        WebMain.injector.getMainPresenter().retrieveConnectorList(myPanel.handleSingleListRequestResponse, true);
        myPanel.initializeDisplay(AclResourceType.CONNECTION, _txtConnectorSelectionInfo);
    }

    @Override
    public void cancel() {
        super.cancel();
        if (null != _finishHandler) {

            _finishHandler.onDataChange(new DataChangeEvent(null));
        }
    }

    @Override
    protected void execute() {

        try{

            DriverBasics mySelection = (DriverBasics)((ResourceSelectorPanel)super.getCurrentPanel()).getSelection();

            if (null != _finishHandler) {

                (new DataSourceWizard(this, getDialogTitle(), getDialogHelp(), mySelection, _finishHandler)).show();

            } else {

                (new DataSourceWizard(_targetResource, this, getDialogTitle(), getDialogHelp(), mySelection)).show();
            }

        } catch (Exception myException) {

            Display.error("ConnectorSelectionDialog", 2, myException);
        }
    }
}
