package csi.client.gwt.csiwizard.dialogs;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardControl;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.panels.ResourceSelectorPanel;
import csi.client.gwt.csiwizard.panels.SingleEntryWizardPanel;
import csi.client.gwt.csiwizard.widgets.RadioSelectionWidget;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.input_boxes.ValidityCheck;
import csi.server.common.util.ValuePair;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.installed_tables.TableInstallResponse;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.service.api.UploadServiceProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by centrifuge on 5/8/2018.
 */
public class ExtractTableDialog extends WizardDialog implements WizardControl, ValidityCheck {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final String myResourceLabel = AclResourceType.DATA_TABLE.getLabel();
    private static final String _nextButton = _constants.dialog_NextButton();
    private static final String _createtButton = _constants.dialog_CreateButton();
    private static final String _updateButton = _constants.dialog_UpdateButton();
    private static final String _txtFailureDialogTitle = _constants.resourceSaveAsDialog_FailureTitle(myResourceLabel);
    private static final String _txtSuccessResponseTitle = _constants.resourceSaveAsDialog_SuccessTitle(myResourceLabel);
    private static final String _txtTitle = _constants.extractTableDialog_Title();
    private static final String _txtHelp = null;
    private static final String _txtNewTable = _constants.extractTableDialog_CreateTable();
    private static final String _txtTableUpdate = _constants.extractTableDialog_UpdateTable();
    private static final String _txtBasicInstructions
                                = _constants.extractTableDialog_BasicInstructions(_txtNewTable, _txtTableUpdate, _nextButton);
    private static final String _txtIdentifyInstructions = _constants.extractTableDialog_FieldSelectionInstructions(_nextButton);
    private static final String _txtMapInstructions = _constants.extractTableDialog_FieldMappingInstructions(_updateButton);
    private static final String _txtCreateInstructions = _constants.extractTableDialog_CreateInstructions(_createtButton);
    private static final String _txtUpdateInstructions = _constants.extractTableDialog_UpdateInstructions(_nextButton);

    private static final String[] _radioPair = new String[] {_txtNewTable, _txtTableUpdate};

    private ResourceSelectorPanel.SelectorMode _mode = null;
    private String _dataViewId;
    private String _vizId;
    private String _tableId = null;
    private String _tableName = null;
    private String _remarks = null;
    private Selection _selection;
    private List<ResourceBasics> _overwriteList = null;
    private List<ResourceBasics> _selectionList = null;
    private List<FieldDef> _fullFieldList = null;
    private List<FieldDef> _finalFieldList = null;
    private List<ValuePair<InstalledColumn, FieldDef>> _pairedList = null;
    private List<InstalledColumn> _columnList = null;
    private int _step = 0;
    private boolean _waiting = false;
    private boolean _readyForRead = false;
    private boolean _readyForWrite = false;
    private boolean _readyForMap = false;

