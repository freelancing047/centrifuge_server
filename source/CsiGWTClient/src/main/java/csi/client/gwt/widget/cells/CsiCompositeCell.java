package csi.client.gwt.widget.cells;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import java.util.List;

/**
 * Created by centrifuge on 10/11/2018.
 */
public class CsiCompositeCell<C> extends CompositeCell<C> {

    private final List<HasCell<C, ?>> _cellList;
    private int _leftPad = 0;
    private int _rightPad = 0;

    public CsiCompositeCell(List<HasCell<C, ?>> cellListIn) {

        super(cellListIn);
        _cellList = cellListIn;
    }

    public CsiCompositeCell(List<HasCell<C, ?>> cellListIn, int leftPadIn, int rightPadIn) {

        this(cellListIn);
        _leftPad = leftPadIn;
        _rightPad = rightPadIn;
    }

    @Override
    protected <X> void render(Context contextIn, C valueIn, SafeHtmlBuilder bufferIn, HasCell<C, X> cellWrapperIn) {

        Cell<X> myCell = cellWrapperIn.getCell();

        for (int i = 0; _leftPad > i; i++) {

            bufferIn.appendHtmlConstant("&nbsp;");
        }
        bufferIn.appendHtmlConstant("<span>");
        myCell.render(contextIn, cellWrapperIn.getValue(valueIn), bufferIn);
        bufferIn.appendHtmlConstant("</span>");
        for (int i = 0; _rightPad > i; i++) {

            bufferIn.appendHtmlConstant("&nbsp;");
        }
    }
}
