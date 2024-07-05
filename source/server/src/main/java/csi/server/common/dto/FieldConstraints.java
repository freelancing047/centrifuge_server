package csi.server.common.dto;



import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.FieldDef;


public class FieldConstraints implements IsSerializable {

    public List<String> availableValues;
    public FieldDef fieldDef;

    // Count of availableValues; this is returned even if the availableValues are not returned (e.g. when the limit is set)
    public int valuesCount;
    public String rangeMin;
    public String rangeMax;
}
