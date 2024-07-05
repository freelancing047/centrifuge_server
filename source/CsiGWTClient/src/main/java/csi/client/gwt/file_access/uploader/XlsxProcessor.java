package csi.client.gwt.file_access.uploader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import csi.client.gwt.file_access.uploader.zip.ZipDirectoryCompleteCallBack;
import csi.server.common.dto.installed_tables.SheetFormat;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.util.ByteBuffer;
import csi.server.common.util.CoercionSupport;
import csi.server.common.util.uploader.BlockLoadedCallBack;
import csi.server.common.util.uploader.CsiSimpleXmlParser;
import csi.server.common.util.uploader.NewExcelConstants;
import csi.server.common.util.uploader.StringLookup;
import csi.server.common.util.uploader.XlsxColumn;
import csi.server.common.util.uploader.XlsxRow;
import csi.server.common.util.uploader.zip.CsiZipEntry;

/**
 * Created by centrifuge on 10/2/2015.
 */
public class XlsxProcessor implements BlockLoadedCallBack, ZipDirectoryCompleteCallBack {

    private enum ZipFile {

        Directory,
        WorkBook,
        Styles,
        Strings,
        WorkSheet
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final long _sheetBufferSize = 250000L;

    private CsiClientZipScanner _zipFile = null;
    private ZipDirectoryCompleteCallBack _directoryCallBack = null;
    private int _step = ZipFile.Directory.ordinal();

    private byte[][] _dataBlock = new byte[ZipFile.values().length][];
    private int[] _byteCount = new int[ZipFile.values().length];

    private int _activeSheet = 0;
    private int _rowCountRequest = 0;

    private CoercionSupport _dataTypeExpert = null;
    private Map<String, List<String>> _componentMap = null; // Directory of zipped component files
    private List<String> _dataSheets = null;
    private List<List<XlsxRow>> _rowList = null;
    private StringLookup _lookup = null;
    private CsiSimpleXmlParser _worksheetParser = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                       Callbacks                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public void onBlockLoaded(byte[] blockIn, int countIn) {

        try {

            ZipFile myFileLoaded = ZipFile.values()[_step++];

            _dataBlock[myFileLoaded.ordinal()] = blockIn;
            _byteCount[myFileLoaded.ordinal()] = countIn;

            switch (myFileLoaded) {

                case Directory:

                    locateComponentsOfInterest();
                    loadWorkbookInfo();
                    break;

                case WorkBook:

                    initializeSheetNameList();
                    _rowList = new ArrayList<List<XlsxRow>>(_dataSheets.size());
                    if (!loadDataTypeInfo()) {

                        onBlockLoaded(null, 0);
                    }
                    break;

                case Styles:

                    initializeDataTypeMap();
                    if (!loadStringData()) {

                        onBlockLoaded(null, 0);
                    }
                    break;

                case Strings:

                    initializeStringTable(new CsiSimpleXmlParser(_dataBlock[ZipFile.Strings.ordinal()],
                            _byteCount[ZipFile.Strings.ordinal()]));
                    _activeSheet = 0;
                    if (!loadWorksheet(_activeSheet++)) {

                        onBlockLoaded(null, 0);
                    }
                    break;

                case WorkSheet:

                    _step--;

                    if (openWorksheet(new CsiSimpleXmlParser(_dataBlock[ZipFile.WorkSheet.ordinal()],
                            _byteCount[ZipFile.WorkSheet.ordinal()]))) {

                        _rowList.add(cacheSampleRows(_rowCountRequest));
                    }
                    if (_dataSheets.size() > _activeSheet) {

                        if (!loadWorksheet(_activeSheet++)) {

                            onBlockLoaded(null, 0);
                        }

                    } else {

                        _directoryCallBack.onDirectoryComplete(null);
                    }
                    break;

                default :

                    break;
            }

        } catch(Exception myException) {

            _directoryCallBack.onDirectoryCompleteError(myException);
        }
    }

    public void onBlockLoadError(Exception myException) {

        _directoryCallBack.onDirectoryCompleteError(myException);
    }

    public void onDirectoryComplete(Map<String, CsiZipEntry> mapIn) {

        try {

            identifyComponentsOfInterest();

        } catch(Exception myException) {

            _directoryCallBack.onDirectoryCompleteError(myException);
        }
    }

