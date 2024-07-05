package csi.client.gwt.widget.ui.uploader.wizards;

import java.util.ArrayList;
import java.util.List;

import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import csi.client.gwt.csiwizard.WizardDialog;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.list_boxes.CsiOverlayTextBox;
import csi.client.gwt.widget.ui.uploader.UploaderControl;
import csi.client.gwt.widget.ui.uploader.wizards.components.InstallerColumnDisplay;
import csi.server.common.dto.installed_tables.ColumnParameters;
import csi.server.common.enumerations.*;
import csi.server.common.model.tables.InstalledTable;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 7/2/2015.
 */
public abstract class SpreadsheetInstallWizard extends FileInstallWizard {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final String _txtRowDelimeterLabel = _constants.installFileWizard_SelectFile_RowDelimiter();
    protected static final String _txtColDelimiterLabel = _constants.installFileWizard_SelectFile_ColDelimiter();
    protected static final String _txtNullIndicatorLabel = _constants.installFileWizard_SelectFile_NullIndicator();
    protected static List<ValuePair<String, String>> _delimiterChoices = null;
    protected static List<ValuePair<String, String>> _nullChoices = null;

    protected Character _delimiter = null;
    protected String _nullIndicator = null;
    protected List<String[]> _gridData = null;
    protected int _headerRow = 0;
    protected int _dataRow = 0;
    protected List<String> _worksheetList = null;
    protected String _sheetName = null;
    protected int _activeSheet = -1;
    protected boolean _fixNulls = false;

    private int _delimiterMenuId = 0;
    private int _nullIndicatorMenuId = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Static Methods                                      //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void retrieveFormattedData(int sheetIndexIn);


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    SelectionChangedHandler<String> handleDelimiterChange = new SelectionChangedHandler<String>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            _delimiter = fileParser.getDropDownSelectionCharacter(_delimiterMenuId);
            prepareDataDisplay();
        }
    };

    SelectionChangedHandler<String> handleNullIndicatorChange = new SelectionChangedHandler<String>() {

        @Override
        public void onSelectionChanged(SelectionChangedEvent<String> eventIn) {

            _nullIndicator = fileParser.getDropDownSelection(_nullIndicatorMenuId);
            prepareDataDisplay();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public SpreadsheetInstallWizard(CsiFileType fileTypeIn, WizardDialog priorDialogIn, UploaderControl controlIn,
                                    boolean fixNullsIn, InstalledTable tableIn, String titleIn, String helpIn) {

        super(fileTypeIn, priorDialogIn, controlIn, titleIn, helpIn, Dialog.txtInstallButton, 0, tableIn);

        _activeSheet = 0;
        _fixNulls = fixNullsIn;

        if (null == _delimiterChoices) {

            _delimiterChoices = new ArrayList<ValuePair<String, String>>();
            _delimiterChoices.add(new ValuePair<String, String>("", null));

            for (int i = 0; CsiColumnDelimiter.values().length > i; i++) {

                _delimiterChoices.add(new ValuePair(CsiColumnDelimiter.values()[i].getLabel(),
                        String.valueOf(CsiColumnDelimiter.values()[i].getCharacter())));
            }
        }

        if (null == _nullChoices) {

            _nullChoices = new ArrayList<ValuePair<String, String>>();
            _nullChoices.add(new ValuePair<String, String>("", null));

            for (int i = 0; CsiNullIndicator.values().length > i; i++) {

                _nullChoices.add(new ValuePair(CsiNullIndicator.values()[i].getLabel(),
                                                CsiNullIndicator.values()[i].getString()));
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void initializeDelimiterDropDown(int indexIn, boolean isRequiredIn) {

        _delimiterMenuId = indexIn;
        fileParser.initializeDropDown(_delimiterMenuId, _txtColDelimiterLabel, _delimiterChoices,
                                        CsiOverlayTextBox.ValidationMode.CHARACTER,
                                        handleDelimiterChange, isRequiredIn);
    }

    protected void initializeNullIndicatorDropDown(int indexIn, boolean isRequiredIn) {

        _nullIndicatorMenuId = indexIn;
        fileParser.initializeDropDown(_nullIndicatorMenuId, _txtNullIndicatorLabel, _nullChoices,
                                        CsiOverlayTextBox.ValidationMode.STRING,
                                        handleNullIndicatorChange, isRequiredIn);
    }

    protected void prepareDataDisplay() {

        prepareDataDisplay(null);
    }

    protected void prepareDataDisplay(CsiDataType[] dataTypeListIn) {

        if (0 <= _activeSheet) {

            retrieveFormattedData(_activeSheet);

            if (0 < _columnCount) {

                fileParser.displayGrid(_gridData, _columnCount);

            } else {

                fileParser.clearGrid();
            }
        }
    }

    protected List<InstallerColumnDisplay> buildColumnDisplayList(ColumnParameters[] parametersIn) {

        List<InstallerColumnDisplay> myList = null;

        initializeColumnDefinitions(parametersIn);

        if (null != _columnParameters) {

            _dataRowData = extractRowData(_dataRow - 1);

            myList = new ArrayList<InstallerColumnDisplay>();

            for (int i = 0; _columnParameters.length > i; i++) {

                myList.add(new InstallerColumnDisplay((i + 1), _dataRowData[i],
                            _columnParameters[i].getName(), DisplayMode.NORMAL,
                            _columnParameters[i].getDataType().getLabel(), _columnParameters[i].isIncluded()));
            }
        }
        return myList;
    }

    protected String getColumnName(int indexIn) {

        String myColumnName = null;
        String[] myData = ((0 < _headerRow) && (_gridData.size() >= _headerRow)) ? _gridData.get(_headerRow - 1) : null;

        if ((null != myData) && (myData.length > indexIn)) {

            myColumnName = myData[indexIn];
        }
        return ((null != myColumnName) && (0 < myColumnName.length())) ? myColumnName : getDefaultColumnName(indexIn);
    }

    protected void initializeColumnDefinitions(ColumnParameters[] parametersIn) {

        _headerRow = fileParser.getColumnNames();
        _dataRow = fileParser.getDataStart();

        if (null != parametersIn) {

            _columnParameters = parametersIn;

            for (int i = 0; _columnParameters.length > i; i++) {

                _columnParameters[i].setName(getColumnName(i + 1));
            }

        } else {

            _columnParameters = new ColumnParameters[_columnCount - 1];

            if (null != _table) {

                String myTableId = _table.getUuid();

                for (int i = 0; _columnParameters.length > i; i++) {

                    _columnParameters[i] = new ColumnParameters(myTableId, CsiDataType.String, true);
                }

            } else {

                for (int i = 0; _columnParameters.length > i; i++) {

                    _columnParameters[i] = new ColumnParameters(getColumnName(i + 1), CsiDataType.String, true, true);
                }
            }
        }
    }

    protected void resetDataFormat() {

        fileParser.resetDropDowns();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private String[] extractRowData(int indexIn) {

        String[] mySource = ((0 <= indexIn) && (_gridData.size() > indexIn)) ? _gridData.get(indexIn) : null;
        String[] myRow = new String[_columnCount - 1];
        int myFillStart = 0;

        if (null != mySource) {

            myFillStart = mySource.length;

            for (int i = 1; myFillStart > i; i++) {

                myRow[i - 1] = mySource[i];
            }
        }
        for (int i = myFillStart; _columnCount > i; i++) {

            myRow[i - 1] = "";
        }
        return myRow;
    }
}
