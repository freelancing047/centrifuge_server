package csi.client.gwt.mapper.cells;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import csi.client.gwt.mapper.data_model.SelectionDataAccess;
import csi.client.gwt.mapper.data_model.SelectionPair;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class LeftGroupMapperCell<S extends SelectionDataAccess<?>, T extends SelectionDataAccess<?>>
        extends AbstractImageTextMapperCell<SelectionPair<S, T>> {

    @Override
    public void render(Cell.Context context, SelectionPair<S, T> value, SafeHtmlBuilder sb) {
        sb.append(template3.html(value.getLeftGroupImageHtml(), value.getLeftGroupDisplayName()));
    }
}
