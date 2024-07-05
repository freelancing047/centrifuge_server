package csi.client.gwt.widget.list_boxes;

import java.util.Collection;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 7/19/2016.
 */
public interface BasicStringListBox {

    public void clear();
    public void addAll(Collection<String> optionsIn);
    public void addAll(String[] optionsIn);
    public void addAllPairs(String[][] optionsIn);
    public void addAllPairs(Collection<ValuePair<String, String>> optionsIn);
    public HandlerRegistration addSelectionChangedHandler(SelectionChangedEvent.SelectionChangedHandler handlerIn);
    public boolean setSelectedIndex(int indexIn);
    public boolean setSelectedValue(String valueIn);
    public int getSelectedIndex();
    public String getSelectedValue();
    public void setEnabled(boolean enabledIn);
    public Widget getWidget();
}
