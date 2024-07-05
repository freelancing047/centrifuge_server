package csi.client.gwt.mapper.cells;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import csi.client.gwt.mapper.data_model.SelectionDataAccess;
import csi.client.gwt.mapper.data_model.SelectionPair;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class LeftSelectionMapperCell<S extends SelectionDataAccess<?>, T extends SelectionDataAccess<?>>
        extends AbstractImageTextMapperCell<SelectionPair<S, T>> {

    int _mode;

    public LeftSelectionMapperCell() {

        _mode = 1 + 2 + 0;
    }

    public LeftSelectionMapperCell(boolean includeGroupIn) {

        _mode = 1 + 2 + (includeGroupIn ? 4 : 0);
    }

    public LeftSelectionMapperCell(boolean includeImageIn, boolean includeTextIn, boolean includeGroupIn) {

        _mode = (includeImageIn ? 1 : 0) + (includeTextIn ? 2 : 0) + (includeGroupIn ? 4 : 0);
    }

    @Override
    public void render(Cell.Context contextIn, SelectionPair<S, T> valueIn, SafeHtmlBuilder builderIn) {

        switch (_mode) {

            case 1:

                builderIn.append(template1.html(valueIn.getLeftCastToTypeImageHtml()));
                break;

            case 2:

                builderIn.append(template2.html(valueIn.getLeftItemDisplayName()));
                break;

            case 3:

                builderIn.append(template3.html(valueIn.getLeftCastToTypeImageHtml(), valueIn.getLeftItemDisplayName()));
                break;

            case 6:

                builderIn.append(template6.html(valueIn.getLeftItemDisplayName(), valueIn.getLeftGroupDisplayName()));
                break;

            case 7:

                builderIn.append(template7.html(valueIn.getLeftCastToTypeImageHtml(), valueIn.getLeftItemDisplayName(), valueIn.getLeftGroupDisplayName()));
                break;

            default:

                builderIn.append(template2.html(" "));
                break;
        }
    }
}
