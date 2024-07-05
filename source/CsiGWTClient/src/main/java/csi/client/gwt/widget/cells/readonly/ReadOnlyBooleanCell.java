package csi.client.gwt.widget.cells.readonly;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * Created by centrifuge on 1/24/2018.
 */
public class ReadOnlyBooleanCell extends AbstractCell<Boolean> {

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    TextCell _renderer = new TextCell();

    public ReadOnlyBooleanCell() {

        super();
    }

    public void render(Cell.Context contextIn, Boolean dataIn, SafeHtmlBuilder builderIn) {

        String myDisplay = ((null != dataIn) && dataIn) ? _constants.nodesTabTrueValue() : _constants.nodesTabFalseValue();
        _renderer.render(contextIn, myDisplay, builderIn);
    }
}
