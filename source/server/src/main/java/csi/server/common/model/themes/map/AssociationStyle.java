package csi.server.common.model.themes.map;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.enumerations.LineStyle;
import csi.server.common.model.themes.VisualItemStyle;
import csi.server.common.util.Format;
import csi.shared.core.color.ClientColorHelper;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AssociationStyle extends VisualItemStyle {

    private Integer color = ClientColorHelper.get().randomHueWheel().getIntColor();
    private Double width = 2.0;

    @Enumerated(EnumType.STRING)
    private LineStyle lineStyle = LineStyle.SOLID;

    public AssociationStyle() {}

    public AssociationStyle(String nameIn, Integer colorIn, Double widthIn, LineStyle lineStyleIn) {

        super(nameIn);
        color = colorIn;
        width = widthIn;
        lineStyle = lineStyleIn;
    }

    public AssociationStyle(String nameIn, Integer colorIn, Double widthIn, LineStyle lineStyleIn, List<String> fieldNamesIn) {

        super(nameIn);
        color = colorIn;
        width = widthIn;
        lineStyle = lineStyleIn;

        if ((fieldNamesIn != null) && !fieldNamesIn.isEmpty()) {
            fieldNames.addAll(fieldNamesIn);
        }
    }
    public LineStyle getLineStyle() {
        return lineStyle;
    }
    public void setLineStyle(LineStyle lineStyle) {
        this.lineStyle = lineStyle;
    }
    public Integer getColor() {
        return color;
    }
    public void setColor(Integer color) {
        this.color = color;
    }
    public Double getWidth() {
        return width;
    }
    public void setWidth(Double width) {
        this.width = width;
    }

    @Override
    public String genKey() {

        return Format.value(getName(), false) + "|" + Format.value(color, false) + "|" + Format.value(width, false) + "|" + Format.value(lineStyle, false);
    }

    @Override
    public VisualItemStyle genEmpty() {

        return new AssociationStyle(getName(), color, width, lineStyle);
    }

    public AssociationStyle clone() {

        return new AssociationStyle(getName(), color, width, lineStyle);
    }
}
