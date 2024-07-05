package csi.client.gwt.widget.cells;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.ListStore;
import csi.client.gwt.events.GridCellClickHandler;
import csi.client.gwt.widget.IsSelectable;

import java.util.List;

/**
 * Created by centrifuge on 4/24/2019.
 */
public class CsiTextCell extends ClickableGridCell<String> {

    interface DisplayTemplate extends XTemplates {

        @XTemplate("<span title=\"{valueIn}\">{valueIn}</span>")
        SafeHtml display(String valueIn);

        @XTemplate("<span title=\"{valueIn}\" style=\"color:{colorIn};\">{valueIn}</span>")
        SafeHtml display(String valueIn, String colorIn);
    }
    private static final DisplayTemplate _template = GWT.create(DisplayTemplate.class);

    ColorProvider _colorProvider = null;

    public CsiTextCell(GridCellClickHandler handlerIn) {

        super(handlerIn);
    }

    public CsiTextCell(GridCellClickHandler handlerIn, ColorProvider colorProviderIn) {

        super(handlerIn);
        _colorProvider = colorProviderIn;
    }

    public CsiTextCell(GridCellClickHandler handlerIn, ListStore<? extends IsSelectable> storeIn) {

        super(handlerIn, storeIn);
    }

    @Override
    public void render(Context contextIn, String valueIn, SafeHtmlBuilder htmlBuilderIn) {

        if (null != valueIn) {

            if (null != _colorProvider) {

                int myRow = contextIn.getIndex();
                int myColumn = contextIn.getColumn();

                htmlBuilderIn.append(_template.display(valueIn, _colorProvider.getColor(myRow, myColumn)));

            } else {

                htmlBuilderIn.append(_template.display(valueIn));
            }
        }
    }
}
