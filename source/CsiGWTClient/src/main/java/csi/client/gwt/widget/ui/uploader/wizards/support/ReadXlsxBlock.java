package csi.client.gwt.widget.ui.uploader.wizards.support;

import java.util.List;

import com.google.gwt.typedarrays.shared.Int8Array;

import csi.client.gwt.file_access.uploader.XlsxProcessor;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.uploader.StringLookup;
import csi.server.common.util.uploader.XlsxColumn;
import csi.server.common.util.uploader.XlsxRow;

/**
 * Created by centrifuge on 11/9/2015.
 */
public class ReadXlsxBlock extends ReadBlock {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final String _nullIndicatorString = "<null>";
    private static final byte[] _nullIndicator = _nullIndicatorString.getBytes();

    private XlsxProcessor _dataAccess = null;
    private CsiDataType[] _dataTypeList = null;
    private List<XlsxRow> _RowList = null;
    private int _dataStart = 0;
    private int _sheet = 0;
    private int _column = 0;
    private int _activeRow = -1;
    private int _activeColumn = -1;
    private List<XlsxColumn> _activeColumnList = null;
    private XlsxColumn _cell = null;
    private StringLookup _lookup = null;
    private boolean _complete = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ReadXlsxBlock(Int8Array dataSourceIn, XlsxProcessor dataAccessIn) {

        super(dataSourceIn);

        _dataAccess = dataAccessIn;
    }

    public ReadXlsxBlock processSheet(int sheetIndexIn, CsiDataType[] dataTypeListIn, int dataStartIn) throws Exception {

        restart();
        _sheet = sheetIndexIn;
        _dataTypeList = dataTypeListIn;
        _dataStart = dataStartIn;
        _RowList = _dataAccess.getListOfDataRows(_sheet);
        _lookup = _dataAccess.getStringLookup();
        seekNextCell();

        return this;
    }

    public ReadBlock restart() throws Exception {

        super.restart();
        _sheet = 0;
        _column = 0;
        _activeRow = -1;
        _activeColumn = -1;
        _activeColumnList = null;
        _cell = null;
        _complete = false;

        return this;
    }

    public String getSheetName() {

        return _dataAccess.getSheetNames().get(_sheet);
    }

    public XlsxRow getWorkingRow(int rowIdIn) {

        if ((0 <= rowIdIn) && (_RowList.size() > rowIdIn)) {

            return _RowList.get(rowIdIn);

        } else {

            return null;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected String getNextColumn() throws CentrifugeException {

        String myResult = null;

        if (getProperCell()) {

            if (((_cell.getRowNumber() - 1) == _lineCount) && ((_cell.getColumnNumber() - 1) == _column)) {

                CsiDataType myType = ((_dataStart <= _lineCount) && (null != _dataTypeList) && (_dataTypeList.length > _column))
                                        ? _dataTypeList[_column] : CsiDataType.String;

                myResult = _cell.coerceValue(null, myType, _nullIndicator, _lookup).toString();
//                myResult = _cell.getString(_nullIndicatorString);

                seekNextCell();

            } else {

                myResult = _nullIndicatorString;
            }
        }
        if ((null == _cell) || ((_cell.getRowNumber() - 1) > _lineCount)) {

            _column = 0;
            _lineCount++;

        } else {

            _column++;
        }
        return myResult;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void advanceRow() {

        _lineCount++;
        _column = -1;
    }

    private boolean getProperCell() {

        if (null == _cell) {

            seekNextCell();
        }

        if (null != _cell) {

            int myAvailableRow = _cell.getRowNumber() - 1;
            int myAvailableColumn = _cell.getColumnNumber() - 1;

            if ((myAvailableRow < _lineCount)
                    || ((myAvailableRow == _lineCount) && (myAvailableColumn < _column))) {

                while (seekNextCell()) {

                    if ((_cell.getRowNumber() > _lineCount) && (_cell.getColumnNumber() > _column)) {

                        return true;
                    }
                }
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean seekNextCell() {

        boolean mySuccess = false;

        _cell = null;

        if (!_complete) {

            if ((0 > _activeRow) && (0 > _activeColumn)) {

                mySuccess = seekNextRow(0);

            } else {

                int myColumn = _activeColumn + 1;

                if (_activeColumnList.size() > myColumn) {

                    XlsxColumn myCell = _activeColumnList.get(myColumn);

                    if (null != myCell) {

                        _cell = myCell;
                        _activeColumn = myColumn;
                        mySuccess = true;
                    }

                } else {

                    mySuccess = seekNextRow(_activeRow + 1);
                }
            }
            _complete = !mySuccess;
        }
        return mySuccess;
    }

    private boolean seekNextRow(int firstRowIn) {

        for (int myRow = firstRowIn; _RowList.size() > myRow; myRow++) {

            List<XlsxColumn> myColumnList = _RowList.get(myRow).getColumnList();

            if ((null != myColumnList) && (0 < myColumnList.size())) {

                XlsxColumn myCell = myColumnList.get(0);

                if (null != myCell) {

                    _cell = myCell;
                    _activeColumnList = myColumnList;
                    _activeRow = myRow;
                    _activeColumn = 0;
                    return true;
                }
            }
        }
        return false;
    }
}
