package csi.client.gwt.csiwizard.dialogs;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.csiwizard.wizards.ResourceParameterWizard;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;

import java.util.List;

/**
 * Created by centrifuge on 6/14/2017.
 */
public class DataViewFromTemplateDialog extends WizardDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    ResourceSelectorPanel panel = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    private static final String _txtWizardTitle = _constants.dataviewFromTemplateWizard_DialogTitle();
    private static final String _txtHelpTarget = _constants.dataviewFromTemplateWizard_HelpTarget();
    private static final String _txtTemplateSelectionInfo = _constants.dataviewFromTemplateWizard_InfoString();
    private static final String _txtRetrievingTemplates = _constants.RetrievingTemplates();
    protected static final String _txtFailureDialogTitle = _constants.serverRequest_FailureDialogTitle();

    private WizardInterface _callBack = this;
    private ResourceBasics _template = null;
    private boolean _sample = false;


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

    //
    //  Create a DataView or sample DataView from the splash page or a menu selection
    //
    public DataViewFromTemplateDialog(boolean sample) {

        super(new ResourceSelectorPanel(null, ResourceSelectorPanel.SelectorMode.READ_ONLY),
                _constants.dataviewFromTemplateWizard_DialogTitle(),
                _constants.dataviewFromTemplateWizard_HelpTarget(),
                _constants.dataviewFromTemplateWizard_TemplatePanel(Dialog.txtNextButton));

        try{

            _sample = sample;
            panel = (ResourceSelectorPanel)super.getCurrentPanel();
            panel.initializeDisplay(_sample ? AclResourceType.SAMPLE : AclResourceType.TEMPLATE,
                                        _txtTemplateSelectionInfo);

        } catch (Exception myException) {

            Display.error("DataViewFromTemplateDialog", 1, myException);
        }
    }

    //
    //  Create a DataView from an identified template.
    //
    public DataViewFromTemplateDialog(String uuidIn) {

        super(new ResourceSelectorPanel(null, ResourceSelectorPanel.SelectorMode.READ_ONLY),
                _constants.dataviewFromTemplateWizard_DialogTitle(),
                _constants.dataviewFromTemplateWizard_HelpTarget(),
                _constants.dataviewFromTemplateWizard_TemplatePanel(Dialog.txtNextButton));

        try{

            panel = (ResourceSelectorPanel)super.getCurrentPanel();
            panel.initializeDisplay(AclResourceType.TEMPLATE, _txtTemplateSelectionInfo);

        } catch (Exception myException) {

            Display.error("DataViewFromTemplateDialog", 2, myException);
        }
    }

    @Override
    public void show(){

        try{

            super.show();
            retrieveTemplateList();

        } catch (Exception myException) {

            Display.error("DataViewFromTemplateDialog", 3, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void createPanel() {

    }

    @Override
    protected void execute() {

        try{

            _template = (ResourceBasics)panel.getSelection();
            (new ResourceParameterWizard(_callBack, _template, _txtWizardTitle, _txtHelpTarget, _sample)).show();

        } catch (Exception myException) {

            Display.error("DataViewFromTemplateDialog", 4, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Request the list templates from the server which the user can use
    //
    private void retrieveTemplateList() {

        VortexFuture<List<ResourceBasics>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {
            myVortexFuture.addEventHandler(panel.handleSingleListRequestResponse);
            if(_sample){
                myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).listSampleBasics(AclControlType.READ);
            }
            else {
                myVortexFuture.execute(DataViewDefActionsServiceProtocol.class).listTemplateBasics(AclControlType.READ);
            }
            showWatchBox(_txtRetrievingTemplates);

        } catch (Exception myException) {

            Display.error("DataViewFromTemplateDialog", 5, myException);
        }
    }
}
