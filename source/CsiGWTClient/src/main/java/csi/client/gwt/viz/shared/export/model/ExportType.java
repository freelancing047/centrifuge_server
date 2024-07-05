package csi.client.gwt.viz.shared.export.model;

/**
 * Available file formats for export.
 * @author Centrifuge Systems, Inc.
 */
public enum ExportType {
    CSV(".csv"),
    PNG(".png"),
    XML(".xml"),
    PDF(".pdf"),
    ANX(".anx"),
    NULL(".file"),
    // so like this maybe
    ZIP(".zip");

    private final String fileSuffix;

    ExportType(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public String getDisplayName(){
        return this.fileSuffix.substring(1).toUpperCase();
    }

    public String getFileSuffix() {
        return fileSuffix;
    }
}
