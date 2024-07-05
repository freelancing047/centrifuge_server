package csi.client.gwt.widget.display_list_widgets;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by centrifuge on 2/19/2015.
 */
public interface DisplayCallback<T extends Widget & HasClickHandlers & CanBeSelected, S> {

    // Cause the display to refresh to reflect changes
    public void refreshDisplay();

    // For tree traversal
    public void beginNode(DisplayListItem<T, S> itemIn);

    // Identify an object to be added to the display
    public void addToDisplay(DisplayListItem<T, S> itemIn);

    // For tree traversal
    public void endNode(DisplayListItem<T, S> itemIn);
}
