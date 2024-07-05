package csi.client.gwt.widget.ui.uploader.wizards;

import java.util.LinkedList;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.panels.SingleEntryWizardPanel;
import csi.client.gwt.csiwizard.widgets.FileParsingWidget;
import csi.client.gwt.file_access.uploader.XlsxProcessor;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.list_boxes.CsiStringListBox;
import csi.client.gwt.widget.ui.uploader.UploaderControl;
import csi.client.gwt.widget.ui.uploader.wizards.support.ReadXlsxBlock;
import csi.server.common.dto.installed_tables.ColumnParameters;
import csi.server.common.dto.installed_tables.NewExcelInstallRequest;
import csi.server.common.dto.installed_tables.NewExcelParameters;
import csi.server.common.dto.installed_tables.SheetFormat;
import csi.server.common.dto.installed_tables.TableInstallRequest;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.Format;
import csi.server.common.util.uploader.XlsxRow;

/**
 * Created by centrifuge on 10/19/2015.
 */
public class NewExcelInstallWizard extends SpreadsheetInstallWizard {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    CsiStringListBox workSheetMenu = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private XlsxProcessor _dataAccess = null;
    private ReadXlsxBlock _dataReader = null;
    private String _tableBase = null;
    private CsiDataType[] _dataTypeList = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    SelectionChangedHandler<String> handleWorksheetSelection = new SelectionChangedHandler<String>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            int myChoice = workSheetMenu.getSelectedIndex();

