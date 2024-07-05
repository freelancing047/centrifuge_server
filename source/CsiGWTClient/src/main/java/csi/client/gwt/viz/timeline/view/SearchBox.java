package csi.client.gwt.viz.timeline.view;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

import csi.client.gwt.viz.shared.search.Searchable;

public class SearchBox<T extends Searchable> extends FlowPanel{


    private static final String SEARCH_BOX_BTN_CLOSE_STYLE = "searchBox-btn-close";

    private static final String SEARCH_BOX_BTN_STYLE = "searchBox-btn";
    
    private static final String SEARCH_BOX_LABEL_STYLE = "searchBox-label";

    private TextBox textBox; 
    private T searchable;
    private HTMLPanel text;
    private Button leftButton, rightButton, closeButton;
    
    public T getSearchable() {
        return searchable;
    }

    public void setSearchable(T searchable) {
        this.searchable = searchable;
    }

    public SearchBox(T searchable){
        super();
        this.searchable = searchable;
        textBox = new TextBox();
        textBox.setWidth("150px");
        this.add(textBox);
        
        textBox.addKeyUpHandler(new KeyUpHandler(){

            @Override
            public void onKeyUp(KeyUpEvent event) {
                getSearchable().searchText(textBox.getText());
            }});

        leftButton = new Button();
        leftButton.addStyleName(SEARCH_BOX_BTN_STYLE);
        leftButton.setIcon(IconType.ARROW_LEFT);
        rightButton = new Button();
        rightButton.addStyleName(SEARCH_BOX_BTN_STYLE);
        rightButton.setIcon(IconType.ARROW_RIGHT);
        closeButton = new Button();
        closeButton.addStyleName(SEARCH_BOX_BTN_STYLE);
        closeButton.addStyleName(SEARCH_BOX_BTN_CLOSE_STYLE);
        closeButton.setIcon(IconType.REMOVE);
        
        text = new HTMLPanel("");
        text.addStyleName(SEARCH_BOX_LABEL_STYLE);
        this.add(leftButton);
        this.add(rightButton);
        this.add(closeButton);
        this.add(text);
        
    }
    
    public void updateCount(int current, int total){
        if(current == 0 || total == 0){
            text.getElement().setInnerHTML("");
        } else {
            text.getElement().setInnerHTML(current + "(" + total + ")");
        }
    }
    
 
    public HandlerRegistration addCloseClickHandler(ClickHandler handler) {
        return closeButton.addClickHandler(handler);
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

    public String getText() {
        return textBox.getText();
    }

    public void disableLeftButton() {
        leftButton.setVisible(false);
        this.setWidth((getOffsetWidth() - 20)  + "px");
    }

}
