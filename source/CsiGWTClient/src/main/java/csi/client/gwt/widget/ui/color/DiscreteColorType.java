package csi.client.gwt.widget.ui.color;

import java.io.Serializable;

public enum DiscreteColorType implements Serializable {

    DIVERGING("Diverging"),
    QUALITATIVE("Qualitative"),
    SEQUENTIAL("Sequential");


    private final String type;


    DiscreteColorType(String type) { this.type = type;}

    public String getType() {
        return type;
    }

}
