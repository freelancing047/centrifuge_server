package csi.shared.core.visualization.map;

public class PlaceSettingsDTO {
    private String name;
    private String latColumn;
    private String longColumn;
    private String labelColumn = null;
    private Integer size;
    private String sizeColumn = null;
    private Boolean sizeColumnNumerical = null;
    private String sizeFunction = null;
    private Boolean sizedByDynamicType = false;
    private String typeName = null;
    private String typeColumn = null;
    private boolean includeNullType = false;
    private boolean iconOverridden;
    private String iconUri = null;
    private String iconColumn = null;
    private boolean colorOverridden;
    private String colorString = null;
    private boolean shapeOverridden;
    private String shapeTypeString = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatColumn() {
        return latColumn;
    }

    public void setLatColumn(String latColumn) {
        this.latColumn = latColumn;
    }

    public String getLongColumn() {
        return longColumn;
    }

    public void setLongColumn(String longColumns) {
        this.longColumn = longColumns;
    }

    public String getLabelColumn() {
        return labelColumn;
    }

    public void setLabelColumn(String labelColumn) {
        this.labelColumn = labelColumn;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSizeColumn() {
        return sizeColumn;
    }

    public void setSizeColumn(String sizeColumn) {
        this.sizeColumn = sizeColumn;
    }

    public Boolean getSizeColumnNumerical() {
        return sizeColumnNumerical;
    }

    public void setSizeColumnNumerical(Boolean sizeColumnsNumerical) {
        this.sizeColumnNumerical = sizeColumnsNumerical;
    }

    public String getSizeFunction() {
        return sizeFunction;
    }

    public void setSizeFunction(String sizeFunction) {
        this.sizeFunction = sizeFunction;
    }

    public Boolean isSizedByDynamicType() {
        return sizedByDynamicType;
    }

    public void setSizedByDynamicType(Boolean sizeByDynamicType) {
        this.sizedByDynamicType = sizeByDynamicType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeColumn() {
        return typeColumn;
    }

    public void setTypeColumn(String typeColumn) {
        this.typeColumn = typeColumn;
    }

    public boolean isIncludeNullType() {
        return includeNullType;
    }

    public void setIncludeNullType(boolean includeNullType) {
        this.includeNullType = includeNullType;
    }

    public boolean isIconOverridden() {
        return iconOverridden;
    }

    public void setIconOverridden(boolean iconOverridden) {
        this.iconOverridden = iconOverridden;
    }

    public String getIconUri() {
        return iconUri;
    }

    public void setIconUri(String iconUri) {
        this.iconUri = iconUri;
    }

    public String getIconColumn() {
        return iconColumn;
    }

    public void setIconColumn(String iconColumn) {
        this.iconColumn = iconColumn;
    }

    public boolean isColorOverridden() {
        return colorOverridden;
    }

    public void setColorOverridden(boolean colorOverridden) {
        this.colorOverridden = colorOverridden;
    }

    public String getColorString() {
        return colorString;
    }

    public void setColorString(String colorString) {
        this.colorString = colorString;
    }

    public boolean isShapeOverridden() {
        return shapeOverridden;
    }

    public void setShapeOverridden(boolean shapeOverridden) {
        this.shapeOverridden = shapeOverridden;
    }

    public String getShapeTypeString() {
        return shapeTypeString;
    }

    public void setShapeTypeString(String shapeTypeString) {
        this.shapeTypeString = shapeTypeString;
    }
}
