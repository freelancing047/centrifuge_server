package csi.client.gwt.widget.boot.columns;

import com.google.gwt.user.cellview.client.Column;
import csi.client.gwt.widget.cells.readonly.CsiTitleCell;

/**
 * Created by centrifuge on 2/14/2019.
 */
public abstract class CsiTextColumn<T> extends Column<T, String> {

    public CsiTextColumn() {

        super(new CsiTitleCell());
    }

    public CsiTextColumn(int limitIn) {

        super(new CsiTitleCell(limitIn));
    }
}
