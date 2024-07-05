package csi.server.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.util.uploader.XlsxColumn;
import csi.server.common.util.uploader.XlsxRow;

/**
 * Created by centrifuge on 10/13/2015.
 */
public class CoercionSupport {

    private enum FormatDataType {

        Unknown, Number, Integer, Date, Time, DateTime, DateOrTime, Boolean, String
    }

    private static final FormatDataType[] _formatDefinedType = new FormatDataType[] {

            FormatDataType.Unknown, FormatDataType.Integer, FormatDataType.Number, FormatDataType.Number,   //  0 -  3
            FormatDataType.Number, FormatDataType.Unknown, FormatDataType.Unknown, FormatDataType.Unknown,  //  4 -  7
            FormatDataType.Unknown, FormatDataType.Integer, FormatDataType.Number, FormatDataType.Number,   //  8 - 11
            FormatDataType.Number, FormatDataType.Number, FormatDataType.Date, FormatDataType.Date,         // 12 - 15
            FormatDataType.Date, FormatDataType.Date, FormatDataType.Time, FormatDataType.Time,             // 16 - 19
            FormatDataType.Time, FormatDataType.Time, FormatDataType.DateTime, FormatDataType.Unknown,      // 20 - 23
            FormatDataType.Unknown, FormatDataType.Unknown, FormatDataType.Unknown, FormatDataType.Date,    // 24 - 27
            FormatDataType.Date, FormatDataType.Date, FormatDataType.Date, FormatDataType.Date,             // 28 - 31
            FormatDataType.Time, FormatDataType.Time, FormatDataType.DateOrTime, FormatDataType.DateOrTime, // 32 - 35
            FormatDataType.Date, FormatDataType.Integer, FormatDataType.Integer, FormatDataType.Number,     // 36 - 39
            FormatDataType.Number, FormatDataType.Unknown, FormatDataType.Unknown, FormatDataType.Unknown,  // 40 - 43
            FormatDataType.Unknown, FormatDataType.Time, FormatDataType.Time, FormatDataType.Time,          // 44 - 47
            FormatDataType.Number, FormatDataType.Unknown, FormatDataType.Date, FormatDataType.Date,        // 48 - 51
            FormatDataType.Date, FormatDataType.Date, FormatDataType.Date, FormatDataType.DateOrTime,       // 52 - 55
            FormatDataType.DateOrTime, FormatDataType.Date, FormatDataType.Date, FormatDataType.Integer,    // 56 - 59
            FormatDataType.Number, FormatDataType.Integer, FormatDataType.Number, FormatDataType.Unknown,   // 60 - 63
            FormatDataType.Unknown, FormatDataType.Unknown, FormatDataType.Unknown, FormatDataType.Integer, // 64 - 67
            FormatDataType.Number, FormatDataType.Number, FormatDataType.Number, FormatDataType.Date,       // 68 - 71
            FormatDataType.Date, FormatDataType.Date, FormatDataType.Date, FormatDataType.Time,             // 72 - 75
            FormatDataType.Time, FormatDataType.DateTime, FormatDataType.Time, FormatDataType.Time,         // 76 - 79
            FormatDataType.Time, FormatDataType.Date                                                        // 80 - 81
    };

