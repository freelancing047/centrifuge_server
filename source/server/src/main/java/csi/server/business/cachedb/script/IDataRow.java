package csi.server.business.cachedb.script;

import csi.server.common.model.FieldDef;

public interface IDataRow {

    public Object get(FieldDef field);

    public Object get(String fieldName);

    String getString(FieldDef field);

    String getString(String fieldName);
}
