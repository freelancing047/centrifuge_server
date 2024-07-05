package csi.client.gwt.widget.misc;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.export.view.widget.ExportSize;

public enum ExportSizeAttribute {

    ALL_DATA,
    SELECTION_ONLY;

    public static String getInternationalizedSize(ExportSize size) {
        if (size == ExportSize.ALL_DATA) {
            return CentrifugeConstantsLocator.get().table_export_size_allData();
        } else if (size == ExportSize.SELECTION_ONLY) {
            return CentrifugeConstantsLocator.get().table_export_size_selectionOnly();
        }
        return CentrifugeConstantsLocator.get().table_export_size_allData();
    };
}

