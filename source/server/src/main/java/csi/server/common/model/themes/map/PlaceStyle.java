package csi.server.common.model.themes.map;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.HasIcon;
import csi.server.common.model.themes.HasShape;
import csi.server.common.model.themes.VisualItemStyle;
import csi.server.common.util.Format;
import csi.shared.core.color.ClientColorHelper;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PlaceStyle extends VisualItemStyle implements Comparable<PlaceStyle>, HasIcon, HasShape {

    //We create a weak link here so icons can be managed separately
    private String iconId;
    private Integer color = ClientColorHelper.get().randomHueWheel().getIntColor();

    @Enumerated(EnumType.STRING)
    private ShapeType shape = ShapeType.NONE;

    private Double iconScale = 1.0;

    public PlaceStyle() {}

    public PlaceStyle(String nameIn, String iconIdIn, Integer colorIn, ShapeType shapeIn, Double iconScaleIn) {

        super(nameIn);
        iconId = iconIdIn;
        color = colorIn;
        shape = (null != shapeIn) ? shapeIn : ShapeType.NONE;
        iconScale = iconScaleIn;
    }

    public PlaceStyle(String nameIn, String iconIdIn, Integer colorIn, ShapeType shapeIn, Double iconScaleIn, List<String> fieldNamesIn) {

        super(nameIn);
        iconId = iconIdIn;
        color = colorIn;
        shape = (null != shapeIn) ? shapeIn : ShapeType.NONE;
        iconScale = iconScaleIn;

        if ((fieldNamesIn != null) && !fieldNamesIn.isEmpty()) {
           fieldNames.addAll(fieldNamesIn);
        }
    }

    public ShapeType getShape() {
        return (null != shape) ? shape : ShapeType.NONE;
    }
    public void setShape(ShapeType shapeIn) {
        shape = (null != shapeIn) ? shapeIn : ShapeType.NONE;
    }
    public String getIconId() {
        return iconId;
    }
    public void setIconId(String iconId) {
        this.iconId = iconId;
    }
    public Integer getColor() {
        return color;
    }
    public void setColor(Integer color) {
        this.color = color;
    }
    public Double getIconScale() {
        return iconScale;
    }
    public void setIconScale(double iconScale) {
        this.iconScale = iconScale;
    }

    @Override
    public String genKey() {

        return Format.value(getName(), false) + "|" + Format.value(color, false) + "|" + Format.value(shape, false) + "|" + Format.value(iconScale, false);
    }

    @Override
    public VisualItemStyle genEmpty() {

        return new PlaceStyle(getName(), iconId, color, shape, iconScale);
    }

    @Override
    public int compareTo(PlaceStyle placeStyle) {
        return this.getUuid().compareTo(placeStyle.getUuid());
    }

    public PlaceStyle clone() {

        return new PlaceStyle(getName(), iconId, color, shape, iconScale, fieldNames);
    }
}
