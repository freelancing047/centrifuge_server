package csi.shared.core.visualization.map;

public class TrackSettingsDTO {
    private String lineStyle;
    private boolean widthOverriden;
    private int width;
    private boolean colorOverriden;
    private String colorString;
    private String place = null;
    private String identityName = null;
    private String identityColumn = null;
    private String sequenceColumn = null;
    private String sequenceValueType = null;
    private String sequenceSortOrder = null;

    public String getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(String lineStyle) {
        this.lineStyle = lineStyle;
    }

    public boolean isWidthOverriden() {
        return widthOverriden;
    }

    public void setWidthOverriden(boolean widthOverriden) {
        this.widthOverriden = widthOverriden;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isColorOverriden() {
        return colorOverriden;
    }

    public void setColorOverriden(boolean colorOverriden) {
        this.colorOverriden = colorOverriden;
    }

    public String getColorString() {
        return colorString;
    }

    public void setColorString(String colorString) {
        this.colorString = colorString;
    }

    public String getIdentityName() {
        return identityName;
    }

    public void setIdentityName(String identityName) {
        this.identityName = identityName;
    }

    public String getIdentityColumn() {
        return identityColumn;
    }

    public void setIdentityColumn(String identityColumn) {
        this.identityColumn = identityColumn;
    }

    public String getSequenceColumn() {
        return sequenceColumn;
    }

    public void setSequenceColumn(String sequenceColumn) {
        this.sequenceColumn = sequenceColumn;
    }

    public String getSequenceValueType() {
        return sequenceValueType;
    }

    public void setSequenceValueType(String sequenceColumnType) {
        this.sequenceValueType = sequenceColumnType;
    }

    public String getSequenceSortOrder() {
        return sequenceSortOrder;
    }

    public void setSequenceSortOrder(String sequenceSortOrder) {
        this.sequenceSortOrder = sequenceSortOrder;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }
}
