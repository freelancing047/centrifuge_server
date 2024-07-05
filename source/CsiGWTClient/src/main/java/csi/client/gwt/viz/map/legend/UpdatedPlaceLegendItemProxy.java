package csi.client.gwt.viz.map.legend;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.window.legend.LegendItemProxy;
import csi.server.business.visualization.legend.UpdatedPlaceLegendItem;

import static com.google.common.base.Preconditions.checkNotNull;

public class UpdatedPlaceLegendItemProxy extends Composite implements LegendItemProxy {
    private final String typeName = CentrifugeConstantsLocator.get().inCommon();
    private final String color = WebMain.getClientStartupInfo().getGraphAdvConfig().getDefaultUpdateGenColor().toString();

    private UpdatedPlaceLegendItem item;
    private DivWidget iconWidget;

    UpdatedPlaceLegendItemProxy(UpdatedPlaceLegendItem item) {
        this.item = checkNotNull(item, "I cannot make something from nothing.");
        Row row = createRow();
        addImageTo(row);
        addLabelTo(row);
        initWidget(row);
    }

    private Row createRow() {
        Row row = new Row();
        row.addStyleName("maplegend-item");
        row.getElement().getStyle().setMarginLeft(0, Style.Unit.PX);
        row.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
        return row;
    }

    private void addImageTo(Row row) {
        createIconWidget();
        row.add(iconWidget);
    }

    private void createIconWidget() {
        iconWidget = new DivWidget();
        iconWidget.getElement().getStyle().setHeight(12, Style.Unit.PX);
        iconWidget.getElement().getStyle().setWidth(12, Style.Unit.PX);
        iconWidget.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        iconWidget.getElement().getStyle().setMarginBottom(-6, Style.Unit.PX);
        if (item.visible)
            useShowIconStyle();
        else
            useHideIconStyle();
    }

    private void useShowIconStyle() {
        iconWidget.getElement().getStyle().setBorderWidth(2, Style.Unit.PX);
        iconWidget.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
        iconWidget.getElement().getStyle().setBorderColor(color);// NON-NLS
        iconWidget.getElement().getStyle().setMarginRight(0, Style.Unit.PX);
    }

    private void useHideIconStyle() {
        iconWidget.getElement().getStyle().setBorderWidth(2, Style.Unit.PX);
        iconWidget.getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
        iconWidget.getElement().getStyle().setBorderColor("#000000");// NON-NLS
        iconWidget.getElement().getStyle().setMarginRight(4, Style.Unit.PX);
    }

    private void addLabelTo(Row row) {
        if (item.clickable)
            row.add(createClickableLabelWidget());
        else
            row.add(createLabelWidget());
    }

    private Widget createClickableLabelWidget() {
        Button label = new Button();
        label.addStyleName("legend-item-label");// NON-NLS
        label.setType(ButtonType.LINK);
        label.setText(typeName);
        return label;
    }

    private Widget createLabelWidget() {
        return CombinedPlaceLegendItemProxy.createLabelWidget(typeName);
    }

    @Override
    public String getKey() {
        return item.key;
    }

    @Override
    public String getType() {
        return typeName;
    }

    @Override
    public String getImageUrl() {
        return null;
    }

}
