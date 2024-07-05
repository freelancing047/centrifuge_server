package csi.server.common.enumerations;

import java.io.Serializable;

import csi.shared.core.util.HasLabel;


public enum ParameterUse implements Serializable, HasLabel {
    
    QUERY("Custom Query", "Custom Queries"),
    FILTER("Filter", "Filters"),
    DERIVED_FIELD("Derived Field", "Derived Fields");

    private String _label;
    private String _plural;

    private ParameterUse(String labelIn, String pluralIn) {

        _label = labelIn;
        _plural = pluralIn;
    }

    public String getLabel() {
        
        return _label;
    }

    public String getLabel(int countIn) {
        
        return (1 == countIn) ? _label : _plural;
    }

}
