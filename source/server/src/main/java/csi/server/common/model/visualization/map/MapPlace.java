package csi.server.common.model.visualization.map;

import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.attribute.AttributeAggregateType;
import csi.shared.core.color.ColorModel;
import csi.shared.core.color.ContinuousColorModel;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MapPlace extends ModelObject {
    protected AttributeAggregateType aggregateFunction;
    private String name;
    @ManyToOne
    private FieldDef latField;
    @ManyToOne
    private FieldDef longField;
    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("listPosition")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<MapTooltipField> tooltipFields = new ArrayList<>();
    private int listPosition;
    @ManyToOne
    private FieldDef labelField;
    private boolean isComputedSize = false;
    private boolean isFixedSize = true;
    private boolean isPlaceSizeByDynamicType = false;
    private Integer size = 1;
    @ManyToOne
    private FieldDef sizeField;
    private boolean isTypeFixed = true;
    private String typeName = null;
    @ManyToOne
    private FieldDef typeField;
    private boolean includeNullType;

    private boolean useDefaultIconSetting = true;
    private boolean isIconFixed = true;
    private String iconId = null;
    @ManyToOne
    private FieldDef iconField = null;

    private boolean useDefaultShapeSetting = true;
    private String shapeTypeString;

    private boolean useDefaultColorSetting = true;
    @Type(type = "csi.server.dao.jpa.xml.SerializedXMLType")
    @Column(columnDefinition = "TEXT")
    private ColorModel colorModel = new ContinuousColorModel();
    private String colorString;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FieldDef getLatField() {
        return latField;
    }

    public void setLatField(FieldDef latField) {
        this.latField = latField;
    }

    public FieldDef getLongField() {
        return longField;
    }

    public void setLongField(FieldDef longField) {
        this.longField = longField;
    }

    public List<MapTooltipField> getTooltipFields() {
        return tooltipFields;
    }

    public void setTooltipFields(List<MapTooltipField> tooltipFields) {
        this.tooltipFields = tooltipFields;
    }

    public List<FieldDef> getTooltipFields(DataModelDef dataModelIn) {
        List<FieldDef> list = new ArrayList<>();
        List<MapTooltipField> tooltipFields = getTooltipFields();
        for (MapTooltipField tf : tooltipFields) {
            FieldDef fieldDef = tf.getFieldDef(dataModelIn);
            if (fieldDef != null) {
                list.add(fieldDef);
            }
        }
        return list;
    }

    public List<String> getTooltipFieldIds() {
        List<String> list = new ArrayList<>();
        List<MapTooltipField> tooltipFields = getTooltipFields();
        for (MapTooltipField tf : tooltipFields) {
            String myId = tf.getFieldId();
            if (myId != null) {
                list.add(myId);
            }
        }
        return list;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public FieldDef getLabelField() {
        return labelField;
    }

    public void setLabelField(FieldDef labelField) {
        this.labelField = labelField;
    }

    public boolean isFixedSize() {
        return isFixedSize;
    }

    public void setFixedSize() {
        isComputedSize = false;
        isFixedSize = true;
    }

    public boolean isComputedSize() {
        return isComputedSize;
    }

    public void setComputedSize() {
        isComputedSize = true;
        isFixedSize = false;
    }

    public boolean isPlaceSizeByDynamicType() {
        return isPlaceSizeByDynamicType;
    }

    public void setPlaceSizeByDynamicType(boolean value) {
        isPlaceSizeByDynamicType = value;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public FieldDef getSizeField() {
        return sizeField;
    }

    public void setSizeField(FieldDef sizeField) {
        this.sizeField = sizeField;
    }

    public AttributeAggregateType getAggregateFunction() {
        return aggregateFunction;
    }

    public void setAggregateFunction(AttributeAggregateType aggregateFunction) {
        this.aggregateFunction = aggregateFunction;
    }

    public boolean isTypeFixed() {
        return isTypeFixed;
    }

    public void setTypeFixed(boolean value) {
        isTypeFixed = value;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public FieldDef getTypeField() {
        return typeField;
    }

    public void setTypeField(FieldDef typeField) {
        this.typeField = typeField;
    }

    public boolean isIncludeNullType() {
        return includeNullType;
    }

    public void setIncludeNullType(boolean includeNullType) {
        this.includeNullType = includeNullType;
    }

    public boolean isUseDefaultIconSetting() {
        return useDefaultIconSetting;
    }

    public void setUseDefaultIconSetting(boolean useDefaultIconSetting) {
        this.useDefaultIconSetting = useDefaultIconSetting;
    }

    public boolean isIconFixed() {
        return isIconFixed;
    }

    public void setIconFixed(boolean value) {
        isIconFixed = value;
    }

    public String getIconId() {
        return iconId;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public FieldDef getIconField() {
        return iconField;
    }

    public void setIconField(FieldDef iconField) {
        this.iconField = iconField;
    }

    public boolean isUseDefaultColorSetting() {
        return useDefaultColorSetting;
    }

    public void setUseDefaultColorSetting(boolean useDefaultColorSetting) {
        this.useDefaultColorSetting = useDefaultColorSetting;
    }

    public ColorModel getColorModel() {
        return colorModel;
    }

    public void setColorModel(ColorModel colorModel) {
        this.colorModel = colorModel;
    }

    public String getColorString() {
        return colorString;
    }

    public void setColorString(String colorString) {
        this.colorString = colorString;
    }

    public boolean isUseDefaultShapeSetting() {
        return useDefaultShapeSetting;
    }

    public void setUseDefaultShapeSetting(boolean useDefaultShapeSetting) {
        this.useDefaultShapeSetting = useDefaultShapeSetting;
    }

    public String getShapeTypeString() {
        return shapeTypeString;
    }

    public void setShapeTypeString(String shapeTypeString) {
        this.shapeTypeString = shapeTypeString;
    }

    public MapPlace clone() {
        MapPlace myClone = new MapPlace();

        super.cloneComponents(myClone);

        myClone.setName(getName());
        myClone.setLatField(getLatField());
        myClone.setLongField(getLongField());
        myClone.setColorModel(getColorModel());
        myClone.setTooltipFields(cloneTooltipFields());
        myClone.setLabelField(getLabelField());
        myClone.setPlaceSizeByDynamicType(isPlaceSizeByDynamicType());
        if (isComputedSize()) {
            myClone.setComputedSize();
        } else {
            myClone.setFixedSize();
        }
        myClone.setSize(getSize());
        myClone.setSizeField(getSizeField());
        myClone.setAggregateFunction(getAggregateFunction());
        myClone.setTypeFixed(isTypeFixed());
        myClone.setTypeName(getTypeName());
        myClone.setTypeField(getTypeField());
        myClone.setIncludeNullType(isIncludeNullType());
        myClone.setUseDefaultIconSetting(isUseDefaultIconSetting());
        myClone.setIconId(getIconId());
        if (!isIconFixed()) {
            myClone.setIconFixed(false);
            myClone.setIconField(getIconField());
        } else {
            myClone.setIconFixed(true);
        }
        myClone.setUseDefaultColorSetting(isUseDefaultColorSetting());
        myClone.setColorString(getColorString());
        myClone.setUseDefaultShapeSetting(isUseDefaultShapeSetting());
        myClone.setShapeTypeString(getShapeTypeString());

        return myClone;
    }

    private List<MapTooltipField> cloneTooltipFields() {
        List<MapTooltipField> myList = new ArrayList<>();

        for (MapTooltipField myItem : getTooltipFields()) {
            myList.add(myItem.clone());
        }

        return myList;
    }

    public <T extends ModelObject> MapPlace copy(Map<String, T> fieldMapIn) {
        MapPlace myCopy = new MapPlace();

        super.cloneComponents(myCopy);

        myCopy.setName(getName());
        myCopy.setLatField(getLatField());
        myCopy.setLongField(getLongField());
        myCopy.setColorModel(getColorModel());
        myCopy.setTooltipFields(copyTooltipFields());
        myCopy.setLabelField(getLabelField());
        myCopy.setPlaceSizeByDynamicType(isPlaceSizeByDynamicType());
        if (isComputedSize()) {
            myCopy.setComputedSize();
        } else {
            myCopy.setFixedSize();
        }
        myCopy.setSize(getSize());
        myCopy.setSizeField(getSizeField());
        myCopy.setAggregateFunction(getAggregateFunction());
        myCopy.setTypeFixed(isTypeFixed());
        myCopy.setTypeName(getTypeName());
        myCopy.setTypeField(getTypeField());
        myCopy.setIncludeNullType(isIncludeNullType());
        myCopy.setUseDefaultIconSetting(isUseDefaultIconSetting());
        myCopy.setIconId(getIconId());
        if (!isIconFixed()) {
            myCopy.setIconFixed(false);
            myCopy.setIconField(getIconField());
        } else {
            myCopy.setIconFixed(true);
        }
        myCopy.setUseDefaultColorSetting(isUseDefaultColorSetting());
        myCopy.setColorString(getColorString());
        myCopy.setUseDefaultShapeSetting(isUseDefaultShapeSetting());
        myCopy.setShapeTypeString(getShapeTypeString());

        return myCopy;
    }

    private List<MapTooltipField> copyTooltipFields() {
        List<MapTooltipField> myList = new ArrayList<>();

        for (MapTooltipField myItem : getTooltipFields()) {
            myList.add(myItem.clone());
        }

        return myList;
    }

}
