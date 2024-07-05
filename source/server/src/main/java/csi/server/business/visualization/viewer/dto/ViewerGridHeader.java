package csi.server.business.visualization.viewer.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import csi.server.common.model.FieldDef;

public class ViewerGridHeader implements IsSerializable{
    private FieldDef fieldDef;
    private boolean visible = true;

    public FieldDef getFieldDef() {
        return fieldDef;
    }

    public void setFieldDef(FieldDef fieldDef) {
        this.fieldDef = fieldDef;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
