package csi.client.gwt.viz.shared;

public class MetricsDisplay {
    String name;

    Number numericaValue;
//    String value;

    public MetricsDisplay(String name, Number value) {
        this.name = name;
        this.numericaValue = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {

        return numericaValue.toString();
    }

    public void setValue(Number value) {
        this.numericaValue = value;
    }
}