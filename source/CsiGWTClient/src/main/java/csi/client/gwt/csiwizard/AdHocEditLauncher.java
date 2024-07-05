package csi.client.gwt.csiwizard;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.dialogs.ConnectorSelectionDialog;
import csi.client.gwt.csiwizard.dialogs.SourceEditDialog;
import csi.client.gwt.csiwizard.panels.FromScratchIntroPanel;
import csi.client.gwt.edit_sources.presenters.DataSourceEditorPresenter;
import csi.client.gwt.edit_sources.presenters.EditAdHocDataPresenter;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.events.TransferCompleteEvent;
import csi.client.gwt.events.TransferCompleteEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.DecisionDialog;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.ButtonDef;
import csi.client.gwt.widget.ui.uploader.FileSelectorDialog;
import csi.server.common.dto.FileUploadBlock;
import csi.server.common.dto.Response;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.service.api.TestActionsServiceProtocol;
import csi.server.common.service.api.UploadServiceProtocol;
import csi.server.common.util.AuthorizationObject;
import csi.server.common.util.Format;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by centrifuge on 9/5/2017.
 */
public class AdHocEditLauncher extends WizardDialog implements AdHocInterface {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static final List<ButtonDef> _yesNoButtons = new ArrayList<ButtonDef>(2);

    static {

        _yesNoButtons.add(new ButtonDef("No"));
        _yesNoButtons.add(new ButtonDef("Yes"));
    }

    private FromScratchIntroPanel _panel = null;

    private TransferCompleteEventHandler _externalHandler = null;
    private AclResourceType _resourceType;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ChoiceMadeEventHandler handleNewInstallChoice = new ChoiceMadeEventHandler() {
        @Override
        public void onChoiceMade(ChoiceMadeEvent eventIn) {

            if (1 == eventIn.getChoice()) {

                cancel();
            }
        }
    };

    private TransferCompleteEventHandler transferComplete = new TransferCompleteEventHandler() {
        @Override
        public void onTransferComplete(TransferCompleteEvent eventIn) {

            final String myTable = eventIn.getItemName();

            if (null != _externalHandler) {

                _externalHandler.onTransferComplete(new TransferCompleteEvent(myTable));
            }
            if ((null != myTable) && AclResourceType.DATA_TABLE.equals(_resourceType)) {

                DeferredCommand.add(new Command() {
                    public void execute() {

                        promptForMore(myTable);
                    }
                });
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public static void open(AclResourceType resourceTypeIn) {

        try {

            (new AdHocEditLauncher(resourceTypeIn, null)).show();

        } catch (Exception myException) {

            Display.error("AdHocEditLauncher", 1, myException);
        }
    }

    public AdHocEditLauncher(AclResourceType resourceTypeIn, TransferCompleteEventHandler handlerIn)
            throws CentrifugeException {

        super(new FromScratchIntroPanel(null, null),
                _constants.fromScratchWizard_DialogTitle(resourceTypeIn.getLabel()),
                _constants.fromScratchWizard_HelpTarget(resourceTypeIn.name()),
                _constants.fromScratchWizard_ModeSelectionPanel(Dialog.txtNextButton));

        _panel = (FromScratchIntroPanel)super.getCurrentPanel();
        _resourceType = resourceTypeIn;
        _externalHandler = handlerIn;
    }

    public void restoreCaller() {

        if (null != _externalHandler) {

            _externalHandler.onTransferComplete(new TransferCompleteEvent((String)null));
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

            if (_panel.useWizard()) {

                if (AclResourceType.DATA_TABLE.equals(_resourceType)) {

                    (new FileSelectorDialog(this, transferComplete, null)).show();

                } else {

                    (new ConnectorSelectionDialog(_resourceType, this,
                            _constants.fromScratchWizard_DialogTitle(_resourceType.getLabel()),
                            _constants.fromScratchWizard_HelpTarget(_resourceType.name()))).show();
                }

            } else {

                SourceEditDialog myDialog = null;
                DataSourceEditorPresenter mySourceEditPresenter
                        = new EditAdHocDataPresenter(new AdHocDataSource(_resourceType, null, new CapcoInfo(),
                                                                            new SecurityTagsInfo()),
                                                     transferComplete);

                try {

                    myDialog = WebMain.injector.getMainPresenter().createSourceEditDialog(mySourceEditPresenter, this,
                                                                            getDialogTitle(), getDialogHelp(), null);
                    myDialog.show();

                } catch (Exception myException) {

                    if (null != myDialog) {

                        myDialog.abort(myException);

                    } else {

                        Display.error("AdHocEditLauncher", 3, myException);
                    }
                }
            }

        } catch (Exception myException) {

            Display.error("AdHocEditLauncher", 4, myException);
        }
    }

    private void promptForMore(final String tableIn) {

        DeferredCommand.add(new Command() {
            public void execute() {

                Dialog myChoiceDialog
                        = new DecisionDialog(_constants.installFileWizard_Success_Message(Format.value(tableIn)),
                        _constants.installFileWizard_NewTableOption(),
                        _yesNoButtons, handleNewInstallChoice);
                myChoiceDialog.getCancelButton().setVisible(false);
                myChoiceDialog.show(70);
            }
        });
    }
}
