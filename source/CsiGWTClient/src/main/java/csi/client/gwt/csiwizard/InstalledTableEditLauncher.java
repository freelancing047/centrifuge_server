package csi.client.gwt.csiwizard;

        import com.google.gwt.user.client.Command;
        import com.google.gwt.user.client.DeferredCommand;
        import csi.client.gwt.WebMain;
        import csi.client.gwt.csiwizard.dialogs.ConnectorSelectionDialog;
        import csi.client.gwt.csiwizard.dialogs.SourceEditDialog;
        import csi.client.gwt.csiwizard.panels.FromScratchIntroPanel;
        import csi.client.gwt.csiwizard.panels.TableUpdatePanel;
        import csi.client.gwt.edit_sources.presenters.DataSourceEditorPresenter;
        import csi.client.gwt.edit_sources.presenters.EditAdHocDataPresenter;
        import csi.client.gwt.events.ChoiceMadeEvent;
        import csi.client.gwt.events.ChoiceMadeEventHandler;
        import csi.client.gwt.events.TransferCompleteEvent;
        import csi.client.gwt.events.TransferCompleteEventHandler;
        import csi.client.gwt.i18n.CentrifugeConstants;
        import csi.client.gwt.i18n.CentrifugeConstantsLocator;
        import csi.client.gwt.util.Display;
        import csi.client.gwt.util.ResponseHandler;
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
        import csi.server.common.util.ValuePair;

        import java.util.ArrayList;
        import java.util.List;

/**
 * Created by centrifuge on 9/5/2017.
 */
public class InstalledTableEditLauncher extends WizardDialog implements AdHocInterface {


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

    private TableUpdatePanel _panel = null;

    private TransferCompleteEventHandler _externalHandler = null;
    private AclResourceType _resourceType;
    private InstalledTable _table = null;
    private boolean _waiting = false;
    private boolean _ready = false;


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

    private VortexEventHandler<Response<String, InstalledTable>> handleRetrievalResponse
            = new AbstractVortexEventHandler<Response<String, InstalledTable>>() {
        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);

            return false;
        }
        @Override
        public void onSuccess(Response<String, InstalledTable> responseIn) {

            _table = responseIn.getResult();
            _panel.attachTable(_table);
            _ready = true;
            if (_waiting) {

                show();
            }
        }
    };

    private VortexEventHandler<Response<String, ValuePair<Boolean, InstalledTable>>> handleUpdateResponse
            = new VortexEventHandler<Response<String, ValuePair<Boolean, InstalledTable>>>() {
        @Override
        public void onSuccess(Response<String, ValuePair<Boolean, InstalledTable>> resultIn) {

            try {

                if (ResponseHandler.isSuccess(resultIn)) {

                    ValuePair<Boolean, InstalledTable> myResult = resultIn.getResult();

                    if (null != myResult) {

                        InstalledTable myTable = myResult.getValue2();

                        if (myResult.getValue1()) {

                            Display.success(_constants.installedTableUpdate_SuccessMessage(Format.value(myTable.getTablePath())));

                        } else {

                            switch (myTable.getFileType()) {

                                case ADHOC:

                                    Display.error(_constants.unexpectedError());
                                    break;

                                case DATAVIEW:

                                    Display.error(_constants.installedTableUpdate_IncorrectMethodTitle(),
                                            _constants.installedTableUpdate_UseCapture(Format.value(myTable.getDriver()),
                                                    Format.value(_constants.menuKeyConstants_spawn()),
                                                    Format.value(_constants.extractTableDialog_UpdateTable()),
                                                    Format.value(Dialog.txtNextButton),
                                                    Format.value(myTable.getTablePath()),
                                                    Format.value(Dialog.txtFinishButton)), true, true, true);
                                    break;

                                default:

                                    Display.error(_constants.notSupportedTitle(),
                                            _constants.installedTableUpdate_NotSupportedMessage());
                                    break;
                            }
                        }

                    } else {

                        Display.error(_constants.unexpectedNullFromServer());
                    }
                }

            } catch (Exception myException) {

                Display.error("DataTableTab", 1, myException);
            }
        }

        @Override
        public boolean onError(Throwable myException) {

            Display.error("Data Update Failed!", myException);

            return false;
        }

        @Override
        public void onUpdate(int taskProgess, String taskMessage) {

        }

        @Override
        public void onCancel() {

        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public InstalledTableEditLauncher(AclResourceType resourceTypeIn, String reloadIn,
                                      TransferCompleteEventHandler handlerIn)
            throws CentrifugeException {

        super(new TableUpdatePanel(null, null),
                _constants.fromScratchWizard_DialogTitle(resourceTypeIn.getLabel()),
                _constants.fromScratchWizard_HelpTarget(resourceTypeIn.name()),
                _constants.fromScratchWizard_ModeSelectionPanel(Dialog.txtNextButton));

        _panel = (TableUpdatePanel)super.getCurrentPanel();
        _resourceType = resourceTypeIn;
        _externalHandler = handlerIn;
        retrieveInstalledTable(reloadIn);
    }

    public void show() {

        if (_ready) {

            super.show();

        } else {

            _waiting = true;
        }
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

                (new FileSelectorDialog(this, transferComplete, _table)).show();

            } else if (_panel.defineDataSource()) {

                AdHocDataSource myDataSource = _table.getSourceDefinition();
                SourceEditDialog myDialog = null;
                DataSourceEditorPresenter mySourceEditPresenter
                        = new EditAdHocDataPresenter(myDataSource, transferComplete);

                try {

                    myDialog = WebMain.injector.getMainPresenter().createSourceEditDialog(mySourceEditPresenter, this, getDialogTitle(), getDialogHelp(), null);
                    myDialog.show();

                } catch (Exception myException) {

                    if (null != myDialog) {

                        myDialog.abort(myException);

                    } else {

                        Display.error("AdHocEditLauncher", 2, myException);
                    }
                }

            } else {

                VortexFuture<Response<String, ValuePair<Boolean, InstalledTable>>> myVortexFuture
                        = WebMain.injector.getVortex().createFuture();

                try {

                    myVortexFuture.addEventHandler(handleUpdateResponse);
                    myVortexFuture.execute(UploadServiceProtocol.class).updateInstalledTable(_table.getUuid(),
//                        List < AuthDO > credentialsIn,
//                        List < LaunchParam > parametersIn);
                            null, null);
                    showWatchBox();

                } catch (Exception myException) {

                    Display.error("AdHocEditLauncher", 3, myException);
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

    private void retrieveInstalledTable(String reloadIdIn) {

        VortexFuture<Response<String, InstalledTable>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        showWatchBox();
        myVortexFuture.addEventHandler(handleRetrievalResponse);

        try {
            myVortexFuture.execute(UploadServiceProtocol.class).getInstalledTable(reloadIdIn);

        } catch (Exception myException) {

            hideWatchBox();
            Dialog.showException(myException);
        }
    }
}
