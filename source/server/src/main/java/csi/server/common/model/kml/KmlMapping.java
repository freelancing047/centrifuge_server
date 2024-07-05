package csi.server.common.model.kml;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.shared.gwt.dataview.export.kml.mapping.KmlIcon;

/**
 * Created by Patrick on 10/20/2014.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

public class KmlMapping extends ModelObject {
    boolean isActive;
    LocationType locationType = LocationType.Address;
    private IconMode iconMode = IconMode.FIXED;
    @ManyToMany
    @JoinTable(name="KmlMapping_DetailFields")
    private List<FieldDef> detailFields;
    @ManyToMany
    @JoinTable(name="KmlMapping_LatFields")
    private List<FieldDef> latFields;
    @ManyToMany
    @JoinTable(name="KmlMapping_LongFields")
    private List<FieldDef> longFields;
    @ManyToMany
    @JoinTable(name="KmlMapping_DurationFields")
    private List<FieldDef> durationFields;
    @ManyToMany
    @JoinTable(name="KmlMapping_AddressFields")
    private List<FieldDef> addressFields;
    @ManyToMany
    @JoinTable(name="KmlMapping_IconFields")
    private List<FieldDef> iconFields;
    @ManyToMany
    @JoinTable(name="KmlMapping_StartTimeFields")
    private List<FieldDef> startTimeFields;
    @ManyToMany
    @JoinTable(name="KmlMapping_EndTimeFields")
    private List<FieldDef> endTimeFields;
    @ManyToMany
    @JoinTable(name="KmlMapping_LabelFields")
    private List<FieldDef> labelFields;
    private String name;
    @Transient
    private KmlIcon icon;
    private String kmlIconUrl;
    private String kmlIconName;
    private boolean selected;

    public KmlMapping() {
        labelFields = new ArrayList<FieldDef>();
        endTimeFields = new ArrayList<FieldDef>();
        startTimeFields = new ArrayList<FieldDef>();
        iconFields = new ArrayList<FieldDef>();
        addressFields = new ArrayList<FieldDef>();
        durationFields = new ArrayList<FieldDef>();
        longFields = new ArrayList<FieldDef>();
        latFields = new ArrayList<FieldDef>();
        detailFields = new ArrayList<FieldDef>();
    }

    public boolean isActive() {
        return isActive;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }


    public IconMode getIconMode() {
        return iconMode;
    }

    public void setIconMode(IconMode iconMode) {
        this.iconMode = iconMode;
    }

    public List<FieldDef> getDetailFields() {
        return detailFields;
    }

    public void setDetailFields(List<FieldDef> detailFields) {
        this.detailFields = detailFields;
    }

    public List<FieldDef> getLatFields() {
        return latFields;
    }

    public void setLatFields(List<FieldDef> latFields) {
        this.latFields = latFields;
    }

    public List<FieldDef> getLongFields() {
        return longFields;
    }

    public void setLongFields(List<FieldDef> longFields) {
        this.longFields = longFields;
    }

    public List<FieldDef> getDurationFields() {
        return durationFields;
    }

    public void setDurationFields(List<FieldDef> durationFields) {
        this.durationFields = durationFields;
    }

    public List<FieldDef> getAddressFields() {
        return addressFields;
    }

    public void setAddressFields(List<FieldDef> addressFields) {
        this.addressFields = addressFields;
    }

    public List<FieldDef> getIconFields() {
        return iconFields;
    }

    public void setIconFields(List<FieldDef> iconFields) {
        this.iconFields = iconFields;
    }

    public List<FieldDef> getStartTimeFields() {
        return startTimeFields;
    }

    public void setStartTimeFields(List<FieldDef> startTimeFields) {
        this.startTimeFields = startTimeFields;
    }

    public List<FieldDef> getEndTimeFields() {
        return endTimeFields;
    }

    public void setEndTimeFields(List<FieldDef> endTimeFields) {
        this.endTimeFields = endTimeFields;
    }

    public List<FieldDef> getLabelFields() {
        return labelFields;
    }

    public void setLabelFields(List<FieldDef> labelFields) {
        this.labelFields = labelFields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public KmlIcon getIcon() {
        if (icon == null) {
            icon = new KmlIcon();
            icon.setName(kmlIconName);
            icon.setURL(kmlIconUrl);
        }
        return icon;
    }

    public void setIcon(KmlIcon icon) {
        if (icon == null) {
            kmlIconName = null;
            kmlIconUrl = null;
        }else {
            kmlIconName = icon.getName();
            kmlIconUrl = icon.getURL();
        }
        this.icon = icon;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void voidFN(){
        //needed for grid
    }

    public static enum LocationType {
        Address, LatLong;
    }

    public static enum IconMode {
        FIXED, FIELD

    }
}
