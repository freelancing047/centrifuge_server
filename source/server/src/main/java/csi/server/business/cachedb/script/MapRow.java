package csi.server.business.cachedb.script;

import java.util.HashMap;
import java.util.Map;

import csi.server.common.model.FieldDef;
import csi.server.util.CsiTypeUtil;

public class MapRow implements IDataRow {

    // map of fieldname/fieldvalue
    Map<String, FieldDef> fieldNameMap;
    Map<FieldDef, Object> valueMap;

    public MapRow() {
        super();
        this.fieldNameMap = new HashMap<String, FieldDef>();
        this.valueMap = new HashMap<FieldDef, Object>();
    }

    @Override
    public Object get(FieldDef field) {
        Object obj = valueMap.get(field);
        if (obj == null) {
            return null;
        } else {
            return CsiTypeUtil.coerceType(obj, field.getValueType(), field.getDisplayFormat());
        }
    }

    @Override
    public Object get(String fieldName) {
        FieldDef field = fieldNameMap.get(FieldDef.makeKey(fieldName));
        if (field == null) {
            return null;
        }

        return get(field);
    }

    @Override
    public String getString(String fieldName) {
        FieldDef field = fieldNameMap.get(FieldDef.makeKey(fieldName));
        if (field == null) {
            return null;
        }
        return getString(field);
    }

    @Override
    public String getString(FieldDef field) {
        Object val = get(field);
        return CsiTypeUtil.coerceString(val, field.getDisplayFormat());
    }

    public void set(FieldDef field, Object value) {
        fieldNameMap.put(field.mapKey(), field);
        valueMap.put(field, value);
    }

    public void putField(FieldDef field) {
        fieldNameMap.put(field.mapKey(), field);
    }

    public void putValue(FieldDef field, Object value) {
        valueMap.put(field, value);
    }

    public void clear() {
        valueMap.clear();
    }
}
