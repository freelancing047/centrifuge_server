package csi.client.gwt.widget.ui.uploader;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.VerticalPanel;
import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.panels.SingleEntryWizardPanel;
import csi.client.gwt.csiwizard.widgets.FileSelectorButtonWidget;
import csi.client.gwt.csiwizard.widgets.RadioSelectionWidget;
import csi.client.gwt.events.ChoiceMadeEvent;
import csi.client.gwt.events.ChoiceMadeEventHandler;
import csi.client.gwt.events.TransferCompleteEvent;
import csi.client.gwt.events.TransferCompleteEventHandler;
import csi.client.gwt.file_access.uploader.ClientUtil;
import csi.client.gwt.file_access.uploader.SandBoxFileStream;
import csi.client.gwt.file_access.uploader.XlsxProcessor;
import csi.client.gwt.file_access.uploader.zip.ZipDirectoryCompleteCallBack;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.KnowsParent;
import csi.client.gwt.widget.ui.ProgressDialog;
import csi.client.gwt.widget.ui.uploader.wizards.FileInstallWizard;
import csi.client.gwt.widget.ui.uploader.wizards.support.ReadBlock;
import csi.client.gwt.widget.ui.uploader.wizards.support.ReadNonBinaryBlock;
import csi.client.gwt.widget.ui.uploader.wizards.support.ReadXlsxBlock;
import csi.server.common.dto.FileUploadBlock;
import csi.server.common.dto.Response;
import csi.server.common.dto.installed_tables.TableInstallRequest;
import csi.server.common.dto.installed_tables.TableInstallResponse;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.dataview.AdHocDataSource;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.service.api.UploadServiceProtocol;
import csi.server.common.util.Format;
import csi.server.common.util.StringUtil;
import csi.server.common.util.uploader.zip.CsiZipEntry;
import org.vectomatic.file.Blob;
import org.vectomatic.file.File;
import org.vectomatic.file.FileReader;
import org.vectomatic.file.events.LoadEndEvent;
import org.vectomatic.file.events.LoadEndHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by centrifuge on 10/23/2015.
 */
public class FileSelectorDialog extends WizardDialog implements UploaderControl, ZipDirectoryCompleteCallBack {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private SingleEntryWizardPanel selectorPanel;
    private FileSelectorButtonWidget fileSelector;
    private RadioSelectionWidget fileTypeSelector;
    private CheckBox compressNulls;
    private ProgressDialog progressDialog = null;
    private FileInstallWizard _wizard = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static final int SCAN_BLOCK_SIZE = 65536;
    private static final long MAX_BLOCK_SIZE = 3 * 1024L * 1024L;

    private String _txtHelp = _constants.installFileWizard_HelpTarget();

    private static final byte[] msSignature = new byte[] {

            (byte)0xD0, (byte)0xCF, (byte)0x11, (byte)0xE0, (byte)0xA1, (byte)0xB1, (byte)0x1A, (byte)0xE1
    };

    private static final String[] _txtIdentifyTypeInstructions = new String[] {

            _constants.installFileWizard_SelectFileType_Directions(),
            _constants.installFileWizard_SelectFileType_Directions_csv(),
            _constants.installFileWizard_SelectFileType_Directions_txt()
    };

    private static final String[] _buttonList = new String[CsiFileType.values().length];

    static {

        for (int i =0; CsiFileType.values().length > i; i++) {

            CsiFileType myType = CsiFileType.values()[i];

            _buttonList[i] = myType.getDescription();
        }
    }
    private static final boolean[] _enabledList = new boolean[CsiFileType.values().length];

    static {

        for (int i =0; CsiFileType.values().length > i; i++) {

            CsiFileType myType = CsiFileType.values()[i];

            _enabledList[i] = myType.isSupported();
        }
    }