    private static final FormatDataType[][] _decisionGrid = new FormatDataType[][] {

        {       // Was Unknown
                FormatDataType.Unknown,
                FormatDataType.Number,
                FormatDataType.Integer,
                FormatDataType.Date,
                FormatDataType.Time,
                FormatDataType.DateTime,
                FormatDataType.DateOrTime,
                FormatDataType.Boolean,
                FormatDataType.String
        },
        {       // Was Number
                FormatDataType.Number,
                FormatDataType.Number,
                FormatDataType.Number,
                FormatDataType.Date,
                FormatDataType.Time,
                FormatDataType.DateTime,
                FormatDataType.DateOrTime,
                FormatDataType.String,
                FormatDataType.String
        },
        {       // Was Integer
                FormatDataType.Integer,
                FormatDataType.Number,
                FormatDataType.Integer,
                FormatDataType.Date,
                FormatDataType.DateTime,
                FormatDataType.DateTime,
                FormatDataType.Date,
                FormatDataType.String,
                FormatDataType.String
        },
        {       // Was Date
                FormatDataType.Date,
                FormatDataType.DateTime,
                FormatDataType.Date,
                FormatDataType.Date,
                FormatDataType.DateTime,
                FormatDataType.DateTime,
                FormatDataType.Date,
                FormatDataType.String,
                FormatDataType.String
        },
        {       // Was Time
                FormatDataType.Time,
                FormatDataType.Time,
                FormatDataType.DateTime,
                FormatDataType.DateTime,
                FormatDataType.Time,
                FormatDataType.DateTime,
                FormatDataType.Time,
                FormatDataType.String,
                FormatDataType.String
        },
        {       // Was DateTime
                FormatDataType.DateTime,
                FormatDataType.DateTime,
                FormatDataType.DateTime,
                FormatDataType.DateTime,
                FormatDataType.DateTime,
                FormatDataType.DateTime,
                FormatDataType.DateTime,
                FormatDataType.String,
                FormatDataType.String
        },
        {       // Was DateOrTime
                FormatDataType.DateOrTime,
                FormatDataType.DateOrTime,
                FormatDataType.Date,
                FormatDataType.Date,
                FormatDataType.Time,
                FormatDataType.DateTime,
                FormatDataType.DateOrTime,
                FormatDataType.String,
                FormatDataType.String
        },
        {       // Was Boolean
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.Boolean,
                FormatDataType.String
        },
        {       // Was String
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.String,
                FormatDataType.String
        }
    };

    private static final CsiDataType[] _finalDecision = new CsiDataType[] {

            CsiDataType.String,
            CsiDataType.Number,
            CsiDataType.Integer,
            CsiDataType.Date,
            CsiDataType.Time,
            CsiDataType.DateTime,
            CsiDataType.DateTime,
            CsiDataType.String,
            CsiDataType.String
    };

    private int _dataStart = 0;
    private int _headerRow = 0;
    private List<FormatDataType> _dataTypeMap = null;
    private Map<Integer, FormatDataType> _customMap = null;

    public CoercionSupport() {

        _dataStart = 0;
        _dataTypeMap = new ArrayList<FormatDataType>();
        _customMap = new TreeMap<Integer, FormatDataType>();
    }

    public void setDataStart(int dataStartIn) {

        _dataStart = dataStartIn;
    }

    public int getDataStart() {

        return _dataStart;
    }

    public void setHeaderRow(int headerRowIn) {

        _headerRow = headerRowIn;
    }

    public int getHeaderRow() {

        return _headerRow;
    }

    public void set(List<FormatDataType> dataTypeMapIn) {

        _dataTypeMap = dataTypeMapIn;
    }

    public List<FormatDataType> getDataTypeMap() {

        return _dataTypeMap;
    }

    public void set(Map<Integer, FormatDataType> customMapIn) {

        _customMap = customMapIn;
    }

    Map<Integer, FormatDataType> getCustomMap() {

        return _customMap;
    }

    /*
        Add a user defined format to those available
     */
    public void addFormat(Integer idIn, String formatIn) {

        _customMap.put(idIn, determineDataType(formatIn));
    }

    /*
        Build list of formats which will be reference by list location
     */
    public void includeFormat(Integer idIn) {

        FormatDataType myType = _customMap.get(idIn);

        if (null == myType) {

            if ((0 <= idIn) && (_formatDefinedType.length > idIn)) {

                myType = _formatDefinedType[idIn];

            } else {

                myType = FormatDataType.String;
            }
        }
        _dataTypeMap.add(myType);
    }

    public List<String> getActiveDataTypes() {

        List<String> myList = new ArrayList<String>();

        for (FormatDataType myType : _dataTypeMap) {

            myList.add(myType.name());
        }
        return myList;
    }

