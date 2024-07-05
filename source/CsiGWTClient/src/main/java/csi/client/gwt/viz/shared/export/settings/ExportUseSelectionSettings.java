package csi.client.gwt.viz.shared.export.settings;

import csi.client.gwt.viz.shared.export.model.ExportType;

/**
 * Used when exporting a CSV.
 * @author Centrifuge Systems, Inc.
 */
public class ExportUseSelectionSettings extends ExportSettings{

    private boolean useSelectionOnly;

    public ExportUseSelectionSettings(String name, ExportType exportType) {
        super(name, exportType);
    }

    public boolean isUseSelectionOnly() {
        return useSelectionOnly;
    }

    public void setUseSelectionOnly(boolean useSelectionOnly) {
        this.useSelectionOnly = useSelectionOnly;
    }
}
