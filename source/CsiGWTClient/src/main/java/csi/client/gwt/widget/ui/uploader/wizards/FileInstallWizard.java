package csi.client.gwt.widget.ui.uploader.wizards;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.Wizard;
import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.panels.*;
import csi.client.gwt.csiwizard.widgets.FileParsingWidget;
import csi.client.gwt.events.ResourceSelectionEvent;
import csi.client.gwt.events.ResourceSelectionEventHandler;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.ui.uploader.UnsupportedUpload;
import csi.client.gwt.widget.ui.uploader.UploaderControl;
import csi.client.gwt.widget.ui.uploader.wizards.components.InstallerColumnDisplay;
import csi.client.gwt.widget.ui.uploader.wizards.components.InstallerColumnPanel;
import csi.client.gwt.widget.ui.uploader.wizards.support.FormatValue;
import csi.client.gwt.widget.ui.uploader.wizards.support.ReadBlock;
import csi.client.gwt.widget.ui.uploader.wizards.support.ReadNonBinaryBlock;
import csi.server.common.dto.Response;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.installed_tables.ColumnParameters;
import csi.server.common.dto.installed_tables.TableInstallRequest;
import csi.server.common.dto.installed_tables.TableParameters;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.*;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.service.api.UploadServiceProtocol;
import csi.server.common.util.Format;
import csi.server.common.util.ValuePair;

import java.util.*;

/**
 * Created by centrifuge on 10/26/2015.
 */
public abstract class FileInstallWizard extends Wizard implements FormatValue {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected FileParsingWidget fileParser;
    protected SingleEntryWizardPanel fileParserPanel;
    private InstallerColumnPanel gridPanel;
    private ColumnMappingPanel mapPanel;
    private ResourceSelectorPanel namingPanel;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final int SCAN_BLOCK_SIZE = 65536;

    protected static final char[] _decimalDigit = new char[] {'0','1', '2', '3', '4', '5', '6', '7', '8', '9'};

    protected static final String _txtEncodingLabel = _constants.installFileWizard_SelectFile_Encoding();
    protected static String[] encodingChoices = null;
    protected static final int _badIndex = -4;

    protected MainPresenter _mainPresenter = null;
    protected String _fileName = null;
    protected String _tableName = null;
    protected String _tablePath = null;
    protected String _prefix = null;
    protected ColumnParameters[] _columnParameters = null;
    protected int _columnCount = 0;
    protected String[] _dataRowData = null;
    protected int _securityTagsIndex = _badIndex;
    protected int _capcoIndex = _badIndex;
    protected int _sharingIndex = _badIndex;
    protected TableInstallRequest _request = null;
    protected InstalledTable _table = null;
    protected boolean _tableReload = false;

