package csi.client.gwt.viz.viewer.lens.field;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.dom.client.Style;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.viz.viewer.lens.shared.ListWithMore;
import csi.server.common.model.visualization.viewer.Objective;
import csi.server.util.sql.api.AggregateFunction;

import java.util.List;
import java.util.Map;

class SingleFieldView extends Composite {
    private FluidContainer container;

    SingleFieldView(String column, Map<String, String> value, List<String> strings, Objective objective, String lensDef) {

        container = new FluidContainer();
        initWidget(container);
        container.getElement().getStyle().setPadding(0, Style.Unit.PX);
        container.getElement().getStyle().setPaddingLeft(8, Style.Unit.PX);
//        Button expand = new Button();
//        expand.setType(ButtonType.LINK);
//        expand.setIcon(IconType.PLUS  );
//        FluidRow row1 = new FluidRow();
//        container.add(row1);
//        row1.add(expand);
//        InlineLabel label = new InlineLabel(column);
//        row1.add(label);
        FluidRow row2 = new FluidRow();

        container.add(row2);
        final FluidContainer c2 = new FluidContainer();
        row2.add(c2);
        for (Map.Entry<String, String> stringStringEntry : value.entrySet()) {
            FluidRow row3 = new FluidRow();
            String aggregateFunctionString = AggregateFunction.valueOf(stringStringEntry.getKey()).getLabel();
            String value1 = stringStringEntry.getValue();
            String _value = formatValue(value1);
            row3.add(new InlineLabel(aggregateFunctionString + " of " + column+": " + _value));
//            row3.add(new InlineLabel("" + _value));
            c2.add(row3);
        }
        if(strings!=null&&!strings.isEmpty()) {
            ListWithMore w = new ListWithMore(strings, objective, lensDef, column, 50, null, null, 3, null);
//            ExpandableItem _l = new ExpandableItem("Values");
//            _l.add(w);
            c2.add(w);
        }
//        c2.setVisible(false);
//        expand.addClickHandler(new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                c2.setVisible(!c2.isVisible());
//            }
//        });
    }

    String formatValue(String value1) {
        String _value;
        double aDouble = Double.valueOf(value1);
        if (aDouble == Math.rint(aDouble)) {
            _value = NumberFormat.getFormat("0").format(aDouble);
        } else {
            _value = NumberFormat.getFormat("0.####").format(aDouble);
        }
        return _value;
    }

    @Override
    public Widget asWidget() {
        return container;
    }
}
