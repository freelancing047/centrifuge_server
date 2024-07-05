package csi.server.common.dto.graph.search;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.model.attribute.AttributeDef;
import csi.server.common.model.filter.FilterOperatorType;

public class AttributeCriterion implements IsSerializable {

    public boolean exclude = false;

    private AttributeDef attribute;

    public FilterOperatorType operator;

    public ArrayList<String> staticValues;

    public boolean getExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public AttributeDef getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeDef attribute) {
        this.attribute = attribute;
    }

    public FilterOperatorType getOperator() {
        return operator;
    }

    public void setOperator(FilterOperatorType operator) {
        this.operator = operator;
    }

    public ArrayList<String> getStaticValues() {
        return staticValues;
    }

    public void setStaticValues(ArrayList<String> staticValues) {
        this.staticValues = staticValues;
    }

}