            if (0 == myChoice) {

                workSheetMenu.setSelectedIndex(_activeSheet + 1);

            } else {

                _activeSheet = myChoice - 1;
                _sheetName = _worksheetList.get(_activeSheet);
                _tableName = _tableBase + "." + _sheetName;
                determineDataFormat();
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public NewExcelInstallWizard(WizardDialog priorDialogIn, UploaderControl controlIn,
                                 InstalledTable tableIn, String titleIn, String helpIn) {

        super(CsiFileType.NEW_EXCEL, priorDialogIn, controlIn, false, tableIn, titleIn, helpIn);

        _dataReader = (ReadXlsxBlock)controlIn.getDataReader();
        _tableBase = _tableName;
//        setPanelCount(2);
    }

    @Override
    public void run() {

        if (isAvailable()) {

            _dataAccess = (XlsxProcessor)getDataAccess();

            if (null != _dataAccess) {

                _worksheetList = _dataAccess.getSheetNames();

                if (0 < _worksheetList.size()) {

                    prepareFileParser();

                } else {

                    abort(_constants.newExcelInstallWizard_EmptyExcelFile());
                }
            }

        } else {

            super.run();
        }
    }

    public String onDataTypeChange(int dataIdIn, CsiDataType dataTypeIn) {

        String myDisplay = "";

        if ((null != _columnParameters) && (0 < _dataRow)
                && (0 < dataIdIn) && (_columnParameters.length >= dataIdIn)) {

            XlsxRow myRow = _dataReader.getWorkingRow(_dataRow - 1);

            if (null != myRow) {

                myDisplay = myRow.coerceColumn(dataIdIn, dataTypeIn);
            }
        }
        return myDisplay;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void prepareFileParser() {

        try {

            HorizontalPanel myPanel = new HorizontalPanel();
            InlineLabel mySpacer = new InlineLabel(_constants.plusplus());

            mySpacer.getElement().getStyle().setColor(Dialog.txtDefaultBackground);

            workSheetMenu = new CsiStringListBox();

            if (_worksheetList.size() > 1) {

                workSheetMenu.addItem("");
                for (int i = 0; _worksheetList.size() > i; i++) {

                    workSheetMenu.addItem(_worksheetList.get(i));
                }
                _activeSheet = -1;
                workSheetMenu.addSelectionChangedHandler(handleWorksheetSelection);

            } else {

                workSheetMenu.addItem(_worksheetList.get(0));
            }
            workSheetMenu.setSelectedIndex(0);

            myPanel.add(new InlineLabel(_constants.newExcelInstallWizard_WorksheetPrompt()));
            myPanel.add(mySpacer);
            myPanel.add(workSheetMenu);

            fileParser = new FileParsingWidget(myPanel, 0);
            fileParserPanel = new SingleEntryWizardPanel(this, "File Selector", fileParser, null);

            if (1 < _worksheetList.size()) {

                fileParser.requireDropDown(workSheetMenu);
            }

            prepareDisplay();

            displayPanel(fileParserPanel,
                    _constants.installFileWizard_ExcelFileFormat_Directions(),
                    false);

        } catch(Exception myException) {

            Display.error(Format.value(myException));
        }
    }

    @Override
    protected TableInstallRequest buildInstallRequest() {

        NewExcelInstallRequest myRequest = null;

        try {

            myRequest = new NewExcelInstallRequest(_dataAccess.getStringListEntry());

            extractGridInformation();

            NewExcelParameters myParameters = new NewExcelParameters();

            myParameters.setTableName(_tableName);
            myParameters.setColumnParameterList(_columnParameters);
            myParameters.setDataStart(_dataRow);
            myParameters.setEntry(_dataAccess.getWorkSheetEntry(_activeSheet));

            myRequest.setTableParameters(myParameters);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }

        return myRequest;
    }

    @Override
    protected TableInstallRequest buildUpdateRequest() {

        NewExcelInstallRequest myRequest = null;

        try {

            myRequest = new NewExcelInstallRequest(_dataAccess.getStringListEntry());

            finalizeColumnParameters();

            NewExcelParameters myParameters = new NewExcelParameters();

            myParameters.setTableName(_tableName);
            myParameters.setColumnParameterList(_columnParameters);
            myParameters.setDataStart(_dataRow);
            myParameters.setEntry(_dataAccess.getWorkSheetEntry(_activeSheet));

            myRequest.setTableParameters(myParameters);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }

        return myRequest;
    }

    @Override
    protected void determineDataFormat() {

        CsiDataType[] myDataTypeList = null;

        _dataAccess = (XlsxProcessor)getDataAccess();

        if (null != _dataAccess) {

            SheetFormat myFormat = _dataAccess.proposeFormat(_activeSheet);

            if ((null != myFormat) && (null != fileParser)) {

                fileParser.setColumnNames(myFormat.getColumnNamesRow());
                fileParser.setDataStart(myFormat.getFirstDataRow());
                _dataTypeList = myFormat.getDataTypeList();
                _columnParameters = initializeColumnParameters();
                _headerRow = fileParser.getColumnNames();
                _dataRow = fileParser.getDataStart();
            }
            prepareDataDisplay(myDataTypeList);
        }
    }

    @Override
    protected void retrieveFormattedData(int sheetIndexIn) {

        _gridData = new LinkedList<String[]>();
        _columnCount = 0;

        try {

            _sheetName = _dataReader.processSheet(sheetIndexIn, _dataTypeList, Math.max(0, _dataRow- 1)).getSheetName();
            _columnCount = _dataReader.getNumberedGridData(_gridData, 15);

        } catch(Exception myException){

            recognizeParsingError(310, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void prepareDisplay() {

        determineDataFormat();
        replaceInstructions(_constants.installFileWizard_ExcelFileFormat_Directions());
    }

    private ColumnParameters[] initializeColumnParameters() {

        ColumnParameters[] myParameters = null;

        if (null != _dataTypeList) {

            myParameters = new ColumnParameters[_dataTypeList.length];

            if (null != _table) {

                String myTableId = _table.getUuid();

                for (int i = 0; _dataTypeList.length > i; i++) {

                    myParameters[i] = new ColumnParameters(myTableId, _dataTypeList[i], true);
                }

            } else {

                for (int i = 0; _dataTypeList.length > i; i++) {

                    myParameters[i] = new ColumnParameters("", _dataTypeList[i], true, true);
                }
            }
        }
        return myParameters;
    }
}