    private AdHocDataSource _dataSource;
    private File _fileAccess = null;
    private CsiFileType _fileType = null;
    private CsiFileType _preferedType = null;
    private long _fileSize = 0L;
    private long _fileOffset = 0L;
    private long _blockSize = 0L;
    private int _blockNumber = 0;
    private FileUploadBlock _uploadRequest = null;
    private TableInstallRequest _installRequest = null;
    private boolean _processing = false;
    private int _handle = 0;
    private String _tablePath = null;
    private String _serverFileName = null;
    private String _fileName = null;
    private boolean _fileUploaded = false;
    private FileReader _testReader = null;
    private FileReader _fileReader = null;
    private XlsxProcessor _zipProcessor = null;
    private Int8Array _testDataSource = null;
    private TransferCompleteEventHandler _resultCallBack = null;
    private Boolean[] _fileTypeStatus = new Boolean[CsiFileType.values().length];
    private boolean _readLaunched = false;
    private ReadBlock _dataReader = null;
    private String _installTaskId = null;
    private List<InstalledTable> _installedTables = new ArrayList<InstalledTable>();
    private InstalledTable _table = null;
    private  boolean _installBegun = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler cancelHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {

            if (_installBegun) {

                cancelInstall();

            } else {

                if (null != _wizard) {

                    _wizard.destroy();
                    _wizard = null;
                }
                cancelUpload();
                fileSelector.resetValue();
                show();
            }
        }
    };

    private ChoiceMadeEventHandler handleFileSelection
            = new ChoiceMadeEventHandler() {

        public void onChoiceMade(ChoiceMadeEvent eventin) {

            cancelUpload();

            _fileAccess = fileSelector.getFile();
            fileSelector.replaceFinalizingWidget(null);
            fileTypeSelector = null;

            for (int i = 0; _fileTypeStatus.length > i;i++) {

                _fileTypeStatus[i] = null;
            }
            _fileType = null;

            if (null != _fileAccess) {

                _processing = true;

                _fileSize = _fileAccess.getSize();
                _fileName = StringUtil.removePath(_fileAccess.getName());
                _fileOffset = 0L;
                _blockNumber = 0;

                showWatchBox(_constants.installFileWizard_ScanningFile());
                launchTestRead();
            }
        }
    };

    private LoadEndHandler handleTestRead = new LoadEndHandler() {

        @Override
        public void onLoadEnd(LoadEndEvent loadEndEvent) {

            ArrayBuffer myDataBlock = _testReader.getArrayBufferResult();

            _testDataSource = null;

            if ((null != myDataBlock) && (0 < myDataBlock.byteLength())) {

                try {

                    _testDataSource = ClientUtil.createInt8Array(myDataBlock);

                } catch (Exception myException) {

                    rejectFileChoice();
                    hideWatchBox();
                    Dialog.showException(myException);
                }

                if ((null != _testDataSource) && (msSignature.length < _testDataSource.length())) {

                    try {

                        launchZipScan();

                    } catch (Exception myException) {

                        rejectFileChoice();
                        hideWatchBox();
                        Dialog.showException(myException);
                    }
                }
            }
        }
    };

    private VortexEventHandler<Response<Integer, FileUploadBlock>> handleUploadResponse
            = new AbstractVortexEventHandler<Response<Integer, FileUploadBlock>>() {
        @Override
        public boolean onError(Throwable myException) {

            Dialog.showException(myException);
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

                                    progressDialog.resetProgress(_constants.installFileWizard_InstallingTable(), _tablePath, true);
                                }
                                _fileUploaded = true;
                            }

                        } else {

                            cancelUpload();
                        }

                    } catch(Exception myException){

                        Display.error(Format.value(myException));
                        cancelUpload();
                    }

                } else {

                    Display.error(responseIn.getException());
                    cancelUpload();
                }

            } else {

                cancelUpload();
            }
        }
    };

    private VortexEventHandler<Response<Integer, TableInstallResponse>> handleInstallResponse
            = new AbstractVortexEventHandler<Response<Integer, TableInstallResponse>>() {
        @Override
        public boolean onError(Throwable myException) {

            cancelUpload();
            Dialog.showException(myException);

            return false;
        }
        @Override
        public void onSuccess(Response<Integer, TableInstallResponse> responseIn) {

            if (responseIn.getKey() == _handle) {

                cancelProgressDialog();

                if (responseIn.isSuccess()) {

                    try {

                        InstalledTable myTable = responseIn.getResult().getTable();
                        if (null != myTable) {

                            if (null != _table) {

                                Display.success(getDialogTitle(), _constants.updateTableWizard_Success(_table.getTablePath()));

                            } else {

                                _installedTables.add(myTable);
                                _wizard.recordSuccess(_tablePath);
                            }
                            clearInstallRequest();
                            exitWizard();
                        }

                    } catch (Exception myException) {

                        Display.error(Format.value(myException));
                        cancelInstall();
                    }

                } else {

                    Display.error(responseIn.getException());
                }
            }
        }
        @Override
        public void onUpdate(int percentComplete, String taskIdIn) {

            _installTaskId = taskIdIn;
            progressDialog.setPercentComplete(percentComplete);
        }
    };

    private ChoiceMadeEventHandler handleNewInstallChoice = new ChoiceMadeEventHandler() {
        @Override
        public void onChoiceMade(ChoiceMadeEvent eventIn) {

            if (1 == eventIn.getChoice()) {

                exitWizard();
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


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                       Call Backs                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public void onDirectoryComplete(Map<String, CsiZipEntry> mapIn) {

        try {

            _fileType = CsiFileType.NEW_EXCEL;
            _dataReader = new ReadXlsxBlock(_testDataSource, _zipProcessor);
            execute();

        } catch(Exception myException) {

            Dialog.showException(myException);
        }
    }

    public void onDirectoryCompleteError(Exception exceptionIn) {

        _fileTypeStatus[CsiFileType.NEW_EXCEL.ordinal()] = false;

        _preferedType = determineFileType();

        if (null != _fileType) {

            execute();

        } else {

            displayFileTypes();
        }
    }

    public String getFileName() {

        return _fileName;
    }

    public void onError(final Exception exceptionIn) {

        if (null != _wizard) {

            _wizard.destroy();
            _wizard = null;
        }

        DeferredCommand.add(new Command() {
            public void execute() {
                show(exceptionIn);
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FileSelectorDialog(WizardInterface priorDialogIn,
                              TransferCompleteEventHandler handlerIn, InstalledTable tableIn)
            throws CentrifugeException {

        super(priorDialogIn, new SingleEntryWizardPanel(priorDialogIn, "File Selector",
                new FileSelectorButtonWidget(), _constants.installFileWizard_SelectFile_Prompt()),
                (null != tableIn) ? _constants.updateTableWizard_Title() : _constants.installFileWizard_Title(),
                _constants.installFileWizard_HelpTarget(),
                _constants.installFileWizard_SelectFile_Directions(_constants.fileInputWidget_Button()));
        getCurrentPanel().setParentDialog(this);

        _table = tableIn;
        _resultCallBack = handlerIn;

        selectorPanel = (SingleEntryWizardPanel)getCurrentPanel();

        compressNulls = new CheckBox(_constants.fileSelector_compressNulls());
        selectorPanel.add(compressNulls);
        selectorPanel.setWidgetBottomHeight(compressNulls, 10, Style.Unit.PX, 20, Style.Unit.PX);

        fileSelector = (FileSelectorButtonWidget)selectorPanel.getInputWidget();
        fileSelector.addSelectionHandler(handleFileSelection);
    }

    public void show() {

        super.show(Dialog.txtInstallButton);
    }

    @Override
    public void cancel() {

        if (null != _installRequest) {

            cancelInstall();

        } else if (null != _uploadRequest) {

            cancelUpload();
        }
        returnSelection();
        super.cancel();
    }

    @Override
    public void beginInstall(TableInstallRequest requestIn) {

        _installRequest = requestIn;
        launchProgressDialog();
        checkUpload();
    }

    @Override
    public Int8Array getTestBlock() {

        return _testDataSource;
    }

    @Override
    public Object getBlockAccess() {

        return CsiFileType.NEW_EXCEL.equals(_fileType) ? _zipProcessor : null;
    }

    public void launchWizard(CsiFileType fileTypeIn, WizardDialog parentIn) {

        _wizard = FileInstallWizard.create(fileTypeIn, parentIn, this,
                                            compressNulls.getValue(), _table, getDialogTitle(), _txtHelp);
        _wizard.run();
    }

    public ReadBlock getDataReader() {

        return _dataReader;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void cancelUpload() {

        _processing = false;
        cancelProgressDialog();
        removeFile(_serverFileName);
        resetDataValues();
    }

    protected void cancelInstall() {

        cancelProgressDialog();
        cancelServerInstall();
        _installBegun = false;
    }

    protected void createPanel() {
    }

    @Override
    protected void execute() {

        boolean myLaunchOk = false;

        try {

            if (!_readLaunched) {

                _readLaunched = true;
                launchChainedRead();
            }

            if (null != fileTypeSelector) {

                _fileType = CsiFileType.values()[fileTypeSelector.getChoice()];
                if (null != _dataReader) {

                    _dataReader = ReadNonBinaryBlock.createReader(_fileType, (ReadNonBinaryBlock)_dataReader,
                                                                    compressNulls.getValue());

                } else {

                    _dataReader = ReadNonBinaryBlock.createReader(_fileType, _testDataSource, compressNulls.getValue());
                }
                showWatchBox(_constants.installFileWizard_ScanningFile());
            }
            myLaunchOk = true;

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
        if (myLaunchOk) {

            launchWizard();
        }
    }

    protected void retrieveResults(KnowsParent childIn) {

    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void rejectFileChoice() {

        _fileAccess = null;
        _fileSize = 0L;
        _fileName = null;
        _processing = false;
        fileSelector.resetValue();
    }

    private void returnSelection() {

        if (null != _resultCallBack) {

            List<String> myInstalledTables = new ArrayList<String>(_installedTables.size());

            for (InstalledTable myTable : _installedTables) {

                myInstalledTables.add(myTable.getTablePath());
            }

            _resultCallBack.onTransferComplete(new TransferCompleteEvent(myInstalledTables));
        }
    }

    private void clearInstallRequest() {

        _installRequest = null;
        _installTaskId = null;
        _installBegun = false;
    }

    private void displayFileTypes() {

        VerticalPanel myPanel = new VerticalPanel();
        int mySelection = (null != _fileType)
                ? _fileType.ordinal()
                : (null != _preferedType) ? _preferedType.ordinal() : -1;

        hideWatchBox();

        fileTypeSelector = new RadioSelectionWidget(_buttonList, _fileTypeStatus, _enabledList, mySelection);

        fileSelector.replaceFinalizingWidget(fileTypeSelector);
    }

    private void cancelUpload(int handleIn, FileUploadBlock blockIn) {

        if (null != blockIn) {

            VortexFuture<Void> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                myVortexFuture.execute(UploadServiceProtocol.class).cancelUpload(handleIn, blockIn);

            } catch (Exception myException) {

                Dialog.showException(myException);
            }
        }
    }

    private void removeFile(String fileNameIn) {

        if (null != fileNameIn) {

            VortexFuture<Void> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                myVortexFuture.execute(UploadServiceProtocol.class).deleteUserFile(fileNameIn);

            } catch (Exception myException) {

                Dialog.showException(myException);
            }
        }
    }

    private void cancelServerInstall() {

        if (null != _installTaskId) {

            VortexFuture<Boolean> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                myVortexFuture.execute(UploadServiceProtocol.class).cancelInstall(_installTaskId);
                clearInstallRequest();

            } catch (Exception myException) {

                Dialog.showException(myException);
            }
        }
    }

    private void launchChainedRead() {

        _handle++;
        _fileReader = new FileReader();
        _fileReader.addLoadEndHandler(handleChainedRead);
        readChainedBlock();
    }

    private void launchWizard() {

        _wizard = FileInstallWizard.create(_fileType, this, this, compressNulls.getValue(), _table, getDialogTitle(), _txtHelp);
        _wizard.run();
        hide();
    }

    private void checkUpload() {

        if (_fileUploaded && (null != _installRequest)) {

            if(null != _table) {

                updateTable();

            } else {

                installFile();
            }

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
    }

    private void launchProgressDialog() {

        progressDialog = new ProgressDialog(_constants.installFileWizard_UploaderTitle(), 2L,
                _constants.installFileWizard_Uploading_Message(),
                _fileSize, _fileName, 2, _fileName,
                _constants.installFileWizard_CancelPrompt(), cancelHandler);
        progressDialog.setProgress(_fileOffset);
        if (_fileUploaded) {

            progressDialog.resetProgress(_constants.installFileWizard_InstallingProgress(), _tablePath, true);
        }
        progressDialog.show();
        progressDialog.showCancelButton();
    }

    private void launchTestRead() {

        try {

            _testReader = new FileReader();
            _testReader.addLoadEndHandler(handleTestRead);
            readTestBlock();

        } catch(Exception myException) {

            Dialog.showException(myException);
        }
    }

    private void launchZipScan() {

        try {

            _zipProcessor = new XlsxProcessor(new SandBoxFileStream(_fileAccess), 15, this,
                                                WebMain.injector.getMainPresenter().getMaxBufferSize());

        } catch(Exception myException) {

            Dialog.showException(myException);
        }
    }
    
    private void resetDataValues() {

        _handle++;

        _fileAccess = null;
        _fileType = null;
        _fileSize = 0L;
        _fileOffset = 0L;
        _blockSize = 0L;
        _blockNumber = 0;
        _uploadRequest = null;
        _installRequest = null;
        _processing = false;
        _tablePath = null;
        _fileName = null;
        _fileUploaded = false;
        _testReader = null;
        _fileReader = null;
        _zipProcessor = null;
        _testDataSource = null;
        _dataReader = null;
        _fileTypeStatus = new Boolean[CsiFileType.values().length];
        _readLaunched = false;
        _installTaskId = null;
        _installBegun = false;
    }

    private void readTestBlock() {

        long mySize = Math.min(SCAN_BLOCK_SIZE, _fileSize);

        Blob myBlock = _fileAccess.slice(0L, mySize);

        _testReader.readAsArrayBuffer(myBlock);
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

    private void uploadFileBlock(String blockIn, long lengthIn) {

        if (_processing) {

            VortexFuture<Response<Integer, FileUploadBlock>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                _uploadRequest = new FileUploadBlock(_serverFileName, _blockNumber++, lengthIn, blockIn);

                myVortexFuture.execute(UploadServiceProtocol.class).receiveFileBlock(_handle, _uploadRequest);
                myVortexFuture.addEventHandler(handleUploadResponse);

            } catch (Exception myException) {

                Dialog.showException(myException);
            }

        } else {

            cancelUpload();
        }
    }

    private void installFile() {

        if (_processing) {

            _installRequest.setFileName(_serverFileName);
            _installRequest.setFileType(_fileType);
            _tablePath = _installRequest.getTableParameters().getTableName();

            VortexFuture<Response<Integer, TableInstallResponse>> myVortexFuture = WebMain.injector.getVortex().createFuture();

            try {

                myVortexFuture.addEventHandler(handleInstallResponse);
                myVortexFuture.execute(UploadServiceProtocol.class).installFile(_handle, _installRequest);
                _installBegun = true;

            } catch (Exception myException) {

                Dialog.showException(myException);
            }

        } else {

            cancelInstall();
        }
    }

    private void updateTable() {

        if (_processing) {

            _installRequest.setFileName(_serverFileName);
            _installRequest.setFileType(_fileType);
            _tablePath = _installRequest.getTableParameters().getTableName();
            _installRequest.setTableId(_table.getUuid());

            doUpdateRequest(_installRequest);

        } else {

            cancelInstall();
        }
    }

    @Override
    public void doUpdateRequest(TableInstallRequest _installRequest) {
        _installRequest.setFileName(_serverFileName);
        _installRequest.setFileType(_fileType);

        VortexFuture<Response<Integer, TableInstallResponse>> myVortexFuture = WebMain.injector.getVortex().createFuture();
        try {

            myVortexFuture.execute(UploadServiceProtocol.class).updateInstalledFile(_handle, _installRequest);
            myVortexFuture.addEventHandler(handleInstallResponse);
            _installBegun = true;

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    private CsiFileType determineFileType() {

        int myOffset;
        byte myByte = 0;
        CsiFileType myBestGuess = null;

        if (null == _fileType) {

            if (msSignature.length <= _testDataSource.length()) {

                _fileType = CsiFileType.OLD_EXCEL;

                for (myOffset = 0; msSignature.length > myOffset; myOffset++) {

                    if (msSignature[myOffset] != _testDataSource.get(myOffset)) {

                        _fileType = null;
                    }
                }
                _fileTypeStatus[CsiFileType.OLD_EXCEL.ordinal()] = CsiFileType.OLD_EXCEL.equals(_fileType);
//                _dataReader = new ReadXlsBlock();

            }
            if (null == _fileType) {

                for (myOffset = 0; _testDataSource.length() > myOffset; myOffset++) {

                    myByte = _testDataSource.get(myOffset);

                    if ((' ' != myByte) && ('\t' != myByte) && ('\n' != myByte) && ('\r' != myByte)) {

                        break;
                    }
                }
                switch (myByte) {

                    case '<' :

                        _fileTypeStatus[CsiFileType.JSON.ordinal()] = false;
                        _fileTypeStatus[CsiFileType.XML.ordinal()] = true;
                        break;

                    case '{' :

                        _fileTypeStatus[CsiFileType.XML.ordinal()] = false;
                        _fileTypeStatus[CsiFileType.JSON.ordinal()] = true;
                        break;

                    default :

                        _fileTypeStatus[CsiFileType.JSON.ordinal()] = false;
                        _fileTypeStatus[CsiFileType.XML.ordinal()] = false;
                        _fileTypeStatus[CsiFileType.CSV.ordinal()] = true;
                        break;
                }
            }
        }
        if (null == _fileType) {

            boolean myEmptyFlag = true;

            for (int i = 0; _fileTypeStatus.length > i; i++) {

                if (null == _fileTypeStatus[i]) {

                    myEmptyFlag = false;
                    _fileType = null;

                } else if (_fileTypeStatus[i]) {

                    if (null == myBestGuess) {

                        myBestGuess = CsiFileType.values()[i];
                    }

                    if ((null == _fileType) && myEmptyFlag) {

                        _fileType = CsiFileType.values()[i];

                    } else {

                        _fileType = null;
                    }

                    myEmptyFlag = false;
                }
            }
            if ((null == myBestGuess) && (null != _fileTypeStatus[CsiFileType.getDefault().ordinal()])) {

                myBestGuess = CsiFileType.getDefault();
            }
        }
        return (null != _fileType) ? _fileType : myBestGuess;
    }

    private void exitWizard() {

        cancelUpload();

        returnSelection();
        if (null != _wizard) {

            _wizard.destroy();
            _wizard = null;
        }
        clickPrior();
        destroy();
    }
}
