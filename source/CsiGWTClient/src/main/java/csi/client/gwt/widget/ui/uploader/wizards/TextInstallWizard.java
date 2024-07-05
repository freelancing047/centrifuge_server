package csi.client.gwt.widget.ui.uploader.wizards;

import java.util.LinkedList;
import java.util.List;

import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.panels.SingleEntryWizardPanel;
import csi.client.gwt.csiwizard.widgets.FileParsingWidget;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.WatchingParent;
import csi.client.gwt.widget.ui.uploader.UploaderControl;
import csi.client.gwt.widget.ui.uploader.wizards.support.BlockAnalysis;
import csi.client.gwt.widget.ui.uploader.wizards.support.ReadNonBinaryBlock;
import csi.client.gwt.widget.ui.uploader.wizards.support.ReadTextBlock;
import csi.server.common.dto.installed_tables.NonBinaryInstallRequest;
import csi.server.common.dto.installed_tables.TableInstallRequest;
import csi.server.common.dto.installed_tables.TxtParameters;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.CsiEncoding;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.Format;

/**
 * Created by centrifuge on 10/19/2015.
 */
public class TextInstallWizard extends SpreadsheetInstallWizard {


    private enum DropDownId {

        ENCODING,
        DELIMITER,
        NULL_INDICATOR
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final int ROW_LIMIT = 8;

    private ReadTextBlock _dataReader;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public TextInstallWizard(WizardDialog priorDialogIn, UploaderControl controlIn,
                             boolean fixNullsIn, InstalledTable tableIn, String titleIn, String helpIn) {

        super(CsiFileType.TEXT, priorDialogIn, controlIn, fixNullsIn, tableIn, titleIn, helpIn);

        _dataReader = (ReadTextBlock)getDataReader();
    }

    @Override
    public void run() {

        if (isAvailable()) {

            prepareFileParser();

        } else {

            super.run();
        }
    }

    public String onDataTypeChange(int dataIdIn, CsiDataType dataTypeIn) {

        if ((null != _columnParameters) && (0 < dataIdIn)
                && (_columnParameters.length >= dataIdIn) && (_dataRowData.length >= dataIdIn)) {

            return _dataRowData[dataIdIn - 1];
        }
        return "?????";
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void prepareFileParser() {

        try {

            fileParser = new FileParsingWidget(3);
            fileParserPanel = new SingleEntryWizardPanel(this, "File Selector", fileParser,
                    _constants.installFileWizard_FileFormat_Prompt());

            prepareDisplay();

            displayPanel(fileParserPanel,
                    _constants.installFileWizard_CsvFileFormat_Directions(),
                    false);

        } catch(Exception myException) {

            Display.error(Format.value(myException));
        }
    }

    protected void determineDataFormat() {

        try {

            List<Integer> myCharacterBlock = _dataReader.getCharacterBlock();

            if (null != myCharacterBlock) {

                CsiEncoding myEncoding = _dataReader.determineEncoding();

                if (null == _nullIndicator) {

                    _nullIndicator = "";
                }
                /*
                    Determine column delimiter
                 */
                if (null == _delimiter) {

                    _delimiter = (new BlockAnalysis(_dataReader)).getDelimiter();

                    if (null == _delimiter) {

                        myEncoding = _dataReader.determineEncoding(true);
                        _delimiter = (new BlockAnalysis(_dataReader)).getDelimiter();
                    }
                }
                fileParser.resetDropDowns();

                if (null != _delimiter) {

                    fileParser.setDropDownChoice(DropDownId.DELIMITER.ordinal(), _delimiter);
                }

                if (null != myEncoding) {

                    fileParser.setDropDownChoice(DropDownId.ENCODING.ordinal(), myEncoding.getLabel());
                }

                if (null != _nullIndicator) {

                    fileParser.setDropDownChoice(DropDownId.NULL_INDICATOR.ordinal(), _nullIndicator);
                }
            }
            prepareDataDisplay();

        } catch(Exception myException){

            recognizeParsingError(110, myException);
        }
    }

    protected TableInstallRequest buildInstallRequest() {

        NonBinaryInstallRequest myRequest = new NonBinaryInstallRequest();

        try {

            TxtParameters myParameters = new TxtParameters();

            extractGridInformation();

            myParameters.setTableName(_tableName);
            myParameters.setColumnParameterList(_columnParameters);

            myParameters.setDelimiter(_delimiter);
            myParameters.setNullIndicator(_nullIndicator);
            myParameters.setDataStart(_dataRow);
            myParameters.setFixNulls(_fixNulls);

            myRequest.setEncoding(((ReadNonBinaryBlock) getDataReader()).determineEncoding());
            myRequest.setTableParameters(myParameters);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }

        return myRequest;
    }

    protected TableInstallRequest buildUpdateRequest() {

        NonBinaryInstallRequest myRequest = new NonBinaryInstallRequest();

        try {

            TxtParameters myParameters = new TxtParameters();

            finalizeColumnParameters();

            myParameters.setTableName(_tableName);
            myParameters.setColumnParameterList(_columnParameters);

            myParameters.setDelimiter(_delimiter);
            myParameters.setNullIndicator(_nullIndicator);
            myParameters.setDataStart(_dataRow);
            myParameters.setFixNulls(_fixNulls);

            myRequest.setEncoding(((ReadNonBinaryBlock) getDataReader()).determineEncoding());
            myRequest.setTableParameters(myParameters);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }

        return myRequest;
    }

    protected void retrieveFormattedData(int sheetIndexIn) {

        _gridData = new LinkedList<String[]>();
        _columnCount = 0;
        _columnParameters = null;

        try {

            if (null != _dataReader.determineEncoding()) {

                if (null != _delimiter) {

                    _dataReader.setDelimiter(_delimiter);
                    _columnCount = _dataReader.getNumberedGridData(_gridData, 15);

                } else {

                    _columnCount = 2;
                    _dataReader.getNumberedListData(_gridData, 15, 0);
                }
            }

        } catch(Exception myException){

            recognizeParsingError(120, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initializeDropDowns() {

        initializeEncodingDropDown(DropDownId.ENCODING.ordinal(), true);
        initializeDelimiterDropDown(DropDownId.DELIMITER.ordinal(), true);
        initializeNullIndicatorDropDown(DropDownId.NULL_INDICATOR.ordinal(), true);
    }

    private void prepareDisplay() {

        initializeDropDowns();
        determineDataFormat();
        replaceInstructions(_constants.installFileWizard_CsvFileFormat_Directions());
    }
}
