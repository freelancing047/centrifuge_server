package csi.client.gwt.widget.boot;

import java.util.List;

import com.github.gwtbootstrap.client.ui.base.HtmlWidget;

public class HtmlList extends HtmlWidget {

    /**
     * Creates an empty paragraph.
     */
    public HtmlList() {
        super("dl");
    }

    /**
     * Creates a widget with  the html set..
     * @param html content html
     */
    public HtmlList(String firsLine, List<String> messages) {
        this();
        String listItems = firsLine + ":";
        //TODO: indent list
        for (String s : messages) {
            listItems += ("<li>" + s + "</li>");
        }
        getElement().setInnerHTML(listItems);
    }

    /**
     * get Inner Text
     * @return inner Text
     */
    public String getText() {
        return getElement().getInnerText();
    }
}
