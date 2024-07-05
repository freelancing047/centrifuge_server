package csi.client.gwt.csi_resource;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.common.base.Joiner;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.panels.SingleEntryWizardPanel;
import csi.client.gwt.csiwizard.widgets.FileSelectorButtonWidget;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.file_access.uploader.ClientUtil;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.CsiModal;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.ui.ProgressDialog;
import csi.server.common.dto.FileUploadBlock;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.installed_tables.TableInstallResponse;
import csi.server.common.dto.resource.*;
import csi.server.common.dto.Response;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.service.api.DataViewDefActionsServiceProtocol;
import csi.server.common.service.api.ModelActionsServiceProtocol;
import csi.server.common.service.api.UploadServiceProtocol;
import csi.server.common.util.Format;
import csi.server.common.util.StringUtil;
import csi.server.common.util.ValuePair;
import org.vectomatic.file.Blob;
import org.vectomatic.file.File;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by centrifuge on 4/16/2019.
 */
public class ImportDialog extends WizardDialog {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private SingleEntryWizardPanel selectorPanel;
    private FileSelectorButtonWidget fileSelector;
    private ProgressDialog progressDialog = null;
    CheckBox _checkBox;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static final int SCAN_BLOCK_SIZE = 65536;
    private static final long MAX_BLOCK_SIZE = 3 * 1024L * 1024L;
    private static final CharSequence _iconMarker = "<IconList".subSequence(0, "<IconList".length());
    private static final CharSequence _mapMarker = "<BasemapList".subSequence(0, "<BasemapList".length());

    private static final byte[] zipSignature = new byte[] {

            (byte)0xD4, (byte)0xC03, (byte)0x4B, (byte)0x50
    };

    private MainPresenter _mainPresenter = null;
    private ImportDialog _this;

    private File _fileAccess = null;
    private long _fileSize = 0L;
    private long _fileOffset = 0L;
    private long _blockSize = 0L;
    private int _blockNumber = 0;
    private String _fileName = null;

    private int _handle = 0;
    private String _serverFileName = null;
    private boolean _processing = false;
    private boolean _fileUploaded = false;
    private boolean _importBegun = false;
    private boolean _readLaunched = false;
    private FileUploadBlock _uploadRequest = null;
    private ImportRequest _importRequest = null;

    private FileReader _testReader = null;
    private FileReader _fileReader = null;
    private String _importTaskId = null;

    private boolean _waitingOnNext = false;
    private boolean _waitingOnDataView = false;
    private boolean _waitingOnTemplate = false;
    private boolean _waitingOnBasemap = false;
    private boolean _waitingOnAdminDataView = false;
    private boolean _waitingOnAdminTemplate = false;
    private boolean _waitingOnAdminBasemap = false;
    private boolean _waitingOnConflicts = false;
    private boolean _iconsFlag = false;
    private boolean _mapsFlag = false;
    private boolean _blockNext = false;
    private boolean _rawXml = false;
    private boolean _errorBlock = false;

