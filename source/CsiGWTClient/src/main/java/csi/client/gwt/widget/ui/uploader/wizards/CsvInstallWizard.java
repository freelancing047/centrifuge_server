package csi.client.gwt.widget.ui.uploader.wizards;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.csiwizard.panels.SingleEntryWizardPanel;
import csi.client.gwt.csiwizard.widgets.FileParsingWidget;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.list_boxes.CsiOverlayTextBox;
import csi.client.gwt.widget.ui.uploader.UploaderControl;
import csi.client.gwt.widget.ui.uploader.wizards.support.BlockAnalysis;
import csi.client.gwt.widget.ui.uploader.wizards.support.ReadCsvBlock;
import csi.client.gwt.widget.ui.uploader.wizards.support.ReadNonBinaryBlock;
import csi.server.common.dto.installed_tables.CsvParameters;
import csi.server.common.dto.installed_tables.NonBinaryInstallRequest;
import csi.server.common.dto.installed_tables.TableInstallRequest;
import csi.server.common.enumerations.CsiColumnQuote;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.CsiEncoding;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.model.column.InstalledColumn;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.Format;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 7/2/2015.
 */
public class CsvInstallWizard extends SpreadsheetInstallWizard {


    private enum DropDownId {

        ENCODING,
        DELIMITER,
        NULL_INDICATOR,
        QUOTE
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final int ROW_LIMIT = 8;

    private static final String _txtQuoteCharacterLabel = _constants.installFileWizard_SelectFile_QuoteCharacter();

    private static List<ValuePair<String,String>> _quotingChoices = null;

    private ReadCsvBlock _dataReader;
    private Character _quote = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    SelectionChangedHandler<String> handleQuotingChange = new SelectionChangedHandler<String>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            _quote = fileParser.getDropDownSelectionCharacter(DropDownId.QUOTE.ordinal());
            prepareDataDisplay();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsvInstallWizard(WizardDialog priorDialogIn, UploaderControl controlIn,
                            boolean fixNullsIn, InstalledTable tableIn, String titleIn, String helpIn) {

        super(CsiFileType.CSV, priorDialogIn, controlIn, fixNullsIn, tableIn, titleIn, helpIn);

        _dataReader = (ReadCsvBlock)getDataReader();
//        setPanelCount(2);

        if (null == _quotingChoices) {

            _quotingChoices = new ArrayList<ValuePair<String, String>>();
            _quotingChoices.add(new ValuePair<String, String>("", null));

            for (int i = 0; CsiColumnQuote.values().length > i; i++) {

                _quotingChoices.add(new ValuePair(CsiColumnQuote.values()[i].getLabel(),
                        String.valueOf(CsiColumnQuote.values()[i].getCharacter())));
            }
        }
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

            fileParser = new FileParsingWidget(4);
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

        int myLocation = 210;

        try {

            List<Integer> myCharacterBlock = _dataReader.getCharacterBlock();

            if (null != myCharacterBlock) {

                myLocation = 211;
                CsiEncoding myEncoding = _dataReader.determineEncoding();

                if (null == _nullIndicator) {

                    _nullIndicator = "";
                }

                /*
                    Determine column delimiter and quoting character if any
                 */
                if ((null == _delimiter) || (null == _quote)) {

                    try {

                        BlockAnalysis myAnalysis = new BlockAnalysis(_dataReader, _delimiter, _quote);

                        _delimiter = myAnalysis.getDelimiter();
                        _quote = myAnalysis.getQuotingCharacter();

                    } catch (Exception IGNORE) {}
                }
                if (null == _delimiter) {

                    myLocation = 212;
                    myEncoding = _dataReader.determineEncoding(true);
                    BlockAnalysis myAnalysis = new BlockAnalysis(_dataReader, _delimiter, _quote);
                    myLocation = 213;
                    _delimiter = myAnalysis.getDelimiter();
                    myLocation = 214;
                    _quote = myAnalysis.getQuotingCharacter();
                }
                myLocation = 215;
                fileParser.resetDropDowns();

                myLocation = 216;
                if (null != _quote) {

                    fileParser.setDropDownChoice(DropDownId.QUOTE.ordinal(), _quote);
                }

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
            myLocation = 217;
            prepareDataDisplay();

        } catch(Exception myException){

            recognizeParsingError(myLocation, myException);
        }
    }

    protected TableInstallRequest buildInstallRequest() {

        NonBinaryInstallRequest myRequest = new NonBinaryInstallRequest();

        try {

            extractGridInformation();

            CsvParameters myParameters = new CsvParameters();

            myParameters.setTableName(_tableName);
            myParameters.setColumnParameterList(_columnParameters);
            myParameters.setDelimiter(_delimiter);
            myParameters.setNullIndicator(_nullIndicator);
            myParameters.setQuote(_quote);
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

            finalizeColumnParameters();

            CsvParameters myParameters = new CsvParameters();

            myParameters.setTableName(_tableName);
            myParameters.setColumnParameterList(_columnParameters);
            myParameters.setDelimiter(_delimiter);
            myParameters.setNullIndicator(_nullIndicator);
            myParameters.setQuote(_quote);
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

        int myLocation = 220;

        _gridData = new LinkedList<String[]>();
        _columnCount = 0;
        _columnParameters = null;

        try {

            if (null != _dataReader.determineEncoding()) {

                if (null != _quote) {

                    myLocation = 221;
                    _dataReader.setQuote(_quote);

                } else {

                    myLocation = 222;
                    _dataReader.disableQuoting();
                }

                if (null != _delimiter) {

                    myLocation = 223;
                    _dataReader.setDelimiter(_delimiter);
                    myLocation = 224;
                    _columnCount = _dataReader.getNumberedGridData(_gridData, 15);

                } else {

                    _columnCount = 2;
                    myLocation = 225;
                    _dataReader.clearDelimiter();
                    myLocation = 226;
                    _dataReader.getNumberedListData(_gridData, 15, 0);
                }
            }

        } catch(Exception myException){

            recognizeParsingError(myLocation, myException);
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
        fileParser.initializeDropDown(DropDownId.QUOTE.ordinal(), _txtQuoteCharacterLabel, _quotingChoices,
                                        CsiOverlayTextBox.ValidationMode.CHARACTER,
                                        handleQuotingChange, false);
    }

    private void prepareDisplay() {

        initializeDropDowns();
        determineDataFormat();
        replaceInstructions(_constants.installFileWizard_CsvFileFormat_Directions());
    }
}