    private SingleEntryWizardPanel panel;
    private RadioSelectionWidget widget;
    private Object childDialog = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private VortexEventHandler<Response<String, TableInstallResponse>> handleSaveAsResponse
            = new AbstractVortexEventHandler<Response<String, TableInstallResponse>>() {

        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                hideWatchBox();

                // Display error message.
                Display.error(_txtFailureDialogTitle, exceptionIn);

            } catch (Exception myException) {

                Display.error("ExtractTableDialog", 1, myException);
            }
            return false;
        }

        @Override
        public void onSuccess(Response<String, TableInstallResponse> responseIn) {

            try {

                hideWatchBox();

                if (ResponseHandler.isSuccess(responseIn)) {

                    TableInstallResponse myResult = responseIn.getResult();

                    Display.success(_txtSuccessResponseTitle,
                            _constants.resourceSaveAsDialog_SuccessMessage(myResourceLabel, myResult.getTable().getTablePath()));
                    destroy();
                }

            } catch (Exception myException) {

                Display.error("ExtractTableDialog", 2, myException);
            }
        }
    };

    private VortexEventHandler<List<FieldDef>> handleFieldListResponse
            = new AbstractVortexEventHandler<List<FieldDef>>() {

        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                hideWatchBox();

                // Display error message.
                Display.error(_txtFailureDialogTitle, exceptionIn);

            } catch (Exception myException) {

                Display.error("ExtractTableDialog", 1, myException);
            }
            return false;
        }

        @Override
        public void onSuccess(List<FieldDef> listIn) {

            try {

                hideWatchBox();

                _fullFieldList = listIn;
                if (_waiting && ResourceSelectorPanel.SelectorMode.NEW.equals(_mode)) {

                    clickComplete();
                }

            } catch (Exception myException) {

                Display.error("ExtractTableDialog", 2, myException);
            }
        }
    };

    private VortexEventHandler<List<ResourceBasics>> handleOverwriteListResponse
            = new AbstractVortexEventHandler<List<ResourceBasics>>() {

        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                hideWatchBox();

                // Display error message.
                Display.error(_txtFailureDialogTitle, exceptionIn);

            } catch (Exception myException) {

                Display.error("ExtractTableDialog", 1, myException);
            }
            return false;
        }

        @Override
        public void onSuccess(List<ResourceBasics> listIn) {

            try {

                hideWatchBox();

                _overwriteList = listIn;
                _readyForWrite = true;
                if (_waiting) {

                    execute();
                }

            } catch (Exception myException) {

                Display.error("ExtractTableDialog", 2, myException);
            }
        }
    };

    private VortexEventHandler<List<ResourceBasics>> handleSelectionListResponse
            = new AbstractVortexEventHandler<List<ResourceBasics>>() {

        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                hideWatchBox();

                // Display error message.
                Display.error(_txtFailureDialogTitle, exceptionIn);

            } catch (Exception myException) {

                Display.error("ExtractTableDialog", 1, myException);
            }
            return false;
        }

        @Override
        public void onSuccess(List<ResourceBasics> listIn) {

            try {

                hideWatchBox();

                _selectionList = listIn;
                _readyForRead = true;
                if (_waiting) {

                    execute();
                }

            } catch (Exception myException) {

                Display.error("ExtractTableDialog", 2, myException);
            }
        }
    };

    private VortexEventHandler<List<InstalledColumn>> handleColumnListResponse
            = new AbstractVortexEventHandler<List<InstalledColumn>>() {

        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                hideWatchBox();

                // Display error message.
                Display.error(_txtFailureDialogTitle, exceptionIn);

            } catch (Exception myException) {

                Display.error("ExtractTableDialog", 1, myException);
            }
            return false;
        }

        @Override
        public void onSuccess(List<InstalledColumn> listIn) {

            try {

                hideWatchBox();

                _columnList = listIn;
                _readyForMap = true;
                if (_waiting) {

                    execute();
                }

            } catch (Exception myException) {

                Display.error("ExtractTableDialog", 2, myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ExtractTableDialog(String dataviewIdIn, String vizIdIn, Selection selectionIn) throws CentrifugeException {

        super(new SingleEntryWizardPanel(null, "RadioPanel", new RadioSelectionWidget(_radioPair, 0, null)),
                _txtTitle, null, _txtBasicInstructions);

        panel = (SingleEntryWizardPanel)getCurrentPanel();
        widget = (RadioSelectionWidget)panel.getInputWidget();
        _dataViewId = dataviewIdIn;
        _vizId = vizIdIn;
        _selection = selectionIn;

        retrieveFieldList();
    }

    @Override
    public void clickComplete() {

        execute();
    }

    public void show() {

        redisplay();
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

        _waiting = false;
        _mode = (0 == widget.getChoice())
                ? ResourceSelectorPanel.SelectorMode.NEW
                : ResourceSelectorPanel.SelectorMode.READ_ONLY;

        if (ResourceSelectorPanel.SelectorMode.NEW.equals(_mode)) {

            performCreate();

        } else {

            performUpdate();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Private Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void redisplay() {

        _waiting = false;
        if (ResourceSelectorPanel.SelectorMode.NEW.equals(_mode)) {

            if (4 == _step) {

                _tableName = null;
                _remarks = null;
                _step = 1;
               execute();

            } else {

                _finalFieldList = null;
                _step = 0;
                super.show();
            }

        } else {

            if (4 == _step) {

                _tableId = null;
                _tableName = null;
                _columnList = null;
                _readyForMap = false;
                _step = 1;
                execute();

            } else {

                _step = 0;
                super.show();
            }
        }
    }

    private void performCreate() {

        switch (_step) {

            case 0:

                if (!_readyForWrite) {

                    retrieveOverwriteLists();
                }
                _step = 1;
                // Continue to next Step

            case 1:

                childDialog = new IdentifyCaptureFieldsDialog(this, _txtTitle, _txtHelp,
                                                                _txtIdentifyInstructions, _fullFieldList);
                ((IdentifyCaptureFieldsDialog)childDialog).show(_createtButton, false);
                _step = 2;
                break;

            case 2:

                _finalFieldList = ((IdentifyCaptureFieldsDialog)childDialog).getResults();
                _step = 3;
                // Continue to next Step

            case 3:

                if (_readyForWrite) {

                    _waiting = false;
                    // Assign name to new Installed Table
                    String myPrefix = WebMain.injector.getMainPresenter().getUserInfo().getName()
                            + "." + CsiFileType.DATAVIEW.getExtension() + ".";
                    childDialog = new SaveAsDialog(this, _txtTitle, _txtHelp,
                                                    _txtCreateInstructions, _mode, myPrefix);
                    ((SaveAsDialog)childDialog).setSingleList(_overwriteList);
                    ((SaveAsDialog)childDialog).show(_createtButton, true);
                    _step = 4;

                } else {

                    _waiting = true;
                }
                break;

            case 4:

                _tableName = ((SaveAsDialog)childDialog).getName();
                _remarks = ((SaveAsDialog)childDialog).getRemarks();
                createTable();
                _step = 5;
                break;
        }
    }

    private void performUpdate() {

        switch (_step) {

            case 0:

                if (!_readyForRead) {

                    retrieveSelectionList();
                }
                _step = 1;
                // Continue to next Step

            case 1:

                if (_readyForRead) {

                    _waiting = false;
                    // Select Installed Table to update
                    childDialog = new SaveAsDialog(this, _txtTitle, _txtHelp, _txtUpdateInstructions, _mode, null,
                                                    _constants.resourceSaveAsDialog_InstalledTableInfoString());
                    ((SaveAsDialog) childDialog).setSingleList(_selectionList);
                    ((SaveAsDialog) childDialog).show(_updateButton, false);
                    _step = 2;

                } else {

                    _waiting = true;
                }
                break;

            case 2:

                _tableId = ((SaveAsDialog)childDialog).getKey();
                _tableName = ((SaveAsDialog)childDialog).getName();
                retrieveColumnList();
                _step = 3;
                // Continue to next Step

            case 3:

                if (_readyForMap) {

                    _waiting = false;
                    childDialog = new MapCapturedFieldsDialog(this, _txtTitle, _txtHelp,
                                                                _txtMapInstructions, _columnList, _fullFieldList);
                    ((MapCapturedFieldsDialog)childDialog).show(_updateButton, true);
                    _step = 4;

                } else {

                    _waiting = true;
                }
                break;

            case 4:

                _pairedList = ((MapCapturedFieldsDialog)childDialog).getResults();
                updateTable();
                _step = 5;
                break;
        }
    }

    private void createTable() {

        VortexFuture<Response<String, TableInstallResponse>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        showWatchBox();
        try {

            myVortexFuture.addEventHandler(handleSaveAsResponse);
            myVortexFuture.execute(DataViewActionServiceProtocol.class).spawnTable(_tableName, _remarks, _dataViewId,
                                                                                    _vizId, _selection,
                                                                                    new ArrayList<FieldDef>(_finalFieldList));

        } catch (Exception myException) {

            hideWatchBox();
            Display.error(_constants.serverRequest_FailureDialogTitle(), myException);
        }
    }

    private void updateTable() {

        VortexFuture<Response<String, TableInstallResponse>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        showWatchBox();
        try {

            myVortexFuture.addEventHandler(handleSaveAsResponse);
            myVortexFuture.execute(DataViewActionServiceProtocol.class).updateTable(_tableId, _dataViewId,
                                                                                    _vizId, _selection, _pairedList);

        } catch (Exception myException) {

            hideWatchBox();
            Display.error(_constants.serverRequest_FailureDialogTitle(), myException);
        }
    }

    private void retrieveFieldList() {

        VortexFuture<List<FieldDef>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        showWatchBox();
        try {

            myVortexFuture.addEventHandler(handleFieldListResponse);
            myVortexFuture.execute(DataViewActionServiceProtocol.class).getFullFieldList(_dataViewId);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    private void retrieveOverwriteLists() {

        VortexFuture<List<ResourceBasics>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        showWatchBox();
        try {

            myVortexFuture.addEventHandler(handleOverwriteListResponse);
            myVortexFuture.execute(UploadServiceProtocol.class).getTableOverWriteControlList(CsiFileType.DATAVIEW);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    private void retrieveSelectionList() {

        VortexFuture<List<ResourceBasics>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        showWatchBox();
        try {
            //
            // !! Not currently bringing back names of templates we cannot access !!
            //
            showWatchBox();
            myVortexFuture.addEventHandler(handleSelectionListResponse);
            myVortexFuture.execute(UploadServiceProtocol.class).getTableSelectionList(CsiFileType.DATAVIEW);

        } catch (Exception myException) {

            hideWatchBox();
            Display.error(_constants.serverRequest_FailureDialogTitle(), myException);
        }
    }

    private void retrieveColumnList() {

        showWatchBox();
        VortexFuture<List<InstalledColumn>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(handleColumnListResponse);
            myVortexFuture.execute(UploadServiceProtocol.class).getTableColumnList(_tableId);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }
}
