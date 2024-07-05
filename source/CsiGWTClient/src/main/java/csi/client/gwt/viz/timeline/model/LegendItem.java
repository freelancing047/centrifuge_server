package csi.client.gwt.viz.timeline.model;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class LegendItem extends Composite implements IsWidget {

    private HTMLPanel colorPanel;
    private Button label;
    private FluidRow flowPanel;
    private Integer color;
    private String text;
    
    public LegendItem(Integer color, String text) {
        this.color = color;
        this.text = text;
        setPanel(new HTMLPanel(""));

        int red = (this.color >> 16) & 0xFF;
        int green = (this.color >> 8) & 0xFF;
        int blue = this.color & 0xFF;
        

        flowPanel = new FluidRow();
        initWidget(flowPanel);
        flowPanel.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
        colorPanel.getElement().getStyle().setBackgroundColor(CssColor.make(red,green,blue).value());
        colorPanel.setHeight("14px");
        colorPanel.setWidth("14px");
        colorPanel.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
        colorPanel.getElement().getStyle().setMarginTop(4, Unit.PX);

        flowPanel.getElement().getStyle().setHeight(16, Unit.PX);
        flowPanel.getElement().getStyle().setPaddingBottom(0, Unit.PX);
        flowPanel.getElement().getStyle().setPaddingTop(0, Unit.PX);
        flowPanel.getElement().getStyle().setPaddingLeft(0, Unit.PX);
        flowPanel.getElement().getStyle().setPaddingRight(0, Unit.PX);
        label = new Button();
        flowPanel.add(colorPanel);
        colorPanel.getElement().getStyle().setProperty("pointerEvents", "none");
        label.addStyleName("legend-item-label");//NON-NLS
        label.setType(ButtonType.LINK);
        HTMLPanel textDiv = new HTMLPanel("");
        textDiv.getElement().setClassName("textDiv");
        textDiv.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
        textDiv.getElement().setInnerHTML(text);
        label.add(textDiv);
        label.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
        label.getElement().getStyle().setDisplay(Display.INLINE);
        flowPanel.add(label);
        
        
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    public HTMLPanel getPanel() {
        return colorPanel;
    }

    public void setPanel(HTMLPanel panel) {
        this.colorPanel = panel;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getKey()  {return text;}

}
