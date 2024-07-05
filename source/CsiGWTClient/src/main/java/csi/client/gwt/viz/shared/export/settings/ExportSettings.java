package csi.client.gwt.viz.shared.export.settings;
import csi.client.gwt.viz.shared.export.model.ExportType;
/**
 * Data that drives the state of the ExportViewDialog.
 * @author Centrifuge Systems, Inc.
 */
public class ExportSettings {

    private String name;
    private ExportType exportType;
    private boolean useSelectionOnly = false;
    private ExportImageSettings imageSettings = null;

    public ExportSettings(){};

    public ExportSettings(String name, ExportType exportType) {
        this.name = name;
        this.exportType = exportType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExportType getExportType() {
        return exportType;
    }

    public void setExportType(ExportType exportType) {
        this.exportType = exportType;
    }

    public boolean isUseSelectionOnly() {
        return useSelectionOnly;
    }

    public void setUseSelectionOnly(boolean useSelectionOnly) {
        this.useSelectionOnly = useSelectionOnly;
    }

    public ExportImageSettings getImageSettings() {
        return imageSettings;
    }

    public void setImageSettings(ExportImageSettings imageSettings) {
        this.imageSettings = imageSettings;
    }
}
