package csi.client.gwt.widget.cells.readonly;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import csi.client.gwt.widget.cells.AbstractNestingCell;
import csi.client.gwt.widget.cells.PaddedDisplays;


public class RightPaddedCell<S> extends AbstractNestingCell<S> {

    PaddedDisplays _cellDisplay;
    AbstractNestingCell<S> _primaryCell = null;

    public RightPaddedCell(int spacingIn) {

        super();

        _cellDisplay = new PaddedDisplays(0, spacingIn);
    }

    public RightPaddedCell(AbstractNestingCell<S> primaryCellIn, int spacingIn) {

        super();

        _primaryCell = primaryCellIn;
        _cellDisplay = new PaddedDisplays(0, spacingIn);
    }

    public SafeHtml genHtml(Cell.Context contextIn, S dataIn) {

        if (null != _primaryCell) {

            return _cellDisplay.display(_primaryCell.genHtml(contextIn, dataIn).asString());

        } else {

            return _cellDisplay.display((null != dataIn) ? dataIn.toString() : "");
        }
    }

    public void render(Cell.Context contextIn, S dataIn, SafeHtmlBuilder builderIn) {

        builderIn.append(genHtml(contextIn, dataIn));
    }
}
