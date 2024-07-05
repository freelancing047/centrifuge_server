package csi.client.gwt.widget.cells.readonly;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import csi.client.gwt.widget.cells.ExtendedDisplays;
import csi.server.common.enumerations.DisplayMode;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 9/15/2015.
 */
public class ColoredStringCell extends AbstractCell<ValuePair<String, DisplayMode>> {
    @Override
    public void render(Context contextIn, ValuePair<String, DisplayMode> valueIn, SafeHtmlBuilder htmlBuilderIn) {

        htmlBuilderIn.append(ExtendedDisplays.displayEntry(valueIn.getValue2(), valueIn.getValue1()));
    }
}
