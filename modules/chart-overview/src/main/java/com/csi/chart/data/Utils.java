package com.csi.chart.data;

import org.apache.empire.data.DataType;
import org.apache.empire.db.DBColumn;
import org.apache.empire.db.DBColumnExpr;

import com.csi.util.data.TypeRegistry;

import csi.server.common.model.CsiDataType;
import csi.server.common.model.FieldDef;

public class Utils
{

    static public boolean requiresCast( String valueType, String cacheType )
    {
        return valueType.equals(cacheType);
    }

    static public boolean isStringOrBooleanField( FieldDef field )
    {
        if (field == null) {
            throw new NullPointerException();
        }
    
        String type = field.getValueType();
        boolean flag = "string".equalsIgnoreCase(type) || "boolean".equalsIgnoreCase(type);
        return flag;
    }

    public static DBColumnExpr castAs( DBColumn column, String valueType )
    {
        CsiDataType type = CsiDataType.valueOf(valueType);
        DataType modelType = TypeRegistry.instance().getModelType(type);
        DBColumnExpr cast = column.convertTo(modelType);
        return cast;
    }

}