    public void onDirectoryCompleteError(Exception myException) {

        _directoryCallBack.onDirectoryCompleteError(myException);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public XlsxProcessor(CsiClientRandomAccess inputIn, int rowCountRequestIn,
                         ZipDirectoryCompleteCallBack callbackIn, long maxSizeIn) {

        _rowCountRequest = rowCountRequestIn;
        _directoryCallBack = callbackIn;
        _zipFile = new CsiClientZipScanner(inputIn, this, maxSizeIn);
    }

    public Map<String, List<String>> getComponentMap() {

        return _componentMap;
    }

    public Map<String, CsiZipEntry> getZipDirectory() {

        return _zipFile.getDirectory();
    }

    public CsiZipEntry getWorkSheetEntry(int indexIn) throws Exception {

        return retrieveEntry(NewExcelConstants._worksheetKey, indexIn);
    }

    public CsiZipEntry getStringListEntry() throws Exception {

        return retrieveEntry(NewExcelConstants._stringsKey, 0);
    }

    public List<String> getSheetNames() {

        return _dataSheets;
    }

    public StringLookup getStringLookup() {

        return _lookup;
    }

    public List<String> getDefaultDataTypes() {

        return _dataTypeExpert.getActiveDataTypes();
    }

    public List<XlsxRow> getListOfDataRows(int sheetIdIn) {

        return (_rowList.size() > sheetIdIn) ? _rowList.get(sheetIdIn) : null;
    }

    public XlsxRow getNextRow() {

        XlsxRow myRow = null;

        try {
            if (_worksheetParser.accessNode(NewExcelConstants._dataRowSubPath)) {

                Integer myRowNumber = _worksheetParser.getFirstAttributeAsInteger(NewExcelConstants._rowNumberAttribute);

                if (_worksheetParser.openNode()) {

                    List<XlsxColumn> myColumnList = new ArrayList<XlsxColumn>();

                    myRow = new XlsxRow(_lookup, null, null, null, 0);

                    while (_worksheetParser.accessNode(NewExcelConstants._dataColumnSubPath)) {

                        byte[] myCellAddress = _worksheetParser.getFirstAttribute(NewExcelConstants._cellAddressAttribute);
                        byte[] myDataTypeBuffer = _worksheetParser.getFirstAttribute(NewExcelConstants._dataTypeAttribute);
                        Integer myDisplayStyle = _worksheetParser.getFirstAttributeAsInteger(NewExcelConstants._displayStyleAttribute);

                        if (_worksheetParser.openNode() && _worksheetParser.openNode(NewExcelConstants._dataValueSubPath)) {

                            List<byte[]> myValueBuffer = _worksheetParser.getDataAndClose();
                            _worksheetParser.closeNode();

                            myColumnList.add(new XlsxColumn(myCellAddress, myDataTypeBuffer, myDisplayStyle, myValueBuffer));
                            myRow.replaceData(myRowNumber, myColumnList);
                        }
                    }
                    _worksheetParser.closeNode();
                }
            }

        } catch(Exception myException) {

        }
        return myRow;
    }

    public SheetFormat proposeFormat(int sheetIndexIn) {

        if ((0 <= sheetIndexIn) && (_rowList.size() > sheetIndexIn)) {

            List<XlsxRow> myRowList = _rowList.get(sheetIndexIn);
            int myColumnCount = countColumns(myRowList);

            if (0 < myColumnCount) {

                Integer myColumnNames = proposeColumnNames(myRowList, myColumnCount);
                Integer myFirstDataRow = proposeDataStart(myRowList, myColumnNames);
                CsiDataType[] myDataTypes = proposeDataTypes(myRowList, myFirstDataRow, myColumnCount);

                return new SheetFormat(myColumnNames, myFirstDataRow, myDataTypes);
            }
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private int countColumns(List<XlsxRow> rowListIn) {

        int myColumnCount = 0;

        for (int i = 0; rowListIn.size() > i; i++) {

            XlsxRow myRow = rowListIn.get(i);
            List<XlsxColumn> myColumns = myRow.getColumnList();

            if (null != myColumns) {

                // Assume columns are in ascending order within list
                XlsxColumn myColumn = myColumns.get(myColumns.size() - 1);
                int myColumnId = myColumn.getColumnNumber();

                if (myColumnId > myColumnCount) {

                    myColumnCount = myColumnId;
                }
            }
        }
        return myColumnCount;
    }

    private Integer proposeColumnNames(List<XlsxRow> rowListIn, int columnCountIn) {

        int myColumnNames = 0;
        boolean myDataFound = false;
        int myCount = 0;

        for (int i = 0; rowListIn.size() > i; i++) {

            XlsxRow myRow = rowListIn.get(i);
            List<XlsxColumn> myColumns = myRow.getColumnList();

            if (null != myColumns) {

                for (int j = 0; myColumns.size() > j; j++) {

                    XlsxColumn myColumn = myColumns.get(j);

                    if (!myColumn.isSharedString()) {

                        myDataFound = true;
                        break;
                    }
                }
                if (myCount < myColumns.size()) {

                    myCount = myColumns.size();
                    myColumnNames = i;
                }
            }
            if (myDataFound) {

                break;
            }
        }
        return (myCount == columnCountIn) ? new Integer(myColumnNames) : null;
    }

    private Integer proposeDataStart(List<XlsxRow> rowListIn, Integer columnNamesIn) {

        Integer myFirstData = null;

        for (int i = (null != columnNamesIn) ? columnNamesIn + 1 : 0; rowListIn.size() > i; i++) {

            XlsxRow myRow = rowListIn.get(i);
            List<XlsxColumn> myColumns = myRow.getColumnList();

            if (null != myColumns) {

                for (int j = 0; myColumns.size() > j; j++) {

                    XlsxColumn myColumn = myColumns.get(j);

                    if (!myColumn.isSharedString()) {

                        myFirstData = new Integer(i);
                        break;
                    }
                }
            }
            if (null != myFirstData) {

                break;
            }
        }
        return myFirstData;
    }

    private CsiDataType[] proposeDataTypes(List<XlsxRow> rowListIn, Integer firstDataRowIn, int columnCountIn) {

        List<CsiDataType> myDataTypes = new ArrayList<CsiDataType>(columnCountIn);

        return _dataTypeExpert.proposeDataTypes(rowListIn, columnCountIn, firstDataRowIn);
    }

    private void identifyComponentsOfInterest() throws Exception {

        _zipFile.loadCompleteEntry(NewExcelConstants._initialFilePath, this);
    }

    private void locateComponentsOfInterest() {

        CsiSimpleXmlParser myParser
                = new CsiSimpleXmlParser(_dataBlock[ZipFile.Directory.ordinal()],
                                                    _byteCount[ZipFile.Directory.ordinal()]);

        _componentMap = myParser.mapAttributes(NewExcelConstants._componentXpath, NewExcelConstants._typeAttribute, NewExcelConstants._pathAttribute);
    }

    private void loadWorkbookInfo() throws Exception {

        loadCompleteEntry(NewExcelConstants._workbookKey, 0);
    }

    private void initializeStringTable(CsiSimpleXmlParser xmlParserIn) {

        try {

            List<byte[]> mySharedStrings = new ArrayList<byte[]>();

            if (xmlParserIn.openFirstNode(NewExcelConstants._stringListPath)) {

                while (xmlParserIn.openNode(NewExcelConstants._sharedItem)) {

                    List<byte[]> myList = new ArrayList<byte[]>();
                    byte[] myString = new byte[0];

                    while (xmlParserIn.openNode(NewExcelConstants._dataPortion)) {

                        myList.addAll(xmlParserIn.getDataAndClose());
                    }
                    if (!myList.isEmpty()) {
                        myString = buildString(myList);
                    }

                    //log.info("Symbol[" + Integer.toString(mySharedStrings.size()) + "] = " + Display.stringValue(myString));
                    mySharedStrings.add(myString);
                    xmlParserIn.closeNode();
                }
            }
            _lookup = new StringLookup(mySharedStrings);

        } catch(Exception myException) {


        }
    }

    private byte[] buildString(List<byte[]> stringIn) {

        if ((null != stringIn) && (0 < stringIn.size())) {

            if (1 < stringIn.size()) {

                ByteBuffer myBuffer = new ByteBuffer();

                for (byte[] myString : stringIn) {

                    myBuffer.append(myString);
                }
                return myBuffer.copyBytes();

            } else {

                return stringIn.get(0);
            }

        } else {

            return new byte[0];
        }
    }

    private boolean openWorksheet(CsiSimpleXmlParser xmlParserIn) {

        _worksheetParser = xmlParserIn;

        return _worksheetParser.openFirstNode(NewExcelConstants._dataSheetPath);
    }

    private void initializeSheetNameList() {

        CsiSimpleXmlParser myParser
                = new CsiSimpleXmlParser(_dataBlock[ZipFile.WorkBook.ordinal()],
                _byteCount[ZipFile.WorkBook.ordinal()]);

        if (myParser.openNode(NewExcelConstants._worksheetListPath)) {

            _dataSheets = new ArrayList<String>();

            while (myParser.accessNode(NewExcelConstants._worksheetNameSubPath)) {

                String myName = myParser.getFirstAttributeAsString(NewExcelConstants._worksheetNameAttribute);

                if (null != myName) {

                    _dataSheets.add(myName);
                }
            }
        }
    }

    private boolean loadDataTypeInfo() throws Exception {

        return loadCompleteEntry(NewExcelConstants._styleKey, 0);
    }

    private void initializeDataTypeMap() {

        CsiSimpleXmlParser myParser
                = new CsiSimpleXmlParser(_dataBlock[ZipFile.Styles.ordinal()],
                _byteCount[ZipFile.Styles.ordinal()]);

        _dataTypeExpert = new CoercionSupport();

        if (myParser.openFirstNode(NewExcelConstants._newFormatListPath)) {

            while (myParser.accessNode(NewExcelConstants._newFormatSubPath)) {

                Integer myId = myParser.getFirstAttributeAsInteger(NewExcelConstants._formatIdAttribute);

                if (null != myId) {

                    String myFormat = myParser.getFirstAttributeAsString(NewExcelConstants._newFormatAttribute);

                    if (null != myFormat) {

                        _dataTypeExpert.addFormat(myId, myFormat);
                    }
                }
            }
        }

        if (myParser.openFirstNode(NewExcelConstants._cellFormatListPath)) {

            while (myParser.accessNode(NewExcelConstants._cellFormatSubPath)) {

                Integer myId = myParser.getFirstAttributeAsInteger(NewExcelConstants._formatIdAttribute);

                if (null != myId) {

                    _dataTypeExpert.includeFormat(myId);
                }
            }
        }
    }

    private boolean loadStringData() throws Exception {

        return loadCompleteEntry(NewExcelConstants._stringsKey, 0);
    }

    private String setDebug(List<byte[]> listIn) {

        return (new String(listIn.get(0)).toLowerCase() + new String(listIn.get(listIn.size() - 1)).toUpperCase());
    }

    private boolean loadWorksheet(int selectionIn) throws Exception {

        return loadPartialEntry(NewExcelConstants._worksheetKey, selectionIn, _sheetBufferSize);
    }

    private List<XlsxRow> cacheSampleRows(int countIn) {

        List<XlsxRow> myList = new ArrayList<XlsxRow>();

        for (int i = 0; countIn > i; i++) {

            XlsxRow myRow = getNextRow();

            if (null != myRow) {

                myList.add(myRow);
            }
        }

        return myList;
    }

    private CsiZipEntry retrieveEntry(String keyIn, int entryIn) throws Exception {

        CsiZipEntry myResult = null;
        List<String> myList = _componentMap.get(keyIn);

        if ((null != myList) && (entryIn < myList.size())) {

            String myPath = myList.get(entryIn);

            if ('/' == myPath.charAt(0)) {

                myResult = _zipFile.retrieveDirectoryEntry(myPath.substring(1));

            } else {

                myResult = _zipFile.retrieveDirectoryEntry(myPath);
            }
        }
        return myResult;
    }

    private boolean loadCompleteEntry(String keyIn, int entryIn) throws Exception {

        List<String> myList = _componentMap.get(keyIn);

        if ((null != myList) && (entryIn < myList.size())) {

            String myPath = myList.get(entryIn);

            if ('/' == myPath.charAt(0)) {

                _zipFile.loadCompleteEntry(myPath.substring(1), this);

            } else {

                _zipFile.loadCompleteEntry(myPath, this);
            }
            return true;
        }
        return false;
    }

    private boolean loadPartialEntry(String keyIn, int entryIn, long sizeIn) throws Exception {

        List<String> myList = _componentMap.get(keyIn);

        if ((null != myList) && (entryIn < myList.size())) {

            String myPath = myList.get(entryIn);

            if ('/' == myPath.charAt(0)) {

                _zipFile.loadPartialEntry(myPath.substring(1), sizeIn, this);

            } else {

                _zipFile.loadPartialEntry(myPath, sizeIn, this);
            }
            return true;
        }
        return false;
    }
}
