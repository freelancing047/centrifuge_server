package csi.client.gwt.viz.graph.tab.pattern.result;


import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

public class PatternResultWidgetModelImpl implements PatternResultWidget.PatternResultWidgetModel {
    private Color color = ClientColorHelper.get().make(0);

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
