package csi.server.common.util;

import java.util.ArrayList;
import java.util.List;

import csi.server.common.enumerations.SystemParameter;
import csi.server.common.model.query.QueryParameterDef;

/**
 * Created by centrifuge on 4/30/2015.
 */
public class SystemParameters {

    private static List<QueryParameterDef> _parameters = null;
    private static SystemValueProvider _valueProvider = null;

    public static void setValueProvider(SystemValueProvider valueProviderIn) {

        _valueProvider = valueProviderIn;
    }

    public static List<QueryParameterDef> getList() {

        if (null != _valueProvider) {

            return _valueProvider.loadValues(refreshValues());
        }
        if (null == _parameters) {

            _parameters = refreshValues();
        }
        return _parameters;
    }

    private static List<QueryParameterDef> refreshValues() {

        List<QueryParameterDef> myParameters = new ArrayList<QueryParameterDef>();
        for (SystemParameter myParameter : SystemParameter.values()) {

            myParameters.add(new QueryParameterDef(myParameter.getName(), myParameter.getName(),
                                                    myParameter.getType(), null, myParameter.getLabel()));
        }
        return myParameters;
    }
}
