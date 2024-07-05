package csi.client.gwt.widget.cells;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Created by centrifuge on 8/13/2015.
 */
public abstract class AbstractNestingCell<T> extends AbstractCell<T> {

    public abstract SafeHtml genHtml(Context contextIn, T itemIn);
}
