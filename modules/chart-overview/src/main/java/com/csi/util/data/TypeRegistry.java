package com.csi.util.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.empire.data.DataType;

import csi.server.common.model.CsiDataType;

public class TypeRegistry
{
    private static TypeRegistry Singleton = new TypeRegistry();
    
    public static TypeRegistry instance() {
        return Singleton;
    }
    
    
    Map<CsiDataType, DataType> typeBridge;
    TypeRegistry() {
        initialize();
    }
    
    private void initialize() {
        typeBridge = new HashMap<CsiDataType, DataType>();
        typeBridge.put(CsiDataType.Boolean, DataType.BOOL);
        typeBridge.put(CsiDataType.Date, DataType.DATE);
        typeBridge.put(CsiDataType.DateTime, DataType.DATETIME);
        typeBridge.put(CsiDataType.Integer, DataType.INTEGER);
        typeBridge.put(CsiDataType.Number, DataType.FLOAT);
        typeBridge.put(CsiDataType.String, DataType.TEXT);
        typeBridge.put(CsiDataType.Time, DataType.DATETIME);
        typeBridge.put(CsiDataType.Unsupported, DataType.TEXT);
    }
    
    public DataType getModelType(CsiDataType src) {
        return typeBridge.get(src);
    }

}