    private List<MinResourceInfo> _resourceList = null;
    private List<ResourceConflictInfo> _conflictList = null;
    private List<List<ResourceBasics>> _dataViewOverWrite = null;
    private List<List<ResourceBasics>> _templateOverWrite = null;
    private List<List<ResourceBasics>> _mapOverWrite = null;
    private Map<String, List<ResourceBasics>> _adminDataViewOverWrite = null;
    private Map<String, List<ResourceBasics>> _adminTemplateOverWrite = null;
    private Map<String, List<ResourceBasics>> _adminMapOverWrite = null;
    private String _contentsString = null;
    private List<ImportResponse> _importResponseList = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler cancelHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            if (_importBegun) {

                cancelImport();

            } else {
/*
                if (null != _wizard) {

                    _wizard.destroy();
                    _wizard = null;
                }
*/
                cancelUpload();
                fileSelector.resetValue();
            }
        }
    };

    private ChoiceMadeEventHandler handleFileSelection
            = new ChoiceMadeEventHandler() {

        public void onChoiceMade(ChoiceMadeEvent eventin) {

            cancelUpload();

            _fileAccess = fileSelector.getFile();
            fileSelector.replaceFinalizingWidget(null);

            if (null != _fileAccess) {

                _processing = true;

                _fileSize = _fileAccess.getSize();
                _fileName = StringUtil.removePath(_fileAccess.getName());
                _fileOffset = 0L;
                _blockNumber = 0;
                _errorBlock = false;

                showWatchBox(_constants.installFileWizard_ScanningFile());
                launchSizeRead();
            }
        }
    };

    private LoadEndHandler handleChainedRead = new LoadEndHandler() {

        @Override
        public void onLoadEnd(LoadEndEvent loadEndEvent) {

            if (_processing) {

                if (null != _fileReader) {

                    String myDataBlock = _fileReader.getStringResult();

                    if ((null != myDataBlock) && (0 < myDataBlock.length())) {

                        uploadFileBlock(myDataBlock, _blockSize);
                    }
                }

            } else {

                cancelUpload();
            }
        }
    };

    private LoadEndHandler handleSizeRead = new LoadEndHandler() {

        @Override
        public void onLoadEnd(LoadEndEvent loadEndEvent) {

            ArrayBuffer myDataBlock = _testReader.getArrayBufferResult();

            Int8Array mySizeArray = null;

            if ((null != myDataBlock) && (2 <= myDataBlock.byteLength())) {

                try {

                    mySizeArray = ClientUtil.createInt8Array(myDataBlock);

                } catch (Exception myException) {

                    rejectFileChoice();
                    hideWatchBox();
                    displayError(1, myException);
                }

                if (null != mySizeArray) {

                    try {

                        long mySize = 0L;

                        if (recognizeXmlHeader(mySizeArray)) {

                            _rawXml = true;
                            _fileOffset = 0L;
                            mySize = ExportImportConstants.XML_HEADER.length + 2000L;

                        } else {

                            _rawXml = false;
                            _fileOffset = 2L;
                            mySize = ((((int)mySizeArray.get(0)) & 255) * 256) + (((int)mySizeArray.get(1)) & 255);
                        }
                        launchTextRead(mySize);

                    } catch (Exception myException) {

                        rejectFileChoice();
                        hideWatchBox();
                        displayError(2, myException);
                    }
                }
            }
            if (null == mySizeArray) {

                rejectFileChoice();
                hideWatchBox();
                displayError("Problem loading test data block.");
            }
        }
    };

    private LoadEndHandler handleTextRead = new LoadEndHandler() {

        @Override
        public void onLoadEnd(LoadEndEvent loadEndEvent) {

            _contentsString = _testReader.getStringResult();

            if ((null != _contentsString) && (0 < _contentsString.length())) {

                try {

                    if (!_readLaunched) {

                        _readLaunched = true;
                        launchChainedRead();
                    }

                } catch (Exception myException) {

                    displayError(3, myException);
                }
                try {

                    launchXmlScan();

                } catch (Exception myException) {

                    rejectFileChoice();
                    hideWatchBox();
                    displayError(4, myException);
                }

            } else {

                rejectFileChoice();
                hideWatchBox();
                displayError("Problem loading test data block.");
            }
        }
    };

    private VortexEventHandler<Response<Integer, FileUploadBlock>> handleUploadResponse
            = new AbstractVortexEventHandler<Response<Integer, FileUploadBlock>>() {
        @Override
        public boolean onError(Throwable myException) {

            displayError(5, myException);
            cancelUpload();

            return false;
        }
        @Override
        public void onSuccess(Response<Integer, FileUploadBlock> responseIn) {

            if (responseIn.getKey() == _handle) {

                if (responseIn.isSuccess()) {

                    _serverFileName = responseIn.getResult().getFileName();

                    try {

                        if (null != progressDialog) {

                            progressDialog.setProgress(_fileOffset);
                        }

                        if (_processing) {

                            if (_fileSize > _fileOffset) {

                                readChainedBlock();

                            } else {

                                if (null != progressDialog) {

                                    progressDialog.resetProgress(_constants.installFileWizard_InstallingTable(), _fileName, true);
                                }
                                _fileUploaded = true;
                            }

                        } else {

                            cancelUpload();
                        }

                    } catch(Exception myException){

                        displayError(6, myException);
                        cancelUpload();
                    }

                } else {

                    displayError(responseIn.getException());
                    cancelUpload();
                }

            } else {

                cancelUpload();
            }
        }
        @Override
        public void onUpdate(int percentComplete, String taskIdIn) {

            _importTaskId = taskIdIn;
            progressDialog.setPercentComplete(percentComplete);
        }
    };

    private VortexEventHandler<List<ResourceConflictInfo>> handleConflictResponse
            = new AbstractVortexEventHandler<List<ResourceConflictInfo>>() {
        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            displayError(7, myException);
            cancelUpload();
            return false;
        }
        @Override
        public void onSuccess(List<ResourceConflictInfo> responseIn) {

            _waitingOnConflicts = false;
            _conflictList = responseIn;
            checkReady();
        }
    };

    public VortexEventHandler<List<List<ResourceBasics>>> handleDataViewConflictResponse
            = new AbstractVortexEventHandler<List<List<ResourceBasics>>>() {
        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            displayError(8, myException);
            cancelUpload();
            return false;
        }

        @Override
        public void onSuccess(List<List<ResourceBasics>> multiListIn) {

            _dataViewOverWrite = multiListIn;
            _waitingOnDataView = false;
            checkReady();
        }
    };

    public VortexEventHandler<List<List<ResourceBasics>>> handleTemplateConflictResponse
            = new AbstractVortexEventHandler<List<List<ResourceBasics>>>() {
        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            displayError(9, myException);
            cancelUpload();
            return false;
        }

        @Override
        public void onSuccess(List<List<ResourceBasics>> multiListIn) {

            _templateOverWrite = multiListIn;
            _waitingOnTemplate = false;
            checkReady();
        }
    };

    public VortexEventHandler<List<List<ResourceBasics>>> handleBasemapConflictResponse
            = new AbstractVortexEventHandler<List<List<ResourceBasics>>>() {
        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            displayError(9, myException);
            cancelUpload();
            return false;
        }

        @Override
        public void onSuccess(List<List<ResourceBasics>> multiListIn) {

            _mapOverWrite = multiListIn;
            _waitingOnBasemap = false;
            checkReady();
        }
    };

    public VortexEventHandler<Map<String, List<ResourceBasics>>> handleDataViewListMapResponse
            = new AbstractVortexEventHandler<Map<String, List<ResourceBasics>>>() {
        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            displayError(8, myException);
            cancelUpload();
            return false;
        }

        @Override
        public void onSuccess(Map<String, List<ResourceBasics>> listMapIn) {

            _adminDataViewOverWrite = listMapIn;
            _waitingOnAdminDataView = false;
            checkReady();
        }
    };

    public VortexEventHandler<Map<String, List<ResourceBasics>>> handleTemplateListMapResponse
            = new AbstractVortexEventHandler<Map<String, List<ResourceBasics>>>() {
        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            displayError(9, myException);
            cancelUpload();
            return false;
        }

        @Override
        public void onSuccess(Map<String, List<ResourceBasics>> listMapIn) {

            _adminTemplateOverWrite = listMapIn;
            _waitingOnAdminTemplate = false;
            checkReady();
        }
    };

    public VortexEventHandler<Map<String, List<ResourceBasics>>> handleBasemapListMapResponse
            = new AbstractVortexEventHandler<Map<String, List<ResourceBasics>>>() {
        @Override
        public boolean onError(Throwable myException) {

            hideWatchBox();
            displayError(9, myException);
            cancelUpload();
            return false;
        }

        @Override
        public void onSuccess(Map<String, List<ResourceBasics>> listMapIn) {

            _adminMapOverWrite = listMapIn;
            _waitingOnAdminBasemap = false;
            checkReady();
        }
    };

    private VortexEventHandler<Response<Integer, List<ImportResponse>>> handleImportResponse
            = new AbstractVortexEventHandler<Response<Integer, List<ImportResponse>>>() {
        @Override
        public boolean onError(Throwable myException) {

            cancelUpload();
            Dialog.showException(myException);

            return false;
        }
        @Override
        public void onSuccess(Response<Integer, List<ImportResponse>> responseIn) {

            if (responseIn.getKey() == _handle) {

                cancelProgressDialog();

                if (responseIn.isSuccess()) {

                    try {

                        MainPresenter myPresenter = getMainPresenter();
                        StringBuilder myErrorBuffer = new StringBuilder();
                        StringBuilder myWarningBuffer = new StringBuilder();
                        StringBuilder myDisplayBuffer = new StringBuilder();
                        int myErrorCount = 0;
                        int myWarningCount = 0;
                        String myCloseString = "";


                        _importResponseList = responseIn.getResult();
                        for (int i = 0; _importResponseList.size() > i; i++) {

                            ImportResponse myResponse = _importResponseList.get(i);
                            AclResourceType myType = myResponse.getType();
                            String myName = myResponse.getName();
                            String myOwner = myResponse.getOwner();
                            String myUuid = myResponse.getUuid();
                            String myMessage = myResponse.getMessage();

                            if (myPresenter.checkDataView(myResponse.getName(), myResponse.getOwner())) {

                                myCloseString = "\nDataView will be closed.";
                            }

                            if (myResponse.getError()) {

                                myErrorCount++;
                                Map<String, String> grabBag = ExportImportHelper.buildFileNameComponent(myType, myName, myOwner, myUuid);
                                myErrorBuffer.append(buildFileName(grabBag));
                                myErrorBuffer.append('\n');
                                myErrorBuffer.append(myMessage);
                                myErrorBuffer.append('\n');

                            } else {

                                if (null != myMessage) {

                                    myWarningCount++;
                                    Map<String, String> grabBag = ExportImportHelper.buildErrorNameComponent(myType, myName, myOwner, myUuid);
                                    myWarningBuffer.append(buildFileName(grabBag));
                                    myWarningBuffer.append('\n');
                                    myWarningBuffer.append(myMessage);
                                    myWarningBuffer.append('\n');
                                }
                            }
                        }
                        if (0 < myErrorCount) {

                            myDisplayBuffer.append("Encountered ");
                            myDisplayBuffer.append(Integer.toString(myErrorCount));

                            if (0 < myWarningCount) {

                                myDisplayBuffer.append(" errors and ");
                                myDisplayBuffer.append(Integer.toString(myWarningCount));
                                myDisplayBuffer.append(" warnings.\n");

                            } else {

                                myDisplayBuffer.append(" errors.\n");
                            }
                            myDisplayBuffer.append(myCloseString);
                        }
                        else if (0 < myWarningCount) {

                            myDisplayBuffer.append("Encountered ");
                            myDisplayBuffer.append(Integer.toString(myWarningCount));
                            myDisplayBuffer.append(" warnings.\n");
                            myDisplayBuffer.append(myCloseString);

                        } else {

                            Display.success("Import Resource(s)", "All " + Integer.toString(_importResponseList.size())
                                    + " items imported successfully." + myCloseString, _this.handleImportComplete);
                        }
                        if (0 < myErrorCount) {

                            myDisplayBuffer.append("ERRORS . . .\n");
                            myDisplayBuffer.append(myErrorBuffer);
                        }
                        if (0 < myWarningCount) {

                            myDisplayBuffer.append("WARNINGS . . .\n");
                            myDisplayBuffer.append(myWarningBuffer);
                        }
                        if (0 < myErrorCount) {

                            Display.error("Import Resource(s)", myDisplayBuffer.toString(), _this.handleImportError);

                        } else if (0 < myWarningCount) {

                            Display.warning("Import Resource(s)", myDisplayBuffer.toString(), _this.handleImportComplete);
                        }

                    } catch (Exception myException) {

                        Display.error(Format.value(myException));
                        cancelImport();
                    }

                } else {

                    Display.error(responseIn.getException());
                }
            }
        }
        @Override
        public void onUpdate(int percentComplete, String taskIdIn) {

            _importTaskId = taskIdIn;
            progressDialog.setPercentComplete(percentComplete);
        }
    };

    private ClickHandler handleUserSelections = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            launchProgressDialog();
        }
    };

    private ClickHandler handleImportComplete = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            for (ImportResponse myResponse : _importResponseList) {

                if (AclResourceType.DATAVIEW.equals(myResponse.getType())) {

                    getMainPresenter().guaranteeDataView(myResponse.getName(), myResponse.getOwner());
                }
            }
            cancel();
        }
    };

    private ClickHandler handleImportError = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            show();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ImportDialog()
            throws CentrifugeException {

        super(null, new SingleEntryWizardPanel(null, "File Selector",
                        new FileSelectorButtonWidget(), _constants.installFileWizard_SelectFile_Prompt()),
                _constants.importWizard_Title(),
                _constants.importWizard_HelpTarget(),
                _constants.importWizard_SelectFile_Directions(_constants.dialog_ImportButton(), _constants.dialog_ImportButton()));

        _this = this;
        selectorPanel = (SingleEntryWizardPanel)getCurrentPanel();
        selectorPanel.setParentDialog(this);

        fileSelector = (FileSelectorButtonWidget)selectorPanel.getInputWidget();
        fileSelector.addSelectionHandler(handleFileSelection);
        _checkBox = new CheckBox("Make me the owner of the resources I am importing.");
        selectorPanel.add(_checkBox);
        _checkBox.setValue(true);
        _checkBox.setEnabled(getMainPresenter().isAdmin() ? true : false);
        selectorPanel.setWidgetLeftWidth(_checkBox, 5, Style.Unit.PX, selectorPanel.getPanelWidth() - 5, Style.Unit.PX);
        selectorPanel.setWidgetBottomHeight(_checkBox, 0, Style.Unit.PX, 30, Style.Unit.PX);
    }

    public void show() {

        _blockNext = false;
        _waitingOnNext = true;
        super.show(_constants.dialog_ImportButton());
    }

    public void launchImport(List<ImportItem> listIn) {

        if ((null != listIn) && (0 < listIn.size())) {

            _importRequest = new ImportRequest(_serverFileName, listIn, _rawXml);
            launchProgressDialog();
            checkUpload();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    protected void createPanel() {
    }

    @Override
    protected void execute() {

        try {

            if (!_readLaunched) {

                _readLaunched = true;
                launchChainedRead();
            }
            _waitingOnNext = false;
            launchSelectionDialog();

        } catch (Exception myException) {

            resetDataValues();
          displayError(10, myException);
        }
    }

    @Override
    protected void onCancel() {

        AbstractDataViewPresenter myDataViewPresenter = getDataViewPresenter();

        removeFile(_serverFileName);
        if (null != myDataViewPresenter) {

            myDataViewPresenter.checkStatus();
        }
    }

    protected void cancelUpload() {

        _processing = false;
        cancelProgressDialog();
        removeFile(_serverFileName);
        resetDataValues();
    }

    protected void cancelImport() {

        cancelProgressDialog();
        cancelServerImport();
        _importBegun = false;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void resetDataValues() {

        _handle++;

        _fileAccess = null;
        _fileSize = 0L;
        _fileOffset = 0L;
        _blockSize = 0L;
        _blockNumber = 0;
        _uploadRequest = null;
        _importRequest = null;
        _processing = false;
        _fileName = null;
        _fileUploaded = false;
        _testReader = null;
        _fileReader = null;
        _readLaunched = false;
        _importTaskId = null;
        _importBegun = false;
    }

    private void rejectFileChoice() {

        _fileAccess = null;
        _fileSize = 0L;
        _fileName = null;
        _processing = false;
        fileSelector.resetValue();
    }

    private void launchXmlScan() {

        try {

            if (_rawXml) {

                _iconsFlag = false;
                _mapsFlag = false;
                _resourceList = ResourceParser.buildResourceList(_contentsString);

            } else {

                _iconsFlag = _contentsString.contains(_iconMarker);
                _mapsFlag = _contentsString.contains(_mapMarker);
                _resourceList = ImportParser.buildResourceList(_contentsString);
            }
            requestConflictInformation();

        } catch(Exception myException) {

            hideWatchBox();
            displayError(11, myException);
        }
    }

    private void launchSizeRead() {

        try {

            _testReader = new FileReader();
            _testReader.addLoadEndHandler(handleSizeRead);
            readSizeBlock();

        } catch(Exception myException) {

            displayError(12, myException);
        }
    }

    private void launchTextRead(long sizeIn) {

        try {

            _testReader = new FileReader();
            _testReader.addLoadEndHandler(handleTextRead);
            readTextBlock(sizeIn);

        } catch(Exception myException) {

            displayError(13, myException);
        }
    }

    private void checkUpload() {

        if (_fileUploaded && (null != _importRequest)) {

            beginServerImport();

        } else if (!_processing) {

            cancelProgressDialog();

        } else {

            DeferredCommand.add(new Command() {
                public void execute() {
                    checkUpload();
                }
            });
        }
    }

    private void cancelProgressDialog() {

        if (null != progressDialog) {

            progressDialog.hide();
            progressDialog = null;
        }
        if (_fileUploaded) {

            launchXmlScan();

        } else {

            show();
        }
    }

    private void launchProgressDialog() {

        progressDialog = new ProgressDialog(_constants.installFileWizard_UploaderTitle(), 2L,
                _constants.installFileWizard_Uploading_Message(),
                _fileSize, _fileName, 2, _fileName,
                _constants.installFileWizard_CancelPrompt(), cancelHandler);
        progressDialog.setProgress(_fileOffset);
        if (_fileUploaded) {

            progressDialog.resetProgress(_constants.installFileWizard_InstallingProgress(), _fileName, true);
        }
        progressDialog.show();
        progressDialog.showCancelButton();
    }

    private void launchChainedRead() {

        _handle++;
        _fileReader = new FileReader();
        _fileReader.addLoadEndHandler(handleChainedRead);
        if (_rawXml) {

            _fileOffset = 0L;
        }
        readChainedBlock();
    }

    private void clearImportRequest() {

        _handle++;
        _importRequest = null;
        _importTaskId = null;
        _importBegun = false;
    }

    private void beginServerImport() {

            VortexFuture<Response<Integer, List<ImportResponse>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                myVortexFuture.addEventHandler(handleImportResponse);
                myVortexFuture.execute(UploadServiceProtocol.class).beginImport(++_handle, _importRequest);

            } catch (Exception myException) {

                displayError(14, myException);
            }
    }

    private void cancelServerImport() {

        if (null != _importTaskId) {

            VortexFuture<Boolean> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                myVortexFuture.execute(UploadServiceProtocol.class).cancelImport(_importTaskId);
                clearImportRequest();

            } catch (Exception myException) {

                displayError(14, myException);
            }
        }
    }

    private void removeFile(String fileNameIn) {

        if (null != fileNameIn) {

            VortexFuture<Void> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                myVortexFuture.execute(UploadServiceProtocol.class).deleteUserFile(fileNameIn);

            } catch (Exception myException) {

                displayError(15, myException);
            }
        }
    }

    private void uploadFileBlock(String blockIn, long lengthIn) {

        if (_processing) {

            VortexFuture<Response<Integer, FileUploadBlock>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                _uploadRequest = new FileUploadBlock(_serverFileName, _blockNumber++, lengthIn, blockIn);

                myVortexFuture.addEventHandler(handleUploadResponse);
                myVortexFuture.execute(UploadServiceProtocol.class).receiveFileBlock(_handle, _uploadRequest);

            } catch (Exception myException) {

                displayError(16, myException);
            }

        } else {

            cancelUpload();
        }
    }

    private void readTextBlock(long sizeIn) {

        long myBase = _fileOffset;

        _fileOffset += (long)sizeIn;
        _testReader.readAsText(_fileAccess.slice(myBase, _fileOffset));
    }

    private void readSizeBlock() {

        // Read enough to recognize a raw XML file
        long mySize = Math.min(ExportImportConstants.XML_HEADER.length, _fileSize);

        _testReader.readAsArrayBuffer(_fileAccess.slice(0L, mySize));
    }

    private void readChainedBlock() {

        if (_processing) {

            long myBase = _fileOffset;

            _blockSize = Math.min(MAX_BLOCK_SIZE, (_fileSize - _fileOffset));

            _fileOffset += _blockSize;

            Blob myBlock = _fileAccess.slice(myBase, myBase + _blockSize);

            _fileReader.readAsDataURL(myBlock);

        } else {

            cancelUpload();
        }
    }

    private void requestConflictInformation() throws Exception {

        if ((null != _resourceList) && (0 < _resourceList.size())) {

            boolean myAdminFlag = getMainPresenter().isAdmin();

            if (_rawXml) {

                _waitingOnConflicts = false;
                _conflictList = new ArrayList<ResourceConflictInfo>();
                _conflictList.add(new ResourceConflictInfo(_resourceList.get(0)));

            } else {

                _waitingOnConflicts = true;
                requestUuidConflicts();
            }
            for (MinResourceInfo myInfo : _resourceList) {

                if (AclResourceType.DATAVIEW.equals(myInfo.getType())) {

                    _waitingOnDataView = true;

                    if (_waitingOnTemplate && _waitingOnBasemap) {

                        break;
                    }

                } else if (AclResourceType.TEMPLATE.equals(myInfo.getType())) {

                    _waitingOnTemplate = true;

                    if (_waitingOnDataView && _waitingOnBasemap) {

                        break;
                    }

                } else if (AclResourceType.MAP_BASEMAP.equals(myInfo.getType())) {

                    _waitingOnBasemap = true;

                    if (_waitingOnDataView && _waitingOnTemplate) {

                        break;
                    }
                }
            }
            resetNameConflicts();
            if (_waitingOnDataView) {

                if (myAdminFlag) {

                    _waitingOnAdminDataView = true;
                    requestAdminNamingConflicts(AclResourceType.DATAVIEW, handleDataViewListMapResponse);
                }
                requestResourceNamingConflicts(AclResourceType.DATAVIEW, handleDataViewConflictResponse);
            }
            if (_waitingOnTemplate) {

                if (myAdminFlag) {

                    _waitingOnAdminTemplate = true;
                    requestAdminNamingConflicts(AclResourceType.TEMPLATE, handleTemplateListMapResponse);
                }
                requestResourceNamingConflicts(AclResourceType.TEMPLATE, handleTemplateConflictResponse);
            }
            if (_waitingOnBasemap) {

                if (myAdminFlag) {

                    _waitingOnAdminBasemap = true;
                    requestAdminNamingConflicts(AclResourceType.MAP_BASEMAP, handleBasemapListMapResponse);
                }
                requestResourceNamingConflicts(AclResourceType.MAP_BASEMAP, handleBasemapConflictResponse);
            }

        } else if (_iconsFlag) {

            _waitingOnConflicts = true;
            requestUuidConflicts();

        } else {

            hideWatchBox();
            Display.error("Unable to identify any resources within the file.");
        }
    }

    private void resetNameConflicts() {

        _dataViewOverWrite = new ArrayList<List<ResourceBasics>>();
        _dataViewOverWrite.add(new ArrayList<ResourceBasics>());
        _dataViewOverWrite.add(new ArrayList<ResourceBasics>());
        _dataViewOverWrite.add(new ArrayList<ResourceBasics>());

        _templateOverWrite = new ArrayList<List<ResourceBasics>>();
        _dataViewOverWrite.add(new ArrayList<ResourceBasics>());
        _dataViewOverWrite.add(new ArrayList<ResourceBasics>());
        _dataViewOverWrite.add(new ArrayList<ResourceBasics>());

        _mapOverWrite = new ArrayList<List<ResourceBasics>>();
        _mapOverWrite.add(new ArrayList<ResourceBasics>());
        _mapOverWrite.add(new ArrayList<ResourceBasics>());
        _mapOverWrite.add(new ArrayList<ResourceBasics>());

        _adminDataViewOverWrite = new TreeMap<String, List<ResourceBasics>>();
        _adminTemplateOverWrite = new TreeMap<String, List<ResourceBasics>>();
        _adminMapOverWrite = new TreeMap<String, List<ResourceBasics>>();
    }

    private void launchSelectionDialog() {

        if ((!_blockNext) && (!_waitingOnNext) && (!_errorBlock)) {
            for (ResourceConflictInfo resourceConflictInfo : _conflictList) {
                String distinctName = UniqueNameUtil.getDistinctName(_dataViewOverWrite.get(0).stream().map(resourceBasics -> resourceBasics.getName()).collect(Collectors.toList()), resourceConflictInfo.getName());
                resourceConflictInfo.setName(distinctName);
            }

            _blockNext = true;
            (new ImportSelectionDialog(_this, _constants.importWizard_Title(),
                    _constants.importWizard_HelpTarget(),
                    _conflictList, _dataViewOverWrite, _templateOverWrite, _mapOverWrite,
                    _adminDataViewOverWrite, _adminTemplateOverWrite, _adminMapOverWrite, _checkBox.getValue())).show();
        }
    }

    private void checkReady() {

        if (!(_waitingOnConflicts || _waitingOnDataView || _waitingOnTemplate || _waitingOnBasemap
                || _waitingOnAdminDataView || _waitingOnAdminTemplate || _waitingOnAdminBasemap)) {

            hideWatchBox();
            setValidity(true);
        }
    }

    private void requestUuidConflicts() throws Exception {

        VortexFuture<List<ResourceConflictInfo>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        showWatchBox();
        myVortexFuture.addEventHandler(handleConflictResponse);
        myVortexFuture.execute(UploadServiceProtocol.class).identifyImportConflicts(_resourceList, _iconsFlag, _mapsFlag);
    }

    private void requestResourceNamingConflicts(AclResourceType typeIn,
                                                VortexEventHandler<List<List<ResourceBasics>>> handlerIn)
            throws Exception {

        VortexFuture<List<List<ResourceBasics>>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        showWatchBox();
        myVortexFuture.addEventHandler(handlerIn);
        myVortexFuture.execute(ModelActionsServiceProtocol.class).getResourceOverWriteControlLists(typeIn);
    }

    private void requestAdminNamingConflicts(AclResourceType typeIn,
                                             VortexEventHandler<Map<String, List<ResourceBasics>>> handlerIn)
            throws Exception {

        VortexFuture<Map<String, List<ResourceBasics>>> myVortexFuture = WebMain.injector.getVortex().createFuture();
        Set<String> myUserList = new TreeSet<String>();

        for (MinResourceInfo myResource : _resourceList) {

            if (myResource.getType().equals(typeIn)) {

                String myOwner = myResource.getOwner();

                if ((null != myOwner) && (0 < myOwner.length())) {

                    myUserList.add(myOwner);
                }
            }
        }
        showWatchBox();
        myVortexFuture.addEventHandler(handlerIn);
        myVortexFuture.execute(ModelActionsServiceProtocol.class).getAdminResourceLists(typeIn, myUserList);
    }

    private boolean recognizeXmlHeader(Int8Array blockIn) {

        byte[] myTestBlock = ExportImportConstants.XML_HEADER;

        for (int i = 0; myTestBlock.length > i; i++) {

            if (myTestBlock[i] != blockIn.get(i)) {

                return false;
            }
        }
        return true;
    }

    private void displayError(int idIn, Throwable exceptionIn) {

        _errorBlock = true;
        setValidity(false);
        Display.error("ImportDialog", idIn, exceptionIn);
    }

    private void displayError(String messageIn) {

        _errorBlock = true;
        setValidity(false);
        Display.error("ImportDialog", messageIn);
    }

    private AbstractDataViewPresenter getDataViewPresenter() {

        return getMainPresenter().getDataViewPresenter(true);
    }

    private MainPresenter getMainPresenter() {

        if (null == _mainPresenter) {

            _mainPresenter = WebMain.injector.getMainPresenter();
        }
        return _mainPresenter;
    }

    static List<String> order = new ArrayList<>();

    static {
        order.add("type");
        order.add("owner");
        order.add("name");
        order.add("date");
    }

    private static String buildFileName(Map<String, String> grabBag) {
        List<String> componentsToUse = new ArrayList<>();
        for(String key : order) {
            if(grabBag.containsKey(key)) {
                componentsToUse.add(grabBag.get(key));
            }
        }
        Joiner joiner = Joiner.on("_");
        String s = joiner.join(componentsToUse);
        return s;
    }
}

