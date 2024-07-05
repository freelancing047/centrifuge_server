package csi.client.gwt.viz.graph.shared;

import csi.client.gwt.viz.graph.node.settings.SizingAttribute;
import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipFunction;
import csi.server.business.visualization.graph.base.ObjectAttributes;
import csi.server.common.model.FieldDef;

public class AbstractGraphObjectScale {

    private static final SizingAttribute DEFAULT_MEASURE = SizingAttribute.DEGREE;
    private TooltipFunction function;
    private FieldDef field;

    public void setFunction(TooltipFunction function) {
        this.function = function;
    }

    public TooltipFunction getFunction() {
        return function;
    }

    public void setField(FieldDef field) {
        this.field = field;
    }

    public FieldDef getField() {
        return field;
    }

    public enum ScaleDimension {
        SIZE, TRANSPARENCY;
    }

    public enum ScaleMode {
        DYNAMIC, FIXED, COMPUTED;
    }

    private boolean isEnabled = true;
    private SizingAttribute measure = DEFAULT_MEASURE;  //$NON-NLS-1$
    ScaleMode mode = ScaleMode.FIXED;
    protected double value = 1;
    public static final String SCALE = ObjectAttributes.CSI_INTERNAL_SIZE;

    public SizingAttribute getMeasure() {
        return measure;
    }

    public ScaleMode getMode() {
        return mode;
    }

    public double getValue() {
        return value;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void setMeasure(SizingAttribute sizingAttribute) {
        this.measure = sizingAttribute;
    }

    public void setMode(ScaleMode scaleMode) {
        this.mode = scaleMode;
    }

    public void setValue(double value) {
        if (value > 5) {
            this.value = 5;
        } else if (value < 0.2) {
            this.value = 0.2;
        } else {
            this.value = value;
        }
    }

}