package csi.client.gwt.viz.shared.export.view;

import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineHTML;

/**
 * A line of text that can mix standard, bold, and linked items.
 * @author Centrifuge Systems, Inc.
 */
public class InlineTextWidget extends Composite {

    private final DivWidget divWidget = new DivWidget();

    public InlineTextWidget(){
        initWidget(divWidget);
    }

    public InlineTextWidget(int fontSize){
        this();
        divWidget.getElement().getStyle().setFontSize(fontSize, Style.Unit.PX);
    }

    public InlineTextWidget add(String text){
        InlineLabel inlineLabel = new InlineLabel(" " + text);
        divWidget.add(inlineLabel);
        return this;
    }

    public InlineTextWidget addBold(String text){
        InlineHTML inlineHTML = new InlineHTML(" <b>"+text+"</b>");
        divWidget.add(inlineHTML);
        return this;
    }

    public InlineTextWidget addLink(String text, ClickHandler clickHandler){
        InlineHTML inlineHTML = new InlineHTML(" <a>"+text+"</a>");
        divWidget.add(inlineHTML);
        inlineHTML.addClickHandler(clickHandler);
        return this;
    }

    public void clear(){
        divWidget.clear();
    }
}