    private UploaderControl _control;
    private CsiFileType _fileType;
    private WizardDialog _priorDialog;
    private Int8Array _testDataSource = null;
    private Object _testDataAccess = null;
    private Map<String, Integer> _rejectionMap = null;
    private List<InstallerColumnDisplay> _columnDisplay = null;
    private int _encodingMenuId = 0;
    private Map<String, ColumnParameters> _columnMapping;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Static Methods                                      //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public static FileInstallWizard create(CsiFileType fileTypeIn, WizardDialog priorDialogIn,
                                           UploaderControl controlIn, boolean fixNullsIn,
                                           InstalledTable tableIn, String titleIn, String helpIn) {

        try {

            switch (fileTypeIn) {

                case CSV: {

                    return new CsvInstallWizard(priorDialogIn, controlIn, fixNullsIn, tableIn, titleIn, helpIn);
                }
                case TEXT: {

                    return new TextInstallWizard(priorDialogIn, controlIn, fixNullsIn, tableIn, titleIn, helpIn);
                }

                case DUMP: {

                    return new BcpInstallWizard(priorDialogIn, controlIn, tableIn, titleIn, helpIn);
                }

                case OLD_EXCEL: {

                    return new OldExcelInstallWizard(priorDialogIn, controlIn, tableIn, titleIn, helpIn);
                }

                case NEW_EXCEL: {

                    return new NewExcelInstallWizard(priorDialogIn, controlIn, tableIn, titleIn, helpIn);
                }

                case XML: {

                    return new XmlInstallWizard(priorDialogIn, controlIn, tableIn, titleIn, helpIn);
                }

                case JSON: {

                    return new JsonInstallWizard(priorDialogIn, controlIn, tableIn, titleIn, helpIn);
                }

                default:

                    throw new UnsupportedUpload(_constants.fileInstallWizard_UnrecognizedType());
            }

        } catch(Exception myException) {

            Dialog.showException(titleIn, myException);
            return new DummyFileInstallWizard(priorDialogIn, controlIn, tableIn, titleIn, helpIn);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public abstract String onDataTypeChange(int dataIdIn, CsiDataType dataTypeIn);
    protected abstract TableInstallRequest buildInstallRequest();
    protected abstract TableInstallRequest buildUpdateRequest();
    protected abstract void prepareFileParser();
    protected abstract List<InstallerColumnDisplay> buildColumnDisplayList(ColumnParameters[] columnParametersIn);
    protected abstract void determineDataFormat();
    protected abstract void resetDataFormat();
    protected abstract String getColumnName(int indexIn);
    protected abstract void initializeColumnDefinitions(ColumnParameters[] parametersIn);


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    SelectionChangedHandler<String> handleEncodingChange = new SelectionChangedHandler<String>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            int myChoice = fileParser.getDropDownSelectedIndex(_encodingMenuId);

            CsiEncoding myEncoding = ((0 < myChoice) && (CsiEncoding.values().length >= myChoice))
                    ? CsiEncoding.values()[myChoice - 1] : null;

            try {

                if (((ReadNonBinaryBlock)_control.getDataReader()).changeEncoding(myEncoding)) {

                    determineDataFormat();
                }

            } catch (Exception myException) {

                try {

                    myEncoding = ((ReadNonBinaryBlock) _control.getDataReader()).determineEncoding();

                    if (null != myEncoding) {

                        fileParser.setDropDownChoice(_encodingMenuId, (myEncoding.ordinal() + 1));

                    } else {

                        resetDataFormat();
                    }

                } catch (Exception myNextException) {

                    resetDataFormat();
                }
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public void run() {

        if (null != _fileType) {

            _control.onError(new CentrifugeException(
                    _constants.fileInstallWizard_UnsupportedType(Format.value(_fileType),
                                                                Format.value(_control.getFileName()))));

        } else {

            _control.onError(new CentrifugeException(
                    _constants.fileInstallWizard_UnrecognizedType2(Format.value(_control.getFileName()))));
        }
    }

    public boolean isAvailable() {

        return _fileType.isSupported();
    }

    public void recordSuccess(String nameIn) {

        _tableName = null;
        _tablePath = null;
        _request = null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected FileInstallWizard(CsiFileType fileTypeIn, WizardDialog priorDialogIn, UploaderControl controlIn,
                                InstalledTable tableIn, String titleIn, String helpIn) {

        super(null, null);

        initialize(fileTypeIn, priorDialogIn, controlIn, tableIn);
    }

    //
    //
    //
    protected FileInstallWizard(CsiFileType fileTypeIn, WizardDialog priorDialogIn, UploaderControl controlIn,
                                String titleIn, String helpTargetIn, String finalizeButtonIn,
                                int basePanelIn, InstalledTable tableIn) {

        super (titleIn, helpTargetIn, finalizeButtonIn, basePanelIn);

        initialize(fileTypeIn, priorDialogIn, controlIn, tableIn);
    }

    protected void abort(String errorStringIn) {

        _control.onError(new CentrifugeException(errorStringIn));
    }

    protected void initializeEncodingDropDown(int indexIn, boolean isRequiredIn) {

        _encodingMenuId = indexIn;
        fileParser.initializeDropDown(_encodingMenuId, _txtEncodingLabel,
                encodingChoices, handleEncodingChange, isRequiredIn);
    }

    @Override
    protected void displayNewPanel(final int indexIn, ClickEvent eventIn) {

        if (0 == indexIn) {

            try {

                prepareFileParser();

            } catch (Exception myException) {

                Dialog.showException(myException);
            }
            hideWatchBox();

        } else {

            if (1 == indexIn) {

                if (null != _table) {

                    prepareColumnMapPanel();

                } else {

                    prepareColumnGridPanel(_tableName, true);
                }

            } else if (null == _table) {

                try {

                    if (indexIn == _securityTagsIndex) {

                        SecurityTagsPanel myPanel
                                = new SecurityTagsPanel<ColumnParameters>(this, Arrays.asList(_columnParameters));

                        displayPanel(myPanel, myPanel.getInstructions(Dialog.txtNextButton), false);

                    } else if (indexIn == _capcoIndex) {

                        CapcoPanel myPanel = new CapcoPanel<ColumnParameters>(this, Arrays.asList(_columnParameters));

                        displayPanel(myPanel, myPanel.getInstructions(Dialog.txtNextButton), false);

                    } else if (indexIn == _sharingIndex) {

                    } else {

                        nameInstalledTable();
                    }

                } catch(Exception myException) {

                    Dialog.showException(myException);
                }
            }
        }
    }

    protected void processTags(AbstractWizardPanel activePanelIn, ClickEvent eventIn) {

    }

    @Override
    protected void execute(AbstractWizardPanel activePanelIn, ClickEvent eventIn) {

        if (null != _table) {

            _request = buildUpdateRequest();
            _request.setTableId(_table.getUuid());
            _control.doUpdateRequest(_request);
            return;

        } else {

            _request = buildInstallRequest();

            if (null != _request) {

                TableParameters myTableParameters = _request.getTableParameters();

                if (null != myTableParameters) {

                    myTableParameters.setTableName(namingPanel.getName());
                    myTableParameters.setRemarks(namingPanel.getRemarks());
                    _request.setTagsInfo((_badIndex != _securityTagsIndex)
                                                ? ((SecurityTagsPanel)getPanel(_securityTagsIndex)).getResults()
                                                : getDefaultTags());
                    _request.setCapcoInfo((_badIndex != _capcoIndex)
                                                ? ((CapcoPanel)getPanel(_capcoIndex)).getResults()
                                                : getDefaultCapco());

                }
            }
        }
        _control.beginInstall(_request);
    }

    @Override
    protected void cancel(AbstractWizardPanel activePanelIn, ClickEvent eventIn) {

        _control.cancel();
    }

    protected ReadBlock getDataReader() {

        return _control.getDataReader();
    }

    protected void prepareColumnMapPanel() {

        if (null != _table) {

            try {

                List<InstalledColumn> myColumnList = _table.getColumns();
                List<FieldDef> myFieldList = new ArrayList<FieldDef>();

                _columnMapping = new TreeMap<String, ColumnParameters>();
                initializeColumnDefinitions(_columnParameters);

                for (int i = 0; _columnParameters.length > i; i++) {

                    ColumnParameters myColumn = _columnParameters[i];
                    FieldDef myField = new FieldDef(getColumnName(i + 1), FieldType.COLUMN_REF, myColumn.getDataType());

                    myFieldList.add(myField);
                    _columnMapping.put(myField.getUuid(), myColumn);
                }

                mapPanel = new ColumnMappingPanel(null, 460,340, null, myColumnList, myFieldList, "Required Data Columns", "Available Data Columns");

                displayPanel(mapPanel,
                        _constants.installFileWizard_ConfigureTable_Directions(Format.value(_constants.gridWidget_Exclude()),
                                Format.value(_constants.gridWidget_Rename())), true);

            } catch (Exception myException) {

                Dialog.showException(_constants.installFileWizard_MapCreationError_Message(), myException);
            }

        } else {

            showWatchBox(_constants.fileInstallWizard_RetrievingNames());
        }
    }

    protected void prepareColumnGridPanel(String nameIn, boolean forceIn) {

        try {

            _columnDisplay = buildColumnDisplayList(_columnParameters);

            gridPanel = new InstallerColumnPanel(this, "Table Configuration", _fileType,
                                                    null, _columnDisplay, this, forceIn);
            displayPanel(gridPanel,
                    _constants.installFileWizard_ConfigureTable_Directions(Format.value(_constants.gridWidget_Exclude()),
                            Format.value(_constants.gridWidget_Rename())), false);

        } catch (Exception myException) {

            Dialog.showException(_constants.installFileWizard_GridCreationError_Message(), myException);
        }
    }

    protected void extractGridInformation() {

        List<InstallerColumnDisplay> myList = gridPanel.getDisplayList();

        if (null != myList) {

            for (InstallerColumnDisplay myDisplay : myList) {

                int myIndex = myDisplay.getColumnNumber() - 1;
                ColumnParameters myColumn = _columnParameters[myIndex];

                myColumn.setName(myDisplay.getName());
                myColumn.setDataType(CsiDataType.getValue(myDisplay.getDataType()));
                myColumn.setIncluded(myDisplay.getInclude());
            }
        }
    }

    protected void finalizeColumnParameters() {

        // Map<String, ColumnParameters> _columnMapping;

        List<ValuePair<InstalledColumn, FieldDef>> myList = mapPanel.getResults();

        if (null != myList) {

            for (ValuePair<InstalledColumn, FieldDef> myPair : myList) {

                InstalledColumn myRequiredColumn = myPair.getValue1();
                FieldDef myDataColumn = myPair.getValue2();
                String myKey = (null != myDataColumn) ? myDataColumn.getUuid() : null;
                ColumnParameters myParameters = (null != myKey) ? _columnMapping.get(myKey) : null;

                if (null != myParameters) {

                    myParameters.setLocalId(myRequiredColumn.getLocalId());
                    myParameters.setIncluded(true);
                }
            }
        }
    }

    protected Int8Array getDataSource() {

        return _testDataSource;
    }

    protected Object getDataAccess() {

        return _testDataAccess;
    }

    protected void recognizeParsingError(int locationIn, final Throwable exceptionIn) {

        final String myTitle = _constants.installFileWizard_ParsingError_Title()
                                + " (" + Integer.toString(locationIn) + ")";
        disableNext();
        hideWatchBox();

        DeferredCommand.add(new Command() {
            public void execute() {

                Dialog.showException(myTitle, exceptionIn);
            }
        });
    }

    public CsiFileType getFileType() {

        return _fileType;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initialize(CsiFileType fileTypeIn, WizardDialog priorDialogIn,
                            UploaderControl controlIn, InstalledTable tableIn) {

        _fileType = fileTypeIn;
        _priorDialog = priorDialogIn;
        _control = controlIn;
        _testDataSource = _control.getTestBlock();
        _testDataAccess = _control.getBlockAccess();
        _fileName = _control.getFileName();
        _tableName = (null != _fileName) ? _fileName.substring(0, _fileName.indexOf('.')) : "";

        if (null != tableIn) {

            _table = tableIn;
            _tableReload = true;
            _tablePath = _table.getTablePath();
            _tableName = _table.getTableName();
            _finalDisplayIndex = 1;
            hideWatchBox();

        } else {

            setPanelCount(2);
            _finalDisplayIndex++;
            createNamingPanel();
        }

        setPriorDialog(_priorDialog);

        if (null == encodingChoices) {

            encodingChoices = new String[CsiEncoding.values().length + 1];
            encodingChoices[0] = "";

            for (int i = 0; CsiEncoding.values().length > i; i++) {

                encodingChoices[i + 1] = CsiEncoding.values()[i].getLabel();
            }
        }
    }

    protected String getDefaultColumnName(int columnIdIn) {

        char[] myColumnName = new char[] {'c', 'o', 'l', '_', '0', '0', '0'};

        myColumnName[4] = _decimalDigit[columnIdIn / 100];
        myColumnName[5] = _decimalDigit[(columnIdIn % 100) / 10];
        myColumnName[6] = _decimalDigit[columnIdIn % 10];

        return new String(myColumnName);
    }

    protected void setPanelCount(int countIn) {

        UserSecurityInfo myUserInfo = WebMain.injector.getMainPresenter().getUserInfo();
        boolean mySetSecurityOk = (null != myUserInfo) ? myUserInfo.getCanSetSecurity() : false;

        _finalDisplayIndex = Math.max(0, countIn - 1);

        if (mySetSecurityOk && (null == _table)) {

            if (WebMain.getClientStartupInfo().isEnforceSecurityTags()
                    || WebMain.getClientStartupInfo().isProvideTagBanners()) {

                _securityTagsIndex = ++_finalDisplayIndex;
            }
            if (WebMain.getClientStartupInfo().isEnforceCapcoRestrictions()
                    || WebMain.getClientStartupInfo().isProvideCapcoBanners()) {

                _capcoIndex = ++_finalDisplayIndex;
            }
        }
    }

    protected CapcoInfo getDefaultCapco() {

        CapcoInfo myCapcoInfo = null;

        if (WebMain.getClientStartupInfo().isEnforceCapcoRestrictions()
                || WebMain.getClientStartupInfo().isProvideCapcoBanners()) {

            myCapcoInfo = new CapcoInfo();
            myCapcoInfo.setMode(CapcoSource.USE_DEFAULT);
        }
        return myCapcoInfo;
    }

    protected SecurityTagsInfo getDefaultTags() {

        SecurityTagsInfo myTagsInfo = null;

        if (WebMain.getClientStartupInfo().isEnforceSecurityTags()
                || WebMain.getClientStartupInfo().isProvideTagBanners()) {

            myTagsInfo = new SecurityTagsInfo();
            myTagsInfo.setMode(CapcoSource.USE_DEFAULT);
        }
        return myTagsInfo;
    }

    //
    // Final display -- name or select an Installed Table to generate
    //
    protected void nameInstalledTable() {
        //
        // Wire in event handlers
        //
        namingPanel.addResourceSelectionEventHandler(handleResourceSelection);
        namingPanel.addReloadButtonClickHandler(handleReloadClickHandler);

        displayPanel(namingPanel, _constants.fileInstallWizard_TableNamingInstructions(_prefix), true);
    }

    protected void createNamingPanel() {

        _prefix = getMainPresenter().getUserName() + "." + _fileType.getExtension() + ".";

        namingPanel = new ResourceSelectorPanel<ResourceBasics>(this, ResourceSelectorPanel.SelectorMode.NEW, _prefix);
        namingPanel.initializeDisplay(AclResourceType.DATA_TABLE, _constants.fileInstallWizard_ExcludedTableNames(), _tableName);
        retrieveInstalledTableLists();
    }

    //
    // Request the list installed tables from the server to prevent naming conflicts
    //
    private void retrieveInstalledTableLists() {

        VortexFuture<List<ResourceBasics>> myVortexFuture = WebMain.injector.getVortex().createFuture();

        try {

            myVortexFuture.addEventHandler(namingPanel.handleSingleListRequestResponse);
            myVortexFuture.execute(UploadServiceProtocol.class).getTableOverWriteControlList(_fileType);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

   private ClickHandler handleReloadClickHandler = new ClickHandler() {
       @Override
       public void onClick(ClickEvent clickEvent) {

           VortexFuture<Response<String, InstalledTable>> myVortexFuture = WebMain.injector.getVortex().createFuture();

           try {

               myVortexFuture.addEventHandler(new AbstractVortexEventHandler<Response<String, InstalledTable>>() {
                   @Override
                   public void onSuccess(Response<String, InstalledTable> result) {
                       _table = result.getResult();
                       displayNewPanel(1, clickEvent);
                   }
               });
               myVortexFuture.execute(UploadServiceProtocol.class).getInstalledTable(namingPanel.getSelection().getKey());

           } catch (Exception myException) {

               Dialog.showException(myException);
           }
       }
   };

    private ResourceSelectionEventHandler handleResourceSelection = new ResourceSelectionEventHandler() {
        @Override
        public void onResourceSelection(ResourceSelectionEvent event) {

        }
    };

    private MainPresenter getMainPresenter() {

        if (null == _mainPresenter) {

            _mainPresenter = WebMain.injector.getMainPresenter();
        }
        return _mainPresenter;
    }
}
