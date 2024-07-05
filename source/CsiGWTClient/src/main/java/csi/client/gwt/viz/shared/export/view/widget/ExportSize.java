package csi.client.gwt.viz.shared.export.view.widget;

/**
 * Created by Ivan on 6/8/2017.
 */
public enum ExportSize {
    ALL_DATA("All data"),
    SELECTION_ONLY("Selection Only");

    private final String size;

    ExportSize(String desc) {
        this.size= desc;
    }

    public String getSizeDescription(){
        return size;
    }


    public static ExportSize getEnumByString(String nm){
        for (ExportSize exportSize : ExportSize.values()) {
            if(exportSize.getSizeDescription().equals(nm)){
                return exportSize;
            }
        }
        return null;
    }

}
