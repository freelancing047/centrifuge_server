package csi.client.gwt.dataview.export.kml.mapping;

import com.google.gwt.activity.shared.Activity;

import csi.server.common.model.FieldDef;
import csi.server.common.model.kml.KmlMapping;
import csi.server.common.model.kml.KmlMapping.LocationType;
import csi.shared.gwt.dataview.export.kml.mapping.KmlIcon;

/**
 * Created by Patrick on 10/21/2014.
 */
public interface KmlMappingPresenter extends Activity {
    void setLocationType(LocationType type);

    void setIconMode(KmlMapping.IconMode mode);

    void useMultipleDetailFields();

    void setDetailsFieldDef(FieldDef fieldDef);

    void setLatFieldDef(FieldDef fieldDef);

    void setLongFieldDef(FieldDef fieldDef);

    void setDurationFieldDef(FieldDef fieldDef);

    void setAddressFieldDef(FieldDef fieldDef);

    void setIconFieldDef(FieldDef fieldDef);

    void setStartTimeFieldDef(FieldDef fieldDef);

    void setEndTimeFieldDef(FieldDef fieldDef);

    void setLabelFieldDef(FieldDef fieldDef);

    void useMultipleAddressFields();

    void useMultipleLabelFields();

    void save();

    void cancel();

    void setName(String name);

    void setIcon(KmlIcon icon);
}
