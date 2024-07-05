package csi.server.business.cachedb.script;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.util.CacheUtil;
import csi.server.util.CsiTypeUtil;

public class CacheRowSet implements IDataRow {
   private static final Logger LOG = LogManager.getLogger(CacheRowSet.class);

   private ResultSet rs;
    private Map<String, Integer> columnOrdinalByColumnName;
    private Map<String, FieldDef> fieldByNameMap;
    private Map<String, Object> fieldValue = null;
    private long rowcnt = 0;

    public CacheRowSet(List<FieldDef> fieldDefs, ResultSet rs) {
        rowcnt = 0;
        this.rs = rs;

        // create field map
        fieldByNameMap = new HashMap<String, FieldDef>();
        for (FieldDef f : fieldDefs) {
            if ((f != null) && (f.getFieldName() != null)) {
                fieldByNameMap.put(f.mapKey(), f);
            }
        }

        try {
            ResultSetMetaData meta = rs.getMetaData();

            // create column map by ordinal and field list ordered by ordinal.
            // note cache table's column names are the same
            // as the field's uuid
            columnOrdinalByColumnName = new HashMap<String, Integer>();
            int columnCount = meta.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String colname = meta.getColumnName(i).toLowerCase();
                columnOrdinalByColumnName.put(colname, i);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize row data", e);
        }
    }

    public long getRowCount() {
        return rowcnt;
    }

    @Override
    public Object get(String fieldName) {
        if (fieldName == null) {
            return null;
        }

        String name = FieldDef.makeKey(fieldName);
        Object val = (null != fieldValue) ? fieldValue.get(name) : null;
        if (null != val) {
            return val;
        }

        // check colmap first since it may be a column name
        Integer idx = columnOrdinalByColumnName.get(name);
        if ((idx != null) && (idx > 0)) {
            try {
                return rs.getObject(idx);
            } catch (SQLException e) {
               LOG.warn("Failed to get value for column: " + fieldName, e);
                return null;
            }
        }

        // check fieldMap
        FieldDef f = fieldByNameMap.get(name);
        if (f != null) {
            return get(f);
        }

        // only log once
        if (rowcnt == 1) {
           LOG.warn("Unknown field or column name: " + fieldName);
        }
        return null;
    }

    @Override
    public Object get(FieldDef field) {
        if (field == null) {
            return null;
        }

        Object val = null;
        if (field.getFieldType() == FieldType.STATIC) {
            val = field.getStaticText();
        } else {
            if(fieldValue != null) {
                val = fieldValue.get(field.mapKey());
            }
            if (null == val) {
                try {
                    String colname = CacheUtil.getColumnName(field);
                    val = rs.getObject(colname);
                } catch (SQLException e) {
                    if (rowcnt == 1) {
                        // only log once
                       LOG.warn("Failed to get value for field: " + field.getFieldName(), e);
                    }
                }
            }
        }

        if (val == null) {
            return null;
        } else {
            return CsiTypeUtil.coerceType(val, field.getValueType(), field.getDisplayFormat());
        }
    }

    @Override
    public String getString(String fieldName) {
        if (fieldName == null) {
            return null;
        }

        String name = FieldDef.makeKey(fieldName);
        Object val = (null != fieldValue) ? fieldValue.get(name) : null;
        if (null != val) {
            return CsiTypeUtil.coerceString(val, (String) null);
        }

        // check colmap first since it may be a column name
        Integer idx = columnOrdinalByColumnName.get(name);
        if ((idx != null) && (idx > 0)) {
            try {
                return CsiTypeUtil.coerceString(rs.getObject(idx), (String) null);
            } catch (SQLException e) {
               LOG.warn("Failed to get value for column: " + fieldName, e);
                return null;
            }
        }

        // check fieldMap
        FieldDef f = fieldByNameMap.get(name);
        if (f != null) {
            return getString(f);
        }

        // only log once
        if (rowcnt == 1) {
           LOG.warn("Unknown field or column name: " + fieldName);
        }
        return null;
    }

    @Override
    public String getString(FieldDef field) {
        Object val = get(field);
        if (val == null) {
            return null;
        } else {
            return CsiTypeUtil.coerceString(val, field.getDisplayFormat());
        }
    }

    public boolean nextRow() throws SQLException {
        fieldValue = null;
        rowcnt++;
        return rs.next();
    }

    public void update(FieldDef fieldIn, Object valueIn) {

        if (null == fieldValue) {

            fieldValue = new TreeMap<>();
        }
        fieldValue.put(fieldIn.mapKey(), valueIn);
        fieldValue.put(CacheUtil.getColumnName(fieldIn).toLowerCase(), valueIn);
    }

    public long close() {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
            }
            rs = null;
        }
        return rowcnt;
    }
}
