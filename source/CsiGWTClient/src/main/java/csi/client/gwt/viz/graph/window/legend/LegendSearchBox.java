package csi.client.gwt.viz.graph.window.legend;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.search.Searchable;

public class LegendSearchBox<T extends Searchable> extends FlowPanel {
    private static final String SEARCH_BOX_BTN_CLOSE_STYLE = "searchBox-btn-close";

    private static final String SEARCH_BOX_BTN_STYLE = "searchBox-btn";

    private static final String SEARCH_BOX_LABEL_STYLE = "searchBox-label";
    private CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private TextBox textBox;
    private T searchable;
    private HTMLPanel text;
    private Button leftButton, rightButton;

    public T getSearchable() {
        return searchable;
    }

    public LegendSearchBox(T searchable){
        super();
        this.searchable = searchable;
        this.setWidth("110px");
        this.setHeight("0px");
        textBox = new TextBox();
        textBox.setWidth("70px");
        textBox.setHeight("9px");
        textBox.setPlaceholder(i18n.graphLegend_placeHolder());
        textBox.getElement().getStyle().clearPadding();
        textBox.getElement().getStyle().clearMargin();
        textBox.getElement().getStyle().clearOutlineColor();
        textBox.getElement().getStyle().setProperty("border", "0px none");
        textBox.getElement().getStyle().setProperty("boxShadow", "none");

        this.add(textBox);

        textBox.addKeyUpHandler(new KeyUpHandler(){

            @Override
            public void onKeyUp(KeyUpEvent event) {
                getSearchable().searchText(textBox.getText());

            }});

        leftButton = new Button();
        leftButton.addStyleName(SEARCH_BOX_BTN_STYLE);
        leftButton.setIcon(IconType.CARET_UP);
        leftButton.getElement().getStyle().setWidth(10.0 , Style.Unit.PX);
        leftButton.getElement().getStyle().setHeight(20.0, Style.Unit.PX);
        leftButton.getElement().getStyle().setPadding(0.0, Style.Unit.PX);
        leftButton.getElement().getStyle().setProperty("border", "0px none");
        leftButton.getElement().getStyle().setProperty("background", "none");
        leftButton.getElement().getStyle().setBackgroundColor("none");
        leftButton.getElement().getStyle().setProperty("boxShadow", "none");
        leftButton.getElement().getFirstChildElement().getStyle().setVerticalAlign(Style.VerticalAlign.TEXT_TOP);

        rightButton = new Button();
        rightButton.addStyleName(SEARCH_BOX_BTN_STYLE);
        rightButton.setIcon(IconType.CARET_DOWN);
        rightButton.getElement().getStyle().setWidth(10.0 , Style.Unit.PX);
        rightButton.getElement().getStyle().setHeight(20.0, Style.Unit.PX);
        rightButton.getElement().getStyle().setPadding(0.0, Style.Unit.PX);
        rightButton.getElement().getStyle().setProperty("border", "0px none");
        rightButton.getElement().getStyle().setProperty("background", "none");
        rightButton.getElement().getStyle().setProperty("boxShadow", "none");
        rightButton.getElement().getStyle().setMarginRight(5.0, Style.Unit.PX);

        rightButton.getElement().getFirstChildElement().getStyle().setVerticalAlign(Style.VerticalAlign.TEXT_TOP);


        text = new HTMLPanel("");
        text.addStyleName(SEARCH_BOX_LABEL_STYLE);
        this.add(rightButton);
        this.add(leftButton);
        this.add(text);

    }

    public HandlerRegistration addRightButtonClickHandler(ClickHandler handler) {
        return rightButton.addClickHandler(handler);
    }

    public HandlerRegistration addLeftButtonClickHandler(ClickHandler handler) {
        return leftButton.addClickHandler(handler);
    }

    public void focus(){
        textBox.setFocus(true);
    }

    public void setText(String text) {
        textBox.setText(text);
    }

    public TextBox getTextBox() { return textBox;}

    public String getText() {
        return textBox.getText();
    }

}
