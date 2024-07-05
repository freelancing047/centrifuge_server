package csi.client.gwt.viz.graph.window.legend;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;

public class MultiTypeLegendItem extends Composite implements LegendItemProxy {
    public static final String CSI_INTERNAL_MULTITYPE = "csi.internal.multitype"; //NON-NLS
    private String typeName = CentrifugeConstantsLocator.get().multitype();

    public MultiTypeLegendItem() {
        Row row = new Row();
        row.getElement().getStyle().setMarginLeft(0, Style.Unit.PX);
        DivWidget divWidget = new DivWidget();
        divWidget.getElement().getStyle().setHeight(12, Style.Unit.PX);
        divWidget.getElement().getStyle().setWidth(12, Style.Unit.PX);
        divWidget.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        divWidget.getElement().getStyle().setBorderWidth(2, Style.Unit.PX);
        divWidget.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
        divWidget.getElement().getStyle().setBorderColor("#00FF00");//NON-NLS
        divWidget.getElement().getStyle().setMarginBottom(-6, Style.Unit.PX);
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

    @Override
    public String getKey() {
        return CSI_INTERNAL_MULTITYPE;
    }
    
    @Override
    public String getType() {
        return typeName;
    }

    @Override
    public String getImageUrl() {
        // TODO Auto-generated method stub
        return null;
    }
}
