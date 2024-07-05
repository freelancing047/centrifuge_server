package csi.shared.core.visualization.map;

import java.io.Serializable;

public class MetricPair implements Serializable {
    private String name;
    private String value;

    public MetricPair(String name, String value){
        this.name = name;
        this.value = value;
    }

    public MetricPair() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
