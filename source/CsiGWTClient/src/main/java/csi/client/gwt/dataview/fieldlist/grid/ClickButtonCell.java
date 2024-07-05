package csi.client.gwt.dataview.fieldlist.grid;

import com.github.gwtbootstrap.client.ui.ButtonCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

/**
 * @author Centrifuge Systems, Inc.
 * A button cell that will invoke the passed in command onClick
 */
public class ClickButtonCell extends ButtonCell {

    private final ClickCellCommand command;

    public ClickButtonCell(ClickCellCommand command){
        this.command = command;
    }
    
    @Override
    public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        if(BrowserEvents.CLICK.equals(event.getType())){
            command.execute(context);
        }
    }

}
