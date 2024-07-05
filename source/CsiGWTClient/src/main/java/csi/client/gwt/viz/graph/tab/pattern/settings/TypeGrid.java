package csi.client.gwt.viz.graph.tab.pattern.settings;

import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;

class TypeGrid extends Composite {
    private final FluidContainer container = new FluidContainer();
    private final List<TypeGridEventHandler> handlers = Lists.newArrayList();
    private Map<Object, GraphType> typeMap = Maps.newHashMap();

    public TypeGrid() {
        container.addStyleName("pattern-type-grid");//NON-NLS
        Style style = container.getElement().getStyle();
        style.setOverflowY(Overflow.AUTO);
        style.setHeight(100.0D, Unit.PCT);
        initWidget(container);
    }

    public static TypeGrid build() {
        TypeGrid grid = new TypeGrid();
        return grid;
    }

    public void add(GraphType type) {
        FluidRow row = new FluidRow();
        Column labelColumn = new Column(10);
        Column addButtonColumn = new Column(2);
        row.add(labelColumn);
        row.add(addButtonColumn);
        if (type instanceof GraphNodeType) {
            GraphNodeType label = (GraphNodeType) type;
            Image button = label.getImage(40);
            button.setWidth("12px");//NON-NLS
            button.addStyleName("pattern-type-grid-image");//NON-NLS
            labelColumn.add(button);
        }

        InlineLabel label1 = new InlineLabel(type.getName());
        final Button button1 = new Button("", IconType.ARROW_RIGHT);
        button1.setType(ButtonType.LINK);
        button1.getElement().getStyle().setTextDecoration(TextDecoration.NONE);
        typeMap.put(row, type);
        labelColumn.add(label1);
        addButtonColumn.add(button1);
        row.addDomHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                for (TypeGridEventHandler handler : handlers) {
                    Object source = event.getSource();
                    GraphType type = typeMap.get(source);
                    handler.onAdd(type);
                }
            }
        }, ClickEvent.getType());
        container.add(row);
    }

    public void addHandler(TypeGridEventHandler handler) {
        handlers.add(handler);
    }

    public abstract static class TypeGridEventHandler {
        public TypeGridEventHandler() {
        }

        abstract void onAdd(GraphType var1);
    }

    public Map<Object, GraphType> getTypeMap() {
        return typeMap;
    }
}
