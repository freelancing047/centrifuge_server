package csi.server.common.publishing;

// FIXME: Rename to be singular
public enum AssetTypes {
    LIVE("LIVE"), //
    SNAPSHOT("Snapshot"), //
    // TODO: Delete the rest after publish is implemented.
    PDF_CHART("PDF.CHART"), //
    PDF_IMG("PDF.IMG"), //
    PDF_TABLE("PDF.TABLE"), //
    ;

    private String alt;

    private AssetTypes(String s) {
        alt = s;
    }

    @Override
    public String toString() {
        return alt;
    }

}
