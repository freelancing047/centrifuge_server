package csi.client.gwt.viz.shared.export.settings;

import csi.client.gwt.viz.shared.export.model.ExportType;

/**
 * Used when exporting an image.
 * @author Centrifuge Systems, Inc.
 */
public class ExportImageSettings {

    private int desiredWidth = -1;
    private int desiredHeight = -1;

    public int getDesiredWidth() {
        return desiredWidth;
    }

    public void setDesiredWidth(int desiredWidth) {
        this.desiredWidth = desiredWidth;
    }

    public int getDesiredHeight() {
        return desiredHeight;
    }

    public void setDesiredHeight(int desiredHeight) {
        this.desiredHeight = desiredHeight;
    }
}
