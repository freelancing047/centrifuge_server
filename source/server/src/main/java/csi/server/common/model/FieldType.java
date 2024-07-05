/**
 *
 */
package csi.server.common.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.shared.core.util.HasLabel;

public enum FieldType implements Serializable, IsSerializable, HasLabel {
    COLUMN_REF("#000000", "Column"), //
    SCRIPTED("#FF0000", "Scripted"), //
    STATIC("#0000FF", "Static"), //
    LINKUP_REF("#007f95", "Linkup"), //
    DERIVED("darkorange", "Derived"), //
    UNMAPPED("#7F7F7F", "Unknown"); //

    private static String[] _i18nLabels = null;
    private static TreeMap<String, FieldType> _i18nValues = null;

    private String _color;
    private String _label;

    public static void setI18nLabels(String[] i18nLabelsIn) {

        _i18nLabels = i18nLabelsIn;
    }

    public static void setI18nValues(TreeMap<String, FieldType> i18nValuesIn) {

        _i18nValues = i18nValuesIn;
    }

    private FieldType(String colorIn, String labelIn) {
        _color = colorIn;
        _label = labelIn;
    }

    public String getColor() {
        return _color;
    }

    @Override
    public String getLabel() {

        String myLabel = (null != _i18nLabels) ? _i18nLabels[ordinal()] : _label;
        return (null != myLabel) ? myLabel : _label;
    }

    public static FieldType getValue(String labelIn) {

        FieldType myValue = null;

        if (null != labelIn) {

            myValue = (null != _i18nValues) ? _i18nValues.get(labelIn) : FieldType.valueOf(labelIn);
        }

        return (null != myValue) ? myValue : UNMAPPED;
    }

    public static Collection<FieldType> sortedValuesByLabel() {

        return (null != _i18nValues) ? _i18nValues.values() : null;
    }

   public boolean isDeletable() {
      return (this != COLUMN_REF);
   }

   public boolean isDependent() {
      return (this == DERIVED) || (this == SCRIPTED);
   }

   public boolean isReference() {
      return (this == COLUMN_REF) || (this == LINKUP_REF);
   }
}
