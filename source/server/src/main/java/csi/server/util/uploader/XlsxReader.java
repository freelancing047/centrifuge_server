package csi.server.util.uploader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.dto.installed_tables.ColumnParameters;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.util.ByteBuffer;
import csi.server.common.util.EncodingByteValues;
import csi.server.common.util.Format;
import csi.server.common.util.uploader.CsiInputStream;
import csi.server.common.util.uploader.CsiSimpleXmlParser;
import csi.server.common.util.uploader.NewExcelConstants;
import csi.server.common.util.uploader.StringLookup;
import csi.server.common.util.uploader.XlsxColumn;
import csi.server.common.util.uploader.XlsxRow;
import csi.server.common.util.uploader.zip.CsiZipEntry;

/**
 * Created by centrifuge on 11/16/2015.
 */
public class XlsxReader implements CsiInputStream {
   private static final Logger LOG = LogManager.getLogger(XlsxReader.class);

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private CsiServerZipScanner _zipScanner;
    private CsiSimpleXmlParser _worksheetParser = null;
    private StringLookup _lookup = null;
    private CsiDataType[] _dataType;
    private boolean[] _include;
    private ByteBuffer _rowBuffer;
    private ByteBuffer _emptyRow;
    private int _rowNumber = 0;
    private XlsxRow _nextRow = null;

    public XlsxReader(CsiServerZipScanner zipScannerIn, CsiZipEntry stringEntryIn) throws Exception {

        _zipScanner = zipScannerIn;
        _rowBuffer = new ByteBuffer();
        _emptyRow = new ByteBuffer();

        initializeStringTable(stringEntryIn);
    }

    public boolean openWorksheet(CsiZipEntry workSheetEntryIn, ColumnParameters[] columnsIn) throws Exception {

        _dataType = new CsiDataType[columnsIn.length];
        _include = new boolean[columnsIn.length];

        for (int i = 0; columnsIn.length > i; i++) {

            _dataType[i] = columnsIn[i].getDataType();
            _include[i] = columnsIn[i].isIncluded();
        }

        _worksheetParser = new CsiSimpleXmlParser(_zipScanner.openDirectoryEntry(workSheetEntryIn));
        _nextRow = new XlsxRow(_rowBuffer, _lookup, _include, _dataType, new byte[0]);
        genEmptyRow();

        return _worksheetParser.openFirstNode(NewExcelConstants._dataSheetPath);
    }

    public ByteBuffer getNextRow() {

        ByteBuffer myRow = null;

        if (null != _nextRow) {

            try {

                if (_nextRow.getRowNumber() < ++_rowNumber) {

                    if (!getNextAvailableRow()) {

                        _nextRow = null;
                    }
                }

                if (null != _nextRow) {

                    if (_nextRow.getRowNumber() == _rowNumber) {

                        myRow = _nextRow.coerceDataRow();

                    } else {

                        myRow = _emptyRow;
                    }
                }

            } catch (Exception myException) {

               LOG.error("Failed getting next data row!");
            }
        }
        return myRow;
    }

    @Override
    public int read() throws IOException {

        throw new IOException("Operation not supported!");
    }

    @Override
    public int read(byte[] bufferIn, int offsetIn, int lengthIn) throws IOException {

        throw new IOException("Operation not supported!");
    }

    @Override
    public void skipLines(long lineCountIn) throws IOException {

        for (int i = 0; lineCountIn > i; i++) {

            if (null == getNextRow()) {

                throw new IOException("Failed advancing to data within spreadsheet.");
            }
        }
    }

    @Override
    public int read(byte[] bufferIn) throws IOException {

        throw new IOException("Operation not supported!");
    }

    @Override
    public int getProgress() {

        return _zipScanner.getProgress();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initializeStringTable(CsiZipEntry stringEntryIn) throws Exception {

        List<byte[]> mySharedStrings = new ArrayList<byte[]>();

        if (null != stringEntryIn) {

            CsiSimpleXmlParser myParser = new CsiSimpleXmlParser(_zipScanner.openDirectoryEntry(stringEntryIn));

            if (myParser.openFirstNode(NewExcelConstants._stringListPath)) {

                while (myParser.openNode(NewExcelConstants._sharedItem)) {

                    List<byte[]> myList = new ArrayList<byte[]>();
                    byte[] myString = new byte[0];

                    while (myParser.openNode(NewExcelConstants._dataPortion)) {

                        myList.addAll(myParser.getDataAndClose());
                    }
                    if (!myList.isEmpty()) {

                        myString = escapeString(myList);
                    }

                    //log.info("Symbol[" + Integer.toString(mySharedStrings.size()) + "] = " + Display.stringValue(myString));
                    mySharedStrings.add(myString);
                    myParser.closeNode();
                }

            } else {

                throw new Exception("Failed loading shared strings.");
            }
        }
        _lookup = new StringLookup(mySharedStrings);
    }

    private boolean getNextAvailableRow() {

        boolean mySuccess = false;

        try {

            if (_worksheetParser.accessNode(NewExcelConstants._dataRowSubPath)) {

                Integer myRowNumber = _worksheetParser.getFirstAttributeAsInteger(NewExcelConstants._rowNumberAttribute);

                if (_worksheetParser.openNode()) {

                    List<XlsxColumn> myColumnList = new ArrayList<XlsxColumn>();

                    while (_worksheetParser.accessNode(NewExcelConstants._dataColumnSubPath)) {

                        byte[] myCellAddress = _worksheetParser.getFirstAttribute(NewExcelConstants._cellAddressAttribute);
                        byte[] myDataTypeBuffer = _worksheetParser.getFirstAttribute(NewExcelConstants._dataTypeAttribute);
                        Integer myDisplayStyle = _worksheetParser.getFirstAttributeAsInteger(NewExcelConstants._displayStyleAttribute);

                        if (_worksheetParser.openNode() && _worksheetParser.openNode(NewExcelConstants._dataValueSubPath)) {

                            List<byte[]> myValueBuffer = _worksheetParser.getDataAndClose();
                            _worksheetParser.closeNode();

                            myColumnList.add(new XlsxColumn(myCellAddress, myDataTypeBuffer, myDisplayStyle, myValueBuffer));
                        }
                    }
                    mySuccess = true;
                    _nextRow.replaceData(myRowNumber, myColumnList);
                    _worksheetParser.closeNode();
                }
            }

        } catch(Exception myException) {

           LOG.error("Failed getting next available row:\n\t\t" + Format.value(myException));
        }
        return mySuccess;
    }

    private void genEmptyRow() {

        _emptyRow.truncate();

        for (int i = 0; _include.length > i; i++) {

            if (_include[i]) {

                _emptyRow.append(EncodingByteValues.asciiComma);
            }
        }
        _emptyRow.clip(1);
    }

    private byte[] escapeString(List<byte[]> stringIn) {

        _rowBuffer.truncate();

        for (byte[] myString : stringIn) {

            boolean myChangeFlag = false;

            for (int i = 0; myString.length > i; i++) {

                if (EncodingByteValues.asciiQuote == myString[i]) {

                    myChangeFlag = true;
                    _rowBuffer.append(myString, 0, i);

                    for (int j = i; myString.length > j; j++) {

                        if (EncodingByteValues.asciiQuote == myString[j]) {

                            _rowBuffer.append(EncodingByteValues.asciiQuote);
                        }
                        _rowBuffer.append(myString[j]);
                    }
                    break;
                }
            }
            if (!myChangeFlag) {

                _rowBuffer.append(myString);
            }
        }
        return _rowBuffer.copyBytes();
    }
}
