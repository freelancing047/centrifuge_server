/**
 * 
 */
package csi.server.common.model.operator;



import java.util.Collection;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.shared.core.util.HasLabel;


public enum OpJoinType implements IsSerializable, HasLabel {
 
    EQUI_JOIN("INNER JOIN", "INNER JOIN"), //
    LEFT_OUTER("LEFT OUTER JOIN", "LEFT OUTER JOIN"), //
    RIGHT_OUTER("RIGHT OUTER JOIN", "RIGHT OUTER JOIN"); //

    private static String[] _i18nLabels = null;
    private static TreeMap<String, OpJoinType> _i18nValues = null;

    private String _label;
    private String _sql;

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    public static void setI18nValues(TreeMap<String, OpJoinType> i18nValuesIn) {

        _i18nValues = i18nValuesIn;
    }

    private OpJoinType(String labelIn, String sqlIn) {
        _label = labelIn;
        _sql = sqlIn;
    }

    public String getLabel() {

        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : null;
        return (null != myLabel) ? myLabel : _label;
    }

    public String getSql() {

        return _sql;
    }

    public static OpJoinType getValue(String labelIn) {

        OpJoinType myValue = ((null != _i18nValues) && (null != labelIn)) ? _i18nValues.get(labelIn) : null;
        return (null != myValue) ? myValue : (null != labelIn) ? OpJoinType.valueOf(labelIn) : null;
    }

    public static Collection<OpJoinType> sortedValuesByLabel() {

        return (null != _i18nValues) ? _i18nValues.values() : null;
    }
}