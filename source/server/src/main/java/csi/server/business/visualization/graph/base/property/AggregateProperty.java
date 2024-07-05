package csi.server.business.visualization.graph.base.property;

import java.util.Collections;
import java.util.List;

public class AggregateProperty extends Property {

    protected Double value;
    protected Property property;

    public AggregateProperty(String name) {
        super(name);
    }

    @Override
    public List<Object> getValues() {
        return Collections.singletonList((Object) value);
    }

    public synchronized Property getProperty() {
        return property;
    }

    public synchronized void setProperty(Property property) {
        this.property = property;
    }

    public synchronized Double getValue() {
        return value;
    }

    public synchronized void setValue(Double value) {
        this.value = value;
    }

    public boolean isIncludeInTooltip() {
        return property.isIncludeInTooltip();
    }

    public void setIncludeInTooltip(boolean includeInTooltip) {
        property.setIncludeInTooltip(includeInTooltip);
    }
    
    public boolean isHideEmptyInTooltip() {
        return property.isHideEmptyInTooltip();
    }

    public void setHideEmptyInTooltip(boolean hideEmptyInTooltip) {
        property.setHideEmptyInTooltip(hideEmptyInTooltip);
    }

}
