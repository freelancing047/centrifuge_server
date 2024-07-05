package csi.client.gwt.viz.graph.link.settings;

import com.google.gwt.activity.shared.Activity;

import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipFunction;
import csi.client.gwt.viz.graph.shared.AbstractGraphObjectScale.ScaleMode;
import csi.server.common.model.FieldDef;
import csi.shared.core.color.ClientColorHelper.Color;
import csi.shared.gwt.viz.graph.LinkDirection;

interface LinkSettingsPresenter extends Activity {

    void cancel();

    void delete();

    void save();

    void setHideLabel(boolean hidden);

    void setLabelFixed(boolean fixed);

    void setLabelText(String text);

    void setLabelField(FieldDef field);

    void setHasSize(boolean value);

    void setSizeMeasure(String measure);

    void setSizeMode(ScaleMode scaleMode);

    void setSizeValue(double value);

    void setTypeField(FieldDef value);

    void setHideType(boolean b);

    void setTypeFixed(boolean value);

    void setTypeText(String value);

    void setColor(Color color);

    void setDirectionMode(LinkDirection valueOf);

    void setDirectionField(FieldDef value);

    void setName(String value);

    void setSizeFunction(TooltipFunction value);

    void setTransparencyFunction(TooltipFunction value);

    void setTransparencyMeasure(String value);

    void setTransparencyMode(ScaleMode dynamic);

    void setTransparencyValue(double value);

    void setSizeField(FieldDef value);

    void setTransparencyField(FieldDef value);

    void setHasTransparency(Boolean value);

    void updateColorSettings(Boolean value, boolean b);
}
