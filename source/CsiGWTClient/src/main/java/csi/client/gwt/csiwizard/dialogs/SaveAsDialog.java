package csi.client.gwt.csiwizard.dialogs;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardControl;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.installed_tables.TableInstallResponse;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.DataViewActionServiceProtocol;

import java.util.List;

/**
 * Created by centrifuge on 5/8/2018.
 */
public class SaveAsDialog extends WizardDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private final String _txtSelectionPrompt
            = _constants.restrictedResourceSaveAsDialog_InfoString(AclResourceType.DATA_TABLE.getLabel());

    private ResourceSelectorPanel.SelectorMode _mode;
    private String _prefix;
    private boolean _ready = false;
    private boolean _showRequested = false;
    private WizardControl _controlDialog;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    ResourceSelectorPanel<ResourceBasics> panel;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SaveAsDialog(WizardControl controlDialogIn, String titleIn, String helpIn, String instructionsIn,
                        ResourceSelectorPanel.SelectorMode modeIn, String prefixIn) {

        this(controlDialogIn, titleIn, helpIn, instructionsIn, modeIn, prefixIn, null);
    }

    public SaveAsDialog(WizardControl controlDialogIn, String titleIn, String helpIn, String instructionsIn,
                        ResourceSelectorPanel.SelectorMode modeIn, String prefixIn, String promptIn) {

        super(controlDialogIn, new ResourceSelectorPanel(null, modeIn, prefixIn), titleIn, helpIn, instructionsIn);

        _controlDialog = controlDialogIn;
        _mode = modeIn;
        _prefix = prefixIn;

        panel = (ResourceSelectorPanel<ResourceBasics>)getCurrentPanel();
        panel.initializeDisplay(AclResourceType.DATA_TABLE, (null != promptIn) ? promptIn : _txtSelectionPrompt);
    }

    public void setSingleList(List<ResourceBasics> listIn) {

        panel.handleSingleListRequestResponse.onSuccess(listIn);
        _ready = true;
        if (_showRequested) {

            super.show();
        }
    }

    @Override
    public void show() {

        if (_ready) {

            super.show();

        } else {

            _showRequested = true;
        }
    }

    public ResourceBasics getSelection() {

        return panel.getSelection();
    }

    public String getKey() {

        return panel.getKey();
    }

    public String getName() {

        return panel.getName();
    }

    public String getRemarks() {

        return panel.getRemarks();
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

        _controlDialog.clickComplete();
    }
}
