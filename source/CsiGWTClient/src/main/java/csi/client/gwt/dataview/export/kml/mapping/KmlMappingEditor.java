package csi.client.gwt.dataview.export.kml.mapping;

import java.util.Collection;

import csi.server.common.model.FieldDef;
import csi.server.common.model.kml.KmlMapping;

public interface KmlMappingEditor {
    Collection<FieldDef> getDataviewFieldDefs();

    void show();

    void save();

    void cancel();

    void addSaveHandler(KmlMappingSaveHandler handler);

    interface KmlMappingSaveHandler {
        public void onSave(KmlMapping kmlMapping);
    }
}
