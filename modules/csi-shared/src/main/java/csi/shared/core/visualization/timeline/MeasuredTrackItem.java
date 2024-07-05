package csi.shared.core.visualization.timeline;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MeasuredTrackItem implements IsSerializable {

    private int value = 0;
    private boolean selected = false;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
