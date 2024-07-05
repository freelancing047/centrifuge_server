package csi.client.gwt.viz.timeline.model;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class GroupingItem implements IsWidget {

    private SimplePanel checkBoxPanel;
    private Button label;
    private FluidRow flowPanel;
    private String text;
    private CheckBox checkBox;
    
    public GroupingItem(boolean visibility, String text) {
        this.text = text;
        setCheckBoxPanel(new SimplePanel());

        flowPanel = new FluidRow();
        flowPanel.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
        checkBoxPanel.setHeight("15px");
        checkBoxPanel.setWidth("15px");
        checkBoxPanel.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);

        flowPanel.getElement().getStyle().setHeight(16, Unit.PX);
        flowPanel.getElement().getStyle().setPaddingBottom(0, Unit.PX);
        flowPanel.getElement().getStyle().setPaddingTop(0, Unit.PX);
        flowPanel.getElement().getStyle().setPaddingLeft(0, Unit.PX);
        flowPanel.getElement().getStyle().setPaddingRight(0, Unit.PX);
        checkBox = new CheckBox();
        checkBox.setValue(visibility);
        
        checkBoxPanel.add(checkBox);
        
        label = new Button();
        flowPanel.add(checkBoxPanel);
        label.addStyleName("legend-item-label");//NON-NLS
        label.setType(ButtonType.LINK);
        label.setText(this.text);
        label.getElement().getStyle().setMarginLeft(-3, Unit.PX);
        label.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
        label.getElement().getStyle().setDisplay(Display.INLINE);
        flowPanel.add(label);
        
        
    }
    
    public void italicize(String text){    
        //label.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
        label.getElement().setInnerHTML("<i> " + text + "</i>");
        label.getElement().getStyle().setPaddingTop(0, Unit.PX);
        label.getElement().getStyle().setMargin(-1, Unit.PX);
        label.getElement().getStyle().clearDisplay();
        
    }
    
    public void updateCheckBox(boolean visible){
        checkBox.setValue(visible);
    }

    @Override
    public Widget asWidget() {
        return flowPanel;
    }

   
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public SimplePanel getCheckBoxPanel() {
        return checkBoxPanel;
    }

    public void setCheckBoxPanel(SimplePanel checkBoxPanel) {
        this.checkBoxPanel = checkBoxPanel;
    }

}
