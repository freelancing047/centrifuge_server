package csi.server.common.util.uploader;

import java.util.List;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.CsiEncoding;
import csi.server.common.util.ByteBuffer;
import csi.server.common.util.EncodingByteValues;

/**
 * Created by centrifuge on 10/14/2015.
 */
public class XlsxRow {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final int _defaultBufferSize = 16384;

    private ByteBuffer _buffer;
    private boolean[] _include = null;
    private StringLookup _lookup = null;
    private CsiDataType[] _coercionType = null;
    private byte[] _nullIndicator;

    private Integer _rowNumber;
    private List<XlsxColumn> _columnList;

    public XlsxRow() {

    }

    public XlsxRow(ByteBuffer bufferIn, StringLookup lookupIn, boolean[] includeIn, CsiDataType[] coercionTypeIn, byte[] nullIndicatorIn) {

        _buffer = bufferIn;
        _lookup = lookupIn;
        _include = includeIn;
        _coercionType = coercionTypeIn;
        _nullIndicator = nullIndicatorIn;
        _rowNumber = 0;
    }

    public XlsxRow(StringLookup lookupIn, boolean[] includeIn, CsiDataType[] coercionTypeIn, byte[] nullIndicatorIn, int bufferSizeIn) {

        _lookup = lookupIn;
        _include = includeIn;
        _coercionType = coercionTypeIn;
        _nullIndicator = nullIndicatorIn;
        _rowNumber = 0;

        _buffer = new ByteBuffer((0 < bufferSizeIn) ? bufferSizeIn : _defaultBufferSize, CsiEncoding.UTF_8);
    }

    public void replaceData(Integer rowNumberIn, List<XlsxColumn> columnListIn) {

        _rowNumber = rowNumberIn;
        _columnList = columnListIn;
    }

    public Integer getRowNumber() {

        return _rowNumber;
    }

    public void setRowNumber(Integer rowNumberIn) {

        _rowNumber = rowNumberIn;
    }

    public List<XlsxColumn> getColumnList() {

        return _columnList;
    }

    public void setColumnList(List<XlsxColumn> columnListIn) {

        _columnList = columnListIn;
    }

    // Client
    public String coerceColumn(int columnIdIn, CsiDataType dataTypeIn) {

        XlsxColumn myColumn = null;

        for (int i = 0; _columnList.size() > i; i++) {

            myColumn = _columnList.get(i);

            if (myColumn.getColumnNumber() == columnIdIn) {

                return myColumn.coerceValue(dataTypeIn, _nullIndicator, _lookup);
            }
        }
        return (null != _nullIndicator) ? new String(_nullIndicator) : "";
    }

    // Server
    public ByteBuffer coerceDataRow() {

        int myColumnSlot = 1;
        int myListSize = (_columnList == null) ? 0 : _columnList.size();
        XlsxColumn myColumn = (_columnList == null) ? null : _columnList.get(0);

        _buffer.truncate();

        for (int i = 0; i < _include.length; i++){

            if (_include[i]) {

                while ((myListSize > myColumnSlot) && (myColumn != null) && (myColumn.getColumnNumber().intValue() <= i)) {

                    myColumn = _columnList.get(myColumnSlot++);
                }

                if ((null != myColumn) && ((i + 1) == myColumn.getColumnNumber().intValue())) {

                    myColumn.coerceQuotedValue(_buffer, _coercionType[i], _nullIndicator, _lookup);
                }
                _buffer.append(EncodingByteValues.asciiComma);
            }
        }
        return _buffer.clip(1);
    }
}
