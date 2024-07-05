package csi.server.business.visualization.graph.base.property;

import java.util.List;

public class ProxiedProperty extends Property {

    protected Property reference;

    public ProxiedProperty(String name) {
        super(name);
    }

    public Property getReference() {
        return reference;
    }

    public void setReference(Property reference) {
        this.reference = reference;
    }

    @Override
    public List<Object> getValues() {
        return reference.getValues();
    }

    @Override
    public void setValues(List<Object> values) {
        reference.setValues(values);
    }

    public boolean isIncludeInTooltip() {
        return reference.isIncludeInTooltip();
    }

    public void setIncludeInTooltip(boolean includeInTooltip) {
        reference.setIncludeInTooltip(includeInTooltip);
    }
    
    public boolean isHideEmptyInTooltip() {
        return reference.isHideEmptyInTooltip();
    }

    public void setHideEmptyInTooltip(boolean hideEmptyInTooltip) {
        reference.setHideEmptyInTooltip(hideEmptyInTooltip);
    }

}
