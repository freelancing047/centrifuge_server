package csi.client.gwt.viz.viewer.lens.shared;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ExpandableItem extends Composite implements Pinnable {

    protected FluidContainer myContainer;
    private Button expand;
    protected FluidRow myRow;
    private FluidContainer childContainer;
    private String label;

    public ExpandableItem(String label) {
        this.label = label;
        initWidget(build());
    }

    public FluidContainer build() {
        myContainer = new FluidContainer();
        myContainer.getElement().getStyle().setPadding(0, Style.Unit.PX);
        buildMyRow();
        buildChild();

        return myContainer;
    }


    void buildMyRow() {
        myRow = new FluidRow();
        buildExpandButton();
        buildLabel();
        myContainer.add(myRow);
    }

    void buildExpandButton() {
        expand = new Button();
        expand.setType(ButtonType.LINK);
        expand.setIcon(IconType.MINUS);
        expand.getElement().getStyle().setProperty("textDecoration", "none !important");

//        myRow.add(expand);
        expand.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                expand.setIcon(childContainer.isVisible() ? IconType.PLUS : IconType.MINUS);
                childContainer.setVisible(!childContainer.isVisible());
            }
        });
    }

    void buildLabel() {
        InlineLabel label = new InlineLabel(this.label);
        Style style = label.getElement().getStyle();
        style.setProperty("maxWidth", "80%");
        style.setColor("#808080");
        style.setBackgroundColor("#FFF");
        style.setPaddingLeft(5, Style.Unit.PX);
        style.setPaddingRight(5, Style.Unit.PX);
        style.setTextOverflow(Style.TextOverflow.ELLIPSIS);
        style.setOverflow(Style.Overflow.HIDDEN);
        style.setDisplay(Style.Display.INLINE_BLOCK);
        style.setWhiteSpace(Style.WhiteSpace.NOWRAP);
//        style.setProperty("lineBreak","anywhere");

        myRow.getElement().getStyle().setTextAlign(Style.TextAlign.CENTER);
        myRow.getElement().getStyle().setProperty("borderBottom","1px solid #d9d9d9");
        myRow.getElement().getStyle().setHeight(9, Style.Unit.PX);
        myRow.getElement().getStyle().setMarginBottom(9, Style.Unit.PX);
        myRow.getElement().getStyle().setMarginLeft(5, Style.Unit.PCT);
        myRow.setWidth("90%");

        myRow.add(label);
    }

    private void buildChild() {
        FluidRow childRow = new FluidRow();
        myContainer.add(childRow);
        childContainer = new FluidContainer();
        childContainer.getElement().getStyle().setPadding(0, Style.Unit.PX);
        childContainer.getElement().getStyle().setPaddingLeft(0, Style.Unit.PX);
        childRow.add(childContainer);
        childContainer.setVisible(true);

    }

    public void add(Widget widget) {
        childContainer.add(widget);
    }
    public void insert(Widget w, int beforeIndex){
        childContainer.insert(w, beforeIndex);
    }
    public int getWidgetIndex(Widget child){
        return childContainer.getWidgetIndex(child);
    }

}
