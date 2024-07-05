package csi.client.gwt.mainapp;

import java.util.Arrays;
import java.util.List;

import csi.client.gwt.WebMain;
import csi.client.gwt.csi_resource.ResourceSelectorDialog;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel.SelectorMode;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewInNewTab;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.events.OpenDataViewEvent;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.DecisionDialog;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.ButtonDef;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.service.api.ModelActionsServiceProtocol;


public class OpenDataviewDialog extends ResourceSelectorDialog {
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                             GUI Objects from the XML File                              //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String _txtHelpTarget = _constants.openDataviewDialog_HelpTarget();
    private static final String _txtInfoString = _constants.openDataviewDialog_InfoString(Dialog.txtOpenButton);
    private static final String _txtChoiceDialogNewButtonString = _constants.openDataviewDialog_Choice_NewButtonString();
    private static final String _txtChoiceDialogCurrentButtonString = _constants.openDataviewDialog_Choice_CurrentButtonString();
    private static final String _txtChoiceDialogTitle = _constants.openDataviewDialog_Choice_DialogTitle();
    private static final String _txtChoiceInfoString = _constants.openDataviewDialog_Choice_InfoString(_txtChoiceDialogNewButtonString, _txtChoiceDialogCurrentButtonString);

    private static final List<ButtonDef> _buttonList = Arrays.asList(
        
        new ButtonDef(_txtChoiceDialogCurrentButtonString),
        new ButtonDef(_txtChoiceDialogNewButtonString)
    );
    
    private ResourceBasics _selection = null;
    private AbstractDataViewPresenter _presenter = null;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle choice being made between current and new browser window
    //
    private ChoiceMadeEventHandler handleChoiceMadeEvent
    = new ChoiceMadeEventHandler() {
        @Override
        public void onChoiceMade(ChoiceMadeEvent eventIn) {

            try {

                switch (eventIn.getChoice()) {

                    case 1:
                        DataViewInNewTab.open(_selection.getUuid(), _selection.getName(), _selection.getOwner());
                        break;
                    case 2:
                        WebMain.injector.getEventBus().fireEvent(new OpenDataViewEvent(_selection.getUuid()));
                        break;
                }
                _selection = null;
                _presenter = null;
                destroy();

            } catch (Exception myException) {

                Dialog.showException("OpenDataviewDialog", myException);
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


 
    public OpenDataviewDialog(AbstractDataViewPresenter presenterIn) {

        super(SelectorMode.READ_ONLY);

        try {

            _presenter = presenterIn;

            retrieveDisplayList();

        } catch (Exception myException) {

            Dialog.showException("OpenDataviewDialog", myException);
        }
    }

    public void show() {

        try {

            show(AclResourceType.DATAVIEW, _txtHelpTarget, _txtInfoString, _constants.dialog_OpenButton());

        } catch (Exception myException) {

            Dialog.showException("OpenDataviewDialog", myException);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void handleSelectionMade(ResourceBasics selectionIn, boolean forceOverwriteIn) {
        
        if (selectionIn instanceof ResourceBasics) {

            WebMain.injector.getMainPresenter().beginOpenDataView(selectionIn);
                
            _selection = null;
            _presenter = null;
            destroy();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Request data from the server to initialize the list dataviews to prevent naming conflicts
    //
    private void retrieveDisplayList() {
        VortexFuture<List<ResourceBasics>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            ResourceFilter myFilter = WebMain.injector.getMainPresenter().getDefaultResourceFilter();

            myVortexFuture.addEventHandler(handleFilteredListRequestResponse);
            myVortexFuture.execute(ModelActionsServiceProtocol.class).getFilteredResourceList(
                                                        AclResourceType.DATAVIEW, myFilter, AclControlType.READ);
            showWatchBox();

        } catch (Exception myException) {

            hideWatchBox();
            Dialog.showException(myException);
        }
    }
}