    public void proposeDataTypes(List<XlsxRow> dataGridIn, int columnCountIn) {

        proposeDataTypes(dataGridIn, columnCountIn, null);
    }

    public CsiDataType[] proposeDataTypes(List<XlsxRow> dataGridIn, int columnCountIn, Integer dataStartIn) {

        CsiDataType[] myDataTypes = new CsiDataType[columnCountIn];
        FormatDataType[] myProposals = new FormatDataType[columnCountIn];
        int myFirstRow = (null != dataStartIn) ? dataStartIn : 0;

        for (int i = 0; columnCountIn > i; i++) {

            myProposals[i] = FormatDataType.Unknown;
        }

        for (int i = myFirstRow; dataGridIn.size() > i; i++) {

            XlsxRow myRow = dataGridIn.get(i);
            List<XlsxColumn> myColumnList = myRow.getColumnList();

            if (null != myColumnList) {

                for (int j = 0; myColumnList.size() > j; j++) {

                    XlsxColumn myColumn = myColumnList.get(j);

                    if (null != myColumn) {

                        int myColumnId = myColumn.getColumnNumber() - 1;

                        if ((0 <= myColumnId) && (columnCountIn > myColumnId)) {

                            myProposals[myColumnId] = mergeDataTypes(myProposals[myColumnId], myColumn);
                        }
                    }
                }
            }
        }
        for (int i = 0; columnCountIn > i; i++) {

            myDataTypes[i] = _finalDecision[myProposals[i].ordinal()];
        }
        return myDataTypes;
    }

    private FormatDataType determineDataType(String formatIn) {

        if (formatIn.contains("h") || formatIn.contains("s")) {

            if (formatIn.contains("y") || formatIn.contains("d")) {

                return FormatDataType.DateTime;
            }
            return FormatDataType.Time;
        }

        if (formatIn.contains("y") || formatIn.contains("d")) {

            return FormatDataType.Date;
        }

        if (formatIn.contains("0") || formatIn.contains("#")) {

            if (formatIn.contains(".") || formatIn.contains("%")) {

                return FormatDataType.Number;
            }
            return FormatDataType.Integer;
        }
        return FormatDataType.Unknown;
    }

    private FormatDataType mergeDataTypes(FormatDataType dataTypeIn, XlsxColumn columnIn) {

        FormatDataType myOldType = (null != dataTypeIn) ? dataTypeIn : FormatDataType.Unknown;
        FormatDataType myNewType;

        if ((FormatDataType.String == dataTypeIn) || (CsiDataType.String == columnIn.getType())) {

            myNewType = FormatDataType.String;

        } else if (FormatDataType.DateTime == dataTypeIn) {

            myNewType = FormatDataType.DateTime;

        } else if (CsiDataType.Boolean == columnIn.getType()) {

            myNewType = FormatDataType.Boolean;

        } else {

            Integer myNewTypeId = columnIn.getStyle();

            myNewType = columnIn.isSharedString() ? FormatDataType.String
                  : ((null != myNewTypeId) && (0 <= myNewTypeId) && (_dataTypeMap.size() > myNewTypeId))
                        ? _dataTypeMap.get(myNewTypeId)
                        : FormatDataType.Unknown;

            if ((FormatDataType.Unknown == myNewType) && ((FormatDataType.Unknown == dataTypeIn)
                    || (FormatDataType.Integer == dataTypeIn) || (FormatDataType.Date == dataTypeIn))) {

                byte[] myData = columnIn.getValue();

                if ((null != myData) && (0 < myData.length)) {

                    myNewType = FormatDataType.Integer;

                    for (int i = 0; myData.length > i; i++) {

                        if (EncodingByteValues.asciiDot == myData[i]) {

                            myNewType = FormatDataType.Number;
                            break;
                        }
                    }
                }
            }
        }
        return _decisionGrid[myOldType.ordinal()][myNewType.ordinal()];
    }
}
