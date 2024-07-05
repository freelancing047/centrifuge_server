package csi.server.common.enumerations;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeMap;

import csi.shared.core.util.HasLabel;


public enum ParameterType implements Serializable, HasLabel {
    
    TABLE("Table"),
    COLUMN("Column"),
    DATA("Data");

    private static String[] _i18nLabels = null;
    private static TreeMap<String, ParameterType> _i18nValues = null;

    private String _label;

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    public static void setI18nValues(TreeMap<String, ParameterType> i18nValuesIn) {

        _i18nValues = i18nValuesIn;
    }

    private ParameterType(String labelIn) {

        _label = labelIn;
    }

    public String getLabel() {

        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }

    public static ParameterType getValue(String labelIn) {

        ParameterType myValue = null;

        if (null != labelIn) {

            myValue = (null != _i18nValues) ? _i18nValues.get(labelIn) : valueOf(labelIn);
        }
        return myValue;
    }

    public static Collection<ParameterType> sortedValuesByLabel() {

        return (null != _i18nValues) ? _i18nValues.values() : null;
    }
}
