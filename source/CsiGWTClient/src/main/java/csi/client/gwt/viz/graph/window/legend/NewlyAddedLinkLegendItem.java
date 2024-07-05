package csi.client.gwt.viz.graph.window.legend;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.model.visualization.graph.GraphConstants;

public class NewlyAddedLinkLegendItem extends Composite implements LegendItemProxy {
    private final String typeName = CentrifugeConstantsLocator.get().newlyAdded();
    private final String color = WebMain.getClientStartupInfo().getGraphAdvConfig().getDefaultNewGenColor().toString();
    public NewlyAddedLinkLegendItem() {
        Row row = new Row();
        row.getElement().getStyle().setMarginLeft(0, Style.Unit.PX);
        DivWidget divWidget = new DivWidget();
        divWidget.getElement().getStyle().setHeight(6, Style.Unit.PX);
        divWidget.getElement().getStyle().setWidth(12, Style.Unit.PX);
        divWidget.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        divWidget.getElement().getStyle().setBorderWidth(2, Style.Unit.PX);
        divWidget.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
        divWidget.getElement().getStyle().setBorderColor(color);
        divWidget.getElement().getStyle().setMarginBottom(-3, Style.Unit.PX);
        divWidget.getElement().getStyle().setMarginRight(-2, Style.Unit.PX);
        row.add(divWidget);
        Button label = new Button();
        row.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
        row.addStyleName("legend-item");
        label.addStyleName("legend-item-label");//NON-NLS
        label.setType(ButtonType.LINK);

        label.setText(typeName);
        row.add(label);
        initWidget(row);
    }

    public static String getStaticKey() {
        return "csi.internal."+GraphConstants.NEW_GENERATION_FIELD_TYPE;
    }

    @Override
    public String getKey() {
        return "csi.internal."+GraphConstants.NEW_GENERATION_FIELD_TYPE+".link";
    }

    @Override
    public String getType() {
        return GraphConstants.NEW_GENERATION_FIELD_TYPE;
    }

    @Override
    public String getImageUrl() {
        // TODO Auto-generated method stub
        return null;
    }
}
