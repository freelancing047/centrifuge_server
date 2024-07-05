package csi.server.common.util.uploader;

/**
 * Created by centrifuge on 11/18/2015.
 */
public class NewExcelConstants {
   public static final String _initialFilePath = "[Content_Types].xml";
   public static final String _componentXpath = "Types/Override";
   public static final String _typeAttribute = "ContentType";
   public static final String _pathAttribute = "PartName";

   public static final String _stringListPath = "/sst";
   public static final String _sharedItem = "si";
   public static final String _dataPortion = "//t";

   public static final String _workbookKey = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml";
   public static final String _worksheetKey = "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml";
   public static final String _styleKey = "application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml";
   public static final String _stringsKey = "application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml";

   public static final String _worksheetListPath = "/workbook/sheets";
   public static final String _worksheetNameSubPath = "sheet";
   public static final String _worksheetNameAttribute = "name";

   public static final String _dataSheetPath = "/worksheet/sheetData";
   public static final String _dataRowSubPath = "row";
   public static final String _dataColumnSubPath = "c";
   public static final String _dataValueSubPath = "v";
   public static final String _rowNumberAttribute = "r";
   public static final String _cellAddressAttribute = "r";
   public static final String _dataTypeAttribute = "t";
   public static final String _displayStyleAttribute = "s";

   public static final String _newFormatListPath = "/styleSheet/numFmts";
   public static final String _newFormatSubPath = "numFmt";
   public static final String _formatIdAttribute = "numFmtId";
   public static final String _newFormatAttribute = "formatCode";
   public static final String _cellFormatListPath = "/styleSheet/cellXfs";
   public static final String _cellFormatSubPath = "xf";

   public static final byte[] _true = { 'T', 'R', 'U', 'E' };
   public static final byte[] _false = { 'F', 'A', 'L', 'S', 'E' };
}
