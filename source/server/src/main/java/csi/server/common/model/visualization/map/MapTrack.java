package csi.server.common.model.visualization.map;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.SortOrder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MapTrack extends ModelObject {
    public static final int DEFAULT_TRACK_WIDTH = 2;
    @ManyToOne
    private FieldDef identityField;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private FieldDef sequenceField;

    @Enumerated(value = EnumType.STRING)
    private SortOrder sequenceSortOrder = SortOrder.ASC;

    private boolean useDefaultWidthSetting = true;
    private boolean useDefaultColorSetting = true;
    private boolean isIdentityFixed = false;
    private boolean isIdentityDynamic = false;
    private boolean isIdentityPlace = true;

    private int listPosition;
    private int width = DEFAULT_TRACK_WIDTH;

    private String styleTypeString = "";
    private String colorString = "333333";
    private String place = "";
    private String identityName = "";
    private boolean useDefaultOpacity = true;

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public String getStyleTypeString() {
        return styleTypeString;
    }

    public void setStyleTypeString(String styleTypeString) {
        this.styleTypeString = styleTypeString;
    }

    public boolean isUseDefaultWidthSetting() {
        return useDefaultWidthSetting;
    }

    public void setUseDefaultWidthSetting(boolean useDefaultWidthSetting) {
        this.useDefaultWidthSetting = useDefaultWidthSetting;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isUseDefaultColorSetting() {
        return useDefaultColorSetting;
    }

    public void setUseDefaultColorSetting(boolean useDefaultColorSetting) {
        this.useDefaultColorSetting = useDefaultColorSetting;
    }

    public String getColorString() {
        return colorString;
    }

    public void setColorString(String colorString) {
        this.colorString = colorString;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public boolean isIdentityFixed() {
        return isIdentityFixed;
    }

    public void setIdentityFixed(boolean value) {
        isIdentityFixed = value;
    }

    public String getIdentityName() {
        return identityName;
    }

    public void setIdentityName(String identityName) {
        this.identityName = identityName;
    }

    public FieldDef getIdentityField() {
        return identityField;
    }

    public void setIdentityField(FieldDef identityField) {
        this.identityField = identityField;
    }

    public FieldDef getSequenceField() {
        return sequenceField;
    }

    public void setSequenceField(FieldDef sequenceField) {
        this.sequenceField = sequenceField;
    }

    public SortOrder getSequenceSortOrder() {
        return sequenceSortOrder;
    }

    public void setSequenceSortOrder(SortOrder sortOrder) {
        this.sequenceSortOrder = sortOrder;
    }

    public boolean isIdentityDynamic() {
        return isIdentityDynamic;
    }

    public void setIdentityDynamic(boolean identityDynamic) {
        isIdentityDynamic = identityDynamic;
    }

    public boolean isIdentityPlace() {
        return isIdentityPlace;
    }

    public void setIdentityPlace(boolean identityPlace) {
        isIdentityPlace = identityPlace;
    }

    public MapTrack clone() {
        MapTrack clone = new MapTrack();
        super.cloneComponents(clone);
        clone.setStyleTypeString(getStyleTypeString());
        clone.setUseDefaultWidthSetting(isUseDefaultWidthSetting());
        clone.setWidth(getWidth());
        clone.setUseDefaultColorSetting(isUseDefaultColorSetting());
        clone.setUseDefaultOpacity(isUseDefaultOpacity());
        clone.setColorString(getColorString());
        clone.setPlace(getPlace());
        clone.setIdentityFixed(isIdentityFixed());
        clone.setIdentityName(getIdentityName());
        clone.setIdentityField(getIdentityField());
        clone.setSequenceField(getSequenceField());
        clone.setSequenceSortOrder(getSequenceSortOrder());
        clone.setIdentityDynamic(isIdentityDynamic());
        clone.setIdentityPlace(isIdentityPlace());
        return clone;
    }

    public boolean isUseDefaultOpacity() {
        return useDefaultOpacity;
    }

    public void setUseDefaultOpacity(boolean useDefaultOpacity) {
        this.useDefaultOpacity = useDefaultOpacity;
    }
}
